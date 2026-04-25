package com.logistics.packages.domain.exception;

import com.logistics.packages.domain.valueobject.CategoriaZona;
import com.logistics.packages.domain.valueobject.TipoMercancia;

import java.util.UUID;

/**
 * Excepción lanzada cuando se intenta clasificar un paquete en una zona no apta.
 * MOD1-IP-005: FR-003
 */
public class ZonaNoAptaException extends RuntimeException {
    
    public ZonaNoAptaException(UUID zonaId, TipoMercancia tipoMercancia, CategoriaZona categoriaZona) {
        super(String.format(
            "La zona con ID %s de categoría %s no es apta para mercancía de tipo %s",
            zonaId, categoriaZona, tipoMercancia
        ));
    }

    public ZonaNoAptaException(String mensaje) {
        super(mensaje);
    }
}
