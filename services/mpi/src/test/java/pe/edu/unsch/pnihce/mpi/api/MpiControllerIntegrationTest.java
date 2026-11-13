package pe.edu.unsch.pnihce.mpi.api;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import pe.edu.unsch.pnihce.mpi.TestcontainersConfiguration;
import pe.edu.unsch.pnihce.mpi.domain.Establecimiento;
import pe.edu.unsch.pnihce.mpi.domain.SectorEstablecimiento;
import pe.edu.unsch.pnihce.mpi.domain.TipoEstablecimiento;
import pe.edu.unsch.pnihce.mpi.repository.EstablecimientoRepository;
import pe.edu.unsch.pnihce.mpi.repository.VinculoPacienteRepository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Import(TestcontainersConfiguration.class)
@SpringBootTest
@AutoConfigureMockMvc
class MpiControllerIntegrationTest {

    @Autowired MockMvc mvc;
    @Autowired EstablecimientoRepository establecimientoRepo;
    @Autowired VinculoPacienteRepository vinculoRepo;

    private static final String DNI = "71234567";
    private static final String RENIPRESS = "TEST0001";

    private static final String VINCULO_JSON = """
            {
              "dni": "%s",
              "codigoRenipress": "%s",
              "idLocal": "%s",
              "fechaAtencion": "%s"
            }
            """;

    @BeforeEach
    void setUp() {
        vinculoRepo.deleteAll();
        establecimientoRepo.deleteAll();

        Establecimiento est = new Establecimiento();
        est.setCodigoRenipress(RENIPRESS);
        est.setNombre("Puesto de Salud Test");
        est.setTipo(TipoEstablecimiento.POSTA);
        est.setSector(SectorEstablecimiento.MINSA);
        est.setFhirBaseUrl("http://posta-test:8091/fhir");
        est.setActivo(true);
        establecimientoRepo.save(est);
    }

    // MPI-RF-01: idempotencia — dos PUT del mismo dni+establecimiento dejan un solo registro
    @Test
    void registrarVinculo_dosVeces_dejaUnSoloRegistro() throws Exception {
        String body = VINCULO_JSON.formatted(DNI, RENIPRESS, "HC-001", "2026-11-01");

        mvc.perform(put("/api/v1/mpi/vinculos")
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isCreated());

        mvc.perform(put("/api/v1/mpi/vinculos")
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isOk());

        assertThat(vinculoRepo.findByDniOrderByUltimaAtencionDesc(DNI)).hasSize(1);
    }

    // MPI-RF-02: DNI sin vínculos devuelve 200 con lista vacía
    @Test
    void buscarPorDni_sinVinculos_devuelveListaVacia() throws Exception {
        mvc.perform(get("/api/v1/mpi/pacientes/00000001/establecimientos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.establecimientos", hasSize(0)));
    }

    // MPI-RF-03: DNI inválido → 400 Problem Details
    @Test
    void buscarPorDni_dniInvalido_devuelve400() throws Exception {
        mvc.perform(get("/api/v1/mpi/pacientes/1234/establecimientos"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").exists());
    }

    // MPI-RF-05: segunda búsqueda del mismo DNI devuelve fuente=cache
    @Test
    void buscarPorDni_segundaConsulta_devuelveFuenteCache() throws Exception {
        String body = VINCULO_JSON.formatted(DNI, RENIPRESS, "HC-002", "2026-11-10");
        mvc.perform(put("/api/v1/mpi/vinculos")
                .contentType(MediaType.APPLICATION_JSON).content(body));

        mvc.perform(get("/api/v1/mpi/pacientes/" + DNI + "/establecimientos"));
        mvc.perform(get("/api/v1/mpi/pacientes/" + DNI + "/establecimientos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fuente").value("cache"));
    }

    // MPI-RF-05: tras un PUT la siguiente búsqueda devuelve fuente=db (caché invalidada)
    @Test
    void buscarPorDni_trasPut_invalidaCache() throws Exception {
        mvc.perform(put("/api/v1/mpi/vinculos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(VINCULO_JSON.formatted(DNI, RENIPRESS, "HC-003", "2026-11-01")));

        mvc.perform(get("/api/v1/mpi/pacientes/" + DNI + "/establecimientos"));

        mvc.perform(put("/api/v1/mpi/vinculos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(VINCULO_JSON.formatted(DNI, RENIPRESS, "HC-003", "2026-11-12")));

        mvc.perform(get("/api/v1/mpi/pacientes/" + DNI + "/establecimientos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fuente").value("db"));
    }

    // MPI-RF-04: vinculación a establecimiento inexistente → 422
    @Test
    void registrarVinculo_establecimientoInexistente_devuelve422() throws Exception {
        String body = VINCULO_JSON.formatted(DNI, "NOEXISTE", "HC-999", "2026-11-13");

        mvc.perform(put("/api/v1/mpi/vinculos")
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isUnprocessableEntity());
    }

    // Criterio 6: DNI que empieza con 7 se guarda (partición vinculo_paciente_p2)
    @Test
    void registrarVinculo_dniParticion70000000_guardaCorrectamente() throws Exception {
        String body = VINCULO_JSON.formatted("71234567", RENIPRESS, "HC-007", "2026-11-13");
        mvc.perform(put("/api/v1/mpi/vinculos")
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isCreated());

        assertThat(vinculoRepo.findByDniOrderByUltimaAtencionDesc("71234567")).hasSize(1);
    }
}
