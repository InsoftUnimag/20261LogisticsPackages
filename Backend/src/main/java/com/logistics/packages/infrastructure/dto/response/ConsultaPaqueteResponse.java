package com.logistics.packages.infrastructure.dto.response;

import com.logistics.packages.domain.valueobject.EstadoPaquete;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.UUID;

@Getter
@AllArgsConstructor
public class ConsultaPaqueteResponse {
    @Schema(description = "ID de la ruta asignada al paquete")
    private UUID idRoute;
    @Schema(description = "ID del paquete consultado")
    private UUID idPaquete;
    @Schema(description = "Estado actual del paquete")
    private EstadoPaquete estado;
}
