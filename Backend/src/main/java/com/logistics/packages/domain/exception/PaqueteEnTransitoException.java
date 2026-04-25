package com.logistics.packages.domain.exception;

/**
 * Excepción lanzada cuando se intenta modificar el estado de un paquete 
 * que ya está en tránsito o en estados posteriores.
 * MOD1-IP-006: Actualizar Estado de Paquete por Novedad
 * FR-006: Bloquear actualizaciones si el paquete ya está en tránsito
 */
public class PaqueteEnTransitoException extends RuntimeException {
    
    public PaqueteEnTransitoException(String mensaje) {
        super(mensaje);
    }
    
    public PaqueteEnTransitoException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}
