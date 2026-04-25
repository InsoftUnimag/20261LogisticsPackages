package com.logistics.packages.domain.exception;

/**
 * Excepción lanzada cuando ocurre un error al almacenar o recuperar archivos.
 * MOD1-IP-006: Actualizar Estado de Paquete por Novedad
 */
public class StorageException extends RuntimeException {
    
    public StorageException(String mensaje) {
        super(mensaje);
    }
    
    public StorageException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}
