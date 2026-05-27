package com.logistics.packages.application.repository;

import com.logistics.packages.domain.model.Sede;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Puerto de repositorio para la entidad Sede.
 * Define las operaciones de persistencia de sedes.
 */
public interface SedeRepository {
    
    /**
     * Obtiene todas las sedes activas del sistema.
     * @return Lista de sedes disponibles
     */
    List<Sede> findAll();
    
    /**
     * Obtiene una sede específica por su ID.
     * @param id ID de la sede
     * @return Optional con la sede si existe
     */
    Optional<Sede> findById(UUID id);
    
    /**
     * Obtiene todas las sedes de un tipo específico (PRINCIPAL o AUXILIAR).
     * @param tipo Tipo de sede
     * @return Lista de sedes del tipo especificado
     */
    List<Sede> findByTipo(String tipo);
}
