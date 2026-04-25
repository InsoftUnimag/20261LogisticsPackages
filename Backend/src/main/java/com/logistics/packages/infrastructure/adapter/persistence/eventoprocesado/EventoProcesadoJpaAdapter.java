package com.logistics.packages.infrastructure.adapter.persistence.eventoprocesado;

import com.logistics.packages.application.ports.EventoProcesadoRepository;
import com.logistics.packages.domain.model.EventoProcesado;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * Adaptador JPA para la persistencia de eventos procesados.
 * MOD1-UC-007: FR-008 - Implementación de persistencia para idempotencia.
 * 
 * Este adaptador implementa el puerto EventoProcesadoRepository siguiendo
 * el principio de Inversión de Dependencias (Dependency Inversion).
 */
@Component
@RequiredArgsConstructor
public class EventoProcesadoJpaAdapter implements EventoProcesadoRepository {
    
    private final EventoProcesadoJpaRepository jpaRepository;
    
    @Override
    public EventoProcesado guardar(EventoProcesado eventoProcesado) {
        EventoProcesadoEntity entity = EventoProcesadoEntity.builder()
                .eventoId(eventoProcesado.getEventoId())
                .paqueteId(eventoProcesado.getPaqueteId())
                .tipoEvento(eventoProcesado.getTipoEvento())
                .fechaProcesamientoUtc(eventoProcesado.getFechaProcesamientoUtc())
                .build();
        
        EventoProcesadoEntity savedEntity = jpaRepository.save(entity);
        
        return EventoProcesado.builder()
                .eventoId(savedEntity.getEventoId())
                .paqueteId(savedEntity.getPaqueteId())
                .tipoEvento(savedEntity.getTipoEvento())
                .fechaProcesamientoUtc(savedEntity.getFechaProcesamientoUtc())
                .build();
    }
    
    @Override
    public boolean yaFueProcesado(String eventoId) {
        return jpaRepository.existsByEventoId(eventoId);
    }
    
    @Override
    public Optional<EventoProcesado> buscarPorEventoId(String eventoId) {
        return jpaRepository.findById(eventoId)
                .map(entity -> EventoProcesado.builder()
                        .eventoId(entity.getEventoId())
                        .paqueteId(entity.getPaqueteId())
                        .tipoEvento(entity.getTipoEvento())
                        .fechaProcesamientoUtc(entity.getFechaProcesamientoUtc())
                        .build());
    }
}
