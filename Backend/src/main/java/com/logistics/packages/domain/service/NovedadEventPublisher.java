package com.logistics.packages.domain.service;

import com.logistics.packages.domain.model.NovedadBodega;

/**
 * Puerto (interface) para publicar eventos de novedades.
 * Define el contrato para notificar al Controlador de Novedades.
 * Arquitectura Hexagonal: Puerto de Salida (Output Port)
 * MOD1-IP-006: Actualizar Estado de Paquete por Novedad
 * FR-005: Notificar automáticamente al Controlador de Novedades
 */
public interface NovedadEventPublisher {
    
    /**
     * Publica un evento indicando que se ha registrado una nueva novedad.
     * El evento será consumido por el Controlador de Novedades (MOD1-UC-007).
     * 
     * @param novedad Novedad que se ha registrado
     */
    void publicarNovedadRegistrada(NovedadBodega novedad);
}
