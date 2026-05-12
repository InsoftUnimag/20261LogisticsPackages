package com.logistics.packages.infrastructure.security;

import com.logistics.packages.application.ports.UsuarioRepository;
import com.logistics.packages.domain.model.Usuario;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UsuarioRepository usuarioRepository;

    public CustomUserDetailsService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Usuario usuario = usuarioRepository.findByUsername(username)
            .orElseThrow(() -> new UsernameNotFoundException(
                "Usuario no encontrado: " + username));

        return new User(
            usuario.getUsername(),
            usuario.getPasswordHash(),
            usuario.isEnabled(),
            true,
            true,
            true,
            List.of(new SimpleGrantedAuthority("ROLE_" + usuario.getRol()))
        );
    }
}
