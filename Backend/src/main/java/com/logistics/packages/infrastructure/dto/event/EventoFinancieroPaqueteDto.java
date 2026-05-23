package com.logistics.packages.infrastructure.dto.event;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

@Getter
@Builder
@AllArgsConstructor
public class EventoFinancieroPaqueteDto {

    @JsonProperty("id_paquete")
    private UUID idPaquete;

    @JsonProperty("id_ruta")
    private UUID idRuta;

    @JsonProperty("estado")
    private String estado;
}
