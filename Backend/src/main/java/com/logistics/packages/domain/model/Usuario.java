package com.logistics.packages.domain.model;

import java.time.Instant;
import java.util.UUID;

public class Usuario {

    private UUID id;
    private String username;
    private String passwordHash;
    private String email;
    private String nombreCompleto;
    private String rol;
    private boolean enabled;
    private Instant fechaCreacion;
    private Instant fechaActualizacion;

    public Usuario() {}

    public Usuario(UUID id, String username, String passwordHash, String email,
                   String nombreCompleto, String rol, boolean enabled,
                   Instant fechaCreacion, Instant fechaActualizacion) {
        this.id = id;
        this.username = username;
        this.passwordHash = passwordHash;
        this.email = email;
        this.nombreCompleto = nombreCompleto;
        this.rol = rol;
        this.enabled = enabled;
        this.fechaCreacion = fechaCreacion;
        this.fechaActualizacion = fechaActualizacion;
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getNombreCompleto() { return nombreCompleto; }
    public void setNombreCompleto(String nombreCompleto) { this.nombreCompleto = nombreCompleto; }
    public String getRol() { return rol; }
    public void setRol(String rol) { this.rol = rol; }
    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }
    public Instant getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(Instant fechaCreacion) { this.fechaCreacion = fechaCreacion; }
    public Instant getFechaActualizacion() { return fechaActualizacion; }
    public void setFechaActualizacion(Instant fechaActualizacion) { this.fechaActualizacion = fechaActualizacion; }
}
