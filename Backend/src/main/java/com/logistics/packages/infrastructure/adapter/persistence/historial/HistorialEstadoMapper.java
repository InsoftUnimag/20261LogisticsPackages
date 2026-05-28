package com.logistics.packages.infrastructure.adapter.persistence.historial;

import com.logistics.packages.domain.model.HistorialEstado;
import com.logistics.packages.domain.valueobject.EstadoPaquete;
import com.logistics.packages.domain.valueobject.TipoNovedad;
import org.springframework.stereotype.Component;

/**
 * Mapper para convertir entre HistorialEstado del dominio y HistorialEstadoEntity de persistencia.
 * MOD1-UC-006: Transformación entre capas siguiendo Arquitectura Hexagonal.
 */
@Component
public class HistorialEstadoMapper {
    
    /**
     * Convierte un HistorialEstado del dominio a una entidad JPA.
     * 
     * @param historial Objeto de dominio
     * @return Entidad JPA para persistencia
     */
    public HistorialEstadoEntity toEntity(HistorialEstado historial) {
        if (historial == null) {
            return null;
        }
        
        HistorialEstadoEntity entity = new HistorialEstadoEntity();
        entity.setId(historial.getId());
        entity.setPaqueteId(historial.getPaqueteId());
        // Estado anterior puede ser null para la primera transición (admisión)
        entity.setEstadoAnterior(historial.getEstadoAnterior() != null ? historial.getEstadoAnterior().name() : null);
        entity.setEstadoNuevo(historial.getEstadoNuevo().name());
        entity.setObservaciones(historial.getObservaciones());
        entity.setUsuarioId(historial.getUsuarioId());
        entity.setUrlEvidencia(historial.getUrlEvidencia());
        entity.setFechaTransicionUtc(historial.getFechaTransicionUtc());
        if (historial.getTipoNovedad() != null) {
            entity.setTipoNovedad(historial.getTipoNovedad().name());
        }
        return entity;
    }
    
    /**
     * Convierte una entidad JPA a un objeto del dominio.
     * 
     * @param entity Entidad JPA
     * @return Objeto de dominio
     */
    public HistorialEstado toDomain(HistorialEstadoEntity entity) {
        if (entity == null) {
            return null;
        }
        
        return new HistorialEstado(
            entity.getId(),
            entity.getPaqueteId(),
            // Estado anterior puede ser null para la primera transición (admisión)
            entity.getEstadoAnterior() != null ? EstadoPaquete.valueOf(entity.getEstadoAnterior()) : null,
            EstadoPaquete.valueOf(entity.getEstadoNuevo()),
            entity.getObservaciones(),
            entity.getUsuarioId(),
            entity.getUrlEvidencia(),
            entity.getTipoNovedad() != null ? TipoNovedad.valueOf(entity.getTipoNovedad()) : null,
            entity.getFechaTransicionUtc()
        );
    }
}
