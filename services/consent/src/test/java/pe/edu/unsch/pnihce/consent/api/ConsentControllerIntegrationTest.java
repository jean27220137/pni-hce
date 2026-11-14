package pe.edu.unsch.pnihce.consent.api;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import pe.edu.unsch.pnihce.consent.TestcontainersConfiguration;
import pe.edu.unsch.pnihce.consent.repository.ConsentimientoRepository;
import pe.edu.unsch.pnihce.consent.util.DniHasher;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Import(TestcontainersConfiguration.class)
@SpringBootTest
@AutoConfigureMockMvc
class ConsentControllerIntegrationTest {

    @Autowired MockMvc mvc;
    @Autowired ConsentimientoRepository repo;

    private static final String DNI = "71234567";

    @BeforeEach
    void setUp() {
        repo.deleteAll();
    }

    // CONS-RF-01: consulta DNI sin consentimiento → 404
    @Test
    void consultar_sinConsentimiento_devuelve404() throws Exception {
        mvc.perform(get("/api/v1/consent/pacientes/" + DNI))
            .andExpect(status().isNotFound());
    }

    // CONS-RF-02: registrar nuevo consentimiento → 201
    @Test
    void registrar_nuevo_devuelve201() throws Exception {
        mvc.perform(put("/api/v1/consent/pacientes/" + DNI)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"estado":"GRANTED","establecimientoOrigen":"POSTA0001"}
                    """))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.estado").value("GRANTED"));
    }

    // CONS-RF-02: actualizar consentimiento existente → 200
    @Test
    void registrar_actualizar_devuelve200() throws Exception {
        String body = """
            {"estado":"GRANTED","establecimientoOrigen":"POSTA0001"}
            """;
        mvc.perform(put("/api/v1/consent/pacientes/" + DNI)
                .contentType(MediaType.APPLICATION_JSON).content(body))
            .andExpect(status().isCreated());

        mvc.perform(put("/api/v1/consent/pacientes/" + DNI)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"estado":"REVOKED","establecimientoOrigen":"POSTA0001"}
                    """))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.estado").value("REVOKED"));
    }

    // CONS-RF-03: DNI inválido → 400 con ProblemDetail
    @Test
    void consultar_dniInvalido_devuelve400() throws Exception {
        mvc.perform(get("/api/v1/consent/pacientes/123"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.title").exists());
    }

    // CONS-RF-04: estado inválido → 400
    @Test
    void registrar_estadoInvalido_devuelve400() throws Exception {
        mvc.perform(put("/api/v1/consent/pacientes/" + DNI)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"estado":"INVALIDO"}
                    """))
            .andExpect(status().isBadRequest());
    }

    // CONS-RF-05: el DNI nunca se persiste en claro (solo el hash SHA-256)
    @Test
    void registrar_persisteSoloHash_nuncaDniEnClaro() throws Exception {
        mvc.perform(put("/api/v1/consent/pacientes/" + DNI)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"estado":"GRANTED"}
                    """))
            .andExpect(status().isCreated());

        var c = repo.findByDniHash(DniHasher.hash(DNI));
        assertThat(c).isPresent();
        assertThat(c.get().getDniHash()).isEqualTo(DniHasher.hash(DNI));
        assertThat(c.get().getDniHash()).doesNotContain(DNI);
    }
}
