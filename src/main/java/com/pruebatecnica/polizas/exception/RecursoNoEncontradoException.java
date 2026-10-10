package com.pruebatecnica.polizas.exception;

/**
 * Se lanza cuando no existe la póliza o el riesgo solicitado (se responde con 404).
 */
public class RecursoNoEncontradoException extends RuntimeException {

    public RecursoNoEncontradoException(String recurso, Long id) {
        super("No existe %s con id %d".formatted(recurso, id));
    }
}
