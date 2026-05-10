package com.logistics.packages.infrastructure.dto.request;

import com.logistics.packages.domain.valueobject.TipoNovedad;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

/**
 * DTO de request para registrar una novedad en un paquete.
 * MOD1-UC-006: Validaciones a nivel de entrada HTTP.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RegistroNovedadRequest {
    
    @NotNull(message = "El tipo de novedad es obligatorio")
    @Schema(description = "Tipo de novedad (DAÑADO | EXTRAVIADO)")
    private TipoNovedad tipoNovedad;
    
    @Schema(description = "Observaciones adicionales sobre la novedad")
    private String observaciones;
    
    @NotNull(message = "El ID del usuario es obligatorio")
    @Schema(description = "ID del usuario que reporta la novedad")
    private UUID usuarioId;
}
