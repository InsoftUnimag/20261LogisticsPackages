package com.logistics.packages.infrastructure.controller;

import com.logistics.packages.application.usecase.ClasificarPaqueteUseCase;
import com.logistics.packages.application.usecase.ClasificacionSugeridaResponse;
import com.logistics.packages.application.ports.ZonaDestinoRepository;
import com.logistics.packages.application.repository.PaqueteRepository;
import com.logistics.packages.domain.model.Paquete;
import com.logistics.packages.infrastructure.dto.response.ClasificacionSugeridaResponseDTO;
import com.logistics.packages.infrastructure.dto.response.ConfirmacionClasificacionResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Controller REST para la clasificación de paquetes por zona de destino.
 * MOD1-IP-005: T512, T513 - Phase 4
 * 
 * Endpoints:
 * - GET /api/paquetes/clasificacion/zonas - Obtiene lista de zonas disponibles
 * - GET /api/paquetes/clasificacion/sugerencia/{paqueteId} - Obtiene sugerencia de zona
 * - POST /api/paquetes/clasificacion/confirmar - Confirma la clasificación
 */
@Tag(name = "Clasificación", description = "Sugerencia y confirmación de zonas de destino para clasificación de paquetes")
@Slf4j
@RestController
@RequestMapping("/api/paquetes")
@RequiredArgsConstructor
public class ClasificacionController {

    private final ClasificarPaqueteUseCase clasificarPaqueteUseCase;
    private final ZonaDestinoRepository zonaDestinoRepository;
    private final PaqueteRepository paqueteRepository;

    /**
     * Obtiene la sugerencia de zona de destino para un paquete.
     * T513: Endpoint GET para obtener zona sugerida
     * 
     * @param paqueteId El ID del paquete
     * @return Respuesta con la zona sugerida
     */
    @Operation(summary = "Obtener sugerencia de zona destino", description = "Obtiene la zona de destino sugerida para un paquete basada en su geolocalización")
    @ApiResponse(responseCode = "200", description = "Sugerencia de zona obtenida exitosamente")
    @ApiResponse(responseCode = "404", description = "Paquete no encontrado")
    @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    @GetMapping("/clasificacion/sugerencia/{paqueteId}")
    public ResponseEntity<ClasificacionSugeridaResponseDTO> obtenerSugerenciaZona(
            @PathVariable UUID paqueteId) {
        
        log.info("Solicitud de sugerencia de zona para paquete: {}", paqueteId);
        
        try {
            ClasificacionSugeridaResponse sugerencia = 
                    clasificarPaqueteUseCase.sugerirZonaParaPaquete(paqueteId);
            
            // Obtener el paquete para extraer la ciudad del destinatario
            Paquete paquete = paqueteRepository.findById(paqueteId)
                    .orElseThrow(() -> new IllegalArgumentException("Paquete no encontrado: " + paqueteId));
            
            String ciudadDestino = paquete.getDireccionDestino() != null && paquete.getDireccionDestino().getCiudad() != null
                    ? paquete.getDireccionDestino().getCiudad()
                    : "Desconocida";
            
            // Obtener la zona de destino para validar capacidad
            var zonaSugerida = zonaDestinoRepository.findById(sugerencia.getZonaDestinoId())
                    .orElse(null);
            
            boolean tieneCapacidad = zonaSugerida != null && zonaSugerida.tieneCapacidadDisponible();
            
            ClasificacionSugeridaResponseDTO response = ClasificacionSugeridaResponseDTO.builder()
                    .paqueteId(sugerencia.getPaqueteId())
                    .zonaDestinoId(sugerencia.getZonaDestinoId())
                    .nombreZona(sugerencia.getNombreZona())
                    .codigoZona(sugerencia.getCodigoZona())
                    .ciudadDestino(ciudadDestino)
                    .tipoMercancia(paquete.getTipoMercancia() != null ? paquete.getTipoMercancia().toString() : "NORMAL")
                    .tieneCapacidad(tieneCapacidad)
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
      * Obtiene la lista de zonas de destino disponibles.
      * 
      * @return Lista de zonas disponibles
      */
     @Operation(summary = "Obtener zonas de destino disponibles", description = "Retorna la lista de zonas de destino disponibles para clasificación de paquetes")
     @ApiResponse(responseCode = "200", description = "Lista de zonas obtenida exitosamente")
     @ApiResponse(responseCode = "500", description = "Error interno del servidor")
     @GetMapping("/clasificacion/zonas")
     public ResponseEntity<List<Map<String, Object>>> obtenerZonasDisponibles() {
         log.info("Solicitud de lista de zonas de destino disponibles");
         
         try {
             // Obtener todas las zonas de destino activas del repositorio
             List<Map<String, Object>> zonas = zonaDestinoRepository.findAllActivas()
                     .stream()
                     .map(zona -> {
                         Map<String, Object> mapa = new HashMap<>();
                         mapa.put("id", zona.getId().toString());
                         mapa.put("nombre", zona.getNombre());
                         mapa.put("codigo", zona.getCodigo());
                         mapa.put("categoria", zona.getCategoria() != null ? zona.getCategoria().toString() : "NORMAL");
                         return mapa;
                     })
                     .toList();
             log.info("Retornando {} zonas disponibles", zonas.size());
             return ResponseEntity.ok(zonas);
         } catch (Exception e) {
             log.error("Error obteniendo zonas disponibles: {}", e.getMessage(), e);
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
    @Operation(summary = "Confirmar clasificación de paquete", description = "Confirma la clasificación de un paquete en una zona de destino específica y actualiza su estado a 'Listo para Despacho'")
    @ApiResponse(responseCode = "200", description = "Clasificación confirmada exitosamente")
    @ApiResponse(responseCode = "400", description = "Solicitud inválida: datos de confirmación incorrectos")
    @ApiResponse(responseCode = "404", description = "Paquete o zona de destino no encontrados")
    @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    @PostMapping(value = "/clasificacion/confirmar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ConfirmacionClasificacionResponse> confirmarClasificacion(
            @RequestParam UUID paqueteId,
            @RequestParam UUID zonaDestinoId,
            @RequestParam(required = false) MultipartFile evidencia) {
        
        log.info("Confirmando clasificación - Paquete: {}, Zona: {}", 
                paqueteId, zonaDestinoId);
        
        try {
            clasificarPaqueteUseCase.confirmarClasificacion(
                    paqueteId, 
                    zonaDestinoId,
                    evidencia
            );
            
            // Obtener el nombre real de la zona desde el repositorio
            String nombreZona = zonaDestinoRepository.findById(zonaDestinoId)
                    .map(zona -> zona.getNombre())
                    .orElse("Zona de destino");
            
            ConfirmacionClasificacionResponse response = ConfirmacionClasificacionResponse.builder()
                    .paqueteId(paqueteId)
                    .zonaDestinoId(zonaDestinoId)
                    .nombreZona(nombreZona)
                    .estadoPaquete("LISTO_PARA_DESPACHO")
                    .mensaje("Paquete clasificado exitosamente y listo para despacho")
                    .build();
            
            return ResponseEntity.status(HttpStatus.OK).body(response);
            
        } catch (Exception e) {
            log.error("Error confirmando clasificación para paquete {}: {}", 
                    paqueteId, e.getMessage(), e);
            throw e;
        }
    }
}
