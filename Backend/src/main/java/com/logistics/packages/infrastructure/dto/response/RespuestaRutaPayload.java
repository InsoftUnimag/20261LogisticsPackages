package com.logistics.packages.infrastructure.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
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
public class RespuestaRutaPayload {

    @JsonProperty("tipo_evento")
    private String tipoEvento;

    @JsonProperty("paquete_id")
    private UUID paqueteId;

    @JsonProperty("ruta_id")
    private UUID rutaId;

    @JsonProperty("fecha_hora_evento")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ssXXX", timezone = "UTC")
    private OffsetDateTime fechaHoraEvento;
}
