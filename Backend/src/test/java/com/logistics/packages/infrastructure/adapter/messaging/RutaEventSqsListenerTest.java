package com.logistics.packages.infrastructure.adapter.messaging;

import com.logistics.packages.application.usecase.gestionnovedad.EventoRutaDto;
import com.logistics.packages.application.usecase.gestionnovedad.ProcesarEventoRutaUseCase;
import com.logistics.packages.domain.exception.EventoDuplicadoException;
import com.logistics.packages.infrastructure.dto.event.EventoPaqueteM2Dto;
import com.logistics.packages.infrastructure.dto.event.PaqueteEnTransitoEvento;
import com.logistics.packages.infrastructure.dto.event.PaqueteEntregadoEvento;
import com.logistics.packages.infrastructure.dto.event.NovedadGraveEvento;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("RutaEventSqsListener: multiplexor de eventos M2")
class RutaEventSqsListenerTest {

    @Mock
    private ProcesarEventoRutaUseCase procesarEventoRutaUseCase;

    @Mock
    private EventoPaqueteM2Mapper eventoMapper;

    @InjectMocks
    private RutaEventSqsListener listener;

    @Captor
    private ArgumentCaptor<EventoRutaDto> eventoCaptor;

    private final UUID paqueteId = UUID.randomUUID();
    private final UUID rutaId = UUID.randomUUID();
    private final Instant fecha = Instant.now();

    @Test
    @DisplayName("PAQUETE_ENTREGADO: mapper produce un comando y el use case lo procesa")
    void testListenerProcesaPaqueteEntregado() {
        EventoPaqueteM2Dto m2Evento = new PaqueteEntregadoEvento(
                "PAQUETE_ENTREGADO", paqueteId, rutaId, fecha, null);

        EventoRutaDto comando = EventoRutaDto.builder()
                .eventoId("M2:ENTREGADO:" + paqueteId + ":" + fecha)
                .paqueteId(paqueteId)
                .rutaId(rutaId)
                .tipoEvento(EventoRutaDto.TipoEventoRuta.ENTREGADO)
                .build();

        when(eventoMapper.mapToEventoRuta(m2Evento)).thenReturn(List.of(comando));

        listener.onEventoPaquete(m2Evento);

        verify(eventoMapper).mapToEventoRuta(m2Evento);
        verify(procesarEventoRutaUseCase).procesar(comando);
    }

    @Test
    @DisplayName("NOVEDAD_GRAVE (DAÑADO): mapea y procesa correctamente")
    void testListenerProcesaNovedadGraveDanado() {
        EventoPaqueteM2Dto m2Evento = new NovedadGraveEvento(
                "NOVEDAD_GRAVE", paqueteId, rutaId, fecha, "DAÑADO_EN_RUTA");

        EventoRutaDto comando = EventoRutaDto.builder()
                .eventoId("M2:DAÑADO:" + paqueteId + ":" + fecha)
                .paqueteId(paqueteId)
                .rutaId(rutaId)
                .tipoEvento(EventoRutaDto.TipoEventoRuta.DAÑADO)
                .build();

        when(eventoMapper.mapToEventoRuta(m2Evento)).thenReturn(List.of(comando));

        listener.onEventoPaquete(m2Evento);

        verify(procesarEventoRutaUseCase).procesar(comando);
    }

    @Test
    @DisplayName("Evento duplicado es atrapado y no propaga excepción")
    void testEventoDuplicadoNoPropagaExcepcion() {
        EventoPaqueteM2Dto m2Evento = new PaqueteEnTransitoEvento(
                "PAQUETE_EN_TRANSITO", paqueteId, rutaId, fecha);

        EventoRutaDto comando = EventoRutaDto.builder()
                .eventoId("M2:EN_TRANSITO:" + paqueteId + ":" + fecha)
                .paqueteId(paqueteId)
                .rutaId(rutaId)
                .tipoEvento(EventoRutaDto.TipoEventoRuta.EN_TRANSITO)
                .build();

        when(eventoMapper.mapToEventoRuta(m2Evento)).thenReturn(List.of(comando));
        doThrow(new EventoDuplicadoException(comando.getEventoId()))
                .when(procesarEventoRutaUseCase).procesar(comando);

        listener.onEventoPaquete(m2Evento);

        verify(procesarEventoRutaUseCase).procesar(comando);
    }

