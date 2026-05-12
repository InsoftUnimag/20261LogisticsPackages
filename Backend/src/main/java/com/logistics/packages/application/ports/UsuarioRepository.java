package com.logistics.packages.application.ports;

import com.logistics.packages.domain.model.Usuario;

import java.util.Optional;
import java.util.UUID;

public interface UsuarioRepository {

    Usuario save(Usuario usuario);

    Optional<Usuario> findById(UUID id);

    Optional<Usuario> findByUsername(String username);

    boolean existsByUsername(String username);
}
