package com.logistics.packages.infrastructure.adapter.persistence.zonaalmacenaje;

import com.logistics.packages.application.repository.ZonaAlmacenajeRepository;
import com.logistics.packages.domain.model.ZonaAlmacenaje;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
@AllArgsConstructor
public class ZonaAlmacenajeJpaAdapter implements ZonaAlmacenajeRepository {

    private final ZonaAlmacenajeJpaRepository zonaAlmacenajeJpaRepository;
    private final ZonaAlmacenajeMapper zonaAlmacenajeMapper;

    @Override
    public ZonaAlmacenaje save(ZonaAlmacenaje zonaAlmacenaje) {
        ZonaAlmacenajeDbo dbo = zonaAlmacenajeMapper.toDbo(zonaAlmacenaje);
        ZonaAlmacenajeDbo savedDbo = zonaAlmacenajeJpaRepository.save(dbo);
        return zonaAlmacenajeMapper.toDomain(savedDbo);
    }

    @Override
    public Optional<ZonaAlmacenaje> findById(UUID id) {
        return zonaAlmacenajeJpaRepository.findById(id).map(zonaAlmacenajeMapper::toDomain);
    }
}
