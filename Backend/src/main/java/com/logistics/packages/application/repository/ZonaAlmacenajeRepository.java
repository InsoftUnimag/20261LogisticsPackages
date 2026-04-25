package com.logistics.packages.application.repository;

import com.logistics.packages.domain.model.ZonaAlmacenaje;

import java.util.Optional;
import java.util.UUID;

public interface ZonaAlmacenajeRepository {

    ZonaAlmacenaje save(ZonaAlmacenaje zonaAlmacenaje);

    Optional<ZonaAlmacenaje> findById(UUID id);
}
