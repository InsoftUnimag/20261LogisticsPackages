package com.logistics.packages.domain.model;

import com.logistics.packages.domain.valueobject.Direccion;
import com.logistics.packages.domain.valueobject.TipoDocumento;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@Embeddable
@AllArgsConstructor
@NoArgsConstructor(force = true)
public class Persona {
    private final TipoDocumento tipoDocumento;
    private final String numeroDocumento;
    private final String nombreCompleto;
    private final String telefono;
    private final String correoElectronico;
    private final Direccion direccion;
}
