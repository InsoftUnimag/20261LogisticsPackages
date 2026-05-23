package com.logistics.packages.domain.model;

import com.logistics.packages.domain.valueobject.EstadoPaquete;
import com.logistics.packages.domain.valueobject.TipoMercancia;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests unitarios para la clasificación de paquetes por zona de destino
 * MOD1-IP-005: T504
 */
@DisplayName("Paquete - Clasificación por Zona de Destino")
class PaqueteClasificacionTest {

    private Paquete paquete;

    @BeforeEach
    void setUp() {
        paquete = Paquete.builder()
                .id(UUID.randomUUID())
                .estado(EstadoPaquete.EN_CLASIFICACION)
                .tipoMercancia(TipoMercancia.ESTANDAR)
                .build();
    }

    @Test
    @DisplayName("Debe asignar zona de destino y cambiar estado a LISTO_PARA_DESPACHO")
    void debeAsignarZonaDestinoYCambiarEstado() {
        // Given
        UUID zonaDestinoId = UUID.randomUUID();

        // When
        paquete.asignarZonaDestino(zonaDestinoId);

        // Then
        assertEquals(zonaDestinoId, paquete.getZonaDestinoId());
        assertEquals(EstadoPaquete.LISTO_PARA_DESPACHO, paquete.getEstado());
    }

    @Test
    @DisplayName("No debe permitir asignar zona de destino si el estado no es EN_CLASIFICACION")
    void noDebePermitirAsignarZonaDestinoSiEstadoIncorrecto() {
        // Given
        paquete.cambiarEstado(EstadoPaquete.RECIBIDO_EN_SEDE);
        UUID zonaDestinoId = UUID.randomUUID();

        // When & Then
        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> paquete.asignarZonaDestino(zonaDestinoId));

        assertTrue(exception.getMessage().contains("EN_CLASIFICACION"));
    }

    @Test
    @DisplayName("No debe permitir asignar zona de destino nula")
    void noDebePermitirAsignarZonaDestinoNula() {
        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> paquete.asignarZonaDestino(null));

        assertTrue(exception.getMessage().contains("zona de destino"));
    }

    @Test
    @DisplayName("Debe permitir reasignar zona de destino si está en EN_CLASIFICACION")
    void debePermitirReasignarZonaDestino() {
        // Given
        UUID primeraZona = UUID.randomUUID();
        paquete.asignarZonaDestino(primeraZona);
        
        // Volvemos a EN_CLASIFICACION para simular reclasificación
        paquete.cambiarEstado(EstadoPaquete.EN_CLASIFICACION);
        UUID segundaZona = UUID.randomUUID();

        // When
        paquete.asignarZonaDestino(segundaZona);

        // Then
        assertEquals(segundaZona, paquete.getZonaDestinoId());
        assertEquals(EstadoPaquete.LISTO_PARA_DESPACHO, paquete.getEstado());
    }
}
