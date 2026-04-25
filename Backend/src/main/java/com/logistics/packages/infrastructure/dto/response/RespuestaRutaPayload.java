package com.logistics.packages.infrastructure.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
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
    private UUID paqueteId;

    @JsonProperty("ruta_id")
    private UUID rutaId;

    @JsonProperty("estado")
    private String estado; // "asignada" o "pendiente"

    @JsonProperty("tiempo_estimado_dias")
    private Integer tiempoEstimadoDias;

    @JsonProperty("mensaje")
    private String mensaje;
}
