package com.logistics.packages.domain.model;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class Persona {
    private final String tipoDocumento;
    private final String numeroDocumento;
    private final String nombreCompleto;
    private final String telefono;
    private final String correoElectronico;
    private final String direccion;
}
