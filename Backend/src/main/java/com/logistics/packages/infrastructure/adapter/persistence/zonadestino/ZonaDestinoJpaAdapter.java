package com.logistics.packages.infrastructure.adapter.persistence.zonadestino;

import com.logistics.packages.application.ports.ZonaDestinoRepository;
import com.logistics.packages.domain.model.ZonaDestino;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Adaptador JPA para ZonaDestinoRepository
 * MOD1-IP-005
 */
@Component
@RequiredArgsConstructor
public class ZonaDestinoJpaAdapter implements ZonaDestinoRepository {

    private final ZonaDestinoJpaRepository jpaRepository;
    private final ZonaDestinoMapper mapper;

    @Override
    public Optional<ZonaDestino> findById(UUID id) {
        return jpaRepository.findById(id)
                .map(mapper::toDomain);
    }

    @Override
    public List<ZonaDestino> findAllActivas() {
        return jpaRepository.findAllActivas().stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public ZonaDestino save(ZonaDestino zonaDestino) {
        ZonaDestinoDbo dbo = mapper.toDbo(zonaDestino);
        ZonaDestinoDbo savedDbo = jpaRepository.save(dbo);
        return mapper.toDomain(savedDbo);
    }

    @Override
    public List<ZonaDestino> findBySedeId(UUID sedeId) {
        return jpaRepository.findByIdSede(sedeId).stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }
}
