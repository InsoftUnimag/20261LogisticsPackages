package com.logistics.packages.infrastructure.controller;

import com.logistics.packages.application.usecase.gestionnovedad.ConsultaPaqueteResponse;
import com.logistics.packages.application.usecase.gestionnovedad.ConsultarEstadoPaqueteUseCase;
import com.logistics.packages.domain.exception.PaqueteNotFoundException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * Controller REST para consultas del Módulo de Gestión de Finanzas.
 * MOD1-UC-007: FR-005, FR-006, FR-007 - Endpoint síncrono para consultas de paquetes.
 * 
 * Este controller expone el endpoint GET /route/{idRoute}/package/{idPaquete}
 * que permite al Módulo de Finanzas obtener información del estado del paquete.
 * 
 * Responses:
 * - 200 OK: Paquete encontrado con información completa
 * - 404 Not Found: Paquete no existe
 * - 500 Internal Server Error: Error interno del servidor
 */
@Tag(name = "Consultas Financieras", description = "Endpoints para consulta de estado de paquetes desde el Módulo de Finanzas")
@RestController
@RequestMapping("/route")
@RequiredArgsConstructor
@Slf4j
public class ConsultaFinancieraController {
    
    private final ConsultarEstadoPaqueteUseCase consultarEstadoPaqueteUseCase;
    
    /**
     * Endpoint para consultar el estado de un paquete.
     * FR-005: GET /route/{idRoute}/package/{idPaquete}
     * 
     * @param idRoute ID de la ruta
     * @param idPaquete ID del paquete
     * @return ResponseEntity con ConsultaPaqueteResponse
     */
    @Operation(summary = "Consultar estado de paquete por ruta", description = "Permite al Módulo de Finanzas consultar el estado de un paquete asociado a una ruta específica")
    @ApiResponse(responseCode = "200", description = "Paquete encontrado con información completa")
    @ApiResponse(responseCode = "404", description = "Paquete no encontrado o no pertenece a la ruta especificada")
    @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    @GetMapping("/{idRoute}/package/{idPaquete}")
    public ResponseEntity<ConsultaPaqueteResponse> consultarEstadoPaquete(
            @PathVariable UUID idRoute,
            @PathVariable UUID idPaquete) {
        
        log.info("Solicitud de consulta recibida - Ruta: {}, Paquete: {}", idRoute, idPaquete);
        
        try {
            ConsultaPaqueteResponse response = consultarEstadoPaqueteUseCase.consultar(idRoute, idPaquete);
            
            // FR-007: Código 200 OK cuando el paquete existe
            return ResponseEntity.ok(response);
            
        } catch (PaqueteNotFoundException e) {
            // FR-007: Código 404 Not Found cuando el paquete no existe
            log.warn("Paquete no encontrado: {}", idPaquete);
            return ResponseEntity.notFound().build();
            
        } catch (IllegalArgumentException e) {
            // El paquete no pertenece a la ruta especificada
            log.warn("El paquete {} no pertenece a la ruta {}", idPaquete, idRoute);
            return ResponseEntity.notFound().build();
            
        } catch (Exception e) {
            // SC-005: Código 500 Internal Server Error para errores inesperados
            log.error("Error al consultar paquete {}: {}", idPaquete, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
