package com.logistics.packages.domain.model;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import com.logistics.packages.domain.valueobject.Direccion;
import com.logistics.packages.domain.valueobject.TipoDocumento;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(force = true)
@JsonDeserialize(builder = Persona.PersonaBuilder.class)
public class Persona {
    private final TipoDocumento tipoDocumento;
    private final String numeroDocumento;
    private final String nombreCompleto;
    private final String telefono;
    private final String correoElectronico;
    private final Direccion direccion;

    @JsonPOJOBuilder(withPrefix = "")
    public static class PersonaBuilder {
    }
}
