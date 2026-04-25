package com.logistics.packages.domain.model;

import com.logistics.packages.domain.exception.ZonaSaturadaException;
import com.logistics.packages.domain.exception.ZonaIncompatibleException;
import com.logistics.packages.domain.valueobject.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Nested;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests unitarios para la entidad ZonaAlmacenaje.
 * T404 [P] Test unitario ZonaAlmacenajeTest
 */
@DisplayName("ZonaAlmacenaje - Tests Unitarios")
class ZonaAlmacenajeTest {

    @Nested
    @DisplayName("puedeAlbergar() - Compatibilidad de tipo de mercancía")
    class PuedeAlbergarTests {

        @Test
        @DisplayName("Debe permitir mercancía PELIGROSO solo en zona ALTO_RIESGO")
        void debePermitirPeligrosoEnAltoRiesgo() {
            // Given
            ZonaAlmacenaje zona = crearZona(CategoriaZona.ALTO_RIESGO);
            Paquete paquete = crearPaquete(TipoMercancia.PELIGROSO, 10.0, 0.1);

            // When
            boolean resultado = zona.puedeAlbergar(paquete);

            // Then
            assertTrue(resultado, "Zona ALTO_RIESGO debe aceptar mercancía PELIGROSO");
        }

        @Test
        @DisplayName("Debe rechazar mercancía PELIGROSO en zona NORMAL")
        void debeRechazarPeligrosoEnNormal() {
            // Given
            ZonaAlmacenaje zona = crearZona(CategoriaZona.NORMAL);
            Paquete paquete = crearPaquete(TipoMercancia.PELIGROSO, 10.0, 0.1);

            // When
            boolean resultado = zona.puedeAlbergar(paquete);

            // Then
            assertFalse(resultado, "Zona NORMAL no debe aceptar mercancía PELIGROSO");
        }

        @Test
        @DisplayName("Debe permitir mercancía FRAGIL en zona DELICADA")
        void debePermitirFragilEnDelicada() {
            // Given
            ZonaAlmacenaje zona = crearZona(CategoriaZona.DELICADA);
            Paquete paquete = crearPaquete(TipoMercancia.FRAGIL, 10.0, 0.1);

            // When
            boolean resultado = zona.puedeAlbergar(paquete);

            // Then
            assertTrue(resultado, "Zona DELICADA debe aceptar mercancía FRAGIL");
        }

        @Test
        @DisplayName("Debe permitir mercancía FRAGIL en zona ALTO_RIESGO")
        void debePermitirFragilEnAltoRiesgo() {
            // Given
            ZonaAlmacenaje zona = crearZona(CategoriaZona.ALTO_RIESGO);
            Paquete paquete = crearPaquete(TipoMercancia.FRAGIL, 10.0, 0.1);

            // When
            boolean resultado = zona.puedeAlbergar(paquete);

            // Then
            assertTrue(resultado, "Zona ALTO_RIESGO debe aceptar mercancía FRAGIL");
        }

        @Test
        @DisplayName("Debe rechazar mercancía FRAGIL en zona NORMAL")
        void debeRechazarFragilEnNormal() {
            // Given
            ZonaAlmacenaje zona = crearZona(CategoriaZona.NORMAL);
            Paquete paquete = crearPaquete(TipoMercancia.FRAGIL, 10.0, 0.1);

            // When
            boolean resultado = zona.puedeAlbergar(paquete);

            // Then
            assertFalse(resultado, "Zona NORMAL no debe aceptar mercancía FRAGIL");
        }

        @Test
        @DisplayName("Debe permitir mercancía ESTANDAR en cualquier zona")
        void debePermitirEstandarEnCualquierZona() {
            // Given
            Paquete paquete = crearPaquete(TipoMercancia.ESTANDAR, 10.0, 0.1);

            // When & Then
            assertTrue(crearZona(CategoriaZona.NORMAL).puedeAlbergar(paquete));
            assertTrue(crearZona(CategoriaZona.DELICADA).puedeAlbergar(paquete));
            assertTrue(crearZona(CategoriaZona.ALTO_RIESGO).puedeAlbergar(paquete));
        }

