package com.logistics.packages.infrastructure.adapter.persistence.sede;

import com.logistics.packages.application.repository.SedeRepository;
import com.logistics.packages.domain.model.Sede;
import com.logistics.packages.domain.valueobject.TipoSede;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Adaptador que implementa SedeRepository usando JPA.
 * Convierte entre el modelo de persistencia (DBO) y el modelo de dominio.
 */
@Component
@RequiredArgsConstructor
public class SedeJpaAdapter implements SedeRepository {
    
    private final SedeJpaRepository sedeJpaRepository;
    
    @Override
    public List<Sede> findAll() {
        return sedeJpaRepository.findAll()
                .stream()
                .map(SedeMapper::toDomain)
                .toList();
    }
    
    @Override
    public Optional<Sede> findById(UUID id) {
        return sedeJpaRepository.findById(id)
                .map(SedeMapper::toDomain);
    }
    
    @Override
    public List<Sede> findByTipo(String tipo) {
        TipoSede tipoSede = TipoSede.valueOf(tipo.toUpperCase());
        return sedeJpaRepository.findByTipo(tipoSede)
                .stream()
                .map(SedeMapper::toDomain)
                .toList();
    }
}
