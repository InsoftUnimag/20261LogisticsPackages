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

    public enum MotivoParadaFallida {
        CLIENTE_AUSENTE,
        DIRECCION_INCORRECTA,
        RECHAZADO_POR_CLIENTE,
        ZONA_DIFICIL_ACCESO
    }

    @JsonProperty("motivo")
    private MotivoParadaFallida motivo;

    public ParadaFallidaEvento(String tipoEvento, UUID paqueteId, UUID rutaId, OffsetDateTime fechaHoraEvento, MotivoParadaFallida motivo) {
        super(tipoEvento, paqueteId, rutaId, fechaHoraEvento);
        this.motivo = motivo;
    }
}
