package com.logistics.packages.infrastructure.dto.event;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
public class PaqueteExcluidoDespachoEvento extends EventoPaqueteM2Dto {

    public PaqueteExcluidoDespachoEvento(String tipoEvento, UUID paqueteId, UUID rutaId, Instant fechaHoraEvento) {
        super(tipoEvento, paqueteId, rutaId, fechaHoraEvento);
    }
}
