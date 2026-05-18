package com.logistics.packages.infrastructure.dto.event;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
public class ParadaFallidaEvento extends EventoPaqueteM2Dto {

    @JsonProperty("motivo")
    private String motivo;

    public ParadaFallidaEvento(String tipoEvento, UUID paqueteId, UUID rutaId, OffsetDateTime fechaHoraEvento, String motivo) {
        super(tipoEvento, paqueteId, rutaId, fechaHoraEvento);
        this.motivo = motivo;
    }
}
