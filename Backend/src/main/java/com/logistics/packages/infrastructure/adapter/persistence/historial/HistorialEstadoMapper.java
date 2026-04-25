package com.logistics.packages.infrastructure.adapter.persistence.historial;

import com.logistics.packages.domain.model.HistorialEstado;
import com.logistics.packages.domain.valueobject.EstadoPaquete;
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
        
        return HistorialEstadoEntity.builder()
            .id(historial.getId())
            .paqueteId(historial.getPaqueteId())
            .estadoAnterior(historial.getEstadoAnterior().name())
            .estadoNuevo(historial.getEstadoNuevo().name())
            .observaciones(historial.getObservaciones())
            .usuarioId(historial.getUsuarioId())
            .urlEvidencia(historial.getUrlEvidencia())
            .fechaTransicionUtc(historial.getFechaTransicionUtc())
            .build();
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
        
        return HistorialEstado.builder()
            .id(entity.getId())
            .paqueteId(entity.getPaqueteId())
            .estadoAnterior(EstadoPaquete.valueOf(entity.getEstadoAnterior()))
            .estadoNuevo(EstadoPaquete.valueOf(entity.getEstadoNuevo()))
            .observaciones(entity.getObservaciones())
            .usuarioId(entity.getUsuarioId())
            .urlEvidencia(entity.getUrlEvidencia())
            .fechaTransicionUtc(entity.getFechaTransicionUtc())
            .build();
    }
}
