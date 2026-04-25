package com.logistics.packages.infrastructure.adapter.persistence.zonadestino;

import com.logistics.packages.domain.model.ZonaDestino;
import org.mapstruct.Mapper;

/**
 * Mapper para conversión entre ZonaDestino (dominio) y ZonaDestinoDbo (persistencia)
 * MOD1-IP-005
 */
@Mapper(componentModel = "spring")
public interface ZonaDestinoMapper {

    /**
     * Convierte de dominio a entidad JPA.
     */
    ZonaDestinoDbo toDbo(ZonaDestino domain);

    /**
     * Convierte de entidad JPA a dominio.
     */
    ZonaDestino toDomain(ZonaDestinoDbo dbo);
}
