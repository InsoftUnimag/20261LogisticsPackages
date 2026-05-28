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
         entity.setEstadoNovedad(historial.getEstadoNovedad());
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
         
         return HistorialEstado.builder()
             .id(entity.getId())
             .paqueteId(entity.getPaqueteId())
             .estadoAnterior(entity.getEstadoAnterior() != null ? EstadoPaquete.valueOf(entity.getEstadoAnterior()) : null)
             .estadoNuevo(EstadoPaquete.valueOf(entity.getEstadoNuevo()))
             .observaciones(entity.getObservaciones())
             .usuarioId(entity.getUsuarioId())
             .urlEvidencia(entity.getUrlEvidencia())
             .tipoNovedad(entity.getTipoNovedad() != null ? TipoNovedad.valueOf(entity.getTipoNovedad()) : null)
             .fechaTransicionUtc(entity.getFechaTransicionUtc())
             .estadoNovedad(entity.getEstadoNovedad())
             .build();
     }
}
