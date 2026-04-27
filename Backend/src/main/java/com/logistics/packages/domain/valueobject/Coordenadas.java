package com.logistics.packages.domain.valueobject;

import com.logistics.packages.domain.exception.CoordenadasInvalidasException;

public record Coordenadas(double latitud, double longitud) {
    public Coordenadas {
        if (latitud < -90 || latitud > 90) {
            throw new CoordenadasInvalidasException("Latitud fuera de rango válido [-90, 90]");
        }
        if (longitud < -180 || longitud > 180) {
            throw new CoordenadasInvalidasException("Longitud fuera de rango válido [-180, 180]");
        }
    }

    @Override
    public String toString() {
        return String.format("(%f, %f)", latitud, longitud);
    }
}
