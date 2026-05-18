package com.logistics.packages.infrastructure.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.logistics.packages.domain.valueobject.MetodoPago;
import com.logistics.packages.domain.valueobject.TipoMercancia;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SolicitudRutaPayload {

    @JsonProperty("tipo_evento")
    private String tipoEvento;

    @JsonProperty("paquete_id")
    private UUID paqueteId;

    @JsonProperty("peso_kg")
    private Double pesoKg;

    @JsonProperty("volumen_m3")
    private Double volumenM3;

    @JsonProperty("direccion")
    private DireccionDto direccion;

    @JsonProperty("latitud")
    private Double latitud;

    @JsonProperty("longitud")
    private Double longitud;

    @JsonProperty("fecha_limite_entrega")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ssXXX", timezone = "UTC")
    private OffsetDateTime fechaLimiteEntrega;

    @JsonProperty("tipo_mercancia")
    private TipoMercancia tipoMercancia;

    @JsonProperty("metodo_pago")
    private MetodoPago metodoPago;

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DireccionDto {
        @JsonProperty("direccion")
        private String direccion;

        @JsonProperty("ciudad")
        private String ciudad;

        @JsonProperty("pais")
        private String pais;
    }
}
