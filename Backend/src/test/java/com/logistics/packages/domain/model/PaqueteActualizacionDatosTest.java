package com.logistics.packages.domain.model;

import com.logistics.packages.domain.valueobject.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Nested;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests unitarios para la actualización de datos físicos del Paquete.
 * T405 [P] Test unitario PaqueteTest: actualizarDatosFisicos()
 */
@DisplayName("Paquete - Tests de Actualización de Datos Físicos")
class PaqueteActualizacionDatosTest {

    @Nested
    @DisplayName("actualizarDatosFisicos() - FR-004")
    class ActualizarDatosFisicosTests {

        @Test
        @DisplayName("Debe actualizar peso y dimensiones correctamente")
        void debeActualizarPesoYDimensiones() {
            // Given
            Paquete paquete = Paquete.builder()
                .id(UUID.randomUUID())
                .estado(EstadoPaquete.RECIBIDO_EN_SEDE)
                .peso(new Peso(10.0))
                .dimensiones(new Dimensiones(30.0, 20.0, 10.0))
                .tipoMercancia(TipoMercancia.ESTANDAR)
                .build();

            // Simular procesamiento inicial
            paquete.procesarPesaje(
                new Peso(10.0), 
                new Dimensiones(30.0, 20.0, 10.0), 
                TipoMercancia.ESTANDAR, 
                false
            );

            Double volumenOriginal = paquete.getVolumenM3();
            Double pesoVolumetricoOriginal = paquete.getPesoVolumetrico();

            // When - Actualizar con nuevos datos físicos por discrepancia
            Peso nuevoPeso = new Peso(15.0);
            Dimensiones nuevasDimensiones = new Dimensiones(40.0, 25.0, 15.0);
            paquete.actualizarDatosFisicos(nuevoPeso, nuevasDimensiones);

            // Then
            assertEquals(15.0, paquete.getPeso().getKilogramos(), "Debe actualizar el peso");
            assertEquals(40.0, paquete.getDimensiones().getLargoCm(), "Debe actualizar el largo");
            assertEquals(25.0, paquete.getDimensiones().getAnchoCm(), "Debe actualizar el ancho");
            assertEquals(15.0, paquete.getDimensiones().getAltoCm(), "Debe actualizar el alto");
            assertNotEquals(volumenOriginal, paquete.getVolumenM3(), "El volumen debe recalcularse");
            assertNotEquals(pesoVolumetricoOriginal, paquete.getPesoVolumetrico(), "El peso volumétrico debe recalcularse");
        }

        @Test
        @DisplayName("Debe recalcular volumen después de actualizar dimensiones")
        void debeRecalcularVolumen() {
            // Given
            Paquete paquete = Paquete.builder()
                .id(UUID.randomUUID())
                .peso(new Peso(10.0))
                .dimensiones(new Dimensiones(30.0, 20.0, 10.0))
                .build();

            paquete.procesarPesaje(
                new Peso(10.0), 
                new Dimensiones(30.0, 20.0, 10.0), 
                TipoMercancia.ESTANDAR, 
                false
            );

            // When
            Peso nuevoPeso = new Peso(10.0);
            Dimensiones nuevasDimensiones = new Dimensiones(50.0, 40.0, 30.0); // Mayor volumen
            paquete.actualizarDatosFisicos(nuevoPeso, nuevasDimensiones);

            // Then
            Double volumenEsperado = (50.0 * 40.0 * 30.0) / 1_000_000.0; // 0.06 m³
            assertEquals(volumenEsperado, paquete.getVolumenM3(), 0.0001, "Debe recalcular el volumen correctamente");
        }

