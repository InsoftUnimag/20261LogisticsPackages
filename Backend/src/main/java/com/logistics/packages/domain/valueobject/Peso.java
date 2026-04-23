package com.logistics.packages.domain.valueobject;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Value Object que representa el peso de un paquete.
 * FR-001: El peso debe ser mayor a 0 y no puede exceder los 70 kg.
 */
@Getter
@AllArgsConstructor
public class Peso {
    private final Double kilogramos;

    public Peso(Double kilogramos) {
        if (kilogramos == null || kilogramos <= 0) {
            throw new IllegalArgumentException("El peso debe ser mayor a cero.");
        }
        if (kilogramos > 70) {
            throw new IllegalArgumentException("El peso no puede exceder los 70 kg.");
        }
        this.kilogramos = kilogramos;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Peso peso = (Peso) o;
        return kilogramos.equals(peso.kilogramos);
    }

    @Override
    public int hashCode() {
        return kilogramos.hashCode();
    }

    @Override
    public String toString() {
        return kilogramos + " kg";
    }
}
