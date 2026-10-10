package com.pruebatecnica.polizas.client;

import com.pruebatecnica.polizas.config.CorrelationIdFilter;
import com.pruebatecnica.polizas.dto.CoreEventoRequest;
import com.pruebatecnica.polizas.service.CoreEdicionPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

/**
 * Adapter que envía los cambios al servicio de edición del CORE por HTTP.
 * Hoy apunta al mock /core-mock/evento; en un ambiente real solo cambia la URL.
 */
@Component
public class CoreEdicionRestAdapter implements CoreEdicionPort {

    private static final Logger log = LoggerFactory.getLogger(CoreEdicionRestAdapter.class);
    private static final String EVENTO_ACTUALIZACION = "ACTUALIZACION";

    private final RestClient coreRestClient;

    public CoreEdicionRestAdapter(RestClient coreRestClient) {
        this.coreRestClient = coreRestClient;
    }

    @Override
    public void notificarActualizacion(Long polizaId) {
        log.info("Enviando evento {} de la póliza {} al CORE", EVENTO_ACTUALIZACION, polizaId);
        String correlationId = MDC.get(CorrelationIdFilter.MDC_KEY);
        try {
            coreRestClient.post()
                    .uri("/core-mock/evento")
                    .headers(headers -> {
                        if (correlationId != null) {
                            headers.set(CorrelationIdFilter.HEADER, correlationId);
                        }
                    })
                    .body(new CoreEventoRequest(EVENTO_ACTUALIZACION, polizaId))
                    .retrieve()
                    .toBodilessEntity();
        } catch (RuntimeException ex) {
            // El cambio ya quedó guardado; una falla del CORE no debe afectar la operación del usuario.
            log.warn("No se pudo enviar al CORE el evento de la póliza {}: {}", polizaId, ex.getMessage());
        }
    }
}
