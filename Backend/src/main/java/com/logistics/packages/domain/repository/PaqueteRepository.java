package com.logistics.packages.domain.repository;

import com.logistics.packages.domain.model.Paquete;

import java.util.Optional;
import java.util.UUID;

/**
 * Puerto (interface) para el repositorio de Paquetes.
 * Define el contrato para persistencia sin acoplar el dominio a detalles de infraestructura.
 * Arquitectura Hexagonal: Puerto de Salida (Output Port)
 */
public interface PaqueteRepository {
    
    /**
     * Busca un paquete por su ID.
     * 
     * @param id ID del paquete
     * @return Optional conteniendo el paquete si existe
     */
    Optional<Paquete> findById(UUID id);
    
    /**
     * Guarda o actualiza un paquete.
     * 
     * @param paquete Paquete a guardar
     * @return Paquete guardado
     */
    Paquete save(Paquete paquete);
    
    /**
     * Verifica si existe un paquete con el ID dado.
     * 
     * @param id ID del paquete
     * @return true si el paquete existe
     */
    boolean existsById(UUID id);
}
