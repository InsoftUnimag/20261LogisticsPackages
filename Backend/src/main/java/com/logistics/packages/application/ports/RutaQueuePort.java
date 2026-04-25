package com.logistics.packages.application.ports;

import com.logistics.packages.infrastructure.dto.request.SolicitudRutaPayload;

/**
 * Puerto de salida para la comunicación asíncrona con el Módulo de Gestión de Rutas
 * MOD1-IP-003 - Phase 2
 * FR-001: Comunicación estrictamente asíncrona mediante payloads JSON
 */
public interface RutaQueuePort {
    
    /**
     * Envía una solicitud de ruta al Módulo de Gestión de Rutas de forma asíncrona
     * 
     * @param payload El payload con los datos del paquete
     */
    void enviarSolicitud(SolicitudRutaPayload payload);
}
