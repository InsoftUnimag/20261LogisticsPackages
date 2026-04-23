package com.logistics.packages.domain.model;

import com.logistics.packages.domain.valueobject.MetodoPago;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class Sede {
    private String id;
    private String nombre;
    private String ciudad;
    private List<MetodoPago> metodosPagoSoportados;

    public boolean validarMetodoPagoSoportado(MetodoPago metodoPago) {
        return metodosPagoSoportados.contains(metodoPago);
    }
}
