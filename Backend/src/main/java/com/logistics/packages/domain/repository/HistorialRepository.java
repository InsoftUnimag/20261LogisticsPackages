package com.logistics.packages.domain.repository;

import com.logistics.packages.domain.model.HistorialEstado;

import java.util.List;
import java.util.UUID;

/**
 * Puerto (interface) para el repositorio de Historial de Estados.
 * Define el contrato para persistencia de transiciones de estado.
 * Arquitectura Hexagonal: Puerto de Salida (Output Port)
 * MOD1-IP-006: Actualizar Estado de Paquete por Novedad
 */
public interface HistorialRepository {
    
    /**
     * Guarda una entrada de historial.
     * FR-003: Historial inmutable (solo INSERT, no UPDATE)
     * 
     * @param entrada Entrada de historial a guardar
     * @return Entrada guardada
     */
    HistorialEstado save(HistorialEstado entrada);
    
    /**
     * Obtiene el historial completo de un paquete ordenado cronológicamente.
     * 
     * @param paqueteId ID del paquete
     * @return Lista ordenada de entradas de historial
     */
    List<HistorialEstado> findByPaqueteIdOrderByFechaTransicionAsc(UUID paqueteId);
    
    /**
     * Obtiene la última entrada del historial de un paquete.
     * 
     * @param paqueteId ID del paquete
     * @return Última entrada del historial si existe
     */
    HistorialEstado findUltimaEntrada(UUID paqueteId);
}
