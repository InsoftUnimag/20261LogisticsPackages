package com.logistics.packages.domain.exception;

/**
 * Excepción lanzada cuando se intenta registrar una novedad de tipo DAÑADO 
 * sin proporcionar evidencia multimedia obligatoria.
 * MOD1-IP-006: Actualizar Estado de Paquete por Novedad
 * FR-004: Requerir evidencia fotográfica o video obligatoria
 */
public class EvidenciaRequeridaException extends RuntimeException {
    
    public EvidenciaRequeridaException(String mensaje) {
        super(mensaje);
    }
    
    public EvidenciaRequeridaException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}
