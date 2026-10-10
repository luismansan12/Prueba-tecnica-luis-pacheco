package com.pruebatecnica.polizas.client;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.content;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;

class CoreEdicionRestAdapterTest {

    private static final String URL_EVENTO = "http://core.test/core-mock/evento";

    private MockRestServiceServer coreServer;
    private CoreEdicionRestAdapter adapter;

    @BeforeEach
    void setUp() {
        RestClient.Builder builder = RestClient.builder().baseUrl("http://core.test");
        coreServer = MockRestServiceServer.bindTo(builder).build();
        adapter = new CoreEdicionRestAdapter(builder.build());
    }

    @AfterEach
    void limpiarMdc() {
        MDC.clear();
    }

    @Test
    void enviaElEventoDeActualizacionConElCorrelationId() {
        MDC.put("correlationId", "abc-123");
        coreServer.expect(requestTo(URL_EVENTO))
                .andExpect(method(HttpMethod.POST))
                .andExpect(header("X-Correlation-Id", "abc-123"))
                .andExpect(content().json("{\"evento\": \"ACTUALIZACION\", \"polizaId\": 555}"))
                .andRespond(withStatus(HttpStatus.ACCEPTED));

        adapter.notificarActualizacion(555L);

        coreServer.verify();
    }

    @Test
    void unaFallaDelCoreNoAfectaLaOperacion() {
        coreServer.expect(requestTo(URL_EVENTO)).andRespond(withServerError());

        assertThatCode(() -> adapter.notificarActualizacion(555L)).doesNotThrowAnyException();
        coreServer.verify();
    }
}
