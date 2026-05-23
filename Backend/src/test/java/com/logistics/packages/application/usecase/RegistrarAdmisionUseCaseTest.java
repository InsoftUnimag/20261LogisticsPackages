package com.logistics.packages.application.usecase;

import com.logistics.packages.application.repository.DistanceService;
import com.logistics.packages.application.repository.GeocodingService;
import com.logistics.packages.application.repository.PaqueteRepository;
import com.logistics.packages.application.repository.CoverageService;
import com.logistics.packages.application.repository.PriceCalculationService;
import com.logistics.packages.domain.event.SolicitudRutaEvent;
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
    private CoverageService coverageService;

    @Mock
    private PriceCalculationService priceCalculationService;

    @Mock
    private DistanceService distanceService;

    @Mock
    private SolicitarRutaUseCase solicitarRutaUseCase;

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
                .sedeId(UUID.fromString("550e8400-e29b-41d4-a716-446655440000"))
                .direccionDestino(direccion)
                .valorDeclarado(new BigDecimal("100000"))
                .metodoPago(MetodoPago.CONTRA_ENTREGA)
                .remitente(remitente)
                .destinatario(destinatario)
                .indicadorFormaIrregular(false);
    }

    @Test
    void registrarAdmisionConGeocodingExitoso() {
        RegistroAdmisionCommand command = createBaseCommandBuilder()
                .peso(10.0)
                .largo(50.0)
                .ancho(30.0)
                .alto(20.0)
                .build();

        Coordenadas coordenadas = new Coordenadas(4.5, -74.5);
        when(geocodingService.localizar(command.direccionDestino())).thenReturn(Optional.of(coordenadas));
        when(coverageService.isWithinCoverage(coordenadas)).thenReturn(true);
        when(priceCalculationService.calculatePrice(any(), anyDouble())).thenReturn(new BigDecimal("5000"));
        when(distanceService.calcularDistanciaDesdeSede(coordenadas)).thenReturn(150.0);

        UUID paqueteIdMock = UUID.randomUUID();
        Paquete paqueteGuardado = mock(Paquete.class);
        when(paqueteGuardado.getId()).thenReturn(paqueteIdMock);
        when(paqueteRepository.save(any(Paquete.class))).thenReturn(paqueteGuardado);

        UUID paqueteId = registrarAdmisionUseCase.registrarAdmision(command);

        assertNotNull(paqueteId);
        verify(paqueteRepository, times(1)).save(any(Paquete.class));
        verify(solicitarRutaUseCase, times(1)).handle(any(SolicitudRutaEvent.class));
    }

    @Test
    void registrarAdmisionConCoordenadasManuales() {
        RegistroAdmisionCommand command = createBaseCommandBuilder()
                .coordenadasManuales(new Coordenadas(4.5, -74.5))
                .build();

        when(coverageService.isWithinCoverage(command.coordenadasManuales())).thenReturn(true);
        when(priceCalculationService.calculatePrice(any(), anyDouble())).thenReturn(new BigDecimal("5000"));
        when(distanceService.calcularDistanciaDesdeSede(command.coordenadasManuales())).thenReturn(150.0);

        Paquete paqueteGuardado = mock(Paquete.class);
        when(paqueteGuardado.getId()).thenReturn(UUID.randomUUID());
        when(paqueteRepository.save(any(Paquete.class))).thenReturn(paqueteGuardado);

        UUID paqueteId = registrarAdmisionUseCase.registrarAdmision(command);

        assertNotNull(paqueteId);
        verify(geocodingService, never()).localizar(any());
        verify(paqueteRepository, times(1)).save(any(Paquete.class));
        verify(solicitarRutaUseCase, never()).handle(any());
    }

    @Test
    void registrarAdmisionDebeLanzarExcepcionConPesoInvalido() {
        RegistroAdmisionCommand command = createBaseCommandBuilder()
                .peso(-10.0)
                .largo(50.0)
                .ancho(30.0)
                .alto(20.0)
                .build();

        when(geocodingService.localizar(any())).thenReturn(Optional.of(new Coordenadas(4.5, -74.5)));
        when(coverageService.isWithinCoverage(any())).thenReturn(true);

        assertThrows(IllegalArgumentException.class, () -> {
            registrarAdmisionUseCase.registrarAdmision(command);
        });
        verify(paqueteRepository, never()).save(any(Paquete.class));
        verify(solicitarRutaUseCase, never()).handle(any());
    }

    @Test
    void registrarAdmisionDebeActivarAlertaDeCargaEspecial() {
        RegistroAdmisionCommand command = createBaseCommandBuilder()
                .peso(60.0)
                .largo(100.0)
                .ancho(100.0)
                .alto(60.0)
                .build();

        when(geocodingService.localizar(any())).thenReturn(Optional.of(new Coordenadas(4.5, -74.5)));
        when(coverageService.isWithinCoverage(any())).thenReturn(true);
        when(priceCalculationService.calculatePrice(any(), anyDouble())).thenReturn(new BigDecimal("10000"));
        when(distanceService.calcularDistanciaDesdeSede(any())).thenReturn(150.0);
        when(paqueteRepository.save(any(Paquete.class))).thenAnswer(invocation -> invocation.getArgument(0));

        registrarAdmisionUseCase.registrarAdmision(command);

        ArgumentCaptor<Paquete> paqueteCaptor = ArgumentCaptor.forClass(Paquete.class);
        verify(paqueteRepository).save(paqueteCaptor.capture());
        Paquete paqueteGuardado = paqueteCaptor.getValue();

        assertTrue(paqueteGuardado.isAlertaCargaEspecial(), "La alerta de carga especial debería estar activada");
    }

    @Test
    void registrarAdmisionDebeActivarAlertaDeDensidadAtipica() {
        RegistroAdmisionCommand command = createBaseCommandBuilder()
                .peso(10.0)
                .largo(100.0)
                .ancho(100.0)
                .alto(100.0)
                .build();

        when(geocodingService.localizar(any())).thenReturn(Optional.of(new Coordenadas(4.5, -74.5)));
        when(coverageService.isWithinCoverage(any())).thenReturn(true);
        when(priceCalculationService.calculatePrice(any(), anyDouble())).thenReturn(new BigDecimal("10000"));
        when(distanceService.calcularDistanciaDesdeSede(any())).thenReturn(150.0);
        when(paqueteRepository.save(any(Paquete.class))).thenAnswer(invocation -> invocation.getArgument(0));

        registrarAdmisionUseCase.registrarAdmision(command);

        ArgumentCaptor<Paquete> paqueteCaptor = ArgumentCaptor.forClass(Paquete.class);
        verify(paqueteRepository).save(paqueteCaptor.capture());
        Paquete paqueteGuardado = paqueteCaptor.getValue();

        assertTrue(paqueteGuardado.isAlertaDensidadAtipica(), "La alerta de densidad atípica debería estar activada");
    }
}
