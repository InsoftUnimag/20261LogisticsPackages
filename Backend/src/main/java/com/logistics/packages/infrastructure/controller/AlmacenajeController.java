package com.logistics.packages.infrastructure.controller;

import com.logistics.packages.application.repository.PrepararAlmacenajeIn;
import com.logistics.packages.application.usecase.PrepararAlmacenajeCommand;
import com.logistics.packages.domain.model.Paquete;
import com.logistics.packages.domain.model.ZonaAlmacenaje;
import com.logistics.packages.infrastructure.dto.request.AsignarZonaRequest;
import com.logistics.packages.infrastructure.dto.response.AsignacionZonaResponse;
import com.logistics.packages.application.repository.PaqueteRepository;
import com.logistics.packages.application.repository.ZonaAlmacenajeRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * Controller REST para la preparación de paquetes para almacenaje.
 * MOD1-UC-004: Endpoints para gestionar zona de almacenamiento física.
 * 
 * Endpoints:
 * - GET /api/paquetes/{paqueteId}/almacenaje/sugerencia - Obtiene zona sugerida
 * - POST /api/paquetes/{paqueteId}/almacenaje - Asigna zona de almacenamiento
 */
@Tag(name = "Almacenaje", description = "Asignación y gestión de zonas de almacenamiento físico")
@Slf4j
@RestController
@RequestMapping("/api/paquetes")
@RequiredArgsConstructor
public class AlmacenajeController {

    private final PaqueteRepository paqueteRepository;
    private final ZonaAlmacenajeRepository zonaAlmacenajeRepository;
    private final PrepararAlmacenajeIn prepararAlmacenajeIn;

    /**
     * Obtiene la zona de almacenamiento sugerida para un paquete.
     * FR-002: Sugerir automáticamente la zona más apropiada.
     * 
     * @param paqueteId El ID del paquete
     * @return Respuesta con la zona sugerida
     */
    @Operation(summary = "Obtener zona de almacenaje sugerida", description = "Sugiere automáticamente la zona de almacenamiento más apropiada para un paquete según su tipo de mercancía y capacidad disponible")
    @ApiResponse(responseCode = "200", description = "Zona sugerida encontrada exitosamente")
    @ApiResponse(responseCode = "404", description = "Paquete no encontrado o no hay zona disponible")
    @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    @GetMapping("/{paqueteId}/almacenaje/sugerencia")
    public ResponseEntity<AsignacionZonaResponse> obtenerZonaSugerida(
            @PathVariable UUID paqueteId) {
        
        log.info("Solicitando zona sugerida para paquete: {}", paqueteId);
        
        Paquete paquete = paqueteRepository.findById(paqueteId)
                .orElseThrow(() -> new IllegalArgumentException("Paquete no encontrado: " + paqueteId));
        
        var zonasCompatibles = zonaAlmacenajeRepository.findCompatibleZonesWithCapacity(
                paquete.getTipoMercancia(), paquete.getSedeId());
        
        ZonaAlmacenaje zonaSugerida = zonasCompatibles.stream()
                .filter(z -> z.tieneCapacidadPara(paquete))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("No hay zona disponible para el paquete: " + paqueteId));
        
        AsignacionZonaResponse response = AsignacionZonaResponse.builder()
                .paqueteId(paqueteId)
                .zonaId(zonaSugerida.getId())
                .nombreZona(zonaSugerida.getNombre())
                .datosActualizados(false)
                .mensaje("Zona sugerida correctamente")
                .build();
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * Asigna una zona de almacenamiento a un paquete.
     * FR-001: Asignación por UUID del paquete
     * FR-003: Actualizar estado a En Clasificación
     * 
     * @param paqueteId El ID del paquete
     * @param request Solicitud con ID de la zona
     * @return Respuesta de asignación confirmada
     */
    @Operation(summary = "Asignar zona de almacenaje", description = "Asigna una zona de almacenamiento a un paquete y actualiza su estado a 'En Clasificación'. Soporta actualización de datos físicos por discrepancia")
    @ApiResponse(responseCode = "201", description = "Zona asignada y paquete actualizado exitosamente")
    @ApiResponse(responseCode = "400", description = "Solicitud inválida: IDs no coinciden, zona no apta o capacidad excedida")
    @ApiResponse(responseCode = "404", description = "Paquete o zona de almacenamiento no encontrados")
    @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    @PostMapping("/{paqueteId}/almacenaje")
    public ResponseEntity<AsignacionZonaResponse> asignarZonaAlmacenamiento(
            @PathVariable UUID paqueteId,
            @Valid @RequestBody AsignarZonaRequest request) {
        
        log.info("Asignando zona {} al paquete {}", request.getZonaId(), paqueteId);
        
        if (!request.getPaqueteId().equals(paqueteId)) {
            return ResponseEntity.badRequest().build();
        }
        
        Paquete paquete = paqueteRepository.findById(paqueteId)
                .orElseThrow(() -> new IllegalArgumentException("Paquete no encontrado: " + paqueteId));
        
        ZonaAlmacenaje zona = zonaAlmacenajeRepository.findById(request.getZonaId())
                .orElseThrow(() -> new IllegalArgumentException("Zona no encontrada: " + request.getZonaId()));
        
        if (!zona.puedeAlbergar(paquete)) {
            throw new IllegalArgumentException("La zona no es apta para el tipo de mercancía: " + paquete.getTipoMercancia());
        }
        
        if (!zona.tieneCapacidadPara(paquete)) {
            throw new IllegalArgumentException("La zona ha alcanzado su capacidad máxima");
        }
        
        boolean datosActualizados = false;
        if (request.tieneDiscrepancias()) {
            var datos = request.getDatosDiscrepancia();
            paquete.actualizarDatosFisicos(
                    new com.logistics.packages.domain.valueobject.Peso(datos.getPesoKg()),
                    new com.logistics.packages.domain.valueobject.Dimensiones(
                            datos.getLargoCm(),
                            datos.getAnchoCm(),
                            datos.getAltoCm())
            );
            datosActualizados = true;
            log.info("Datos físicos actualizados por discrepancia para paquete: {}", paqueteId);
        }
        
        PrepararAlmacenajeCommand command = new PrepararAlmacenajeCommand(paqueteId, request.getZonaId());
        prepararAlmacenajeIn.prepararAlmacenaje(command);
        
        AsignacionZonaResponse response = AsignacionZonaResponse.builder()
                .paqueteId(paqueteId)
                .zonaId(zona.getId())
                .nombreZona(zona.getNombre())
                .datosActualizados(datosActualizados)
                .mensaje("Paquete asignado a zona de almacenamiento y estado actualizado a En Clasificación")
                .build();
        
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}