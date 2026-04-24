package com.logistics.packages.domain.exception;

import java.util.UUID;

/**
 * Excepción lanzada cuando no se encuentra un paquete con el ID especificado.
 */
public class PaqueteNotFoundException extends RuntimeException {
    
    public PaqueteNotFoundException(UUID paqueteId) {
        super("Paquete no encontrado con ID: " + paqueteId);
    }
    
    public PaqueteNotFoundException(String message) {
        super(message);
    }
}
