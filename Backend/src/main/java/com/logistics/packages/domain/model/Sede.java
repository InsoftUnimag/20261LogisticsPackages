package com.logistics.packages.domain.model;

import com.logistics.packages.domain.valueobject.Direccion;
import com.logistics.packages.domain.valueobject.MetodoPago;
import com.logistics.packages.domain.valueobject.TipoSede;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Getter
@Builder
@AllArgsConstructor
public class Sede {
    private UUID id;
    private String nombre;
    private Direccion direccion;
    private TipoSede tipo;
    private BigDecimal capacidadMaximaPeso;
    private BigDecimal capacidadMaximaVolumen;
    private BigDecimal tarifaBase;
    private BigDecimal tarifaPorKg;
    private BigDecimal tarifaPorKm;
    private List<MetodoPago> metodosPagoHabilitados;
    private Double latitud;
    private Double longitud;

    public boolean validarMetodoPagoSoportado(MetodoPago metodoPago) {
        return metodosPagoHabilitados != null && metodosPagoHabilitados.contains(metodoPago);
    }

    public boolean tieneCapacidadPara(BigDecimal peso, BigDecimal volumen) {
        boolean capacidadPeso = capacidadMaximaPeso == null || capacidadMaximaPeso.compareTo(peso) >= 0;
        boolean capacidadVolumen = capacidadMaximaVolumen == null || capacidadMaximaVolumen.compareTo(volumen) >= 0;
        return capacidadPeso && capacidadVolumen;
    }
}