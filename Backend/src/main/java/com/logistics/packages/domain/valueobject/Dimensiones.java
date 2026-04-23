package com.logistics.packages.domain.valueobject;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Value Object que representa las dimensiones de un paquete en centímetros.
 * FR-003: Las dimensiones deben ser mayores a 0.
 */
@Getter
@AllArgsConstructor
public class Dimensiones {
    private final Double largoCm;
    private final Double anchoCm;
    private final Double altoCm;

    public Dimensiones(Double largoCm, Double anchoCm, Double altoCm) {
        if (largoCm == null || largoCm <= 0) {
            throw new IllegalArgumentException("El largo debe ser mayor a cero.");
        }
        if (anchoCm == null || anchoCm <= 0) {
            throw new IllegalArgumentException("El ancho debe ser mayor a cero.");
        }
        if (altoCm == null || altoCm <= 0) {
            throw new IllegalArgumentException("El alto debe ser mayor a cero.");
        }
        this.largoCm = largoCm;
        this.anchoCm = anchoCm;
        this.altoCm = altoCm;
    }

    /**
     * Calcula el volumen en metros cúbicos.
     * FR-004: Volumen = (L × A × H) / 1,000,000
     */
    public Double calcularVolumenM3() {
        return (largoCm * anchoCm * altoCm) / 1_000_000.0;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Dimensiones that = (Dimensiones) o;
        return largoCm.equals(that.largoCm) && 
               anchoCm.equals(that.anchoCm) && 
               altoCm.equals(that.altoCm);
    }

    @Override
    public int hashCode() {
        int result = largoCm.hashCode();
        result = 31 * result + anchoCm.hashCode();
        result = 31 * result + altoCm.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return largoCm + "cm × " + anchoCm + "cm × " + altoCm + "cm";
    }
}
