package com.logistics.packages.domain.model;

import com.logistics.packages.domain.valueobject.*;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import static org.junit.jupiter.api.Assertions.*;

class PaqueteTest {

    @Test
    void creacionPaqueteGeneraUuidYFechaIngreso() {
        Paquete paquete = new Paquete();
        paquete.prePersist();
        assertNotNull(paquete.getId());
        assertNotNull(paquete.getFechaIngresoUtc());
    }

    @Test
    void estadoInicialPaqueteEsRecibidoEnSede() {
        Paquete paquete = new Paquete();
        paquete.prePersist();
        assertEquals(EstadoPaquete.RECIBIDO_EN_SEDE, paquete.getEstado());
    }

    @Test
    void asignarCoordenadasActualizaEstadoGps() {
        Paquete paquete = new Paquete();
        Coordenadas coordenadas = new Coordenadas(11.22, -74.18);
        paquete.asignarCoordenadas(coordenadas);
        assertEquals(EstadoGps.RESUELTO, paquete.getEstadoGps());
        assertEquals(coordenadas, paquete.getCoordenadas());
    }

    @Test
    void procesarPesajeCalculaVolumenYPesos() {
        Paquete paquete = new Paquete();
        paquete.procesarPesaje(10.0, 50.0, 30.0, 20.0, 250);
        assertEquals(0.03, paquete.getVolumenM3());
        assertEquals(7.5, paquete.getPesoVolumetrico());
        assertEquals(10.0, paquete.getPesoFacturable());
    }

    @Test
    void procesarPesajeDetectaCargaEspecialPorPeso() {
        Paquete paquete = new Paquete();
        paquete.procesarPesaje(55.0, 50.0, 30.0, 20.0, 250);
        assertEquals(CategoriaCarga.CARGA_ESPECIAL, paquete.getCategoriaCarga());
        assertTrue(paquete.isAlertaCargaEspecial());
    }

    @Test
    void procesarPesajeDetectaCargaEspecialPorVolumen() {
        Paquete paquete = new Paquete();
        paquete.procesarPesaje(10.0, 100.0, 100.0, 60.0, 250);
        assertEquals(CategoriaCarga.CARGA_ESPECIAL, paquete.getCategoriaCarga());
        assertTrue(paquete.isAlertaCargaEspecial());
    }

    @Test
    void procesarPesajeNoDetectaCargaEspecial() {
        Paquete paquete = new Paquete();
        paquete.procesarPesaje(40.0, 50.0, 30.0, 20.0, 250);
        assertEquals(CategoriaCarga.NORMAL, paquete.getCategoriaCarga());
        assertFalse(paquete.isAlertaCargaEspecial());
    }

    @Test
    void procesarPesajeLanzaExcepcionPorPesoExcesivo() {
        Paquete paquete = new Paquete();
        assertThrows(IllegalArgumentException.class, () -> {
            paquete.procesarPesaje(71.0, 50.0, 30.0, 20.0, 250);
        });
    }

    @Test
    void procesarPesajeLanzaExcepcionPorDimensionesInvalidas() {
        Paquete paquete = new Paquete();
        assertThrows(IllegalArgumentException.class, () -> {
            paquete.procesarPesaje(10.0, 0.0, 30.0, 20.0, 250);
        });
    }

    @Test
    void procesarPesajeDetectaDensidadAtipica() {
        Paquete paquete = new Paquete();
        paquete.procesarPesaje(10.0, 100.0, 100.0, 100.0, 250);
        assertTrue(paquete.isAlertaDensidadAtipica());
    }

    @Test
    void procesarPesajeNoDetectaDensidadAtipica() {
        Paquete paquete = new Paquete();
        paquete.procesarPesaje(10.0, 50.0, 30.0, 20.0, 250);
        assertFalse(paquete.isAlertaDensidadAtipica());
    }

    @Test
    void calcularPrecioEnvio() {
        Paquete paquete = new Paquete();
        paquete.setPesoFacturable(10.0);
        paquete.setDistanciaEstimadaKm(100.0);
        paquete.calcularPrecioEnvio(new BigDecimal("5000"), new BigDecimal("100"), new BigDecimal("50"), BigDecimal.ZERO, BigDecimal.ZERO);
        assertEquals(new BigDecimal("11000"), paquete.getPrecioEnvio().getValor());
    }
}
