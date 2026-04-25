package com.logistics.packages.infrastructure.adapter.persistence.zonadestino;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * Repositorio JPA para ZonaDestino
 * MOD1-IP-005
 */
@Repository
public interface ZonaDestinoJpaRepository extends JpaRepository<ZonaDestinoDbo, UUID> {
    
    /**
     * Encuentra todas las zonas de destino activas (con capacidad disponible).
     * 
     * @return Lista de zonas activas
     */
    @Query("SELECT z FROM ZonaDestinoDbo z WHERE z.contadorPaquetes < z.capacidadMaxPaquetes OR z.capacidadMaxPaquetes IS NULL")
    List<ZonaDestinoDbo> findAllActivas();
    
    /**
     * Encuentra zonas de destino por sede.
     * 
     * @param sedeId El ID de la sede
     * @return Lista de zonas de la sede
     */
    List<ZonaDestinoDbo> findByIdSede(UUID sedeId);
}
