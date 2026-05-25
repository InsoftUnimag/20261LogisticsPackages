package com.logistics.packages.infrastructure.controller;

import com.logistics.packages.application.repository.PrepararAlmacenajeIn;
import com.logistics.packages.application.usecase.PrepararAlmacenajeCommand;
import com.logistics.packages.domain.exception.PaqueteNotFoundException;
import com.logistics.packages.domain.exception.ZonaAlmacenajeNotFoundException;
import com.logistics.packages.domain.model.Paquete;
import com.logistics.packages.domain.model.ZonaAlmacenaje;
import com.logistics.packages.domain.valueobject.EstadoPaquete;
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
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;
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
        
        try {
            Paquete paquete = paqueteRepository.findById(paqueteId)
                    .orElseThrow(() -> new PaqueteNotFoundException(paqueteId));
            
            // Validar que el paquete esté en estado RECIBIDO_EN_SEDE
            if (paquete.getEstado() != EstadoPaquete.RECIBIDO_EN_SEDE) {
                throw new IllegalStateException(
                    "El paquete " + paqueteId + " ya fue procesado. Estado actual: " + paquete.getEstado() + 
                    ". No puede sugerir una nueva zona de almacenaje para un paquete ya clasificado."
                );
            }
            
            // Buscar zonas compatibles con la sede del paquete
            var zonasCompatibles = zonaAlmacenajeRepository.findCompatibleZonesWithCapacity(
                    paquete.getTipoMercancia(), paquete.getSedeId());
            
            if (zonasCompatibles.isEmpty()) {
                throw new IllegalArgumentException("No hay zonas disponibles para el tipo de mercancía: " + paquete.getTipoMercancia());
            }
            
            // Intentar encontrar la zona ideal (con capacidad disponible)
            ZonaAlmacenaje zonaSugerida = zonasCompatibles.stream()
                    .filter(z -> z.tieneCapacidadPara(paquete))
                    .findFirst()
                    .orElse(null);
            
            // Si no hay zona con capacidad, usar la zona de contingencia de la primera zona compatible
            boolean zonaPrincipalSaturada = false;
            String nombreZonaPrincipal = null;
            if (zonaSugerida == null) {
                zonaPrincipalSaturada = true;
                ZonaAlmacenaje zonaPrincipal = zonasCompatibles.get(0);
                nombreZonaPrincipal = zonaPrincipal.getNombre();
                
                if (zonaPrincipal.getZonaContingenciaId() != null) {
                    zonaSugerida = zonaAlmacenajeRepository.findById(zonaPrincipal.getZonaContingenciaId())
                            .orElse(zonaPrincipal); // Fallback a la zona principal
                } else {
                    zonaSugerida = zonaPrincipal; // Usar la zona principal aunque esté saturada
                }
                
                log.warn("Zona principal saturada para paquete {}. Usando zona de contingencia: {}", 
                        paqueteId, zonaSugerida.getNombre());
            }
            
            AsignacionZonaResponse response = AsignacionZonaResponse.builder()
                    .paqueteId(paqueteId)
                    .zonaId(zonaSugerida.getId())
                    .nombreZona(zonaSugerida.getNombre())
                    .nombreZonaPrincipal(nombreZonaPrincipal)
                    .categoria(zonaSugerida.getCategoria())
                    .tipoMercancia(paquete.getTipoMercancia())
                    .pesoActualKg(zonaSugerida.getPesoActualKg() != null ? zonaSugerida.getPesoActualKg().doubleValue() : 0.0)
                    .capacidadMaxKg(zonaSugerida.getCapacidadMaxKg() != null ? zonaSugerida.getCapacidadMaxKg().doubleValue() : 0.0)
                    .volumenActualM3(zonaSugerida.getVolumenActualM3() != null ? zonaSugerida.getVolumenActualM3().doubleValue() : 0.0)
                    .capacidadMaxM3(zonaSugerida.getCapacidadMaxM3() != null ? zonaSugerida.getCapacidadMaxM3().doubleValue() : 0.0)
                    .contadorPaquetes(zonaSugerida.getContadorPaquetes() != null ? zonaSugerida.getContadorPaquetes() : 0)
                    .capacidadMaxPaquetes(zonaSugerida.getCapacidadMaxPaquetes() != null ? zonaSugerida.getCapacidadMaxPaquetes() : 0)
                    .zonaSaturada(zonaPrincipalSaturada)
                    .datosActualizados(false)
                    .mensaje(zonaPrincipalSaturada ? "Zona principal saturada. Usando zona de contingencia." : "Zona sugerida correctamente")
                    .build();
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Error al obtener zona sugerida para paquete {}: {}", paqueteId, e.getMessage(), e);
            throw e;
        }
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
        
        // Obtener usuarioId del contexto de seguridad
         String username = SecurityContextHolder.getContext().getAuthentication().getName();
          // En un caso de producción, buscaríamos el usuarioId por username desde el repositorio
          // Por ahora usamos el username como identificador único
          UUID usuarioId = UUID.nameUUIDFromBytes(username.getBytes());
          
          // Delegar toda la lógica de negocio al UseCase
          PrepararAlmacenajeCommand command = new PrepararAlmacenajeCommand(
                  paqueteId,
                  request.getZonaId(),
                  usuarioId,
                  request.tieneDiscrepancias() ? Optional.of(request.getDatosDiscrepancia()) : Optional.empty()
          );
         
         prepararAlmacenajeIn.prepararAlmacenaje(command);
         
         // Cargar la zona asignada para la respuesta
         ZonaAlmacenaje zona = zonaAlmacenajeRepository.findById(request.getZonaId())
                 .orElseThrow(() -> new IllegalArgumentException("Zona no encontrada: " + request.getZonaId()));
         
         // Cargar el paquete actualizado para obtener su estado actual
         Paquete paqueteActualizado = paqueteRepository.findById(paqueteId)
                 .orElseThrow(() -> new IllegalArgumentException("Paquete no encontrado: " + paqueteId));
        
        AsignacionZonaResponse response = AsignacionZonaResponse.builder()
                .paqueteId(paqueteId)
                .zonaId(zona.getId())
                .nombreZona(zona.getNombre())
                .estadoPaquete(paqueteActualizado.getEstado())
                .datosActualizados(request.tieneDiscrepancias())
                .mensaje("Paquete asignado a zona de almacenamiento y estado actualizado a En Clasificación")
                .build();
        
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
