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

    @Test
    void registrarAdmisionConGeocodingExitoso() {
        // Given
        Persona remitente = Persona.builder().tipoDocumento(TipoDocumento.CEDULA_CIUDADANIA).numeroDocumento("12345678").nombreCompleto("Remitente").telefono("3001234567").build();
        Persona destinatario = Persona.builder().tipoDocumento(TipoDocumento.CEDULA_CIUDADANIA).numeroDocumento("87654321").nombreCompleto("Destinatario").telefono("3109876543").build();
        Direccion direccion = new Direccion("Calle Falsa 123", "Apto 101", "Barrio", "Ciudad");

        RegistroAdmisionCommand command = RegistroAdmisionCommand.builder()
                .sedeId("Sede-Principal")
                .direccionDestino(direccion)
                .valorDeclarado(new BigDecimal("100000"))
                .metodoPago(MetodoPago.CONTRA_ENTREGA)
                .remitente(remitente)
                .destinatario(destinatario)
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
        Persona remitente = Persona.builder().tipoDocumento(TipoDocumento.CEDULA_CIUDADANIA).numeroDocumento("12345678").nombreCompleto("Remitente").telefono("3001234567").build();
        Persona destinatario = Persona.builder().tipoDocumento(TipoDocumento.CEDULA_CIUDADANIA).numeroDocumento("87654321").nombreCompleto("Destinatario").telefono("3109876543").build();
        Direccion direccion = new Direccion("Calle Falsa 123", "Apto 101", "Barrio", "Ciudad");

        RegistroAdmisionCommand command = RegistroAdmisionCommand.builder()
                .sedeId("Sede-Principal")
                .direccionDestino(direccion)
                .valorDeclarado(new BigDecimal("100000"))
                .metodoPago(MetodoPago.CONTRA_ENTREGA)
                .remitente(remitente)
                .destinatario(destinatario)
                .build();

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
}
