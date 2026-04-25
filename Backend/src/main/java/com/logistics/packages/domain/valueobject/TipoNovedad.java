package com.logistics.packages.domain.valueobject;

/**
 * Tipo de novedad detectada en bodega.
 * MOD1-IP-006: Actualizar Estado de Paquete por Novedad
 */
public enum TipoNovedad {
    /**
     * Paquete con daño físico visible (requiere evidencia obligatoria)
     */
    DAÑADO,
    
    /**
     * Paquete no localizado físicamente en bodega
     */
    EXTRAVIADO
}
