package com.logistics.packages.infrastructure.adapter.persistence.persona;

import com.logistics.packages.domain.model.Persona;
import com.logistics.packages.domain.valueobject.Direccion;
import com.logistics.packages.domain.valueobject.TipoDocumento;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

@Mapper(componentModel = "spring")
public interface PersonaMapper {

    /**
     * FIX Bug 6: Mapea Persona domain a PersonaDbo.
     * Se elimina la conversión innecesaria de TipoDocumento a String ya que PersonaDbo.tipoDocumento
     * también es de tipo TipoDocumento enum. MapStruct puede hacer el mapeo directo enum→enum.
     */
    @Mapping(target = "id", ignore = true)
    PersonaDbo toDbo(Persona domain);

    Persona toDomain(PersonaDbo dbo);
}
