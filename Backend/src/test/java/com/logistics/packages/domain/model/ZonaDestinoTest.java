package com.logistics.packages.domain.model;

import com.logistics.packages.domain.valueobject.CategoriaZona;
import com.logistics.packages.domain.valueobject.Coordenadas;
import com.logistics.packages.domain.valueobject.TipoMercancia;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests unitarios para ZonaDestino
 * MOD1-IP-005: T503, T504
 */
@DisplayName("ZonaDestino - Tests Unitarios")
class ZonaDestinoTest {

    @Test
    @DisplayName("Debe crear una zona de destino con todos los atributos")
    void debeCrearZonaDestinoConTodosLosAtributos() {
        // Given
        UUID id = UUID.randomUUID();
        String nombre = "Zona Norte";
        String codigo = "ZD-NORTE-01";
        CategoriaZona categoria = CategoriaZona.NORMAL;
        Integer capacidadMaxPaquetes = 100;

        // When
        ZonaDestino zona = ZonaDestino.builder()
                .id(id)
                .nombre(nombre)
                .codigo(codigo)
                .categoria(categoria)
                .capacidadMaxPaquetes(capacidadMaxPaquetes)
                .contadorPaquetes(0)
                .build();

        // Then
        assertNotNull(zona);
        assertEquals(id, zona.getId());
        assertEquals(nombre, zona.getNombre());
        assertEquals(codigo, zona.getCodigo());
        assertEquals(categoria, zona.getCategoria());
        assertEquals(capacidadMaxPaquetes, zona.getCapacidadMaxPaquetes());
        assertEquals(0, zona.getContadorPaquetes());
    }

    @Test
    @DisplayName("Debe verificar si una zona es apta para mercancía estándar")
    void debeVerificarZonaAptaParaMercanciaEstandar() {
        // Given
        ZonaDestino zona = ZonaDestino.builder()
                .categoria(CategoriaZona.NORMAL)
                .build();

        // When & Then
        assertTrue(zona.esAptaPara(TipoMercancia.ESTANDAR));
    }

    @Test
    @DisplayName("Debe verificar que zona NORMAL no es apta para mercancía peligrosa")
    void debeVerificarZonaNormalNoAptaParaPeligroso() {
        // Given
        ZonaDestino zona = ZonaDestino.builder()
                .categoria(CategoriaZona.NORMAL)
                .build();

        // When & Then
        assertFalse(zona.esAptaPara(TipoMercancia.PELIGROSO));
    }

    @Test
    @DisplayName("Debe verificar que zona ALTO_RIESGO es apta para mercancía peligrosa")
    void debeVerificarZonaAltoRiesgoAptaParaPeligroso() {
        // Given
        ZonaDestino zona = ZonaDestino.builder()
                .categoria(CategoriaZona.ALTO_RIESGO)
                .build();

        // When & Then
        assertTrue(zona.esAptaPara(TipoMercancia.PELIGROSO));
    }

    @Test
    @DisplayName("Debe verificar que zona DELICADA es apta para mercancía frágil")
    void debeVerificarZonaDelicadaAptaParaFragil() {
        // Given
        ZonaDestino zona = ZonaDestino.builder()
                .categoria(CategoriaZona.DELICADA)
                .build();

        // When & Then
        assertTrue(zona.esAptaPara(TipoMercancia.FRAGIL));
    }

    @Test
    @DisplayName("Debe verificar si una zona tiene capacidad disponible")
    void debeVerificarCapacidadDisponible() {
        // Given
        ZonaDestino zona = ZonaDestino.builder()
                .capacidadMaxPaquetes(100)
                .contadorPaquetes(50)
                .build();

        // When & Then
        assertTrue(zona.tieneCapacidadDisponible());
    }

    @Test
    @DisplayName("Debe verificar cuando una zona no tiene capacidad disponible")
    void debeVerificarSinCapacidadDisponible() {
        // Given
        ZonaDestino zona = ZonaDestino.builder()
                .capacidadMaxPaquetes(100)
                .contadorPaquetes(100)
                .build();

        // When & Then
        assertFalse(zona.tieneCapacidadDisponible());
    }

    @Test
    @DisplayName("Debe incrementar el contador de paquetes")
    void debeIncrementarContadorPaquetes() {
        // Given
        ZonaDestino zona = ZonaDestino.builder()
                .capacidadMaxPaquetes(100)
                .contadorPaquetes(10)
                .build();

        // When
        zona.incrementarContador();

        // Then
        assertEquals(11, zona.getContadorPaquetes());
    }

    @Test
    @DisplayName("Debe determinar si una coordenada pertenece a la zona")
    void debeDeterminarSiCoordenadasPertenecenAZona() {
        // Given
        ZonaDestino zona = ZonaDestino.builder()
                .latitudMin(4.60)
                .latitudMax(4.80)
                .longitudMin(-74.10)
                .longitudMax(-74.00)
                .build();

        Coordenadas coordenadas = new Coordenadas(4.70, -74.05);

        // When & Then
        assertTrue(zona.contieneCoordenas(coordenadas));
    }

    @Test
    @DisplayName("Debe determinar si una coordenada no pertenece a la zona")
    void debeDeterminarSiCoordenadasNoPertenecenAZona() {
        // Given
        ZonaDestino zona = ZonaDestino.builder()
                .latitudMin(4.60)
                .latitudMax(4.80)
                .longitudMin(-74.10)
                .longitudMax(-74.00)
                .build();

        Coordenadas coordenadas = new Coordenadas(5.00, -74.05);

        // When & Then
        assertFalse(zona.contieneCoordenas(coordenadas));
    }
}
