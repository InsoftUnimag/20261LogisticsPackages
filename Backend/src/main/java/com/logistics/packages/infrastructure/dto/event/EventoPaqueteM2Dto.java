package com.logistics.packages.infrastructure.dto.event;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    property = "tipo_evento",
    visible = true
)
@JsonSubTypes({
    @JsonSubTypes.Type(value = PaqueteEnTransitoEvento.class, name = "PAQUETE_EN_TRANSITO"),
    @JsonSubTypes.Type(value = PaqueteEntregadoEvento.class, name = "PAQUETE_ENTREGADO"),
    @JsonSubTypes.Type(value = ParadaFallidaEvento.class, name = "PARADA_FALLIDA"),
    @JsonSubTypes.Type(value = NovedadGraveEvento.class, name = "NOVEDAD_GRAVE"),
    @JsonSubTypes.Type(value = ParadasSinGestionarEvento.class, name = "PARADAS_SIN_GESTIONAR"),
    @JsonSubTypes.Type(value = PaqueteExcluidoDespachoEvento.class, name = "PAQUETE_EXCLUIDO_DESPACHO")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public abstract class EventoPaqueteM2Dto {

    @JsonProperty("tipo_evento")
    private String tipoEvento;

    @JsonProperty("paquete_id")
    private UUID paqueteId;

    @JsonProperty("ruta_id")
    private UUID rutaId;

    @JsonProperty("fecha_hora_evento")
    private Instant fechaHoraEvento;
}
