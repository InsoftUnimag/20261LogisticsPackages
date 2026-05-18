package com.logistics.packages.infrastructure.adapter.messaging;

import com.logistics.packages.application.usecase.gestionnovedad.EventoRutaDto;
import com.logistics.packages.application.usecase.gestionnovedad.EventoRutaDto.TipoEventoRuta;
import com.logistics.packages.infrastructure.dto.event.*;
import com.logistics.packages.infrastructure.dto.event.NovedadGraveEvento;
import com.logistics.packages.infrastructure.dto.event.ParadaFallidaEvento;
import com.logistics.packages.infrastructure.dto.event.ParadasSinGestionarEvento.PaqueteEnRutaDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("EventoPaqueteM2Mapper: mapeo de eventos M2 → aplicación")
class EventoPaqueteM2MapperTest {

    private EventoPaqueteM2Mapper mapper;
    private UUID paqueteId;
    private UUID rutaId;
    private OffsetDateTime fecha;

    @BeforeEach
    void setUp() {
        mapper = new EventoPaqueteM2Mapper();
        paqueteId = UUID.randomUUID();
        rutaId = UUID.randomUUID();
        fecha = OffsetDateTime.now(ZoneOffset.UTC);
    }

    @Test
    @DisplayName("PAQUETE_EN_TRANSITO → EN_TRANSITO con idempotencia correcta")
    void testPaqueteEnTransito() {
        var evento = new com.logistics.packages.infrastructure.dto.event.PaqueteEnTransitoEvento(
                "PAQUETE_EN_TRANSITO", paqueteId, rutaId, fecha);

        List<EventoRutaDto> resultado = mapper.mapToEventoRuta(evento);

        assertEquals(1, resultado.size());
        EventoRutaDto dto = resultado.getFirst();
        assertEquals(paqueteId, dto.getPaqueteId());
        assertEquals(rutaId, dto.getRutaId());
        assertEquals(TipoEventoRuta.EN_TRANSITO, dto.getTipoEvento());
        assertTrue(dto.getEventoId().startsWith("M2:EN_TRANSITO:" + paqueteId));
    }

    @Test
    @DisplayName("PAQUETE_ENTREGADO con evidencia de foto y firma → ENTREGADO")
    void testPaqueteEntregadoConEvidencia() {
        var evidencia = new com.logistics.packages.infrastructure.dto.event.PaqueteEntregadoEvento.EvidenciaDto(
                "https://storage.com/foto.jpg", "https://storage.com/firma.png");
        var evento = new com.logistics.packages.infrastructure.dto.event.PaqueteEntregadoEvento(
                "PAQUETE_ENTREGADO", paqueteId, rutaId, fecha, evidencia);

        List<EventoRutaDto> resultado = mapper.mapToEventoRuta(evento);

        assertEquals(1, resultado.size());
        EventoRutaDto dto = resultado.getFirst();
        assertEquals(TipoEventoRuta.ENTREGADO, dto.getTipoEvento());
        assertEquals("https://storage.com/foto.jpg", dto.getUrlEvidencia());
        assertEquals(paqueteId, dto.getPaqueteId());
    }

    @Test
    @DisplayName("PAQUETE_ENTREGADO sin evidencia → ENTREGADO con urlEvidencia null")
    void testPaqueteEntregadoSinEvidencia() {
        var evento = new com.logistics.packages.infrastructure.dto.event.PaqueteEntregadoEvento(
                "PAQUETE_ENTREGADO", paqueteId, rutaId, fecha, null);

        List<EventoRutaDto> resultado = mapper.mapToEventoRuta(evento);

        assertEquals(1, resultado.size());
        assertNull(resultado.getFirst().getUrlEvidencia());
    }

    @Test
    @DisplayName("PARADA_FALLIDA con motivo → DEVOLUCION con motivo mapeado")
    void testParadaFallida() {
        var evento = new com.logistics.packages.infrastructure.dto.event.ParadaFallidaEvento(
                "PARADA_FALLIDA", paqueteId, rutaId, fecha, ParadaFallidaEvento.MotivoParadaFallida.CLIENTE_AUSENTE);

        List<EventoRutaDto> resultado = mapper.mapToEventoRuta(evento);

        assertEquals(1, resultado.size());
        EventoRutaDto dto = resultado.getFirst();
        assertEquals(TipoEventoRuta.DEVOLUCION, dto.getTipoEvento());
        assertEquals("CLIENTE_AUSENTE", dto.getMotivo());
    }

    @Test
    @DisplayName("NOVEDAD_GRAVE tipo DAÑADO_EN_RUTA → DAÑADO")
    void testNovedadGraveDanado() {
        var evento = new com.logistics.packages.infrastructure.dto.event.NovedadGraveEvento(
                "NOVEDAD_GRAVE", paqueteId, rutaId, fecha, NovedadGraveEvento.TipoNovedadGrave.DAÑADO_EN_RUTA);

        List<EventoRutaDto> resultado = mapper.mapToEventoRuta(evento);

        assertEquals(1, resultado.size());
        assertEquals(TipoEventoRuta.DAÑADO, resultado.getFirst().getTipoEvento());
    }

