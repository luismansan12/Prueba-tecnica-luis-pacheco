package com.pruebatecnica.polizas.service;

import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * Envía el cambio al CORE solo cuando la transacción se confirmó, para no avisar
 * de cambios que finalmente no quedaron guardados.
 */
@Component
public class CoreEdicionListener {

    private final CoreEdicionPort coreEdicionPort;

    public CoreEdicionListener(CoreEdicionPort coreEdicionPort) {
        this.coreEdicionPort = coreEdicionPort;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void alActualizarPoliza(PolizaActualizadaEvent evento) {
        coreEdicionPort.notificarActualizacion(evento.polizaId());
    }
}
