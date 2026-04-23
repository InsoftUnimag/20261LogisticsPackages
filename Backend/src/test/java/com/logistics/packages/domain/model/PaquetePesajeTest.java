package com.logistics.packages.domain.model;

import com.logistics.packages.domain.valueobject.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests unitarios para la funcionalidad de pesaje del Paquete (MOD1-UC-002)
 * T201 y T202: Validación de cálculos y reglas de negocio
 */
@DisplayName("Paquete - Procesamiento de Pesaje")
class PaquetePesajeTest {

    private Paquete paquete;

    @BeforeEach
    void setUp() {
        paquete = Paquete.builder()
                .id(UUID.randomUUID())
                .sedeId("SEDE-001")
                .distanciaEstimadaKm(50.0)
                .build();
    }

    // T201 - Cálculos del dominio

    @Test
    @DisplayName("FR-004: Debe calcular correctamente el volumen en m³")
    void debeCalcularVolumenCorrectamente() {
        // Given: 50cm x 40cm x 30cm = 60,000 cm³ = 0.06 m³
        Peso peso = new Peso(10.0);
        Dimensiones dimensiones = new Dimensiones(50.0, 40.0, 30.0);

        // When
        paquete.procesarPesaje(peso, dimensiones, TipoMercancia.ESTANDAR, false);

        // Then
        assertNotNull(paquete.getVolumenM3());
        assertEquals(0.06, paquete.getVolumenM3(), 0.001);
    }

    @Test
    @DisplayName("FR-005: Debe calcular correctamente el peso volumétrico")
    void debeCalcularPesoVolumetricoCorrectamente() {
        // Given: Volumen 0.06 m³ × 250 = 15 kg
        Peso peso = new Peso(10.0);
        Dimensiones dimensiones = new Dimensiones(50.0, 40.0, 30.0);

        // When
        paquete.procesarPesaje(peso, dimensiones, TipoMercancia.ESTANDAR, false);

        // Then
        assertNotNull(paquete.getPesoVolumetrico());
        assertEquals(15.0, paquete.getPesoVolumetrico(), 0.001);
    }

    @Test
    @DisplayName("FR-006: Debe determinar el peso facturable como el mayor entre peso real y volumétrico")
    void debeDeterminarPesoFacturableCorrectamente() {
        // Given: Peso real 10 kg vs Peso volumétrico 15 kg
        Peso peso = new Peso(10.0);
        Dimensiones dimensiones = new Dimensiones(50.0, 40.0, 30.0);

        // When
        paquete.procesarPesaje(peso, dimensiones, TipoMercancia.ESTANDAR, false);

        // Then
        assertNotNull(paquete.getPesoFacturable());
        assertEquals(15.0, paquete.getPesoFacturable(), 0.001);
    }

    @Test
    @DisplayName("FR-006: Peso facturable debe ser el peso real cuando es mayor al volumétrico")
    void pesoFacturableDebeSePesoRealCuandoEsMayor() {
        // Given: Peso real 60 kg vs Peso volumétrico 15 kg
        Peso peso = new Peso(60.0);
        Dimensiones dimensiones = new Dimensiones(50.0, 40.0, 30.0);

        // When
        paquete.procesarPesaje(peso, dimensiones, TipoMercancia.ESTANDAR, false);

        // Then
        assertEquals(60.0, paquete.getPesoFacturable(), 0.001);
    }

    @Test
    @DisplayName("FR-002: Debe asignar Carga Especial si peso > 50 kg")
    void debeAsignarCargaEspecialPorPeso() {
        // Given: Peso 55 kg
        Peso peso = new Peso(55.0);
        Dimensiones dimensiones = new Dimensiones(30.0, 20.0, 10.0);

        // When
        paquete.procesarPesaje(peso, dimensiones, TipoMercancia.ESTANDAR, false);

        // Then
        assertEquals(CategoriaCarga.CARGA_ESPECIAL, paquete.getCategoriaCarga());
        assertTrue(paquete.isAlertaCargaEspecial());
    }

    @Test
    @DisplayName("FR-002: Debe asignar Carga Especial si volumen > 0.5 m³")
    void debeAsignarCargaEspecialPorVolumen() {
        // Given: 100cm x 100cm x 60cm = 0.6 m³
        Peso peso = new Peso(20.0);
        Dimensiones dimensiones = new Dimensiones(100.0, 100.0, 60.0);

        // When
        paquete.procesarPesaje(peso, dimensiones, TipoMercancia.ESTANDAR, false);

        // Then
        assertEquals(CategoriaCarga.CARGA_ESPECIAL, paquete.getCategoriaCarga());
        assertTrue(paquete.isAlertaCargaEspecial());
    }

