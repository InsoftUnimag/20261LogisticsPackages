package com.logistics.packages.infrastructure.adapter.out.persistence.paquete;

import com.logistics.packages.application.ports.out.PaqueteRepository;
import com.logistics.packages.domain.model.Paquete;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
@AllArgsConstructor
public class PaqueteJpaAdapter implements PaqueteRepository {

    private final PaqueteJpaRepository paqueteJpaRepository;
    private final PaqueteMapper paqueteMapper;

    @Override
    public Paquete save(Paquete paquete) {
        PaqueteDbo dbo = paqueteMapper.toDbo(paquete);
        PaqueteDbo savedDbo = paqueteJpaRepository.save(dbo);
        return paqueteMapper.toDomain(savedDbo);
    }

    @Override
    public Optional<Paquete> findById(UUID id) {
        return paqueteJpaRepository.findById(id).map(paqueteMapper::toDomain);
    }
}
