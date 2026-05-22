package com.logistics.packages.infrastructure.dto.response;

import com.logistics.packages.domain.valueobject.EstadoPaquete;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

/**
 * Respuesta de consulta de paquete - MOD1-UC-003 (SC-004)
 * Expone el rutaId y un indicador de "fecha sujeta a confirmación" para que el frontend
 * pueda mostrar el estado correcto cuando M2 aún no ha asignado una ruta.
 */
@Getter
@Builder
@AllArgsConstructor
public class ConsultaPaqueteResponse {
    @Schema(description = "ID de la ruta asignada al paquete (null si aún no ha sido asignada)")
    private UUID rutaId;
    
    @Schema(description = "ID del paquete consultado")
    private UUID idPaquete;
    
    @Schema(description = "Estado actual del paquete")
    private EstadoPaquete estado;
    
    @Schema(description = "Indica si la fecha de entrega está sujeta a confirmación por M2. True si rutaId == null",
            example = "true")
    private Boolean fechaEntregaSujetaConfirmacion;
    
    /**
     * Constructor legado para retrocompatibilidad con el campo idRoute.
     * @deprecated Usar el constructor con @Builder y proporcionar rutaId explícitamente
     */
    @Deprecated(since = "2026-05-21")
    public ConsultaPaqueteResponse(UUID idRoute, UUID idPaquete, EstadoPaquete estado) {
        this.rutaId = idRoute;
        this.idPaquete = idPaquete;
        this.estado = estado;
        this.fechaEntregaSujetaConfirmacion = idRoute == null;
    }
}