    @Test
    @DisplayName("Excepción no duplicada se propaga como RuntimeException")
    void testErrorNoDuplicadoPropagaExcepcion() {
        EventoPaqueteM2Dto m2Evento = new PaqueteEnTransitoEvento(
                "PAQUETE_EN_TRANSITO", paqueteId, rutaId, fecha);

        EventoRutaDto comando = EventoRutaDto.builder()
                .eventoId("M2:EN_TRANSITO:" + paqueteId + ":" + fecha)
                .paqueteId(paqueteId)
                .rutaId(rutaId)
                .tipoEvento(EventoRutaDto.TipoEventoRuta.EN_TRANSITO)
                .build();

        when(eventoMapper.mapToEventoRuta(m2Evento)).thenReturn(List.of(comando));
        doThrow(new RuntimeException("Error de red"))
                .when(procesarEventoRutaUseCase).procesar(comando);

        assertThrows(RuntimeException.class, () -> listener.onEventoPaquete(m2Evento));
    }

    @Test
    @DisplayName("PARADAS_SIN_GESTIONAR produce N comandos, cada uno se procesa")
    void testListenerProcesaMultiplesComandos() {
        UUID p1 = UUID.randomUUID();
        UUID p2 = UUID.randomUUID();
        EventoPaqueteM2Dto m2Evento = mock(EventoPaqueteM2Dto.class);
        when(m2Evento.getTipoEvento()).thenReturn("PARADAS_SIN_GESTIONAR");
        when(m2Evento.getPaqueteId()).thenReturn(null);

        EventoRutaDto cmd1 = EventoRutaDto.builder()
                .eventoId("M2:DEVOLUCION:" + p1 + ":" + fecha)
                .paqueteId(p1).rutaId(rutaId)
                .tipoEvento(EventoRutaDto.TipoEventoRuta.DEVOLUCION).build();
        EventoRutaDto cmd2 = EventoRutaDto.builder()
                .eventoId("M2:DEVOLUCION:" + p2 + ":" + fecha)
                .paqueteId(p2).rutaId(rutaId)
                .tipoEvento(EventoRutaDto.TipoEventoRuta.DEVOLUCION).build();

        when(eventoMapper.mapToEventoRuta(m2Evento)).thenReturn(List.of(cmd1, cmd2));

        listener.onEventoPaquete(m2Evento);

        verify(procesarEventoRutaUseCase).procesar(cmd1);
        verify(procesarEventoRutaUseCase).procesar(cmd2);
    }

    @Test
    @DisplayName("Listener interactúa con mapper + use case en el flujo completo")
    void testFlujoCompletoListenerMapperUseCase() {
        EventoPaqueteM2Dto m2Evento = new PaqueteEnTransitoEvento(
                "PAQUETE_EN_TRANSITO", paqueteId, rutaId, fecha);

        when(eventoMapper.mapToEventoRuta(m2Evento)).thenAnswer(invocation -> {
            var mapper = new EventoPaqueteM2Mapper();
            return mapper.mapToEventoRuta(invocation.getArgument(0));
        });

        listener.onEventoPaquete(m2Evento);

        verify(procesarEventoRutaUseCase).procesar(eventoCaptor.capture());
        EventoRutaDto dto = eventoCaptor.getValue();
        assertEquals(paqueteId, dto.getPaqueteId());
        assertEquals(rutaId, dto.getRutaId());
        assertEquals(EventoRutaDto.TipoEventoRuta.EN_TRANSITO, dto.getTipoEvento());
    }
}
