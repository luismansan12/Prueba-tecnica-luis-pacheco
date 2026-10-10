package com.pruebatecnica.polizas.service;

/**
 * Evento interno que se publica cada vez que cambia el estado de una póliza o de sus riesgos.
 */
public record PolizaActualizadaEvent(Long polizaId) {
}
