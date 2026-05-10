package com.logistics.packages.infrastructure.dto.request;

import com.logistics.packages.domain.model.Persona;
import com.logistics.packages.domain.valueobject.Direccion;
import com.logistics.packages.domain.valueobject.MetodoPago;
import com.logistics.packages.domain.valueobject.TipoMercancia;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
public class RegistroAdmisionRequest {
    @NotNull
    @Schema(description = "ID de la sede donde se registra el paquete")
    private UUID sedeId;
    @NotNull
    @Schema(description = "Dirección de destino del paquete")
    private Direccion direccionDestino;
    @NotNull
    @Schema(description = "Valor declarado del paquete para asegurar")
    private BigDecimal valorDeclarado;
    @NotNull
    @Schema(description = "Método de pago del envío (PREPAGO | CONTRA_ENTREGA)")
    private MetodoPago metodoPago;
    @NotNull
    @Schema(description = "Datos del remitente")
    private Persona remitente;
    @NotNull
    @Schema(description = "Datos del destinatario")
    private Persona destinatario;
    @NotNull
    @Schema(description = "Tipo de mercancía (ESTANDAR | FRAGIL | PELIGROSO)")
    private TipoMercancia tipoMercancia;
    @NotNull
    @Schema(description = "Indica si el paquete tiene forma irregular")
    private Boolean indicadorFormaIrregular;
    @Schema(description = "Peso del paquete en kg (opcional, se procesa en pesaje)")
    private Double peso;
    @Schema(description = "Largo del paquete en cm (opcional, se procesa en pesaje)")
    private Double largo;
    @Schema(description = "Ancho del paquete en cm (opcional, se procesa en pesaje)")
    private Double ancho;
    @Schema(description = "Alto del paquete en cm (opcional, se procesa en pesaje)")
    private Double alto;
}
