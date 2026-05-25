package com.logistics.packages.infrastructure.dto.response;

import com.logistics.packages.domain.valueobject.EstadoPaquete;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO de respuesta para un elemento del historial de estados de un paquete.
 * MOD1-UC-007: Expone el historial inmutable de transiciones al cliente HTTP.
 * 
 * Este DTO se utiliza para la API GET /api/paquetes/{paqueteId}/historial
 */
@Getter
@Builder
@AllArgsConstructor
public class HistorialEstadoItemDto {
    
    @Schema(description = "ID único del registro de historial", example = "550e8400-e29b-41d4-a716-446655440000")
    private UUID id;
    
    @Schema(description = "ID del paquete asociado al registro", example = "660e8400-e29b-41d4-a716-446655440111")
    private UUID paqueteId;
    
    @Schema(description = "Estado anterior del paquete antes de la transición", example = "LISTO_PARA_DESPACHO")
    private String estadoAnterior;
    
    @Schema(description = "Estado nuevo del paquete después de la transición", example = "EN_TRANSITO")
    private String estadoNuevo;
    
    @Schema(description = "Observaciones o notas adicionales sobre la transición", example = "Paquete iniciado en ruta")
    private String observaciones;
    
    @Schema(description = "ID del usuario o módulo responsable de la transición", example = "00000000-0000-0000-0000-000000000002")
    private UUID usuarioId;
    
    @Schema(description = "URL de la evidencia multimedia (obligatoria para tipo DAÑADO)", nullable = true, example = "https://s3.amazonaws.com/novedades/paquete-123.jpg")
    private String urlEvidencia;
    
    @Schema(description = "Tipo de novedad si aplica (DAÑADO, EXTRAVIADO, etc.)", nullable = true, example = "DAÑADO")
    private String tipoNovedad;
    
    @Schema(description = "Timestamp UTC del momento en que se registró la transición", example = "2026-05-25T16:15:00.000")
    private LocalDateTime fechaTransicionUtc;
}
