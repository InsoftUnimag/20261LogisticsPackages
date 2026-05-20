package com.logistics.packages.infrastructure.dto.response;

import com.logistics.packages.domain.valueobject.EstadoGps;
import com.logistics.packages.domain.valueobject.EstadoPaquete;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

/**
 * DTO de respuesta para el registro de admisión de paquete (MOD1-UC-001)
 * 
 * BE-4: Enriquecida con etiquetaDigital, estadoGps y estado
 * para que el frontend sepa si debe mostrar el fallback de coordenadas manuales.
 */
@Getter
@Builder
@AllArgsConstructor
public class RegistroAdmisionResponse {
    @Schema(description = "ID único del paquete registrado en el sistema")
    private UUID paqueteId;
    
    @Schema(description = "Etiqueta digital generada automáticamente para el paquete (ej: PK-xxxxx-xxxxx)")
    private String etiquetaDigital;
    
    @Schema(description = "Estado del GPS (PENDIENTE o RESUELTO). Si es PENDIENTE, el frontend debe mostrar campos de coordenadas manuales")
    private String estadoGps;
    
    @Schema(description = "Estado actual del paquete (ej: RECIBIDO_EN_SEDE)")
    private String estado;
}
