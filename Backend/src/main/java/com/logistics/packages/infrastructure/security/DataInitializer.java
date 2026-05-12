package com.logistics.packages.infrastructure.security;

import com.logistics.packages.application.ports.UsuarioRepository;
import com.logistics.packages.domain.model.Usuario;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.UUID;

@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        if (!usuarioRepository.existsByUsername("admin")) {
            Usuario admin = new Usuario();
            admin.setId(UUID.randomUUID());
            admin.setUsername("admin");
            admin.setPasswordHash(passwordEncoder.encode("admin123"));
            admin.setEmail("admin@logistics.com");
            admin.setNombreCompleto("Administrador del Sistema");
            admin.setRol("ADMIN");
            admin.setEnabled(true);
            admin.setFechaCreacion(Instant.now());

            usuarioRepository.save(admin);
            log.info("Usuario admin creado por defecto (password: admin123)");
        }

        if (!usuarioRepository.existsByUsername("operador")) {
            Usuario operador = new Usuario();
            operador.setId(UUID.randomUUID());
            operador.setUsername("operador");
            operador.setPasswordHash(passwordEncoder.encode("operador123"));
            operador.setEmail("operador@logistics.com");
            operador.setNombreCompleto("Operador de Bodega");
            operador.setRol("OPERADOR_BODEGA");
            operador.setEnabled(true);
            operador.setFechaCreacion(Instant.now());

            usuarioRepository.save(operador);
            log.info("Usuario operador creado por defecto (password: operador123)");
        }
    }
}
