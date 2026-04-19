package com.logistics.packages.domain.valueobject;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class Direccion {
    private String direccion;
    private String ciudad;
    private String departamento;
    private String pais;
}
