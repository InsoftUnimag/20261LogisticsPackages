package com.logistics.packages.infrastructure.adapter.persistence.sede;

import com.logistics.packages.application.ports.SedeRepository;
import com.logistics.packages.domain.model.Sede;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Adaptador JPA que implementa el puerto SedeRepository.
 * Mapea entre la capa de persistencia (SedeDbo) y el dominio (Sede).
 */
@Component
@AllArgsConstructor
public class SedeJpaAdapter implements SedeRepository {

    private final SedeJpaRepository sedeJpaRepository;
    private final SedeMapper sedeMapper;

    @Override
    public List<Sede> findAll() {
        return sedeJpaRepository.findAll().stream()
                .map(sedeMapper::toDomain)
                .toList();
    }

    @Override
    public Optional<Sede> findById(UUID id) {
        return sedeJpaRepository.findById(id)
                .map(sedeMapper::toDomain);
    }
}
