package com.logistics.packages.infrastructure.adapter.persistence.persona;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Entity
@Table(name = "personas")
@Getter
@Setter
public class PersonaDbo {

    @Id
    private UUID id;

    @Column(name = "tipo_documento")
    private String tipoDocumento;

    @Column(name = "numero_documento", unique = true)
    private String numeroDocumento;

    @Column(name = "nombre_completo")
    private String nombreCompleto;

    private String telefono;

    @Column(name = "correo_electronico")
    private String correoElectronico;

    private String direccion;
}
