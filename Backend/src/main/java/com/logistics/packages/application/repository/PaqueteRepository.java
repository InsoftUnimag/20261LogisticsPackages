package com.logistics.packages.application.repository;

import com.logistics.packages.domain.model.Paquete;
import com.logistics.packages.domain.valueobject.EstadoPaquete;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

public interface PaqueteRepository {

    Paquete save(Paquete paquete);

    Optional<Paquete> findById(UUID id);

    Page<Paquete> findAll(EstadoPaquete estado, LocalDateTime fechaDesde, LocalDateTime fechaHasta, Pageable pageable);
}