        @Test
        @DisplayName("Debe retornar false si el paquete no tiene tipo de mercancía")
        void debeRechazarPaqueteSinTipoMercancia() {
            // Given
            ZonaAlmacenaje zona = crearZona(CategoriaZona.NORMAL);
            Paquete paquete = Paquete.builder().id(UUID.randomUUID()).build();

            // When
            boolean resultado = zona.puedeAlbergar(paquete);

            // Then
            assertFalse(resultado, "Debe rechazar paquetes sin tipo de mercancía definido");
        }
    }

    @Nested
    @DisplayName("agregarPaquete() - Actualización de contadores")
    class AgregarPaqueteTests {

        @Test
        @DisplayName("Debe actualizar correctamente peso, volumen y cantidad al agregar paquete")
        void debeActualizarContadoresCorrectamente() {
            // Given
            ZonaAlmacenaje zona = ZonaAlmacenaje.builder()
                .id(UUID.randomUUID())
                .nombre("Zona A")
                .categoria(CategoriaZona.NORMAL)
                .capacidadMaxKg(BigDecimal.valueOf(1000))
                .capacidadMaxM3(BigDecimal.valueOf(50))
                .capacidadMaxPaquetes(100)
                .pesoActualKg(BigDecimal.valueOf(100))
                .volumenActualM3(BigDecimal.valueOf(5))
                .contadorPaquetes(10)
                .estado(EstadoZona.DISPONIBLE)
                .build();

            Paquete paquete = crearPaquete(TipoMercancia.ESTANDAR, 25.0, 2.5);

            // When
            zona.agregarPaquete(paquete);

            // Then
            assertEquals(BigDecimal.valueOf(125.0), zona.getPesoActualKg(), "Debe sumar el peso del paquete");
            assertEquals(BigDecimal.valueOf(7.5), zona.getVolumenActualM3(), "Debe sumar el volumen del paquete");
            assertEquals(11, zona.getContadorPaquetes(), "Debe incrementar el contador de paquetes");
        }

        @Test
        @DisplayName("Debe lanzar ZonaSaturadaException cuando se excede capacidad de peso")
        void debeLanzarExcepcionCuandoExcedeCapacidadPeso() {
            // Given
            ZonaAlmacenaje zona = ZonaAlmacenaje.builder()
                .id(UUID.randomUUID())
                .nombre("Zona B")
                .categoria(CategoriaZona.NORMAL)
                .capacidadMaxKg(BigDecimal.valueOf(100))
                .capacidadMaxM3(BigDecimal.valueOf(50))
                .capacidadMaxPaquetes(100)
                .pesoActualKg(BigDecimal.valueOf(95))
                .volumenActualM3(BigDecimal.valueOf(5))
                .contadorPaquetes(10)
                .zonaContingenciaId(UUID.randomUUID())
                .build();

            Paquete paquete = crearPaquete(TipoMercancia.ESTANDAR, 10.0, 1.0);

            // When & Then
            ZonaSaturadaException exception = assertThrows(
                ZonaSaturadaException.class,
                () -> zona.agregarPaquete(paquete),
                "Debe lanzar ZonaSaturadaException cuando se excede la capacidad de peso"
            );

            assertEquals("peso", exception.getTipoCapacidadExcedida());
            assertNotNull(exception.getZonaContingenciaId());
        }

        @Test
        @DisplayName("Debe lanzar ZonaSaturadaException cuando se excede capacidad de volumen")
        void debeLanzarExcepcionCuandoExcedeCapacidadVolumen() {
            // Given
            ZonaAlmacenaje zona = ZonaAlmacenaje.builder()
                .id(UUID.randomUUID())
                .nombre("Zona C")
                .categoria(CategoriaZona.NORMAL)
                .capacidadMaxKg(BigDecimal.valueOf(1000))
                .capacidadMaxM3(BigDecimal.valueOf(10))
                .capacidadMaxPaquetes(100)
                .pesoActualKg(BigDecimal.valueOf(50))
                .volumenActualM3(BigDecimal.valueOf(9))
                .contadorPaquetes(10)
                .zonaContingenciaId(UUID.randomUUID())
                .build();

            Paquete paquete = crearPaquete(TipoMercancia.ESTANDAR, 10.0, 2.0);

            // When & Then
            ZonaSaturadaException exception = assertThrows(
                ZonaSaturadaException.class,
                () -> zona.agregarPaquete(paquete),
                "Debe lanzar ZonaSaturadaException cuando se excede la capacidad de volumen"
            );

            assertEquals("volumen", exception.getTipoCapacidadExcedida());
        }

