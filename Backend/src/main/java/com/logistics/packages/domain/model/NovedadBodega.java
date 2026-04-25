package com.logistics.packages.domain.model;

import com.logistics.packages.domain.exception.EvidenciaRequeridaException;
import com.logistics.packages.domain.valueobject.Evidencia;
import com.logistics.packages.domain.valueobject.TipoNovedad;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Entidad que representa una novedad detectada en bodega (paquete dañado o extraviado).
 * MOD1-IP-006: Actualizar Estado de Paquete por Novedad
 * FR-004: Evidencia obligatoria para tipo DAÑADO
 */
public class NovedadBodega {
    
    private final UUID id;
    private final UUID paqueteId;
    private final TipoNovedad tipo;
    private final String descripcion;
    private final LocalDateTime fechaDeteccion;
    private final String almacenistaResponsable;
    private final List<Evidencia> evidencias;
    
    /**
     * Constructor para crear una nueva novedad en bodega.
     * 
     * @param paqueteId ID del paquete afectado
     * @param tipo Tipo de novedad (DAÑADO o EXTRAVIADO)
     * @param descripcion Descripción detallada de la novedad
     * @param almacenistaResponsable Identificador del almacenista que detectó la novedad
     * @param evidencias Lista de evidencias multimedia (obligatoria para tipo DAÑADO)
     * @throws EvidenciaRequeridaException si tipo es DAÑADO y no hay evidencias
     */
    public NovedadBodega(UUID paqueteId, TipoNovedad tipo, String descripcion,
                        String almacenistaResponsable, List<Evidencia> evidencias) {
        
        if (paqueteId == null) {
            throw new IllegalArgumentException("El ID del paquete no puede ser nulo");
        }
        
        if (tipo == null) {
            throw new IllegalArgumentException("El tipo de novedad no puede ser nulo");
        }
        
        if (descripcion == null || descripcion.trim().isEmpty()) {
            throw new IllegalArgumentException("La descripción no puede estar vacía");
        }
        
        if (almacenistaResponsable == null || almacenistaResponsable.trim().isEmpty()) {
            throw new IllegalArgumentException("El almacenista responsable no puede estar vacío");
        }
        
        // FR-004: Validar evidencia obligatoria para tipo Dañado
        if (tipo == TipoNovedad.DAÑADO && (evidencias == null || evidencias.isEmpty())) {
            throw new EvidenciaRequeridaException(
                "La novedad de tipo DAÑADO requiere al menos una evidencia multimedia (foto o video)"
            );
        }
        
        this.id = UUID.randomUUID();
        this.paqueteId = paqueteId;
        this.tipo = tipo;
        this.descripcion = descripcion;
        this.fechaDeteccion = LocalDateTime.now(ZoneOffset.UTC);
        this.almacenistaResponsable = almacenistaResponsable;
        this.evidencias = evidencias != null ? List.copyOf(evidencias) : List.of();
    }
    
    /**
     * Constructor para reconstitución desde persistencia.
     */
    public NovedadBodega(UUID id, UUID paqueteId, TipoNovedad tipo, String descripcion,
                        LocalDateTime fechaDeteccion, String almacenistaResponsable,
                        List<Evidencia> evidencias) {
        this.id = id;
        this.paqueteId = paqueteId;
        this.tipo = tipo;
        this.descripcion = descripcion;
        this.fechaDeteccion = fechaDeteccion;
        this.almacenistaResponsable = almacenistaResponsable;
        this.evidencias = evidencias != null ? List.copyOf(evidencias) : List.of();
    }
    
    public UUID getId() {
        return id;
    }
    
    public UUID getPaqueteId() {
        return paqueteId;
    }
    
    public TipoNovedad getTipo() {
        return tipo;
    }
    
    public String getDescripcion() {
        return descripcion;
    }
    
    public LocalDateTime getFechaDeteccion() {
        return fechaDeteccion;
    }
    
    public String getAlmacenistaResponsable() {
        return almacenistaResponsable;
    }
    
    /**
     * @return Copia inmutable de la lista de evidencias
     */
    public List<Evidencia> getEvidencias() {
        return new ArrayList<>(evidencias);
    }
    
    /**
     * @return true si esta novedad tiene evidencias adjuntas
     */
    public boolean tieneEvidencias() {
        return !evidencias.isEmpty();
    }
    
    /**
     * @return true si esta novedad es de tipo DAÑADO
     */
    public boolean esPaqueteDanado() {
        return tipo == TipoNovedad.DAÑADO;
    }
    
    /**
     * @return true si esta novedad es de tipo EXTRAVIADO
     */
    public boolean esPaqueteExtraviado() {
        return tipo == TipoNovedad.EXTRAVIADO;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NovedadBodega that = (NovedadBodega) o;
        return Objects.equals(id, that.id);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
    
    @Override
    public String toString() {
        return "NovedadBodega{" +
               "id=" + id +
               ", paqueteId=" + paqueteId +
               ", tipo=" + tipo +
               ", fechaDeteccion=" + fechaDeteccion +
               ", almacenistaResponsable='" + almacenistaResponsable + '\'' +
               ", evidencias=" + evidencias.size() +
               '}';
    }
}
