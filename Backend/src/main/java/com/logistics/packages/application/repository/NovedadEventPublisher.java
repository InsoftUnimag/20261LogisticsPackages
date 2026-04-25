package com.logistics.packages.application.repository;

import java.util.UUID;

/**
 * Puerto de salida para publicar eventos de novedades al sistema de mensajería.
 * MOD1-UC-006: FR-005 - Notificar al Controlador de Novedades.
 */
public interface NovedadEventPublisher {
    
    /**
     * Publica un evento cuando se registra una novedad en un paquete.
     * El evento debe ser procesado por el sistema de gestión de novedades (MOD1-UC-007).
     * 
     * @param paqueteId ID del paquete con novedad
     * @param historialId ID del registro de historial asociado
     */
    void publicarNovedadRegistrada(UUID paqueteId, UUID historialId);
}
