package com.logistics.packages.infrastructure.dto.request;

import com.logistics.packages.domain.model.Persona;
import com.logistics.packages.domain.valueobject.Direccion;
import com.logistics.packages.domain.valueobject.MetodoPago;
import com.logistics.packages.domain.valueobject.TipoMercancia;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
public class RegistroAdmisionRequest {
    @NotNull
    private UUID sedeId;
    @NotNull
    private Direccion direccionDestino;
    @NotNull
    private BigDecimal valorDeclarado;
    @NotNull
    private MetodoPago metodoPago;
    @NotNull
    private Persona remitente;
    @NotNull
    private Persona destinatario;
    @NotNull
    private TipoMercancia tipoMercancia;
    @NotNull
    private Boolean indicadorFormaIrregular;
    private Double peso;
    private Double largo;
    private Double ancho;
    private Double alto;
}
