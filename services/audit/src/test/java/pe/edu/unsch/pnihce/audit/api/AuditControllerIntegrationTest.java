package pe.edu.unsch.pnihce.audit.api;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import pe.edu.unsch.pnihce.audit.TestcontainersConfiguration;
import pe.edu.unsch.pnihce.audit.repository.AccesoRepository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Import(TestcontainersConfiguration.class)
@SpringBootTest
@AutoConfigureMockMvc
class AuditControllerIntegrationTest {

    @Autowired MockMvc mvc;
    @Autowired AccesoRepository repo;

    @BeforeEach
    void setUp() {
        repo.deleteAll();
    }

    // AUD-RF-01: registro correcto → 201 sin cuerpo
    @Test
    void registrar_accesoCorrecto_devuelve201() throws Exception {
        mvc.perform(post("/api/v1/audit/accesos")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "dniMasked": "7***567",
                      "renipressConsultado": "POSTA0001",
                      "operacion": "CONSULTA_HISTORIA",
                      "exitoso": true
                    }
                    """))
            .andExpect(status().isCreated());

        assertThat(repo.count()).isEqualTo(1);
    }

    // AUD-RF-02: formato dniMasked inválido → 400
    @Test
    void registrar_dniMaskedInvalido_devuelve400() throws Exception {
        mvc.perform(post("/api/v1/audit/accesos")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "dniMasked": "71234567",
                      "renipressConsultado": "POSTA0001",
                      "operacion": "CONSULTA_HISTORIA",
                      "exitoso": true
                    }
                    """))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.title").exists());
    }

    // AUD-RF-03: renipressConsultado vacío → 400
    @Test
    void registrar_sinRenipress_devuelve400() throws Exception {
        mvc.perform(post("/api/v1/audit/accesos")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "dniMasked": "7***567",
                      "operacion": "CONSULTA_HISTORIA",
                      "exitoso": false
                    }
                    """))
            .andExpect(status().isBadRequest());
    }

    // AUD-RF-03: operacion vacía → 400
    @Test
    void registrar_sinOperacion_devuelve400() throws Exception {
        mvc.perform(post("/api/v1/audit/accesos")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "dniMasked": "7***567",
                      "renipressConsultado": "POSTA0001",
                      "exitoso": true
                    }
                    """))
            .andExpect(status().isBadRequest());
    }

    // AUD-RF-04: creado_en es inmutable — el registro no se puede actualizar
    @Test
    void registrar_creadoEnAsignadoAutomaticamente() throws Exception {
        mvc.perform(post("/api/v1/audit/accesos")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "dniMasked": "7***567",
                      "renipressConsultado": "CLINI0001",
                      "operacion": "CONSULTA_HISTORIA",
                      "exitoso": false,
                      "detalle": "Adaptador no disponible"
                    }
                    """))
            .andExpect(status().isCreated());

        var acceso = repo.findAll().getFirst();
        assertThat(acceso.getCreadoEn()).isNotNull();
    }
}
