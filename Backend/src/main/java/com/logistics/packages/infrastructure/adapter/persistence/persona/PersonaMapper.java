package com.logistics.packages.infrastructure.adapter.persistence.persona;

import com.logistics.packages.domain.model.Persona;
import com.logistics.packages.domain.valueobject.Direccion;
import com.logistics.packages.domain.valueobject.TipoDocumento;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

@Mapper(componentModel = "spring")
public interface PersonaMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(source = "tipoDocumento", target = "tipoDocumento", qualifiedByName = "tipoDocumentoToString")
    @Mapping(source = "direccion", target = "direccion", qualifiedByName = "direccionToDto")
    PersonaDbo toDbo(Persona domain);

    @Mapping(source = "tipoDocumento", target = "tipoDocumento", qualifiedByName = "stringToTipoDocumento")
    @Mapping(source = "direccion", target = "direccion", qualifiedByName = "dtoToDireccion")
    Persona toDomain(PersonaDbo dbo);

    @Named("tipoDocumentoToString")
    default String tipoDocumentoToString(TipoDocumento tipoDocumento) {
        return tipoDocumento != null ? tipoDocumento.name() : null;
    }

    @Named("stringToTipoDocumento")
    default TipoDocumento stringToTipoDocumento(String tipoDocumento) {
        return tipoDocumento != null ? TipoDocumento.valueOf(tipoDocumento) : null;
    }

    @Named("direccionToDto")
    default com.logistics.packages.domain.valueobject.Direccion mapToDto(com.logistics.packages.domain.valueobject.Direccion direccion) {
        return direccion;
    }

    @Named("dtoToDireccion")
    default com.logistics.packages.domain.valueobject.Direccion mapFromDto(com.logistics.packages.domain.valueobject.Direccion direccion) {
        return direccion;
    }
}
