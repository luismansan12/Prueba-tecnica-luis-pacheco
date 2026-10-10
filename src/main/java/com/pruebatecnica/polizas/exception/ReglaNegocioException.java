package com.pruebatecnica.polizas.exception;

/**
 * Se lanza cuando una operación no cumple una regla de negocio (se responde con 409).
 */
public class ReglaNegocioException extends RuntimeException {

    public ReglaNegocioException(String mensaje) {
        super(mensaje);
    }
}
