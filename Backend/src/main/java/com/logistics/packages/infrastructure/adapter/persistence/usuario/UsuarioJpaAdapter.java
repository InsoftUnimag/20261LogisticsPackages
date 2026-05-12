package com.logistics.packages.infrastructure.adapter.persistence.usuario;

import com.logistics.packages.application.ports.UsuarioRepository;
import com.logistics.packages.domain.model.Usuario;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
public class UsuarioJpaAdapter implements UsuarioRepository {

    private final UsuarioJpaRepository jpaRepository;
    private final UsuarioMapper mapper;

    public UsuarioJpaAdapter(UsuarioJpaRepository jpaRepository, UsuarioMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    @Override
    public Usuario save(Usuario usuario) {
        UsuarioEntity entity = mapper.toEntity(usuario);
        UsuarioEntity saved = jpaRepository.save(entity);
        return mapper.toDomain(saved);
    }

    @Override
    public Optional<Usuario> findById(UUID id) {
        return jpaRepository.findById(id).map(mapper::toDomain);
    }

    @Override
    public Optional<Usuario> findByUsername(String username) {
        return jpaRepository.findByUsername(username).map(mapper::toDomain);
    }

    @Override
    public boolean existsByUsername(String username) {
        return jpaRepository.existsByUsername(username);
    }
}
