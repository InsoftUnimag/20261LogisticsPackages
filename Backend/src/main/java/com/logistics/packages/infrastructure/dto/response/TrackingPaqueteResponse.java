package com.logistics.packages.infrastructure.dto.response;

import com.logistics.packages.domain.valueobject.EstadoPaquete;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * DTO de respuesta para el tracking en tiempo real de un paquete.
 * FT-3: feature/tracking-paquete-en-ruta
 * 
 * Este DTO se utiliza para la API GET /api/paquetes/{paqueteId}/tracking
 * Incluye datos completos del paquete + historial de transiciones.
 */
@Getter
@Builder
@AllArgsConstructor
public class TrackingPaqueteResponse {
    
    @Schema(description = "ID único del paquete", example = "550e8400-e29b-41d4-a716-446655440000")
    private UUID paqueteId;
    
    @Schema(description = "Etiqueta digital del paquete", example = "PK-550E8400-E29B-41D4")
    private String etiquetaDigital;
    
    @Schema(description = "Estado actual del paquete", example = "EN_TRANSITO")
    private String estado;
    
    @Schema(description = "Nombre completo del remitente", example = "Juan Pérez")
    private String remitenteNombre;
    
    @Schema(description = "Nombre completo del destinatario", example = "María García")
    private String destinatarioNombre;
    
    @Schema(description = "Ciudad de destino", example = "Bogotá")
    private String ciudadDestino;
    
    @Schema(description = "Departamento de destino", example = "Cundinamarca")
    private String departamentoDestino;
    
    @Schema(description = "Distancia estimada en km desde la sede de origen", example = "250.5")
    private Double distanciaEstimadaKm;
    
    @Schema(description = "ID de la ruta asignada", example = "770e8400-e29b-41d4-a716-446655440001", nullable = true)
    private UUID rutaId;
    
    @Schema(description = "Fecha y hora de entrega (si ya fue entregado)", nullable = true, example = "2026-05-25T16:15:00.000")
    private LocalDateTime fechaEntregaUtc;
    
    @Schema(description = "Nombre de quien recibió el paquete (si ya fue entregado)", nullable = true, example = "Pedro López")
    private String nombreFirmante;
    
    @Schema(description = "URL de la evidencia de entrega (POD) - foto o firma", nullable = true, example = "https://s3.amazonaws.com/entregas/paquete-123-pod.jpg")
    private String urlEvidenciaEntrega;
    
    @Schema(description = "Historial inmutable de todas las transiciones de estado", type = "array")
    private List<HistorialEstadoItemDto> historial;
}
