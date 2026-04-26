package com.logistics.packages.domain.valueobject;

public record Coordenadas(double latitud, double longitud) {
    public Coordenadas {
        if (latitud < -90 || latitud > 90) {
            throw new IllegalArgumentException("Latitud fuera de rango");
        }
        if (longitud < -180 || longitud > 180) {
            throw new IllegalArgumentException("Longitud fuera de rango");
        }
    }

    @Override
    public String toString() {
        return String.format("(%f, %f)", latitud, longitud);
    }
}
