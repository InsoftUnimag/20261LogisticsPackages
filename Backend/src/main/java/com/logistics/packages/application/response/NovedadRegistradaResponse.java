package com.logistics.packages.application.response;

import com.logistics.packages.domain.valueobject.EstadoPaquete;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Respuesta después de registrar una novedad exitosamente.
 * MOD1-IP-006: Actualizar Estado de Paquete por Novedad
 */
public class NovedadRegistradaResponse {
    
    private final UUID paqueteId;
    private final EstadoPaquete estadoActual;
    private final UUID historialEntradaId;
    private final LocalDateTime fechaTransicion;
    private final String mensaje;
    
    public NovedadRegistradaResponse(UUID paqueteId, EstadoPaquete estadoActual,
                                    UUID historialEntradaId, LocalDateTime fechaTransicion) {
        this.paqueteId = paqueteId;
        this.estadoActual = estadoActual;
        this.historialEntradaId = historialEntradaId;
        this.fechaTransicion = fechaTransicion;
        this.mensaje = "Novedad registrada exitosamente";
    }
    
    public UUID getPaqueteId() {
        return paqueteId;
    }
    
    public EstadoPaquete getEstadoActual() {
        return estadoActual;
    }
    
    public UUID getHistorialEntradaId() {
        return historialEntradaId;
    }
    
    public LocalDateTime getFechaTransicion() {
        return fechaTransicion;
    }
    
    public String getMensaje() {
        return mensaje;
    }
}
