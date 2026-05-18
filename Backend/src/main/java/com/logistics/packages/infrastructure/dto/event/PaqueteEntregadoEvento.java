package com.logistics.packages.infrastructure.dto.event;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
public class PaqueteEntregadoEvento extends EventoPaqueteM2Dto {

    @JsonProperty("evidencia")
    private EvidenciaDto evidencia;

    public PaqueteEntregadoEvento(String tipoEvento, UUID paqueteId, UUID rutaId, OffsetDateTime fechaHoraEvento, EvidenciaDto evidencia) {
        super(tipoEvento, paqueteId, rutaId, fechaHoraEvento);
        this.evidencia = evidencia;
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class EvidenciaDto {
        @JsonProperty("url_foto")
        private String urlFoto;

        @JsonProperty("url_firma")
        private String urlFirma;
    }
}
