package com.logistics.packages.domain.model;

import com.logistics.packages.domain.valueobject.EstadoPaquete;
import com.logistics.packages.domain.valueobject.Evidencia;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Entidad que representa una entrada inmutable en el historial de estados de un paquete.
 * Cada transición de estado genera una nueva entrada que no puede ser modificada posteriormente.
 * MOD1-IP-006: Actualizar Estado de Paquete por Novedad
 * FR-003: Mantener historial cronológico inmutable
 */
public class HistorialEstado {
    
    private final UUID id;
    private final UUID paqueteId;
    private final EstadoPaquete estadoAnterior;
    private final EstadoPaquete estadoNuevo;
    private final LocalDateTime fechaTransicion;
    private final String usuarioResponsable;
    private final String notas;
    private final List<Evidencia> evidencias;
    
    /**
     * Constructor completo para crear una entrada de historial.
     * 
     * @param paqueteId ID del paquete al que pertenece este historial
     * @param estadoAnterior Estado previo del paquete (puede ser null si es el primer estado)
     * @param estadoNuevo Estado al que transiciona el paquete
     * @param usuarioResponsable Identificador del usuario que realizó el cambio
     * @param notas Notas o comentarios adicionales sobre la transición
     * @param evidencias Lista de evidencias multimedia adjuntas (si aplica)
     */
    public HistorialEstado(UUID paqueteId, EstadoPaquete estadoAnterior, 
                          EstadoPaquete estadoNuevo, String usuarioResponsable, 
                          String notas, List<Evidencia> evidencias) {
        
        if (paqueteId == null) {
            throw new IllegalArgumentException("El ID del paquete no puede ser nulo");
        }
        
        if (estadoNuevo == null) {
            throw new IllegalArgumentException("El estado nuevo no puede ser nulo");
        }
        
        if (usuarioResponsable == null || usuarioResponsable.trim().isEmpty()) {
            throw new IllegalArgumentException("El usuario responsable no puede estar vacío");
        }
        
        this.id = UUID.randomUUID();
        this.paqueteId = paqueteId;
        this.estadoAnterior = estadoAnterior;
        this.estadoNuevo = estadoNuevo;
        this.fechaTransicion = LocalDateTime.now(ZoneOffset.UTC);
        this.usuarioResponsable = usuarioResponsable;
        this.notas = notas;
        this.evidencias = evidencias != null ? List.copyOf(evidencias) : List.of();
    }
    
    /**
     * Constructor para reconstitución desde persistencia.
     * Usado por adaptadores de infraestructura.
     */
    public HistorialEstado(UUID id, UUID paqueteId, EstadoPaquete estadoAnterior,
                          EstadoPaquete estadoNuevo, LocalDateTime fechaTransicion,
                          String usuarioResponsable, String notas, List<Evidencia> evidencias) {
        this.id = id;
        this.paqueteId = paqueteId;
        this.estadoAnterior = estadoAnterior;
        this.estadoNuevo = estadoNuevo;
        this.fechaTransicion = fechaTransicion;
        this.usuarioResponsable = usuarioResponsable;
        this.notas = notas;
        this.evidencias = evidencias != null ? List.copyOf(evidencias) : List.of();
    }
    
    public UUID getId() {
        return id;
    }
    
    public UUID getPaqueteId() {
        return paqueteId;
    }
    
    public EstadoPaquete getEstadoAnterior() {
        return estadoAnterior;
    }
    
    public EstadoPaquete getEstadoNuevo() {
        return estadoNuevo;
    }
    
    public LocalDateTime getFechaTransicion() {
        return fechaTransicion;
    }
    
    public String getUsuarioResponsable() {
        return usuarioResponsable;
    }
    
    public String getNotas() {
        return notas;
    }
    
    /**
     * @return Copia inmutable de la lista de evidencias
     */
    public List<Evidencia> getEvidencias() {
        return new ArrayList<>(evidencias);
    }
    
    /**
     * @return true si esta entrada tiene evidencias adjuntas
     */
    public boolean tieneEvidencias() {
        return !evidencias.isEmpty();
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        HistorialEstado that = (HistorialEstado) o;
        return Objects.equals(id, that.id);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
    
    @Override
    public String toString() {
        return "HistorialEstado{" +
               "id=" + id +
               ", paqueteId=" + paqueteId +
               ", estadoAnterior=" + estadoAnterior +
               ", estadoNuevo=" + estadoNuevo +
               ", fechaTransicion=" + fechaTransicion +
               ", usuarioResponsable='" + usuarioResponsable + '\'' +
               ", evidencias=" + evidencias.size() +
               '}';
    }
}
