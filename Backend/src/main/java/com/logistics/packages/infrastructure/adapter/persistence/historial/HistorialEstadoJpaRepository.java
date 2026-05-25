package com.logistics.packages.infrastructure.adapter.persistence.historial;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * Repositorio JPA para el historial de estados.
 * MOD1-UC-006: Acceso a datos del historial.
 */
@Repository
public interface HistorialEstadoJpaRepository extends JpaRepository<HistorialEstadoEntity, UUID> {
    
    /**
     * Encuentra todos los registros de historial de un paquete ordenados cronológicamente.
     * 
     * @param paqueteId ID del paquete
     * @return Lista ordenada de registros de historial
     */
    List<HistorialEstadoEntity> findByPaqueteIdOrderByFechaTransicionUtcAsc(UUID paqueteId);

    /**
     * Encuentra todas las novedades (registros donde tipoNovedad no es null).
     * Ordenadas del más reciente al más antiguo para listar primero las pendientes.
     * 
     * @return Lista de todas las novedades ordenadas por fecha descendente
     */
    List<HistorialEstadoEntity> findByTipoNovedadIsNotNullOrderByFechaTransicionUtcDesc();
}
