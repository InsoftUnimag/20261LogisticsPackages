package com.logistics.packages.application.usecase.gestionnovedad;

import com.logistics.packages.application.ports.EventoProcesadoRepository;
import com.logistics.packages.application.ports.NotificacionPort;
import com.logistics.packages.application.repository.HistorialEstadoRepository;
import com.logistics.packages.application.repository.PaqueteRepository;
import com.logistics.packages.domain.exception.EventoDuplicadoException;
import com.logistics.packages.domain.exception.PaqueteNotFoundException;
import com.logistics.packages.domain.model.HistorialEstado;
import com.logistics.packages.domain.model.Paquete;
import com.logistics.packages.domain.model.Persona;
import com.logistics.packages.domain.valueobject.EstadoPaquete;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Tests unitarios para ProcesarEventoRutaUseCase.
 * MOD1-UC-007: T708, T709 - Tests del servicio de procesamiento de eventos.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ProcesarEventoRutaUseCase")
class ProcesarEventoRutaUseCaseTest {
    
    @Mock
    private PaqueteRepository paqueteRepository;
    
    @Mock
    private HistorialEstadoRepository historialEstadoRepository;
    
    @Mock
    private EventoProcesadoRepository eventoProcesadoRepository;
    
    @Mock
    private NotificacionPort notificacionPort;
    
    @InjectMocks
    private ProcesarEventoRutaUseCase useCase;
    
    private Paquete paquete;
    private EventoRutaDto eventoDto;
    
    @BeforeEach
    void setUp() {
        UUID paqueteId = UUID.randomUUID();
        UUID rutaId = UUID.randomUUID();
        
        // Configurar paquete con remitente y destinatario
        paquete = Paquete.builder()
                .id(paqueteId)
                .estado(EstadoPaquete.LISTO_PARA_DESPACHO)
                .rutaId(rutaId)
                .remitente(Persona.builder()
                        .telefono("+57123456789")
                        .build())
                .destinatario(Persona.builder()
                        .telefono("+57987654321")
                        .correoElectronico("destinatario@example.com")
                        .build())
                .build();
        
        // Configurar evento DTO
        eventoDto = EventoRutaDto.builder()
                .eventoId("evento-123")
                .paqueteId(paqueteId)
                .rutaId(rutaId)
                .tipoEvento(EventoRutaDto.TipoEventoRuta.EN_TRANSITO)
                .observaciones("Paquete en tránsito")
                .build();
    }
    
    @Test
    @DisplayName("T708: Procesar evento de 'Entregado' actualiza el paquete correctamente")
    void testProcesarEventoEntregado() {
        // Given
        eventoDto.setTipoEvento(EventoRutaDto.TipoEventoRuta.ENTREGADO);
        eventoDto.setUrlEvidencia("https://storage.com/pod.jpg");
        eventoDto.setNombreFirmante("Juan Pérez");
        
        paquete.cambiarEstado(EstadoPaquete.EN_PARADA_DE_ENTREGA);
        
        when(eventoProcesadoRepository.yaFueProcesado(eventoDto.getEventoId())).thenReturn(false);
        when(paqueteRepository.findById(eventoDto.getPaqueteId())).thenReturn(Optional.of(paquete));
        
        // When
        useCase.procesar(eventoDto);
        
        // Then
        assertEquals(EstadoPaquete.ENTREGADO, paquete.getEstado());
        verify(paqueteRepository).save(paquete);
        verify(historialEstadoRepository).guardar(any(HistorialEstado.class));
        verify(eventoProcesadoRepository).guardar(any());
        
        // Verificar que se enviaron notificaciones
        verify(notificacionPort, atLeastOnce()).enviarSms(anyString(), anyString());
        verify(notificacionPort, atLeastOnce()).enviarEmail(anyString(), anyString(), anyString());
    }
    
    @Test
    @DisplayName("T709: Evento duplicado lanza EventoDuplicadoException")
    void testEventoDuplicado() {
        // Given
        when(eventoProcesadoRepository.yaFueProcesado(eventoDto.getEventoId())).thenReturn(true);
        
        // When & Then
        EventoDuplicadoException exception = assertThrows(
                EventoDuplicadoException.class,
                () -> useCase.procesar(eventoDto)
        );
        
        assertEquals(eventoDto.getEventoId(), exception.getEventoId());
        
        // Verificar que NO se procesó el evento
        verify(paqueteRepository, never()).save(any());
        verify(historialEstadoRepository, never()).guardar(any());
    }
    
    @Test
    @DisplayName("Procesar evento de 'En Tránsito' actualiza correctamente")
    void testProcesarEventoEnTransito() {
        // Given
        when(eventoProcesadoRepository.yaFueProcesado(eventoDto.getEventoId())).thenReturn(false);
        when(paqueteRepository.findById(eventoDto.getPaqueteId())).thenReturn(Optional.of(paquete));
        
        // When
        useCase.procesar(eventoDto);
        
        // Then
        assertEquals(EstadoPaquete.EN_TRANSITO, paquete.getEstado());
        verify(paqueteRepository).save(paquete);
        verify(eventoProcesadoRepository).guardar(any());
    }
    
    @Test
    @DisplayName("Lanza PaqueteNotFoundException si el paquete no existe")
    void testPaqueteNoEncontrado() {
        // Given
        when(eventoProcesadoRepository.yaFueProcesado(eventoDto.getEventoId())).thenReturn(false);
        when(paqueteRepository.findById(eventoDto.getPaqueteId())).thenReturn(Optional.empty());
        
        // When & Then
        assertThrows(PaqueteNotFoundException.class, () -> useCase.procesar(eventoDto));
    }
    
    @Test
    @DisplayName("Procesar evento de 'Devolución' actualiza con motivo")
    void testProcesarEventoDevolucion() {
        // Given
        eventoDto.setTipoEvento(EventoRutaDto.TipoEventoRuta.DEVOLUCION);
        eventoDto.setMotivo("Dirección incorrecta");
        
        paquete.cambiarEstado(EstadoPaquete.EN_TRANSITO);
        
        when(eventoProcesadoRepository.yaFueProcesado(eventoDto.getEventoId())).thenReturn(false);
        when(paqueteRepository.findById(eventoDto.getPaqueteId())).thenReturn(Optional.of(paquete));
        
        // When
        useCase.procesar(eventoDto);
        
        // Then
        assertEquals(EstadoPaquete.DEVOLUCION_EN_RUTA, paquete.getEstado());
        verify(paqueteRepository).save(paquete);
    }
}
