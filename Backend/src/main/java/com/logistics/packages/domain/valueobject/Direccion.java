package com.logistics.packages.domain.valueobject;

import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Embeddable
public class Direccion {
    private String direccion;
    private String ciudad;
    private String departamento;
    private String pais;

    public String getDireccionCompleta() {
        return String.format("%s, %s, %s, %s", direccion, ciudad, departamento, pais);
    }

    @Override
    public String toString() {
        return getDireccionCompleta();
    }
}
