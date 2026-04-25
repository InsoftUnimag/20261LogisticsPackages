package com.logistics.packages.infrastructure.adapter.persistence.eventoprocesado;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repositorio JPA para eventos procesados.
 * MOD1-UC-007: FR-008 - Persistencia de eventos para idempotencia.
 */
@Repository
public interface EventoProcesadoJpaRepository extends JpaRepository<EventoProcesadoEntity, String> {
    
    /**
     * Verifica si un evento ya existe en la base de datos.
     * 
     * @param eventoId ID del evento
     * @return true si existe, false en caso contrario
     */
    boolean existsByEventoId(String eventoId);
}
