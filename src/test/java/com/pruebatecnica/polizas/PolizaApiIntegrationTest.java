package com.pruebatecnica.polizas;

import com.pruebatecnica.polizas.service.CoreEdicionPort;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Prueba la API completa sobre los datos de data.sql. Cada prueba que modifica
 * datos usa una póliza distinta para que no dependan del orden de ejecución.
 * El puerto hacia el CORE se reemplaza por un mock para verificar que se notifique.
 */
@SpringBootTest
@AutoConfigureMockMvc
class PolizaApiIntegrationTest {

    private static final String API_KEY_HEADER = "api-key";
    private static final String API_KEY = "123456";

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CoreEdicionPort coreEdicionPort;

    @Test
    void sinApiKeyResponde401() throws Exception {
        mockMvc.perform(get("/polizas"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.title").value("No autorizado"));
    }

    @Test
    void conApiKeyIncorrectaResponde401() throws Exception {
        mockMvc.perform(get("/polizas").header(API_KEY_HEADER, "000000"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void listarPorTipoYEstadoDevuelveSoloLasQueCumplenElFiltro() throws Exception {
        mockMvc.perform(conApiKey(get("/polizas").param("tipo", "COLECTIVA").param("estado", "ACTIVA")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].tipo", everyItem(is("COLECTIVA"))))
                .andExpect(jsonPath("$[*].estado", everyItem(is("ACTIVA"))))
                .andExpect(jsonPath("$[*].id", hasItem(2)));
    }

    @Test
    void listarConUnTipoInvalidoResponde400() throws Exception {
        mockMvc.perform(conApiKey(get("/polizas").param("tipo", "XYZ")))
                .andExpect(status().isBadRequest());
    }

    @Test
    void listarRiesgosDeUnaPoliza() throws Exception {
        mockMvc.perform(conApiKey(get("/polizas/1/riesgos")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].polizaId").value(1));
    }

    @Test
    void renovarIncrementaCanonYPrimaYNotificaAlCore() throws Exception {
        // Póliza 555: canon 2.000.000 x (1 + 0,052) = 2.104.000 ; prima = 2.104.000 x 12
        mockMvc.perform(conApiKey(post("/polizas/555/renovar")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("RENOVADA"))
                .andExpect(jsonPath("$.canonMensual").value(2104000))
                .andExpect(jsonPath("$.prima").value(25248000));

        verify(coreEdicionPort).notificarActualizacion(555L);
    }

    @Test
    void renovarUnaPolizaCanceladaResponde409() throws Exception {
        mockMvc.perform(conApiKey(post("/polizas/3/renovar")))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.detail").value("No se puede renovar una póliza cancelada"));

        verify(coreEdicionPort, never()).notificarActualizacion(anyLong());
    }

    @Test
    void cancelarUnaPolizaCancelaTodosSusRiesgos() throws Exception {
        mockMvc.perform(conApiKey(post("/polizas/6/cancelar")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("CANCELADA"));

        mockMvc.perform(conApiKey(get("/polizas/6/riesgos")))
                .andExpect(jsonPath("$[*].estado", everyItem(is("CANCELADO"))));
        verify(coreEdicionPort).notificarActualizacion(6L);
    }

    @Test
    void agregarRiesgoAUnaColectivaResponde201() throws Exception {
        mockMvc.perform(conApiKey(post("/polizas/2/riesgos"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(riesgoJson("Cl 10 # 43-12 Apto 903")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.polizaId").value(2))
                .andExpect(jsonPath("$.estado").value("ACTIVO"));

        verify(coreEdicionPort).notificarActualizacion(2L);
    }

    @Test
    void agregarRiesgoAUnaIndividualResponde409() throws Exception {
        mockMvc.perform(conApiKey(post("/polizas/1/riesgos"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(riesgoJson("Cra 15 # 93-22")))
                .andExpect(status().isConflict());
    }

    @Test
    void agregarRiesgoConDatosIncompletosResponde400() throws Exception {
        mockMvc.perform(conApiKey(post("/polizas/2/riesgos"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"direccionInmueble\": \"\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errores.direccionInmueble").exists());
    }

    @Test
    void cancelarUnRiesgoYaCanceladoResponde409() throws Exception {
        mockMvc.perform(conApiKey(post("/riesgos/4/cancelar")))
                .andExpect(status().isConflict());
    }

    @Test
    void cancelarUnRiesgoActivoDeUnaColectiva() throws Exception {
        mockMvc.perform(conApiKey(post("/riesgos/7/cancelar")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("CANCELADO"));

        verify(coreEdicionPort).notificarActualizacion(5L);
    }

    @Test
    void unaPolizaInexistenteResponde404() throws Exception {
        mockMvc.perform(conApiKey(post("/polizas/9999/cancelar")))
                .andExpect(status().isNotFound());
    }

    @Test
    void elMockDelCoreAceptaElEvento() throws Exception {
        mockMvc.perform(conApiKey(post("/core-mock/evento"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"evento\": \"ACTUALIZACION\", \"polizaId\": 555}"))
                .andExpect(status().isAccepted())
                .andExpect(header().exists("X-Correlation-Id"));
    }

    @Test
    void elMockDelCoreSinPolizaIdResponde400() throws Exception {
        mockMvc.perform(conApiKey(post("/core-mock/evento"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"evento\": \"ACTUALIZACION\"}"))
                .andExpect(status().isBadRequest());
    }

    private static MockHttpServletRequestBuilder conApiKey(MockHttpServletRequestBuilder request) {
        return request.header(API_KEY_HEADER, API_KEY);
    }

    private static String riesgoJson(String direccion) {
        return """
                {"direccionInmueble": "%s", "arrendatario": "Nuevo arrendatario", "arrendador": "María Restrepo"}
                """.formatted(direccion);
    }
}
