package com.logistics.packages.application.ports.out;

import com.logistics.packages.domain.model.Paquete;
import java.util.Optional;
import java.util.UUID;

public interface PaqueteRepository {
    Paquete guardar(Paquete paquete);
    Optional<Paquete> buscarPorId(UUID id);
}
