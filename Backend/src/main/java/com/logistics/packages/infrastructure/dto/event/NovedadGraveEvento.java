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
public class NovedadGraveEvento extends EventoPaqueteM2Dto {

    @JsonProperty("tipo_novedad")
    private String tipoNovedad;

    public NovedadGraveEvento(String tipoEvento, UUID paqueteId, UUID rutaId, OffsetDateTime fechaHoraEvento, String tipoNovedad) {
        super(tipoEvento, paqueteId, rutaId, fechaHoraEvento);
        this.tipoNovedad = tipoNovedad;
    }
}
