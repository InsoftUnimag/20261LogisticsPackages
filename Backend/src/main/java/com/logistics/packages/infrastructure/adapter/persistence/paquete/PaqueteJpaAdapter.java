package com.logistics.packages.infrastructure.adapter.persistence.paquete;

import com.logistics.packages.application.repository.PaqueteRepository;
import com.logistics.packages.domain.model.Paquete;
import com.logistics.packages.domain.valueobject.EstadoPaquete;
import jakarta.persistence.criteria.Predicate;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
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

    @Override
    public Page<Paquete> findAll(EstadoPaquete estado, LocalDateTime fechaDesde, LocalDateTime fechaHasta, Pageable pageable) {
        Specification<PaqueteDbo> spec = (root, query, cb) -> {
            Predicate predicate = cb.conjunction();
            if (estado != null) {
                predicate = cb.and(predicate, cb.equal(root.get("estado"), estado));
            }
            if (fechaDesde != null) {
                predicate = cb.and(predicate, cb.greaterThanOrEqualTo(root.get("fechaIngresoUtc"), fechaDesde));
            }
            if (fechaHasta != null) {
                predicate = cb.and(predicate, cb.lessThanOrEqualTo(root.get("fechaIngresoUtc"), fechaHasta));
            }
            return predicate;
        };
        return paqueteJpaRepository.findAll(spec, pageable).map(paqueteMapper::toDomain);
    }
}
