package com.logistics.packages.application.ports;

import com.logistics.packages.domain.model.ZonaDestino;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Puerto para el repositorio de ZonaDestino
 * MOD1-IP-005: Define las operaciones necesarias para gestionar zonas de destino
 */
public interface ZonaDestinoRepository {
    
    /**
     * Encuentra una zona de destino por su ID.
     * 
     * @param id El ID de la zona de destino
     * @return Optional con la zona de destino si existe
     */
    Optional<ZonaDestino> findById(UUID id);
    
    /**
     * Obtiene todas las zonas de destino activas.
     * 
     * @return Lista de zonas de destino activas
     */
    List<ZonaDestino> findAllActivas();
    
    /**
     * Guarda o actualiza una zona de destino.
     * 
     * @param zonaDestino La zona de destino a guardar
     * @return La zona de destino guardada
     */
    ZonaDestino save(ZonaDestino zonaDestino);
    
    /**
     * Encuentra zonas de destino por sede.
     * 
     * @param sedeId El ID de la sede
     * @return Lista de zonas de destino de la sede
     */
    List<ZonaDestino> findBySedeId(UUID sedeId);
}
