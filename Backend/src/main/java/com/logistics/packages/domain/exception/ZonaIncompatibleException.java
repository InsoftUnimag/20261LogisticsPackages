package com.logistics.packages.domain.exception;

import com.logistics.packages.domain.valueobject.CategoriaZona;
import com.logistics.packages.domain.valueobject.TipoMercancia;

import java.util.UUID;

/**
 * Excepción lanzada cuando se intenta asignar un paquete a una zona incompatible con su tipo de mercancía.
 * FR-006: Bloqueo de asignación de mercancía Frágil o Peligrosa a zonas no aptas
 */
public class ZonaIncompatibleException extends RuntimeException {
    
    private final UUID paqueteId;
    private final TipoMercancia tipoMercancia;
    private final UUID zonaId;
    private final CategoriaZona categoriaZona;
    
    public ZonaIncompatibleException(UUID paqueteId, TipoMercancia tipoMercancia, UUID zonaId, CategoriaZona categoriaZona) {
        super(String.format("El paquete de tipo '%s' no puede ser asignado a una zona de categoría '%s'. " +
            "Seleccione una zona apropiada para este tipo de mercancía.", 
            tipoMercancia, categoriaZona));
        this.paqueteId = paqueteId;
        this.tipoMercancia = tipoMercancia;
        this.zonaId = zonaId;
        this.categoriaZona = categoriaZona;
    }
    
    public UUID getPaqueteId() {
        return paqueteId;
    }
    
    public TipoMercancia getTipoMercancia() {
        return tipoMercancia;
    }
    
    public UUID getZonaId() {
        return zonaId;
    }
    
    public CategoriaZona getCategoriaZona() {
        return categoriaZona;
    }
}
