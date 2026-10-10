package com.pruebatecnica.polizas.service;

/**
 * Puerto hacia el servicio de edición que mantiene actualizado el CORE de seguros.
 * La implementación concreta (hoy el mock, mañana WebLogic) vive en el paquete client.
 */
public interface CoreEdicionPort {

    void notificarActualizacion(Long polizaId);
}
