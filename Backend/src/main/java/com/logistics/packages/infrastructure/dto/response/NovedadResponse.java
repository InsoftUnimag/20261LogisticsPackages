package com.logistics.packages.infrastructure.dto.response;

import com.logistics.packages.domain.valueobject.EstadoPaquete;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO para la respuesta REST de novedad registrada.
 * MOD1-IP-006: Actualizar Estado de Paquete por Novedad
 */
public class NovedadResponse {
    
    private UUID paqueteId;
    private EstadoPaquete estadoActual;
    private UUID historialEntradaId;
    private LocalDateTime fechaTransicion;
    private String mensaje;
    
    // Constructor vacío requerido por Jackson
    public NovedadResponse() {
    }
    
    public NovedadResponse(UUID paqueteId, EstadoPaquete estadoActual,
                          UUID historialEntradaId, LocalDateTime fechaTransicion,
                          String mensaje) {
        this.paqueteId = paqueteId;
        this.estadoActual = estadoActual;
        this.historialEntradaId = historialEntradaId;
        this.fechaTransicion = fechaTransicion;
        this.mensaje = mensaje;
    }
    
    public UUID getPaqueteId() {
        return paqueteId;
    }
    
    public void setPaqueteId(UUID paqueteId) {
        this.paqueteId = paqueteId;
    }
    
    public EstadoPaquete getEstadoActual() {
        return estadoActual;
    }
    
    public void setEstadoActual(EstadoPaquete estadoActual) {
        this.estadoActual = estadoActual;
    }
    
    public UUID getHistorialEntradaId() {
        return historialEntradaId;
    }
    
    public void setHistorialEntradaId(UUID historialEntradaId) {
        this.historialEntradaId = historialEntradaId;
    }
    
    public LocalDateTime getFechaTransicion() {
        return fechaTransicion;
    }
    
    public void setFechaTransicion(LocalDateTime fechaTransicion) {
        this.fechaTransicion = fechaTransicion;
    }
    
    public String getMensaje() {
        return mensaje;
    }
    
    public void setMensaje(String mensaje) {
        this.mensaje = mensaje;
    }
}
