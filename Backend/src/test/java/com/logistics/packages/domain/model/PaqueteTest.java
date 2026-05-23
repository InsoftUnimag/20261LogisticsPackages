package com.logistics.packages.domain.model;

import com.logistics.packages.domain.exception.EstadoTransicionInvalidaException;
import com.logistics.packages.domain.exception.EvidenciaRequeridaException;
import com.logistics.packages.domain.valueobject.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class PaqueteTest {

    private Paquete paquete;

    @BeforeEach
    void setUp() {
        paquete = Paquete.crearNuevo(
                UUID.randomUUID(),
                UUID.randomUUID(),
                null, null, null, null, null, null, null
        );
    }

    @Test
    void creacionPaqueteGeneraUuidYFechaIngreso() {
        assertNotNull(paquete.getId());
        assertNotNull(paquete.getFechaIngresoUtc());
    }

    @Test
    void estadoInicialPaqueteEsRecibidoEnSede() {
        assertEquals(EstadoPaquete.RECIBIDO_EN_SEDE, paquete.getEstado());
    }

    @Test
    void asignarCoordenadasActualizaEstadoGps() {
        Coordenadas coordenadas = new Coordenadas(11.22, -74.18);
        paquete.asignarCoordenadas(coordenadas);
        assertEquals(EstadoGps.RESUELTO, paquete.getEstadoGps());
        assertEquals(coordenadas, paquete.getCoordenadas());
    }

    @Test
    void procesarPesajeCalculaVolumenYPesos() {
        Peso peso = new Peso(10.0);
        Dimensiones dimensiones = new Dimensiones(50.0, 30.0, 20.0);
        paquete.procesarPesaje(peso, dimensiones, TipoMercancia.ESTANDAR, false);

        assertEquals(0.03, paquete.getVolumenM3(), 0.001);
        assertEquals(7.5, paquete.getPesoVolumetrico(), 0.001);
        assertEquals(10.0, paquete.getPesoFacturable(), 0.001);
    }

    @Test
    void procesarPesajeDetectaCargaEspecialPorPeso() {
        Peso peso = new Peso(55.0);
        Dimensiones dimensiones = new Dimensiones(50.0, 30.0, 20.0);
        paquete.procesarPesaje(peso, dimensiones, TipoMercancia.ESTANDAR, false);

        assertEquals(CategoriaCarga.CARGA_ESPECIAL, paquete.getCategoriaCarga());
        assertTrue(paquete.isAlertaCargaEspecial());
    }

    @Test
    void procesarPesajeDetectaCargaEspecialPorVolumen() {
        Peso peso = new Peso(10.0);
        Dimensiones dimensiones = new Dimensiones(100.0, 100.0, 60.0);
        paquete.procesarPesaje(peso, dimensiones, TipoMercancia.ESTANDAR, false);

        assertEquals(CategoriaCarga.CARGA_ESPECIAL, paquete.getCategoriaCarga());
        assertTrue(paquete.isAlertaCargaEspecial());
    }

    @Test
    void procesarPesajeNoDetectaCargaEspecial() {
        Peso peso = new Peso(40.0);
        Dimensiones dimensiones = new Dimensiones(50.0, 30.0, 20.0);
        paquete.procesarPesaje(peso, dimensiones, TipoMercancia.ESTANDAR, false);

        assertEquals(CategoriaCarga.NORMAL, paquete.getCategoriaCarga());
        assertFalse(paquete.isAlertaCargaEspecial());
    }

    @Test
    void procesarPesajeLanzaExcepcionPorPesoExcesivo() {
        assertThrows(IllegalArgumentException.class, () -> new Peso(71.0));
    }

    @Test
    void procesarPesajeLanzaExcepcionPorDimensionesInvalidas() {
        assertThrows(IllegalArgumentException.class, () -> new Dimensiones(0.0, 30.0, 20.0));
    }

    @Test
    void procesarPesajeDetectaDensidadAtipica() {
        Peso peso = new Peso(10.0);
        Dimensiones dimensiones = new Dimensiones(100.0, 100.0, 100.0);
        paquete.procesarPesaje(peso, dimensiones, TipoMercancia.ESTANDAR, false);
        assertTrue(paquete.isAlertaDensidadAtipica());
    }

    @Test
    void procesarPesajeNoDetectaDensidadAtipica() {
        Peso peso = new Peso(10.0);
        Dimensiones dimensiones = new Dimensiones(50.0, 30.0, 20.0);
        paquete.procesarPesaje(peso, dimensiones, TipoMercancia.ESTANDAR, false);
        assertFalse(paquete.isAlertaDensidadAtipica());
    }

    @Test
    void calcularPrecioEnvio() {
        Peso peso = new Peso(10.0);
        Dimensiones dimensiones = new Dimensiones(50.0, 30.0, 20.0);
        paquete.procesarPesaje(peso, dimensiones, TipoMercancia.ESTANDAR, false);
        paquete.calcularPrecioEnvio(new BigDecimal("5000"), new BigDecimal("100"), new BigDecimal("50"), BigDecimal.ZERO, BigDecimal.ZERO, 100.0);
        assertEquals(0, new BigDecimal("11000").compareTo(paquete.getPrecioEnvio().getValor()));
    }

    @Test
    void asignarRutaCambiaEstadoYGuardaId() {
        UUID rutaId = UUID.randomUUID();
        paquete.asignarRuta(rutaId);
        assertEquals(EstadoPaquete.LISTO_PARA_DESPACHO, paquete.getEstado());
        assertEquals(rutaId, paquete.getRutaId());
    }

    @Test
    void asignarRutaLanzaExcepcionSiYaTieneRuta() {
        UUID rutaId1 = UUID.randomUUID();
        paquete.asignarRuta(rutaId1);
        UUID rutaId2 = UUID.randomUUID();
        assertThrows(IllegalStateException.class, () -> paquete.asignarRuta(rutaId2));
    }

    @Test
    void asignarZonaAlmacenamientoCambiaEstado() {
        UUID zonaId = UUID.randomUUID();
        paquete.asignarZonaAlmacenamiento(zonaId);
        assertEquals(EstadoPaquete.EN_CLASIFICACION, paquete.getEstado());
        assertEquals(zonaId, paquete.getZonaAlmacenamientoId());
    }

    @Test
    void asignarZonaDestinoCambiaEstado() {
        paquete.cambiarEstado(EstadoPaquete.EN_CLASIFICACION);
        UUID zonaId = UUID.randomUUID();
        paquete.asignarZonaDestino(zonaId);
        assertEquals(EstadoPaquete.LISTO_PARA_DESPACHO, paquete.getEstado());
        assertEquals(zonaId, paquete.getZonaDestinoId());
    }

    @Test
    void asignarZonaDestinoLanzaExcepcionSiEstadoNoEsEnClasificacion() {
        UUID zonaId = UUID.randomUUID();
        assertThrows(IllegalStateException.class, () -> paquete.asignarZonaDestino(zonaId));
    }

    @Test
    void actualizarDatosFisicosRecalculaValores() {
        Peso pesoInicial = new Peso(10.0);
        Dimensiones dimsIniciales = new Dimensiones(50.0, 30.0, 20.0);
        paquete.procesarPesaje(pesoInicial, dimsIniciales, TipoMercancia.ESTANDAR, false);
        assertEquals(10.0, paquete.getPesoFacturable());

        Peso nuevoPeso = new Peso(15.0);
        Dimensiones nuevasDims = new Dimensiones(60.0, 40.0, 20.0);
        paquete.actualizarDatosFisicos(nuevoPeso, nuevasDims);

        assertEquals(nuevoPeso, paquete.getPeso());
        assertEquals(nuevasDims, paquete.getDimensiones());
        assertEquals(0.048, paquete.getVolumenM3(), 0.001);
        assertEquals(12.0, paquete.getPesoVolumetrico(), 0.001);
        assertEquals(15.0, paquete.getPesoFacturable(), 0.001);
    }

    @Test
    void registrarNovedadDañadoCambiaEstadoYRetornaHistorial() {
        UUID usuarioId = UUID.randomUUID();
        String urlEvidencia = "http://example.com/foto.jpg";
        
        HistorialEstado historial = paquete.registrarNovedad(TipoNovedad.DAÑADO, "Caja rota", usuarioId, urlEvidencia);

        assertEquals(EstadoPaquete.NOVEDAD_EN_BODEGA, paquete.getEstado());
        assertNotNull(historial);
        assertEquals(paquete.getId(), historial.getPaqueteId());
        assertEquals(EstadoPaquete.RECIBIDO_EN_SEDE, historial.getEstadoAnterior());
        assertEquals(EstadoPaquete.NOVEDAD_EN_BODEGA, historial.getEstadoNuevo());
        assertEquals("DAÑADO - Caja rota", historial.getObservaciones());
        assertEquals(usuarioId, historial.getUsuarioId());
        assertEquals(urlEvidencia, historial.getUrlEvidencia());
    }

    @Test
    void registrarNovedadExtraviadoCambiaEstado() {
        UUID usuarioId = UUID.randomUUID();
        paquete.registrarNovedad(TipoNovedad.EXTRAVIADO, "No se encuentra", usuarioId, null);
        assertEquals(EstadoPaquete.NOVEDAD_EN_BODEGA, paquete.getEstado());
    }

    @Test
    void registrarNovedadDañadoSinEvidenciaLanzaExcepcion() {
        UUID usuarioId = UUID.randomUUID();
        assertThrows(EvidenciaRequeridaException.class, () -> {
            paquete.registrarNovedad(TipoNovedad.DAÑADO, "Caja rota", usuarioId, null);
        });
    }

    @Test
    void registrarNovedadEnEstadoInvalidoLanzaExcepcion() {
        paquete.cambiarEstado(EstadoPaquete.EN_TRANSITO);
        UUID usuarioId = UUID.randomUUID();
        assertThrows(EstadoTransicionInvalidaException.class, () -> {
            paquete.registrarNovedad(TipoNovedad.EXTRAVIADO, "No se encuentra", usuarioId, null);
        });
    }
}
