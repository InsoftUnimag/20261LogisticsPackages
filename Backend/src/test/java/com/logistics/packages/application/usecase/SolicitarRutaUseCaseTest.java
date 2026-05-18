package com.logistics.packages.application.usecase;

import com.logistics.packages.application.repository.PaqueteRepository;
import com.logistics.packages.application.ports.RutaQueuePort;
import com.logistics.packages.domain.event.SolicitudRutaEvent;
import com.logistics.packages.domain.exception.PaqueteNotFoundException;
import com.logistics.packages.domain.model.Paquete;
import com.logistics.packages.domain.valueobject.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

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

        paqueteCompleto.procesarPesaje(
                paqueteCompleto.getPeso(),
                paqueteCompleto.getDimensiones(),
                paqueteCompleto.getTipoMercancia(),
                false
        );
    }

    @Test
    @DisplayName("Debe enviar el paquete al puerto cuando existe")
    void debeEnviarSolicitudConPaqueteCompleto() {
        when(paqueteRepository.findById(paqueteId)).thenReturn(Optional.of(paqueteCompleto));

        SolicitudRutaEvent event = SolicitudRutaEvent.of(paqueteId);
        solicitarRutaUseCase.handle(event);

        verify(paqueteRepository).findById(paqueteId);

        ArgumentCaptor<Paquete> paqueteCaptor = ArgumentCaptor.forClass(Paquete.class);
        verify(rutaQueuePort).enviarSolicitud(paqueteCaptor.capture());

        Paquete enviado = paqueteCaptor.getValue();
        assertEquals(paqueteId, enviado.getId());
        assertEquals(25.5, enviado.getPeso().getKilogramos());
        assertNotNull(enviado.getVolumenM3());
        assertEquals(TipoMercancia.ESTANDAR, enviado.getTipoMercancia());
        assertNotNull(enviado.getDireccionDestino());
        assertEquals(4.7110, enviado.getCoordenadas().latitud());
        assertEquals(-74.0721, enviado.getCoordenadas().longitud());
    }

    @Test
    @DisplayName("Debe lanzar excepción cuando el paquete no existe")
    void debeLanzarExcepcionCuandoPaqueteNoExiste() {
        UUID paqueteInexistente = UUID.randomUUID();
        when(paqueteRepository.findById(paqueteInexistente)).thenReturn(Optional.empty());

        SolicitudRutaEvent event = SolicitudRutaEvent.of(paqueteInexistente);

        assertThrows(PaqueteNotFoundException.class, () -> solicitarRutaUseCase.handle(event));

        verify(paqueteRepository).findById(paqueteInexistente);
        verify(rutaQueuePort, never()).enviarSolicitud(any());
    }

    @Test
    @DisplayName("Debe enviar paquete incluso con datos opcionales nulos")
    void debeEnviarPaqueteConDatosOpcionalesNulos() {
        Paquete paqueteMinimo = Paquete.builder()
                .id(paqueteId)
                .estado(EstadoPaquete.RECIBIDO_EN_SEDE)
                .build();

        when(paqueteRepository.findById(paqueteId)).thenReturn(Optional.of(paqueteMinimo));

        SolicitudRutaEvent event = SolicitudRutaEvent.of(paqueteId);
        solicitarRutaUseCase.handle(event);

        ArgumentCaptor<Paquete> paqueteCaptor = ArgumentCaptor.forClass(Paquete.class);
        verify(rutaQueuePort).enviarSolicitud(paqueteCaptor.capture());

        Paquete enviado = paqueteCaptor.getValue();
        assertEquals(paqueteId, enviado.getId());
        assertNull(enviado.getPeso());
        assertNull(enviado.getCoordenadas());
    }
}
