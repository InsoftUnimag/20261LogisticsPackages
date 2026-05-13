package com.logistics.packages.infrastructure.dto.response;

import com.logistics.packages.domain.model.Paquete;
import com.logistics.packages.domain.valueobject.EstadoPaquete;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Builder
@AllArgsConstructor
public class PaqueteListadoResponse {

    @Schema(description = "ID único del paquete")
    private UUID id;

    @Schema(description = "Etiqueta digital del paquete")
    private String etiquetaDigital;

    @Schema(description = "Estado actual del paquete")
    private EstadoPaquete estado;

    @Schema(description = "Tipo de mercancía")
    private String tipoMercancia;

    @Schema(description = "Nombre del remitente")
    private String remitente;

    @Schema(description = "Nombre del destinatario")
    private String destinatario;

    @Schema(description = "Ciudad de destino")
    private String ciudadDestino;

    @Schema(description = "Fecha de ingreso en UTC")
    private LocalDateTime fechaIngresoUtc;

    public static PaqueteListadoResponse fromDomain(Paquete paquete) {
        return PaqueteListadoResponse.builder()
                .id(paquete.getId())
                .etiquetaDigital(paquete.getEtiquetaDigital())
                .estado(paquete.getEstado())
                .tipoMercancia(paquete.getTipoMercancia() != null ? paquete.getTipoMercancia().name() : null)
                .remitente(paquete.getRemitente() != null ? paquete.getRemitente().getNombreCompleto() : null)
                .destinatario(paquete.getDestinatario() != null ? paquete.getDestinatario().getNombreCompleto() : null)
                .ciudadDestino(paquete.getDireccionDestino() != null ? paquete.getDireccionDestino().getCiudad() : null)
                .fechaIngresoUtc(paquete.getFechaIngresoUtc())
                .build();
    }
}
