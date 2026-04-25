package com.logistics.packages.domain.repository;

import com.logistics.packages.domain.model.NovedadBodega;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Puerto (interface) para el repositorio de Novedades en Bodega.
 * Define el contrato para persistencia de novedades.
 * Arquitectura Hexagonal: Puerto de Salida (Output Port)
 * MOD1-IP-006: Actualizar Estado de Paquete por Novedad
 */
public interface NovedadRepository {
    
    /**
     * Guarda una novedad en bodega.
     * 
     * @param novedad Novedad a guardar
     * @return Novedad guardada
     */
    NovedadBodega save(NovedadBodega novedad);
    
    /**
     * Busca una novedad por su ID.
     * 
     * @param id ID de la novedad
     * @return Optional conteniendo la novedad si existe
     */
    Optional<NovedadBodega> findById(UUID id);
    
    /**
     * Obtiene todas las novedades de un paquete.
     * 
     * @param paqueteId ID del paquete
     * @return Lista de novedades del paquete
     */
    List<NovedadBodega> findByPaqueteId(UUID paqueteId);
}
