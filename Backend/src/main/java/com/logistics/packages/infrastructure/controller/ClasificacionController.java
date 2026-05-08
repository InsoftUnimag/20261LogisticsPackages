package com.logistics.packages.infrastructure.controller;

import com.logistics.packages.application.usecase.ClasificarPaqueteUseCase;
import com.logistics.packages.application.usecase.ClasificacionSugeridaResponse;
import com.logistics.packages.infrastructure.dto.request.ConfirmarZonaRequest;
import com.logistics.packages.infrastructure.dto.response.ClasificacionSugeridaResponseDTO;
import com.logistics.packages.infrastructure.dto.response.ConfirmacionClasificacionResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * Controller REST para la clasificación de paquetes por zona de destino.
 * MOD1-IP-005: T512, T513 - Phase 4
 * 
 * Endpoints:
 * - GET /api/clasificacion/sugerencia/{paqueteId} - Obtiene sugerencia de zona
 * - POST /api/clasificacion/confirmar - Confirma la clasificación
 */
@Slf4j
@RestController
@RequestMapping("/api/paquetes")
@RequiredArgsConstructor
public class ClasificacionController {

    private final ClasificarPaqueteUseCase clasificarPaqueteUseCase;

    /**
     * Obtiene la sugerencia de zona de destino para un paquete.
     * T513: Endpoint GET para obtener zona sugerida
     * 
     * @param paqueteId El ID del paquete
     * @return Respuesta con la zona sugerida
     */
    @GetMapping("/clasificacion/sugerencia/{paqueteId}")
    public ResponseEntity<ClasificacionSugeridaResponseDTO> obtenerSugerenciaZona(
            @PathVariable UUID paqueteId) {
        
        log.info("Solicitud de sugerencia de zona para paquete: {}", paqueteId);
        
        try {
            ClasificacionSugeridaResponse sugerencia = 
                    clasificarPaqueteUseCase.sugerirZonaParaPaquete(paqueteId);
            
            ClasificacionSugeridaResponseDTO response = ClasificacionSugeridaResponseDTO.builder()
                    .paqueteId(sugerencia.getPaqueteId())
                    .zonaDestinoId(sugerencia.getZonaDestinoId())
                    .nombreZona(sugerencia.getNombreZona())
                    .codigoZona(sugerencia.getCodigoZona())
                    .tieneCapacidad(true) // La validación se hace en la confirmación
                    .mensaje("Zona sugerida exitosamente")
                    .build();
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Error obteniendo sugerencia de zona para paquete {}: {}", 
                    paqueteId, e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Confirma la clasificación de un paquete en una zona de destino.
     * T512: Endpoint POST para confirmar clasificación
     * FR-002, FR-003, FR-004
     * 
     * @param request Solicitud con paqueteId y zonaDestinoId
     * @return Respuesta de confirmación
     */
    @PostMapping("/clasificacion/confirmar")
    public ResponseEntity<ConfirmacionClasificacionResponse> confirmarClasificacion(
            @Valid @RequestBody ConfirmarZonaRequest request) {
        
        log.info("Confirmando clasificación - Paquete: {}, Zona: {}", 
                request.getPaqueteId(), request.getZonaDestinoId());
        
        try {
            clasificarPaqueteUseCase.confirmarClasificacion(
                    request.getPaqueteId(), 
                    request.getZonaDestinoId()
            );
            
            ConfirmacionClasificacionResponse response = ConfirmacionClasificacionResponse.builder()
                    .paqueteId(request.getPaqueteId())
                    .zonaDestinoId(request.getZonaDestinoId())
                    .nombreZona("Zona asignada") // Se podría obtener del repositorio si es necesario
                    .estadoPaquete("LISTO_PARA_DESPACHO")
                    .mensaje("Paquete clasificado exitosamente y listo para despacho")
                    .build();
            
            return ResponseEntity.status(HttpStatus.OK).body(response);
            
        } catch (Exception e) {
            log.error("Error confirmando clasificación para paquete {}: {}", 
                    request.getPaqueteId(), e.getMessage(), e);
            throw e;
        }
    }
}
