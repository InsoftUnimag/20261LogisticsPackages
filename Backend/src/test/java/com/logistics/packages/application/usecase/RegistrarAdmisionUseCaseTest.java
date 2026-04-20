package com.logistics.packages.application.usecase;

import com.logistics.packages.application.ports.out.GeocodingService;
import com.logistics.packages.application.ports.out.PaqueteRepository;
import com.logistics.packages.application.ports.out.RutaEventPublisher;
import com.logistics.packages.domain.model.Paquete;
import com.logistics.packages.domain.model.Persona;
import com.logistics.packages.domain.valueobject.TipoDocumento;
import com.logistics.packages.domain.valueobject.Coordenadas;
import com.logistics.packages.domain.valueobject.Direccion;
import com.logistics.packages.domain.valueobject.MetodoPago;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

class RegistrarAdmisionUseCaseTest {

    @Mock
    private PaqueteRepository paqueteRepository;

    @Mock
    private GeocodingService geocodingService;

    @Mock
    private RutaEventPublisher eventPublisher;

    @InjectMocks
    private RegistrarAdmisionUseCase registrarAdmisionUseCase;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    private RegistroAdmisionCommand.RegistroAdmisionCommandBuilder createBaseCommandBuilder() {
        Persona remitente = Persona.builder().tipoDocumento(TipoDocumento.CEDULA_CIUDADANIA).numeroDocumento("12345678").nombreCompleto("Remitente").telefono("3001234567").build();
        Persona destinatario = Persona.builder().tipoDocumento(TipoDocumento.CEDULA_CIUDADANIA).numeroDocumento("87654321").nombreCompleto("Destinatario").telefono("3109876543").build();
        Direccion direccion = new Direccion("Calle Falsa 123", "Apto 101", "Barrio", "Ciudad");

        return RegistroAdmisionCommand.builder()
                .sedeId("Sede-Principal")
                .direccionDestino(direccion)
                .valorDeclarado(new BigDecimal("100000"))
                .metodoPago(MetodoPago.CONTRA_ENTREGA)
                .remitente(remitente)
                .destinatario(destinatario);
    }

    @Test
    void registrarAdmisionConGeocodingExitoso() {
        // Given
        RegistroAdmisionCommand command = createBaseCommandBuilder()
                .peso(10.0)
                .largo(50.0)
                .ancho(30.0)
                .alto(20.0)
                .build();

        Coordenadas coordenadas = new Coordenadas(11.22, -74.18);
        when(geocodingService.localizar(command.direccionDestino())).thenReturn(Optional.of(coordenadas));

        Paquete paqueteGuardado = new Paquete();
        paqueteGuardado.prePersist();
        when(paqueteRepository.save(any(Paquete.class))).thenReturn(paqueteGuardado);

        // When
        UUID paqueteId = registrarAdmisionUseCase.registrarAdmision(command);

        // Then
        assertNotNull(paqueteId);
        verify(paqueteRepository, times(1)).save(any(Paquete.class));
        verify(eventPublisher, times(1)).publicarSolicitudRuta(paqueteGuardado.getId());
    }

    @Test
    void registrarAdmisionConGeocodingFallido() {
        // Given
        RegistroAdmisionCommand command = createBaseCommandBuilder().build();

        when(geocodingService.localizar(command.direccionDestino())).thenReturn(Optional.empty());
        Paquete paqueteGuardado = new Paquete();
        paqueteGuardado.prePersist();
        when(paqueteRepository.save(any(Paquete.class))).thenReturn(paqueteGuardado);

        // When
        UUID paqueteId = registrarAdmisionUseCase.registrarAdmision(command);

        // Then
        assertNotNull(paqueteId);
        verify(paqueteRepository, times(1)).save(any(Paquete.class));
        verify(eventPublisher, never()).publicarSolicitudRuta(any(UUID.class));
    }

    @Test
    void registrarAdmisionDebeLanzarExcepcionConPesoInvalido() {
        // Given
        RegistroAdmisionCommand command = createBaseCommandBuilder()
                .peso(-10.0) // Peso inválido
                .largo(50.0)
                .ancho(30.0)
                .alto(20.0)
                .build();

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> {
            registrarAdmisionUseCase.registrarAdmision(command);
        });
        verify(paqueteRepository, never()).save(any(Paquete.class));
    }

    @Test
    void registrarAdmisionDebeActivarAlertaDeCargaEspecial() {
        // Given
        RegistroAdmisionCommand command = createBaseCommandBuilder()
                .peso(60.0) // Peso que activa la alerta
                .largo(100.0)
                .ancho(100.0)
                .alto(60.0) // Volumen que también activa la alerta
                .build();
        
        when(geocodingService.localizar(any())).thenReturn(Optional.of(new Coordenadas(1.0, 1.0)));
        when(paqueteRepository.save(any(Paquete.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        registrarAdmisionUseCase.registrarAdmision(command);

        // Then
        ArgumentCaptor<Paquete> paqueteCaptor = ArgumentCaptor.forClass(Paquete.class);
        verify(paqueteRepository).save(paqueteCaptor.capture());
        Paquete paqueteGuardado = paqueteCaptor.getValue();

        assertTrue(paqueteGuardado.isAlertaCargaEspecial(), "La alerta de carga especial debería estar activada");
    }

    @Test
    void registrarAdmisionDebeActivarAlertaDeDensidadAtipica() {
        // Given
        RegistroAdmisionCommand command = createBaseCommandBuilder()
                .peso(10.0) // Peso real bajo
                .largo(100.0)
                .ancho(100.0)
                .alto(100.0) // Volumen muy alto
                .build();

        // El peso volumétrico será (100*100*100)/1_000_000 * 250 = 250 kg
        // La diferencia con el peso real (10kg) es > 30%

        when(geocodingService.localizar(any())).thenReturn(Optional.of(new Coordenadas(1.0, 1.0)));
        when(paqueteRepository.save(any(Paquete.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        registrarAdmisionUseCase.registrarAdmision(command);

        // Then
        ArgumentCaptor<Paquete> paqueteCaptor = ArgumentCaptor.forClass(Paquete.class);
        verify(paqueteRepository).save(paqueteCaptor.capture());
        Paquete paqueteGuardado = paqueteCaptor.getValue();

        assertTrue(paqueteGuardado.isAlertaDensidadAtipica(), "La alerta de densidad atípica debería estar activada");
    }
}
