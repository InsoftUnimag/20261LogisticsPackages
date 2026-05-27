package com.logistics.packages.infrastructure.controller;

import com.logistics.packages.application.repository.SedeRepository;
import com.logistics.packages.domain.model.Sede;
import com.logistics.packages.infrastructure.dto.response.SedeResponseDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Controlador REST para operaciones relacionadas con sedes.
 * Feature: Permitir que los operadores seleccionen la sede de admisión dinámicamente.
 */
@Tag(name = "Sedes", description = "Gestión de sedes de operación")
@Slf4j
@RestController
@RequestMapping("/api/sedes")
@RequiredArgsConstructor
public class SedeController {
    
    private final SedeRepository sedeRepository;
    
    /**
     * Obtiene la lista de todas las sedes disponibles.
     * Útil para que el operador pueda seleccionar la sede en la pantalla de admisión.
     * 
     * @return Lista de sedes disponibles con todos sus datos (tarifas, capacidades, etc.)
     */
    @Operation(summary = "Listar todas las sedes", description = "Retorna la lista completa de sedes disponibles en el sistema. Se usa en el selector de sedes de la pantalla de admisión.")
    @ApiResponse(responseCode = "200", description = "Lista de sedes obtenida exitosamente")
    @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    @GetMapping
    public ResponseEntity<List<SedeResponseDto>> listarSedes() {
        log.info("Recibida solicitud de listado de sedes");
        
        List<Sede> sedes = sedeRepository.findAll();
        List<SedeResponseDto> response = sedes.stream()
                .map(SedeResponseDto::fromDomain)
                .toList();
        
        log.info("Listado de sedes completado: {} sedes encontradas", response.size());
        
        return ResponseEntity.ok(response);
    }
}
