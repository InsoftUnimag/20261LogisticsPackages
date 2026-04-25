package com.logistics.packages.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * T301 [P] - Tests unitarios para la asignación de ruta en Paquete
 * MOD1-IP-003 - Phase 1
 */
@DisplayName("Paquete - Asignación de Ruta")
class PaqueteRutaTest {

    @Test
    @DisplayName("Debe asignar ruta correctamente y cambiar estado a LISTO_PARA_DESPACHO")
    void debeAsignarRutaCorrectamente() {
        // Given: Un paquete sin ruta asignada
        Paquete paquete = Paquete.builder().build();
        paquete.prePersist();
        UUID rutaId = UUID.randomUUID();

        // When: Se asigna una ruta
        paquete.asignarRuta(rutaId);

        // Then: La ruta se asigna y el estado cambia
        assertEquals(rutaId, paquete.getRutaId());
        assertEquals(com.logistics.packages.domain.valueobject.EstadoPaquete.LISTO_PARA_DESPACHO, paquete.getEstado());
    }

    @Test
    @DisplayName("No debe permitir asignar ruta si ya tiene una asignada")
    void noDebePermitirReasignarRuta() {
        // Given: Un paquete con ruta ya asignada
        Paquete paquete = Paquete.builder().build();
        paquete.prePersist();
        UUID primeraRuta = UUID.randomUUID();
        paquete.asignarRuta(primeraRuta);

        // When & Then: Intentar asignar otra ruta debe lanzar excepción
        UUID segundaRuta = UUID.randomUUID();
        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> paquete.asignarRuta(segundaRuta)
        );

        assertEquals("El paquete ya tiene una ruta asignada.", exception.getMessage());
        assertEquals(primeraRuta, paquete.getRutaId()); // Mantiene la primera ruta
    }

    @Test
    @DisplayName("No debe permitir asignar ruta nula")
    void noDebePermitirRutaNula() {
        // Given: Un paquete sin ruta
        Paquete paquete = Paquete.builder().build();
        paquete.prePersist();

        // When & Then: Intentar asignar ruta nula debe lanzar excepción
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> paquete.asignarRuta(null)
        );

        assertEquals("El ID de ruta no puede ser nulo.", exception.getMessage());
        assertNull(paquete.getRutaId());
    }
}