        @Test
        @DisplayName("Debe lanzar ZonaSaturadaException cuando se excede cantidad de paquetes")
        void debeLanzarExcepcionCuandoExcedeCantidadPaquetes() {
            // Given
            ZonaAlmacenaje zona = ZonaAlmacenaje.builder()
                .id(UUID.randomUUID())
                .nombre("Zona D")
                .categoria(CategoriaZona.NORMAL)
                .capacidadMaxKg(BigDecimal.valueOf(1000))
                .capacidadMaxM3(BigDecimal.valueOf(50))
                .capacidadMaxPaquetes(20)
                .pesoActualKg(BigDecimal.valueOf(50))
                .volumenActualM3(BigDecimal.valueOf(5))
                .contadorPaquetes(20)
                .zonaContingenciaId(UUID.randomUUID())
                .build();

            Paquete paquete = crearPaquete(TipoMercancia.ESTANDAR, 5.0, 0.5);

            // When & Then
            ZonaSaturadaException exception = assertThrows(
                ZonaSaturadaException.class,
                () -> zona.agregarPaquete(paquete),
                "Debe lanzar ZonaSaturadaException cuando se excede la cantidad de paquetes"
            );

            assertEquals("cantidad de paquetes", exception.getTipoCapacidadExcedida());
        }

        @Test
        @DisplayName("Debe lanzar ZonaIncompatibleException cuando el tipo de mercancía no es compatible")
        void debeLanzarExcepcionCuandoMercanciaNoEsCompatible() {
            // Given
            ZonaAlmacenaje zona = crearZona(CategoriaZona.NORMAL);
            Paquete paquete = crearPaquete(TipoMercancia.PELIGROSO, 10.0, 1.0);

            // When & Then
            ZonaIncompatibleException exception = assertThrows(
                ZonaIncompatibleException.class,
                () -> zona.agregarPaquete(paquete),
                "Debe lanzar ZonaIncompatibleException cuando la mercancía no es compatible"
            );

            assertEquals(TipoMercancia.PELIGROSO, exception.getTipoMercancia());
            assertEquals(CategoriaZona.NORMAL, exception.getCategoriaZona());
        }

        @Test
        @DisplayName("Debe cambiar estado a PARCIAL cuando ocupación está entre 70% y 90%")
        void debeCambiarEstadoAParcial() {
            // Given
            ZonaAlmacenaje zona = ZonaAlmacenaje.builder()
                .id(UUID.randomUUID())
                .nombre("Zona E")
                .categoria(CategoriaZona.NORMAL)
                .capacidadMaxKg(BigDecimal.valueOf(100))
                .capacidadMaxM3(BigDecimal.valueOf(50))
                .capacidadMaxPaquetes(100)
                .pesoActualKg(BigDecimal.valueOf(60))
                .volumenActualM3(BigDecimal.valueOf(5))
                .contadorPaquetes(10)
                .estado(EstadoZona.DISPONIBLE)
                .build();

            Paquete paquete = crearPaquete(TipoMercancia.ESTANDAR, 15.0, 1.0);

            // When
            zona.agregarPaquete(paquete);

            // Then
            assertEquals(EstadoZona.PARCIAL, zona.getEstado(), "El estado debe cambiar a PARCIAL cuando ocupación > 70%");
        }

