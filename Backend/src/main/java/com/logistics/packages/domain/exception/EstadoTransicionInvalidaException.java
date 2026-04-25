package com.logistics.packages.domain.exception;

import com.logistics.packages.domain.valueobject.EstadoPaquete;

import java.util.UUID;

/**
 * Excepción lanzada cuando se intenta registrar una novedad en un paquete
 * que no está en un estado válido para recibirla.
 * MOD1-UC-006: FR-006 - Bloquear actualizaciones de estado si el paquete ya está en tránsito o posterior.
 */
public class EstadoTransicionInvalidaException extends RuntimeException {
    
    private final UUID paqueteId;
    private final EstadoPaquete estadoActual;
    
    public EstadoTransicionInvalidaException(UUID paqueteId, EstadoPaquete estadoActual) {
        super(String.format(
            "No se puede registrar novedad en el paquete %s. Estado actual '%s' no permite cambios desde bodega. " +
            "Solo se permiten novedades en estados RECIBIDO_EN_SEDE o EN_CLASIFICACION.",
            paqueteId, estadoActual
        ));
        this.paqueteId = paqueteId;
        this.estadoActual = estadoActual;
    }
    
    public UUID getPaqueteId() {
        return paqueteId;
    }
    
    public EstadoPaquete getEstadoActual() {
        return estadoActual;
    }
}
