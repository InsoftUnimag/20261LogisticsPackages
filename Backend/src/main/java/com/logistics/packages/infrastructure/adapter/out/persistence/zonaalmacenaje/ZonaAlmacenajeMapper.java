package com.logistics.packages.infrastructure.adapter.out.persistence.zonaalmacenaje;

import com.logistics.packages.domain.model.ZonaAlmacenaje;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

@Mapper(componentModel = "spring")
public interface ZonaAlmacenajeMapper {

    @Mapping(source = "categoria", target = "categoria", qualifiedByName = "categoriaZonaToString")
    @Mapping(source = "estado", target = "estado", qualifiedByName = "estadoZonaToString")
    ZonaAlmacenajeDbo toDbo(ZonaAlmacenaje domain);

    @Mapping(source = "categoria", target = "categoria", qualifiedByName = "stringToCategoriaZona")
    @Mapping(source = "estado", target = "estado", qualifiedByName = "stringToEstadoZona")
    ZonaAlmacenaje toDomain(ZonaAlmacenajeDbo dbo);

    @Named("categoriaZonaToString")
    default String categoriaZonaToString(ZonaAlmacenaje.CategoriaZona categoria) {
        return categoria != null ? categoria.name() : null;
    }

    @Named("stringToCategoriaZona")
    default ZonaAlmacenaje.CategoriaZona stringToCategoriaZona(String categoria) {
        return categoria != null ? ZonaAlmacenaje.CategoriaZona.valueOf(categoria) : null;
    }

    @Named("estadoZonaToString")
    default String estadoZonaToString(ZonaAlmacenaje.EstadoZona estado) {
        return estado != null ? estado.name() : null;
    }

    @Named("stringToEstadoZona")
    default ZonaAlmacenaje.EstadoZona stringToEstadoZona(String estado) {
        return estado != null ? ZonaAlmacenaje.EstadoZona.valueOf(estado) : null;
    }
}
