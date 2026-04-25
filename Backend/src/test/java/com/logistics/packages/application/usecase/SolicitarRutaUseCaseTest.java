package com.logistics.packages.application.usecase;

import com.logistics.packages.application.ports.PaqueteRepository;
import com.logistics.packages.application.ports.RutaQueuePort;
import com.logistics.packages.domain.event.SolicitudRutaEvent;
import com.logistics.packages.domain.exception.PaqueteNotFoundException;
import com.logistics.packages.domain.model.Paquete;
import com.logistics.packages.domain.valueobject.*;
import com.logistics.packages.infrastructure.dto.request.SolicitudRutaPayload;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * T304 [P] [US3] - Tests unitarios para SolicitarRutaUseCase
 * MOD1-IP-003 - Phase 2
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Solicitar Ruta Use Case")
class SolicitarRutaUseCaseTest {

    @Mock
    private PaqueteRepository paqueteRepository;

    @Mock
    private RutaQueuePort rutaQueuePort;

    @InjectMocks
    private SolicitarRutaUseCase solicitarRutaUseCase;

    private Paquete paqueteCompleto;
    private UUID paqueteId;

    @BeforeEach
    void setUp() {
        paqueteId = UUID.randomUUID();
        
        // Given: Un paquete completo (admitido y pesado)
        paqueteCompleto = Paquete.builder()
                .id(paqueteId)
                .estado(EstadoPaquete.RECIBIDO_EN_SEDE)
                .peso(new Peso(25.5))
                .dimensiones(new Dimensiones(100.0, 50.0, 30.0))
                .tipoMercancia(TipoMercancia.ESTANDAR)
                .direccionDestino(new Direccion("Calle 123", "Bogotá", "Cundinamarca", "Colombia"))
                .coordenadas(new Coordenadas(4.7110, -74.0721))
                .categoriaCarga(CategoriaCarga.NORMAL)
                .build();
        
        // Simular cálculos del pesaje
        paqueteCompleto.procesarPesaje(
                paqueteCompleto.getPeso(),
                paqueteCompleto.getDimensiones(),
                paqueteCompleto.getTipoMercancia(),
                false
        );
    }

    @Test
    @DisplayName("Debe construir el payload y enviar solicitud cuando el paquete existe")
    void debeEnviarSolicitudConPayloadCompleto() {
        // Given: El paquete existe en el repositorio
        when(paqueteRepository.buscarPorId(paqueteId)).thenReturn(Optional.of(paqueteCompleto));
        
        SolicitudRutaEvent event = SolicitudRutaEvent.of(paqueteId);

        // When: Se ejecuta el caso de uso
        solicitarRutaUseCase.handle(event);

        // Then: Se construye el payload y se envía a la cola
        verify(paqueteRepository).buscarPorId(paqueteId);
        
        ArgumentCaptor<SolicitudRutaPayload> payloadCaptor = ArgumentCaptor.forClass(SolicitudRutaPayload.class);
        verify(rutaQueuePort).enviarSolicitud(payloadCaptor.capture());
        
        SolicitudRutaPayload payload = payloadCaptor.getValue();
        assertEquals(paqueteId, payload.getPaqueteId());
        assertEquals(25.5, payload.getPesoKg());
        assertNotNull(payload.getVolumenM3());
        assertEquals(TipoMercancia.ESTANDAR, payload.getTipoMercancia());
        assertNotNull(payload.getDireccionDestino());
        assertEquals(4.7110, payload.getLatitud());
        assertEquals(-74.0721, payload.getLongitud());
    }

    @Test
    @DisplayName("Debe lanzar excepción cuando el paquete no existe")
    void debeLanzarExcepcionCuandoPaqueteNoExiste() {
        // Given: El paquete no existe
        UUID paqueteInexistente = UUID.randomUUID();
        when(paqueteRepository.buscarPorId(paqueteInexistente)).thenReturn(Optional.empty());
        
        SolicitudRutaEvent event = SolicitudRutaEvent.of(paqueteInexistente);

        // When & Then: Debe lanzar excepción
        assertThrows(PaqueteNotFoundException.class, () -> solicitarRutaUseCase.handle(event));
        
        verify(paqueteRepository).buscarPorId(paqueteInexistente);
        verify(rutaQueuePort, never()).enviarSolicitud(any());
    }

    @Test
    @DisplayName("Debe enviar payload incluso con datos opcionales nulos")
    void debeEnviarPayloadConDatosOpcionalesNulos() {
        // Given: Un paquete con datos mínimos
        Paquete paqueteMinimo = Paquete.builder()
                .id(paqueteId)
                .estado(EstadoPaquete.RECIBIDO_EN_SEDE)
                .build();
        
        when(paqueteRepository.buscarPorId(paqueteId)).thenReturn(Optional.of(paqueteMinimo));
        
        SolicitudRutaEvent event = SolicitudRutaEvent.of(paqueteId);

        // When: Se ejecuta el caso de uso
        solicitarRutaUseCase.handle(event);

        // Then: Se construye y envía el payload
        ArgumentCaptor<SolicitudRutaPayload> payloadCaptor = ArgumentCaptor.forClass(SolicitudRutaPayload.class);
        verify(rutaQueuePort).enviarSolicitud(payloadCaptor.capture());
        
        SolicitudRutaPayload payload = payloadCaptor.getValue();
        assertEquals(paqueteId, payload.getPaqueteId());
        // Los campos opcionales son nulos, pero el payload se envía
        assertNull(payload.getPesoKg());
        assertNull(payload.getLatitud());
    }
}
