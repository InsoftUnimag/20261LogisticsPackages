package com.logistics.packages.domain.exception;

import java.util.UUID;

/**
 * Excepción lanzada cuando no se encuentra un paquete con el ID especificado.
 */
public class PaqueteNoEncontradoException extends RuntimeException {
    
    private final UUID paqueteId;
    
    public PaqueteNoEncontradoException(UUID paqueteId) {
        super("No se encontró el paquete con ID: " + paqueteId);
        this.paqueteId = paqueteId;
    }
    
    public PaqueteNoEncontradoException(UUID paqueteId, String mensaje) {
        super(mensaje);
        this.paqueteId = paqueteId;
    }
    
    public UUID getPaqueteId() {
        return paqueteId;
    }
}
