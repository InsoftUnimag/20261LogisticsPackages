package com.logistics.packages.infrastructure.adapter.persistence.historial;

import com.logistics.packages.application.repository.HistorialEstadoRepository;
import com.logistics.packages.domain.model.HistorialEstado;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Adaptador JPA para el repositorio de historial de estados.
 * MOD1-UC-006: Implementación del puerto HistorialEstadoRepository.
 */
@Repository
@RequiredArgsConstructor
public class HistorialEstadoJpaAdapter implements HistorialEstadoRepository {
    
    private final HistorialEstadoJpaRepository jpaRepository;
    private final HistorialEstadoMapper mapper;

    @Override
    public HistorialEstado guardar(HistorialEstado historial) {
        HistorialEstadoEntity entity = mapper.toEntity(historial);
        HistorialEstadoEntity savedEntity = jpaRepository.save(entity);
        return mapper.toDomain(savedEntity);
    }

    @Override
    public List<HistorialEstado> obtenerHistorialPorPaqueteId(UUID paqueteId) {
        List<HistorialEstadoEntity> entities = jpaRepository.findByPaqueteIdOrderByFechaTransicionUtcAsc(paqueteId);
        return entities.stream()
            .map(mapper::toDomain)
            .collect(Collectors.toList());
    }

    @Override
    public List<HistorialEstado> obtenerTodasLasNovedades() {
        List<HistorialEstadoEntity> entities = jpaRepository.findByTipoNovedadIsNotNullOrderByFechaTransicionUtcDesc();
        return entities.stream()
            .map(mapper::toDomain)
            .collect(Collectors.toList());
    }
}
