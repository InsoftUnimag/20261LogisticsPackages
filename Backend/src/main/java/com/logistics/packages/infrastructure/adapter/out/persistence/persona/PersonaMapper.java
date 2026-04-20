package com.logistics.packages.infrastructure.adapter.out.persistence.persona;

import com.logistics.packages.domain.model.Persona;
import com.logistics.packages.domain.valueobject.TipoDocumento;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

@Mapper(componentModel = "spring")
public interface PersonaMapper {

    @Mapping(source = "tipoDocumento", target = "tipoDocumento", qualifiedByName = "tipoDocumentoToString")
    PersonaDbo toDbo(Persona domain);

    @Mapping(source = "tipoDocumento", target = "tipoDocumento", qualifiedByName = "stringToTipoDocumento")
    Persona toDomain(PersonaDbo dbo);

    @Named("tipoDocumentoToString")
    default String tipoDocumentoToString(TipoDocumento tipoDocumento) {
        return tipoDocumento != null ? tipoDocumento.name() : null;
    }

    @Named("stringToTipoDocumento")
    default TipoDocumento stringToTipoDocumento(String tipoDocumento) {
        return tipoDocumento != null ? TipoDocumento.valueOf(tipoDocumento) : null;
    }
}
