package com.logistics.packages.infrastructure.dto.event;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
public class ParadasSinGestionarEvento extends EventoPaqueteM2Dto {

    @JsonProperty("tipo_cierre")
    private String tipoCierre;

    @JsonProperty("paquetes")
    private List<PaqueteEnRutaDto> paquetes;

    public ParadasSinGestionarEvento(String tipoEvento, UUID rutaId, OffsetDateTime fechaHoraEvento,
                                     String tipoCierre, List<PaqueteEnRutaDto> paquetes) {
        super(tipoEvento, null, rutaId, fechaHoraEvento);
        this.tipoCierre = tipoCierre;
        this.paquetes = paquetes != null ? paquetes : Collections.emptyList();
    }

    @Getter
    @Setter
    @NoArgsConstructor
    public static class PaqueteEnRutaDto {
        @JsonProperty("paquete_id")
        private UUID paqueteId;
    }
}
