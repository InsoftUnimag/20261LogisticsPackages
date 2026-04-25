package com.logistics.packages.application.ports;

import com.logistics.packages.domain.model.ZonaAlmacenaje;
import com.logistics.packages.domain.valueobject.CategoriaZona;
import com.logistics.packages.domain.valueobject.TipoMercancia;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Puerto de salida para la gestión de zonas de almacenamiento.
 * Define las operaciones necesarias para la persistencia de zonas.
 */
public interface ZonaAlmacenajeRepository {
    
    /**
     * Busca una zona de almacenamiento por su ID.
     * 
     * @param id El ID de la zona
     * @return Optional con la zona si existe
     */
    Optional<ZonaAlmacenaje> findById(UUID id);
    
    /**
     * Guarda o actualiza una zona de almacenamiento.
     * 
     * @param zona La zona a guardar
     * @return La zona guardada
     */
    ZonaAlmacenaje save(ZonaAlmacenaje zona);
    
    /**
     * FR-002: Busca zonas compatibles con un tipo de mercancía específico.
     * Retorna solo zonas que tengan capacidad disponible.
     * 
     * @param tipoMercancia El tipo de mercancía
     * @param sedeId El ID de la sede
     * @return Lista de zonas compatibles y con capacidad
     */
    List<ZonaAlmacenaje> findZonasCompatiblesConCapacidad(TipoMercancia tipoMercancia, UUID sedeId);
    
    /**
     * Busca todas las zonas de una sede específica.
     * 
     * @param sedeId El ID de la sede
     * @return Lista de zonas de la sede
     */
    List<ZonaAlmacenaje> findBySedeId(UUID sedeId);
    
    /**
     * Busca zonas por categoría en una sede específica.
     * 
     * @param categoria La categoría de la zona
     * @param sedeId El ID de la sede
     * @return Lista de zonas con esa categoría
     */
    List<ZonaAlmacenaje> findByCategoriaAndSedeId(CategoriaZona categoria, UUID sedeId);
}