        @Test
        @DisplayName("Debe cambiar estado a SATURADO cuando ocupación supera 90%")
        void debeCambiarEstadoASaturado() {
            // Given
            ZonaAlmacenaje zona = ZonaAlmacenaje.builder()
                .id(UUID.randomUUID())
                .nombre("Zona F")
                .categoria(CategoriaZona.NORMAL)
                .capacidadMaxKg(BigDecimal.valueOf(100))
                .capacidadMaxM3(BigDecimal.valueOf(50))
                .capacidadMaxPaquetes(100)
                .pesoActualKg(BigDecimal.valueOf(85))
                .volumenActualM3(BigDecimal.valueOf(5))
                .contadorPaquetes(10)
                .estado(EstadoZona.DISPONIBLE)
                .build();

            Paquete paquete = crearPaquete(TipoMercancia.ESTANDAR, 8.0, 1.0);

            // When
            zona.agregarPaquete(paquete);

            // Then
            assertEquals(EstadoZona.SATURADO, zona.getEstado(), "El estado debe cambiar a SATURADO cuando ocupación > 90%");
        }
    }

    @Nested
    @DisplayName("tieneCapacidadPara() - Verificación de capacidad")
    class TieneCapacidadParaTests {

        @Test
        @DisplayName("Debe retornar true cuando hay capacidad suficiente")
        void debeRetornarTrueCuandoHayCapacidad() {
            // Given
            ZonaAlmacenaje zona = ZonaAlmacenaje.builder()
                .id(UUID.randomUUID())
                .categoria(CategoriaZona.NORMAL)
                .capacidadMaxKg(BigDecimal.valueOf(100))
                .capacidadMaxM3(BigDecimal.valueOf(50))
                .capacidadMaxPaquetes(100)
                .pesoActualKg(BigDecimal.valueOf(50))
                .volumenActualM3(BigDecimal.valueOf(10))
                .contadorPaquetes(20)
                .build();

            Paquete paquete = crearPaquete(TipoMercancia.ESTANDAR, 10.0, 2.0);

            // When
            boolean resultado = zona.tieneCapacidadPara(paquete);

            // Then
            assertTrue(resultado, "Debe retornar true cuando hay capacidad suficiente");
        }

        @Test
        @DisplayName("Debe retornar false cuando no hay capacidad de peso")
        void debeRetornarFalseCuandoNoHayCapacidadPeso() {
            // Given
            ZonaAlmacenaje zona = ZonaAlmacenaje.builder()
                .id(UUID.randomUUID())
                .categoria(CategoriaZona.NORMAL)
                .capacidadMaxKg(BigDecimal.valueOf(100))
                .capacidadMaxM3(BigDecimal.valueOf(50))
                .capacidadMaxPaquetes(100)
                .pesoActualKg(BigDecimal.valueOf(95))
                .volumenActualM3(BigDecimal.valueOf(10))
                .contadorPaquetes(20)
                .build();

            Paquete paquete = crearPaquete(TipoMercancia.ESTANDAR, 10.0, 2.0);

            // When
            boolean resultado = zona.tieneCapacidadPara(paquete);

            // Then
            assertFalse(resultado, "Debe retornar false cuando no hay capacidad de peso");
        }
    }

    // Métodos auxiliares

    private ZonaAlmacenaje crearZona(CategoriaZona categoria) {
        return ZonaAlmacenaje.builder()
            .id(UUID.randomUUID())
            .nombre("Zona Test")
            .categoria(categoria)
            .capacidadMaxKg(BigDecimal.valueOf(1000))
            .capacidadMaxM3(BigDecimal.valueOf(100))
            .capacidadMaxPaquetes(100)
            .pesoActualKg(BigDecimal.ZERO)
            .volumenActualM3(BigDecimal.ZERO)
            .contadorPaquetes(0)
            .estado(EstadoZona.DISPONIBLE)
            .build();
    }

    private Paquete crearPaquete(TipoMercancia tipo, Double pesoKg, Double volumenM3) {
        return Paquete.builder()
            .id(UUID.randomUUID())
            .tipoMercancia(tipo)
            .peso(new Peso(pesoKg))
            .volumenM3(volumenM3)
            .build();
    }
}