        @Test
        @DisplayName("Debe recalcular peso volumétrico después de actualizar")
        void debeRecalcularPesoVolumetrico() {
            // Given
            Paquete paquete = Paquete.builder()
                .id(UUID.randomUUID())
                .peso(new Peso(10.0))
                .dimensiones(new Dimensiones(30.0, 20.0, 10.0))
                .build();

            paquete.procesarPesaje(
                new Peso(10.0), 
                new Dimensiones(30.0, 20.0, 10.0), 
                TipoMercancia.ESTANDAR, 
                false
            );

            // When
            Peso nuevoPeso = new Peso(10.0);
            Dimensiones nuevasDimensiones = new Dimensiones(50.0, 40.0, 30.0);
            paquete.actualizarDatosFisicos(nuevoPeso, nuevasDimensiones);

            // Then
            Double volumen = (50.0 * 40.0 * 30.0) / 1_000_000.0; // 0.06 m³
            Double pesoVolumetricoEsperado = volumen * 250; // 15 kg
            assertEquals(pesoVolumetricoEsperado, paquete.getPesoVolumetrico(), 0.0001, 
                "Debe recalcular el peso volumétrico correctamente");
        }

        @Test
        @DisplayName("Debe recalcular peso facturable después de actualizar")
        void debeRecalcularPesoFacturable() {
            // Given
            Paquete paquete = Paquete.builder()
                .id(UUID.randomUUID())
                .peso(new Peso(10.0))
                .dimensiones(new Dimensiones(30.0, 20.0, 10.0))
                .build();

            paquete.procesarPesaje(
                new Peso(10.0), 
                new Dimensiones(30.0, 20.0, 10.0), 
                TipoMercancia.ESTANDAR, 
                false
            );

            // When - Actualizar con dimensiones que generan peso volumétrico mayor
            Peso nuevoPeso = new Peso(10.0);
            Dimensiones nuevasDimensiones = new Dimensiones(60.0, 50.0, 40.0); // 0.12 m³ -> 30 kg volumétrico
            paquete.actualizarDatosFisicos(nuevoPeso, nuevasDimensiones);

            // Then
            assertTrue(paquete.getPesoFacturable() > 10.0, 
                "El peso facturable debe ser el mayor entre peso real y volumétrico");
            assertEquals(30.0, paquete.getPesoFacturable(), 0.0001, 
                "Debe usar el peso volumétrico como facturable cuando es mayor");
        }

        @Test
        @DisplayName("Debe actualizar categoría de carga si el nuevo peso excede 50 kg")
        void debeActualizarCategoriaCargaPorPeso() {
            // Given
            Paquete paquete = Paquete.builder()
                .id(UUID.randomUUID())
                .peso(new Peso(40.0))
                .dimensiones(new Dimensiones(30.0, 20.0, 10.0))
                .build();

            paquete.procesarPesaje(
                new Peso(40.0), 
                new Dimensiones(30.0, 20.0, 10.0), 
                TipoMercancia.ESTANDAR, 
                false
            );

            assertEquals(CategoriaCarga.NORMAL, paquete.getCategoriaCarga(), 
                "Inicialmente debe ser NORMAL con 40 kg");

            // When - Actualizar con peso que excede el límite
            Peso nuevoPeso = new Peso(55.0);
            Dimensiones nuevasDimensiones = new Dimensiones(30.0, 20.0, 10.0);
            paquete.actualizarDatosFisicos(nuevoPeso, nuevasDimensiones);

            // Then
            assertEquals(CategoriaCarga.CARGA_ESPECIAL, paquete.getCategoriaCarga(), 
                "Debe cambiar a CARGA_ESPECIAL cuando peso > 50 kg");
            assertTrue(paquete.isAlertaCargaEspecial(), "Debe activar la alerta de carga especial");
        }

        @Test
        @DisplayName("Debe actualizar categoría de carga si el nuevo volumen excede 0.5 m³")
        void debeActualizarCategoriaCargaPorVolumen() {
            // Given
            Paquete paquete = Paquete.builder()
                .id(UUID.randomUUID())
                .peso(new Peso(30.0))
                .dimensiones(new Dimensiones(50.0, 40.0, 20.0))
                .build();

            paquete.procesarPesaje(
                new Peso(30.0), 
                new Dimensiones(50.0, 40.0, 20.0), 
                TipoMercancia.ESTANDAR, 
                false
            );

            // When - Actualizar con dimensiones que generan volumen > 0.5 m³
            Peso nuevoPeso = new Peso(30.0);
            Dimensiones nuevasDimensiones = new Dimensiones(100.0, 100.0, 80.0); // 0.8 m³
            paquete.actualizarDatosFisicos(nuevoPeso, nuevasDimensiones);

            // Then
            assertTrue(paquete.getVolumenM3() > 0.5, "El volumen debe ser mayor a 0.5 m³");
            assertEquals(CategoriaCarga.CARGA_ESPECIAL, paquete.getCategoriaCarga(), 
                "Debe cambiar a CARGA_ESPECIAL cuando volumen > 0.5 m³");
            assertTrue(paquete.isAlertaCargaEspecial(), "Debe activar la alerta de carga especial");
        }

