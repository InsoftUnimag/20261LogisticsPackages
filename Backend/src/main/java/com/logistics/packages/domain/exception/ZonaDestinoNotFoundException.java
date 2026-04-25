package com.logistics.packages.domain.exception;

import java.util.UUID;

/**
 * Excepción lanzada cuando no se encuentra una zona de destino.
 * MOD1-IP-005
 */
public class ZonaDestinoNotFoundException extends RuntimeException {
    
    public ZonaDestinoNotFoundException(UUID zonaDestinoId) {
        super("No se encontró la zona de destino con ID: " + zonaDestinoId);
    }

    public ZonaDestinoNotFoundException(String mensaje) {
        super(mensaje);
    }
}
