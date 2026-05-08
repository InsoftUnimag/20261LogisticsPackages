package com.logistics.packages.infrastructure.adapter.external;

import com.logistics.packages.domain.valueobject.Coordenadas;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests unitarios para CoverageAreaAdapter
 * MOD1-UC-001: FR-001 - Validación de cobertura
 */
@DisplayName("CoverageAreaAdapter")
class CoverageAreaAdapterTest {

    private final CoverageAreaAdapter coverageService = new CoverageAreaAdapter();

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(coverageService, "latitudMin", 4.0);
        ReflectionTestUtils.setField(coverageService, "latitudMax", 5.0);
        ReflectionTestUtils.setField(coverageService, "longitudMin", -75.0);
        ReflectionTestUtils.setField(coverageService, "longitudMax", -74.0);
    }

    @Test
    @DisplayName("Debe retornar true cuando coordenadas están dentro del área")
    void coordenadasDentroDelArea() {
        Coordenadas coordenadas = new Coordenadas(4.5, -74.5);
        assertTrue(coverageService.isWithinCoverage(coordenadas));
    }

    @Test
    @DisplayName("Debe retornar false cuando coordenadas están fuera del área")
    void coordenadasFueraDelArea() {
        Coordenadas coordenadas = new Coordenadas(6.0, -74.5);
        assertFalse(coverageService.isWithinCoverage(coordenadas));
    }

    @Test
    @DisplayName("Debe retornar false cuando coordenadas son nulas")
    void coordenadasNulas() {
        assertFalse(coverageService.isWithinCoverage(null));
    }

    @Test
    @DisplayName("Debe retornar true en el límite exacto de latitud mínima")
    void limiteLatitudMinima() {
        Coordenadas coordenadas = new Coordenadas(4.0, -74.5);
        assertTrue(coverageService.isWithinCoverage(coordenadas));
    }

    @Test
    @DisplayName("Debe retornar true en el límite exacto de latitud máxima")
    void limiteLatitudMaxima() {
        Coordenadas coordenadas = new Coordenadas(5.0, -74.5);
        assertTrue(coverageService.isWithinCoverage(coordenadas));
    }

    @Test
    @DisplayName("Debe retornar false fuera del límite de latitud mínima")
    void fueraDelLimiteLatitudMinima() {
        Coordenadas coordenadas = new Coordenadas(3.99, -74.5);
        assertFalse(coverageService.isWithinCoverage(coordenadas));
    }
}