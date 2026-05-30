package com.logistics.packages.infrastructure.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsConfig implements WebMvcConfigurer {

    // Soporta múltiples orígenes separados por coma en tu application.yml
    @Value("${app.cors.allowed-origins:http://localhost:3000,http://localhost:5173,http://3.146.113.126:3000}")
    private String[] allowedOrigins;

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins(allowedOrigins)
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*") // BUENA PRÁCTICA: Permite cualquier cabecera para que el handshake de CORS no falle con JWT
                .exposedHeaders("Authorization") // Permite al frontend leer el token si viene en las cabeceras de respuesta
                .allowCredentials(true);
    }
}