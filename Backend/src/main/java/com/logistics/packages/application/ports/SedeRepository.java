package com.logistics.packages.application.ports;

import com.logistics.packages.domain.model.Sede;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Puerto de aplicación para acceso a datos de Sedes.
 * Desacopla la lógica de aplicación de la persistencia.
 */
public interface SedeRepository {
    
    /**
     * Obtiene todas las sedes disponibles.
     * 
     * @return Lista de todas las sedes
     */
    List<Sede> findAll();
    
    /**
     * Obtiene una sede por su ID.
     * 
     * @param id ID de la sede
     * @return Sede envuelta en Optional, o vacío si no existe
     */
    Optional<Sede> findById(UUID id);
}
