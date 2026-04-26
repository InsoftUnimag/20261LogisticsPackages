package com.logistics.packages.infrastructure.adapter.persistence.zonaalmacenaje;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ZonaAlmacenajeJpaRepository extends JpaRepository<ZonaAlmacenajeDbo, UUID> {
    List<ZonaAlmacenajeDbo> findByIdSede(UUID sedeId);
}
