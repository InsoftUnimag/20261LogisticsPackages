package com.logistics.packages.domain.exception;

/**
 * Excepción lanzada cuando se intenta realizar una transición de estado no permitida.
 * MOD1-IP-006: Actualizar Estado de Paquete por Novedad
 */
public class TransicionInvalidaException extends RuntimeException {
    
    public TransicionInvalidaException(String mensaje) {
        super(mensaje);
    }
    
    public TransicionInvalidaException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}
