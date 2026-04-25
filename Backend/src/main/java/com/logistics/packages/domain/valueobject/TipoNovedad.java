package com.logistics.packages.domain.valueobject;

/**
 * Enum que representa los tipos de novedades que pueden ocurrir con un paquete en bodega.
 * MOD1-UC-006: Actualizar Estado de Paquete por Novedad
 */
public enum TipoNovedad {
    /**
     * Paquete con daños físicos visibles (caída, rotura, abolladura, temperatura)
     * Requiere evidencia fotográfica obligatoria
     */
    DAÑADO,
    
    /**
     * Paquete que no se encuentra físicamente en bodega pero está registrado en el sistema
     */
    EXTRAVIADO
}
