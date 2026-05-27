package com.logistics.packages.infrastructure.controller;

import com.logistics.packages.application.ports.SedeRepository;
import com.logistics.packages.domain.model.Sede;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

/**
 * Controlador REST para operaciones de Sedes.
 * Expone el endpoint GET /api/sedes para obtener la lista de sedes disponibles.
 */
@Tag(name = "Sedes", description = "Gestión de sedes logísticas")
@Slf4j
@RestController
@RequestMapping("/api/sedes")
@AllArgsConstructor
public class SedeController {

    private final SedeRepository sedeRepository;

    /**
     * Obtiene la lista de todas las sedes disponibles.
     * Usado por el frontend para poblar el selector de sedes en el formulario de admisión.
     * 
     * @return Lista de DTOs de sedes con id, nombre, ciudad, tipo
     */
    @Operation(summary = "Obtener todas las sedes", description = "Retorna la lista completa de sedes disponibles para asignar paquetes")
    @ApiResponse(responseCode = "200", description = "Lista de sedes obtenida exitosamente")
    @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    @GetMapping
    public ResponseEntity<List<SedeResponseDto>> obtenerTodasLasSedes() {
        log.info("Solicitud de listado de todas las sedes");
        
        try {
            List<Sede> sedes = sedeRepository.findAll();
            List<SedeResponseDto> response = sedes.stream()
                    .map(sede -> new SedeResponseDto(
                            sede.getId(),
                            sede.getNombre(),
                            sede.getDireccion() != null ? sede.getDireccion().getCiudad() : null,
                            sede.getTipo() != null ? sede.getTipo().toString() : null
                    ))
                    .toList();
            
            log.info("Retornando {} sedes disponibles", response.size());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error obteniendo sedes: {}", e.getMessage(), e);
            throw e;
        }
    }

    /**
     * DTO simple para la respuesta del endpoint GET /api/sedes.
     * Contiene solo los campos necesarios para el selector del frontend.
     */
    public record SedeResponseDto(
            UUID id,
            String nombre,
            String ciudad,
            String tipo
    ) {}
}
