package com.logistics.packages.application.ports.in;

import com.logistics.packages.domain.model.Paquete;

import java.util.Optional;
import java.util.UUID;

public interface ConsultarPaqueteIn {
    Optional<Paquete> consultarPaquete(UUID paqueteId);
}