    @Test
    @DisplayName("FR-002: Debe asignar categoría Normal si no cumple condiciones de Carga Especial")
    void debeAsignarCategoriaNormal() {
        // Given: Peso 30 kg, Volumen 0.06 m³
        Peso peso = new Peso(30.0);
        Dimensiones dimensiones = new Dimensiones(50.0, 40.0, 30.0);

        // When
        paquete.procesarPesaje(peso, dimensiones, TipoMercancia.ESTANDAR, false);

        // Then
        assertEquals(CategoriaCarga.NORMAL, paquete.getCategoriaCarga());
        assertFalse(paquete.isAlertaCargaEspecial());
    }

    // T202 - Validaciones

    @Test
    @DisplayName("FR-001: Debe lanzar excepción si el peso excede los 70 kg")
    void debeLanzarExcepcionSiPesoExcede70Kg() {
        // Given/When/Then
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> new Peso(71.0)
        );
        assertEquals("El peso no puede exceder los 70 kg.", exception.getMessage());
    }

    @Test
    @DisplayName("FR-001: Debe lanzar excepción si el peso es cero")
    void debeLanzarExcepcionSiPesoEsCero() {
        // Given/When/Then
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> new Peso(0.0)
        );
        assertEquals("El peso debe ser mayor a cero.", exception.getMessage());
    }

    @Test
    @DisplayName("FR-001: Debe lanzar excepción si el peso es negativo")
    void debeLanzarExcepcionSiPesoEsNegativo() {
        // Given/When/Then
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> new Peso(-5.0)
        );
        assertEquals("El peso debe ser mayor a cero.", exception.getMessage());
    }

    @Test
    @DisplayName("FR-003: Debe lanzar excepción si el largo es cero o negativo")
    void debeLanzarExcepcionSiLargoInvalido() {
        // Given/When/Then
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> new Dimensiones(0.0, 40.0, 30.0)
        );
        assertEquals("El largo debe ser mayor a cero.", exception.getMessage());
    }

    @Test
    @DisplayName("FR-003: Debe lanzar excepción si el ancho es cero o negativo")
    void debeLanzarExcepcionSiAnchoInvalido() {
        // Given/When/Then
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> new Dimensiones(50.0, -10.0, 30.0)
        );
        assertEquals("El ancho debe ser mayor a cero.", exception.getMessage());
    }

    @Test
    @DisplayName("FR-003: Debe lanzar excepción si el alto es cero o negativo")
    void debeLanzarExcepcionSiAltoInvalido() {
        // Given/When/Then
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> new Dimensiones(50.0, 40.0, 0.0)
        );
        assertEquals("El alto debe ser mayor a cero.", exception.getMessage());
    }

    @Test
    @DisplayName("FR-010: Debe generar alerta de densidad atípica si diferencia > 30%")
    void debeGenerarAlertaDensidadAtipica() {
        // Given: Peso real 10 kg, Peso volumétrico 50 kg (diferencia 80%)
        Peso peso = new Peso(10.0);
        Dimensiones dimensiones = new Dimensiones(100.0, 100.0, 80.0); // 0.8 m³ × 250 = 200 kg

        // When
        paquete.procesarPesaje(peso, dimensiones, TipoMercancia.ESTANDAR, false);

        // Then
        assertTrue(paquete.isAlertaDensidadAtipica());
    }

    @Test
    @DisplayName("FR-010: No debe generar alerta de densidad atípica si diferencia <= 30%")
    void noDebeGenerarAlertaDensidadAtipicaSiDiferenciaEsMenor() {
        // Given: Peso real 20 kg, Peso volumétrico 25 kg (diferencia 25%)
        Peso peso = new Peso(20.0);
        Dimensiones dimensiones = new Dimensiones(50.0, 50.0, 40.0); // 0.1 m³ × 250 = 25 kg

        // When
        paquete.procesarPesaje(peso, dimensiones, TipoMercancia.ESTANDAR, false);

        // Then
        assertFalse(paquete.isAlertaDensidadAtipica());
    }

    @Test
    @DisplayName("Debe asignar correctamente el tipo de mercancía y el indicador de forma irregular")
    void debeAsignarTipoMercanciaEIndicadorIrregular() {
        // Given
        Peso peso = new Peso(15.0);
        Dimensiones dimensiones = new Dimensiones(50.0, 40.0, 30.0);

        // When
        paquete.procesarPesaje(peso, dimensiones, TipoMercancia.FRAGIL, true);

        // Then
        assertEquals(TipoMercancia.FRAGIL, paquete.getTipoMercancia());
        assertTrue(paquete.getIndicadorFormaIrregular());
    }

    @Test
    @DisplayName("Debe permitir peso exacto de 70 kg")
    void debePermitirPesoExacto70Kg() {
        // Given/When/Then
        assertDoesNotThrow(() -> new Peso(70.0));
    }
}
