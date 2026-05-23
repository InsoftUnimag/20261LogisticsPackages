package com.logistics.packages.infrastructure.adapter.messaging;

import com.logistics.packages.application.usecase.gestionnovedad.EventoRutaDto;
import com.logistics.packages.application.usecase.gestionnovedad.EventoRutaDto.TipoEventoRuta;
import com.logistics.packages.infrastructure.dto.event.NovedadGraveEvento;
import com.logistics.packages.infrastructure.dto.event.NovedadGraveEvento.TipoNovedadGrave;
import com.logistics.packages.infrastructure.dto.event.PaqueteEntregadoEvento;
import com.logistics.packages.infrastructure.dto.event.PaqueteExcluidoDespachoEvento;
import com.logistics.packages.infrastructure.dto.event.ParadaFallidaEvento;
import com.logistics.packages.infrastructure.dto.event.ParadaFallidaEvento.MotivoParadaFallida;
import com.logistics.packages.infrastructure.dto.event.ParadasSinGestionarEvento;
import com.logistics.packages.infrastructure.dto.event.PaqueteEnTransitoEvento;
import com.logistics.packages.infrastructure.dto.event.EventoPaqueteM2Dto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
@Component
public class EventoPaqueteM2Mapper {

    public List<EventoRutaDto> mapToEventoRuta(EventoPaqueteM2Dto m2Dto) {
        List<EventoRutaDto> resultados = new ArrayList<>();

        switch (m2Dto.getTipoEvento()) {
            case "PAQUETE_EN_TRANSITO" -> {
                PaqueteEnTransitoEvento evento = (PaqueteEnTransitoEvento) m2Dto;
                resultados.add(construirDto(evento.getPaqueteId(), evento.getRutaId(),
                        evento.getFechaHoraEvento(), TipoEventoRuta.EN_TRANSITO, null, null, null, null));
            }
            case "PAQUETE_ENTREGADO" -> {
                PaqueteEntregadoEvento evento = (PaqueteEntregadoEvento) m2Dto;
                String urlEvidencia = evento.getEvidencia() != null ? evento.getEvidencia().getUrlFoto() : null;
                resultados.add(construirDto(evento.getPaqueteId(), evento.getRutaId(),
                        evento.getFechaHoraEvento(), TipoEventoRuta.ENTREGADO, null, urlEvidencia, null, null));
            }
            case "PARADA_FALLIDA" -> {
                ParadaFallidaEvento evento = (ParadaFallidaEvento) m2Dto;
                String motivo = mapMotivo(evento.getMotivo());
                resultados.add(construirDto(evento.getPaqueteId(), evento.getRutaId(),
                        evento.getFechaHoraEvento(), TipoEventoRuta.DEVOLUCION, motivo, null, null, null));
            }
            case "NOVEDAD_GRAVE" -> {
                NovedadGraveEvento evento = (NovedadGraveEvento) m2Dto;
                TipoEventoRuta tipoApp = mapTipoNovedad(evento.getTipoNovedad());
                if (tipoApp != null) {
                    resultados.add(construirDto(evento.getPaqueteId(), evento.getRutaId(),
                            evento.getFechaHoraEvento(), tipoApp, null, null, null, null));
                }
            }
            case "PARADAS_SIN_GESTIONAR" -> {
                ParadasSinGestionarEvento evento = (ParadasSinGestionarEvento) m2Dto;
                for (ParadasSinGestionarEvento.PaqueteEnRutaDto p : evento.getPaquetes()) {
                    resultados.add(construirDto(p.getPaqueteId(), evento.getRutaId(),
                            evento.getFechaHoraEvento(), TipoEventoRuta.DEVOLUCION,
                            "Parada sin gestionar - cierre: " + evento.getTipoCierre(), null, null, null));
                }
            }
            case "PAQUETE_EXCLUIDO_DESPACHO" -> {
                PaqueteExcluidoDespachoEvento evento = (PaqueteExcluidoDespachoEvento) m2Dto;
                resultados.add(construirDto(evento.getPaqueteId(), evento.getRutaId(),
                        evento.getFechaHoraEvento(), TipoEventoRuta.DEVOLUCION,
                        "Paquete excluido del despacho", null, null, null));
            }
            default -> log.warn("Tipo de evento M2 desconocido: {}", m2Dto.getTipoEvento());
        }

        return resultados;
    }

    private EventoRutaDto construirDto(UUID paqueteId, UUID rutaId, Instant fechaHoraEventoInstant,
                                       TipoEventoRuta tipoEvento, String motivo,
                                       String urlEvidencia, String nombreFirmante, String observaciones) {
        OffsetDateTime fechaHoraEvento = fechaHoraEventoInstant != null
                ? fechaHoraEventoInstant.atOffset(ZoneOffset.UTC)
                : OffsetDateTime.now(ZoneOffset.UTC);

        String eventoId = String.format("M2:%s:%s:%s",
                tipoEvento.name(), paqueteId, fechaHoraEvento);

        return EventoRutaDto.builder()
                .eventoId(eventoId)
                .paqueteId(paqueteId)
                .rutaId(rutaId)
                .tipoEvento(tipoEvento)
                .fechaHoraEvento(fechaHoraEvento)
                .observaciones(observaciones)
                .urlEvidencia(urlEvidencia)
                .nombreFirmante(nombreFirmante)
                .motivo(motivo)
                .build();
    }

    private String mapMotivo(String motivoRaw) {
        if (motivoRaw == null) return null;
        try {
            MotivoParadaFallida.valueOf(motivoRaw);
        } catch (IllegalArgumentException e) {
            log.warn("Valor desconocido para motivo: {}. Usando MOTIVO_DESCONOCIDO por defecto.", motivoRaw);
            return "MOTIVO_DESCONOCIDO";
        }
        return motivoRaw;
    }

    private TipoEventoRuta mapTipoNovedad(String tipoNovedadRaw) {
        if (tipoNovedadRaw == null) return null;
        try {
            TipoNovedadGrave tipoNovedad = TipoNovedadGrave.valueOf(tipoNovedadRaw);
            return switch (tipoNovedad) {
                case DAÑADO_EN_RUTA -> TipoEventoRuta.DAÑADO;
                case EXTRAVIADO -> TipoEventoRuta.EXTRAVIADO;
                case DEVOLUCION -> TipoEventoRuta.DEVOLUCION;
            };
        } catch (IllegalArgumentException e) {
            log.warn("Valor desconocido para tipoNovedad: {}. Usando DEVOLUCION por defecto.", tipoNovedadRaw);
            return TipoEventoRuta.DEVOLUCION;
        }
    }
}
