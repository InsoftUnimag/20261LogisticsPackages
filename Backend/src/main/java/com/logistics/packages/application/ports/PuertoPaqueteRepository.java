package com.logistics.packages.application.ports;

import com.logistics.packages.domain.model.Paquete;
import java.util.Optional;
import java.util.UUID;

public interface PuertoPaqueteRepository {
    Paquete save(Paquete paquete);
    Optional<Paquete> findById(UUID id);
}