        @Test
        @DisplayName("Debe actualizar la alerta de densidad atípica correctamente")
        void debeActualizarAlertaDensidadAtipica() {
            // Given
            Paquete paquete = Paquete.builder()
                .id(UUID.randomUUID())
                .peso(new Peso(20.0))
                .dimensiones(new Dimensiones(30.0, 20.0, 10.0))
                .build();
            assertFalse(paquete.isAlertaDensidadAtipica(), "Inicialmente no debe tener alerta");

            paquete.procesarPesaje(
                new Peso(20.0),
                new Dimensiones(30.0, 20.0, 10.0),
                TipoMercancia.ESTANDAR, 
                false
            );

            // When - Actualizar con dimensiones que crean alta diferencia entre peso real y volumétrico
            Peso nuevoPeso = new Peso(5.0); // Peso muy bajo
            Dimensiones nuevasDimensiones = new Dimensiones(80.0, 60.0, 50.0); // Volumen grande -> 60 kg volumétrico
            paquete.actualizarDatosFisicos(nuevoPeso, nuevasDimensiones);

            // Then
            assertTrue(paquete.isAlertaDensidadAtipica(), 
                "Debe activar alerta cuando la diferencia entre peso real y volumétrico > 30%");
        }

        @Test
        @DisplayName("Debe lanzar IllegalArgumentException si el peso es nulo")
        void debeLanzarExcepcionSiPesoEsNulo() {
            // Given
            Paquete paquete = Paquete.builder()
                .id(UUID.randomUUID())
                .peso(new Peso(10.0))
                .dimensiones(new Dimensiones(30.0, 20.0, 10.0))
                .build();

            // When & Then
            IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> paquete.actualizarDatosFisicos(null, new Dimensiones(40.0, 30.0, 20.0)),
                "Debe lanzar excepción si el peso es nulo"
            );

            assertTrue(exception.getMessage().contains("peso"), 
                "El mensaje debe mencionar el peso");
        }

        @Test
        @DisplayName("Debe lanzar IllegalArgumentException si las dimensiones son nulas")
        void debeLanzarExcepcionSiDimensionesSonNulas() {
            // Given
            Paquete paquete = Paquete.builder()
                .id(UUID.randomUUID())
                .peso(new Peso(10.0))
                .dimensiones(new Dimensiones(30.0, 20.0, 10.0))
                .build();

            // When & Then
            IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> paquete.actualizarDatosFisicos(new Peso(15.0), null),
                "Debe lanzar excepción si las dimensiones son nulas"
            );

            assertTrue(exception.getMessage().contains("dimensiones"), 
                "El mensaje debe mencionar las dimensiones");
        }
    }

    @Nested
    @DisplayName("asignarZonaAlmacenamiento() - Cambio de estado")
    class AsignarZonaAlmacenamientoTests {

        @Test
        @DisplayName("Debe cambiar estado a EN_CLASIFICACION al asignar zona")
        void debeCambiarEstadoAlAsignarZona() {
            // Given
            Paquete paquete = Paquete.builder()
                .id(UUID.randomUUID())
                .estado(EstadoPaquete.RECIBIDO_EN_SEDE)
                .build();

            UUID zonaId = UUID.randomUUID();

            // When
            paquete.asignarZonaAlmacenamiento(zonaId);

            // Then
            assertEquals(EstadoPaquete.EN_CLASIFICACION, paquete.getEstado(), 
                "El estado debe cambiar a EN_CLASIFICACION");
            assertEquals(zonaId, paquete.getZonaAlmacenamientoId(), 
                "Debe asignar el ID de zona correctamente");
        }
    }
}
