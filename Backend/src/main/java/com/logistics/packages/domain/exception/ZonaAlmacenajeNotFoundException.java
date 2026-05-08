package com.logistics.packages.domain.exception;

import java.util.UUID;

public class ZonaAlmacenajeNotFoundException extends RuntimeException {

    public ZonaAlmacenajeNotFoundException(UUID zonaId) {
        super("Zona de almacenaje no encontrada con ID: " + zonaId);
    }
}
