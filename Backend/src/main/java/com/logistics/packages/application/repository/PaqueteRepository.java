package com.logistics.packages.application.repository;

import com.logistics.packages.domain.model.Paquete;

import java.util.Optional;
import java.util.UUID;

public interface PaqueteRepository {

    Paquete save(Paquete paquete);

    Optional<Paquete> findById(UUID id);
}