    @Test
    @DisplayName("NOVEDAD_GRAVE tipo EXTRAVIADO → EXTRAVIADO")
    void testNovedadGraveExtraviado() {
        var evento = new com.logistics.packages.infrastructure.dto.event.NovedadGraveEvento(
                "NOVEDAD_GRAVE", paqueteId, rutaId, fecha, NovedadGraveEvento.TipoNovedadGrave.EXTRAVIADO);

        List<EventoRutaDto> resultado = mapper.mapToEventoRuta(evento);

        assertEquals(1, resultado.size());
        assertEquals(TipoEventoRuta.EXTRAVIADO, resultado.getFirst().getTipoEvento());
    }

    @Test
    @DisplayName("NOVEDAD_GRAVE tipo DEVOLUCION → DEVOLUCION")
    void testNovedadGraveDevolucion() {
        var evento = new com.logistics.packages.infrastructure.dto.event.NovedadGraveEvento(
                "NOVEDAD_GRAVE", paqueteId, rutaId, fecha, NovedadGraveEvento.TipoNovedadGrave.DEVOLUCION);

        List<EventoRutaDto> resultado = mapper.mapToEventoRuta(evento);

        assertEquals(1, resultado.size());
        assertEquals(TipoEventoRuta.DEVOLUCION, resultado.getFirst().getTipoEvento());
    }

    @Test
    @DisplayName("NOVEDAD_GRAVE con tipoNovedad null → lista vacía (null safe)")
    void testNovedadGraveTipoNulo() {
        var evento = new com.logistics.packages.infrastructure.dto.event.NovedadGraveEvento(
                "NOVEDAD_GRAVE", paqueteId, rutaId, fecha, null);

        List<EventoRutaDto> resultado = mapper.mapToEventoRuta(evento);

        assertTrue(resultado.isEmpty());
    }

    @Test
    @DisplayName("PARADAS_SIN_GESTIONAR con 3 paquetes → 3 eventos DEVOLUCION")
    void testParadasSinGestionarMultiplesPaquetes() {
        UUID p1 = UUID.randomUUID();
        UUID p2 = UUID.randomUUID();
        UUID p3 = UUID.randomUUID();
        var paquetes = List.of(
                crearPaqueteEnRuta(p1),
                crearPaqueteEnRuta(p2),
                crearPaqueteEnRuta(p3));
        var evento = new com.logistics.packages.infrastructure.dto.event.ParadasSinGestionarEvento(
                "PARADAS_SIN_GESTIONAR", rutaId, fecha, "AUTOMATICO", paquetes);

        List<EventoRutaDto> resultado = mapper.mapToEventoRuta(evento);

        assertEquals(3, resultado.size());
        assertTrue(resultado.stream().allMatch(d -> d.getTipoEvento() == TipoEventoRuta.DEVOLUCION));
        assertEquals(p1, resultado.get(0).getPaqueteId());
        assertEquals(p2, resultado.get(1).getPaqueteId());
        assertEquals(p3, resultado.get(2).getPaqueteId());
    }

    @Test
    @DisplayName("PARADAS_SIN_GESTIONAR con 0 paquetes → lista vacía")
    void testParadasSinGestionarSinPaquetes() {
        var evento = new com.logistics.packages.infrastructure.dto.event.ParadasSinGestionarEvento(
                "PARADAS_SIN_GESTIONAR", rutaId, fecha, "FORZADO", List.of());

        List<EventoRutaDto> resultado = mapper.mapToEventoRuta(evento);

        assertTrue(resultado.isEmpty());
    }

    @Test
    @DisplayName("PAQUETE_EXCLUIDO_DESPACHO → DEVOLUCION con motivo")
    void testPaqueteExcluidoDespacho() {
        var evento = new com.logistics.packages.infrastructure.dto.event.PaqueteExcluidoDespachoEvento(
                "PAQUETE_EXCLUIDO_DESPACHO", paqueteId, rutaId, fecha);

        List<EventoRutaDto> resultado = mapper.mapToEventoRuta(evento);

        assertEquals(1, resultado.size());
        EventoRutaDto dto = resultado.getFirst();
        assertEquals(TipoEventoRuta.DEVOLUCION, dto.getTipoEvento());
        assertEquals("Paquete excluido del despacho", dto.getMotivo());
    }

    @Test
    @DisplayName("Todos los eventos generan eventoId con formato M2:TIPO:paqueteId:timestamp")
    void testEventoIdFormat() {
        var evento = new com.logistics.packages.infrastructure.dto.event.PaqueteEnTransitoEvento(
                "PAQUETE_EN_TRANSITO", paqueteId, rutaId, fecha);

        List<EventoRutaDto> resultado = mapper.mapToEventoRuta(evento);

        String esperado = "M2:EN_TRANSITO:" + paqueteId + ":" + fecha;
        assertEquals(esperado, resultado.getFirst().getEventoId());
    }

    private PaqueteEnRutaDto crearPaqueteEnRuta(UUID id) {
        var dto = new PaqueteEnRutaDto();
        dto.setPaqueteId(id);
        return dto;
    }
}
