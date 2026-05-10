package com.logistics.packages.infrastructure.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * Payload JSON para la respuesta de asignación de ruta del Módulo de Gestión de Rutas
 * FR-003: Contiene el ID de ruta asignado por el módulo de rutas
 * MOD1-IP-003 - Phase 3
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RespuestaRutaPayload {

    @JsonProperty("paquete_id")
    @Schema(description = "ID del paquete")
    private UUID paqueteId;

    @JsonProperty("ruta_id")
    @Schema(description = "ID de la ruta asignada")
    private UUID rutaId;

    @JsonProperty("estado")
    @Schema(description = "Estado de la ruta (asignada | pendiente)")
    private String estado;

    @JsonProperty("tiempo_estimado_dias")
    @Schema(description = "Tiempo estimado de entrega en días")
    private Integer tiempoEstimadoDias;

    @JsonProperty("mensaje")
    @Schema(description = "Mensaje informativo de la operación")
    private String mensaje;
}
