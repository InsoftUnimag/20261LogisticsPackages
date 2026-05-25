package com.logistics.packages.infrastructure.dto.response;

import java.util.UUID;

public class JwtResponse {

    private String token;
    private String type = "Bearer";
    private String username;
    private String rol;
    private UUID userId;

    public JwtResponse() {}

    public JwtResponse(String token, String username, String rol) {
        this.token = token;
        this.username = username;
        this.rol = rol;
    }

    public JwtResponse(String token, String username, String rol, UUID userId) {
        this.token = token;
        this.username = username;
        this.rol = rol;
        this.userId = userId;
    }

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getRol() { return rol; }
    public void setRol(String rol) { this.rol = rol; }
    public UUID getUserId() { return userId; }
    public void setUserId(UUID userId) { this.userId = userId; }
}
