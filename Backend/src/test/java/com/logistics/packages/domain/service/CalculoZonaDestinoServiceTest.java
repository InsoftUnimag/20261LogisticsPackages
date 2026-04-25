package com.logistics.packages.domain.service;

import com.logistics.packages.application.ports.ZonaDestinoRepository;
import com.logistics.packages.domain.exception.ZonaDestinoNoEncontradaException;
import com.logistics.packages.domain.model.Paquete;
import com.logistics.packages.domain.model.ZonaDestino;
import com.logistics.packages.domain.valueobject.CategoriaZona;
import com.logistics.packages.domain.valueobject.Coordenadas;
import com.logistics.packages.domain.valueobject.EstadoPaquete;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Tests unitarios para CalculoZonaDestinoService
 * MOD1-IP-005: T503
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("CalculoZonaDestinoService - Tests Unitarios")
class CalculoZonaDestinoServiceTest {

    @Mock
    private ZonaDestinoRepository zonaDestinoRepository;

    private CalculoZonaDestinoService calculoService;

    @BeforeEach
    void setUp() {
        calculoService = new CalculoZonaDestinoService(zonaDestinoRepository);
    }

    @Test
    @DisplayName("Debe calcular la zona correcta cuando las coordenadas coinciden")
    void debeCalcularZonaCorrectaConCoordenadasCoincidentes() {
        // Given
        Coordenadas coordenadas = new Coordenadas(4.70, -74.05);
        Paquete paquete = Paquete.builder()
                .id(UUID.randomUUID())
                .coordenadas(coordenadas)
                .estado(EstadoPaquete.EN_CLASIFICACION)
                .build();

        ZonaDestino zonaNorte = ZonaDestino.builder()
                .id(UUID.randomUUID())
                .nombre("Zona Norte")
                .codigo("ZD-NORTE")
                .categoria(CategoriaZona.NORMAL)
                .latitudMin(4.60)
                .latitudMax(4.80)
                .longitudMin(-74.10)
                .longitudMax(-74.00)
                .build();

        List<ZonaDestino> zonas = Collections.singletonList(zonaNorte);
        when(zonaDestinoRepository.findAllActivas()).thenReturn(zonas);

        // When
        ZonaDestino resultado = calculoService.calcularZona(paquete);

        // Then
        assertNotNull(resultado);
        assertEquals(zonaNorte.getId(), resultado.getId());
        assertEquals("Zona Norte", resultado.getNombre());
        verify(zonaDestinoRepository).findAllActivas();
    }

    @Test
    @DisplayName("Debe lanzar excepción cuando no se encuentra zona para las coordenadas")
    void debeLanzarExcepcionCuandoNoSeEncuentraZona() {
        // Given
        Coordenadas coordenadas = new Coordenadas(5.50, -75.00);
        Paquete paquete = Paquete.builder()
                .id(UUID.randomUUID())
                .coordenadas(coordenadas)
                .estado(EstadoPaquete.EN_CLASIFICACION)
                .build();

        ZonaDestino zonaNorte = ZonaDestino.builder()
                .latitudMin(4.60)
                .latitudMax(4.80)
                .longitudMin(-74.10)
                .longitudMax(-74.00)
                .build();

        List<ZonaDestino> zonas = Collections.singletonList(zonaNorte);
        when(zonaDestinoRepository.findAllActivas()).thenReturn(zonas);

        // When & Then
        assertThrows(ZonaDestinoNoEncontradaException.class,
                () -> calculoService.calcularZona(paquete));
        verify(zonaDestinoRepository).findAllActivas();
    }

    @Test
    @DisplayName("Debe seleccionar la primera zona que coincida cuando hay múltiples zonas")
    void debeSeleccionarPrimeraZonaCuandoHayMultiples() {
        // Given
        Coordenadas coordenadas = new Coordenadas(4.70, -74.05);
        Paquete paquete = Paquete.builder()
                .id(UUID.randomUUID())
                .coordenadas(coordenadas)
                .estado(EstadoPaquete.EN_CLASIFICACION)
                .build();

        ZonaDestino zona1 = ZonaDestino.builder()
                .id(UUID.randomUUID())
                .nombre("Zona 1")
                .latitudMin(4.60)
                .latitudMax(4.80)
                .longitudMin(-74.10)
                .longitudMax(-74.00)
                .build();

        ZonaDestino zona2 = ZonaDestino.builder()
                .id(UUID.randomUUID())
                .nombre("Zona 2")
                .latitudMin(4.65)
                .latitudMax(4.75)
                .longitudMin(-74.08)
                .longitudMax(-74.02)
                .build();

        List<ZonaDestino> zonas = Arrays.asList(zona1, zona2);
        when(zonaDestinoRepository.findAllActivas()).thenReturn(zonas);

        // When
        ZonaDestino resultado = calculoService.calcularZona(paquete);

        // Then
        assertNotNull(resultado);
        assertEquals(zona1.getId(), resultado.getId());
        assertEquals("Zona 1", resultado.getNombre());
    }

    @Test
    @DisplayName("Debe lanzar excepción cuando el paquete no tiene coordenadas")
    void debeLanzarExcepcionCuandoPaqueteSinCoordenadas() {
        // Given
        Paquete paquete = Paquete.builder()
                .id(UUID.randomUUID())
                .coordenadas(null)
                .estado(EstadoPaquete.EN_CLASIFICACION)
                .build();

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> calculoService.calcularZona(paquete));
        
        assertTrue(exception.getMessage().contains("coordenadas"));
        verify(zonaDestinoRepository, never()).findAllActivas();
    }

    @Test
    @DisplayName("Debe lanzar excepción cuando no hay zonas configuradas")
    void debeLanzarExcepcionCuandoNoHayZonasConfiguradas() {
        // Given
        Coordenadas coordenadas = new Coordenadas(4.70, -74.05);
        Paquete paquete = Paquete.builder()
                .id(UUID.randomUUID())
                .coordenadas(coordenadas)
                .estado(EstadoPaquete.EN_CLASIFICACION)
                .build();

        when(zonaDestinoRepository.findAllActivas()).thenReturn(Collections.emptyList());

        // When & Then
        assertThrows(ZonaDestinoNoEncontradaException.class,
                () -> calculoService.calcularZona(paquete));
        verify(zonaDestinoRepository).findAllActivas();
    }
}
