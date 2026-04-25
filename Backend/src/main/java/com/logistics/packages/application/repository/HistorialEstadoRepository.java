package com.logistics.packages.application.repository;

import com.logistics.packages.domain.model.HistorialEstado;

import java.util.List;
import java.util.UUID;

/**
 * Puerto de salida (Repository) para persistir el historial de estados de paquetes.
 * MOD1-UC-006: FR-003 - Mantener historial cronológico inmutable.
 */
public interface HistorialEstadoRepository {
    
    /**
     * Guarda un nuevo registro en el historial de estados.
     * 
     * @param historial Registro de historial a persistir
     * @return HistorialEstado persistido con ID generado
     */
    HistorialEstado guardar(HistorialEstado historial);
    
    /**
     * Obtiene todo el historial de un paquete específico, ordenado cronológicamente.
     * 
     * @param paqueteId ID del paquete
     * @return Lista de registros de historial ordenados por fecha
     */
    List<HistorialEstado> obtenerHistorialPorPaqueteId(UUID paqueteId);
}
