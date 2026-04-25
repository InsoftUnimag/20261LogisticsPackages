package com.logistics.packages.domain.model;

import com.logistics.packages.domain.exception.EstadoTransicionInvalidaException;
import com.logistics.packages.domain.exception.EvidenciaRequeridaException;
import com.logistics.packages.domain.valueobject.EstadoPaquete;
import com.logistics.packages.domain.valueobject.TipoNovedad;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests unitarios para el método registrarNovedad de la entidad Paquete.
 * MOD1-UC-006: T604 - Validación de reglas de negocio para novedades.
 */
@DisplayName("Paquete - Registro de Novedades")
class PaqueteNovedadTest {

    private Paquete paquete;
    private UUID usuarioId;

    @BeforeEach
    void setUp() {
        paquete = Paquete.builder()
            .id(UUID.randomUUID())
            .estado(EstadoPaquete.RECIBIDO_EN_SEDE)
            .build();
        usuarioId = UUID.randomUUID();
    }

    @Test
    @DisplayName("Debe lanzar EstadoTransicionInvalidaException cuando el paquete está EN_TRANSITO")
    void registrarNovedad_PaqueteEnTransito_DebeRechazar() {
        // Given
        paquete.setEstado(EstadoPaquete.EN_TRANSITO);
        
        // When & Then
        EstadoTransicionInvalidaException exception = assertThrows(
            EstadoTransicionInvalidaException.class,
            () -> paquete.registrarNovedad(TipoNovedad.DAÑADO, "Test", usuarioId, "http://evidencia.com/foto.jpg")
        );
        
        assertTrue(exception.getMessage().contains("EN_TRANSITO"));
        assertEquals(paquete.getId(), exception.getPaqueteId());
    }

    @Test
    @DisplayName("Debe lanzar EstadoTransicionInvalidaException cuando el paquete está ENTREGADO")
    void registrarNovedad_PaqueteEntregado_DebeRechazar() {
        // Given
        paquete.setEstado(EstadoPaquete.ENTREGADO);
        
        // When & Then
        assertThrows(
            EstadoTransicionInvalidaException.class,
            () -> paquete.registrarNovedad(TipoNovedad.EXTRAVIADO, "Test", usuarioId, null)
        );
    }

    @Test
    @DisplayName("Debe lanzar EvidenciaRequeridaException cuando tipo DAÑADO sin URL de evidencia")
    void registrarNovedad_DañadoSinEvidencia_DebeRechazar() {
        // When & Then
        EvidenciaRequeridaException exception = assertThrows(
            EvidenciaRequeridaException.class,
            () -> paquete.registrarNovedad(TipoNovedad.DAÑADO, "Paquete dañado", usuarioId, null)
        );
        
        assertTrue(exception.getMessage().contains("obligatoria"));
        assertEquals(paquete.getId(), exception.getPaqueteId());
    }

    @Test
    @DisplayName("Debe lanzar EvidenciaRequeridaException cuando tipo DAÑADO con URL vacía")
    void registrarNovedad_DañadoConEvidenciaVacia_DebeRechazar() {
        // When & Then
        assertThrows(
            EvidenciaRequeridaException.class,
            () -> paquete.registrarNovedad(TipoNovedad.DAÑADO, "Paquete dañado", usuarioId, "")
        );
    }

    @Test
    @DisplayName("Debe actualizar estado a NOVEDAD_EN_BODEGA cuando la transición es válida")
    void registrarNovedad_TransicionValida_DebeActualizarEstado() {
        // Given
        EstadoPaquete estadoOriginal = paquete.getEstado();
        String urlEvidencia = "https://s3.amazonaws.com/evidencia/foto.jpg";
        
        // When
        HistorialEstado historial = paquete.registrarNovedad(
            TipoNovedad.DAÑADO, 
            "Caja dañada por caída", 
            usuarioId, 
            urlEvidencia
        );
        
        // Then
        assertEquals(EstadoPaquete.NOVEDAD_EN_BODEGA, paquete.getEstado());
        assertNotNull(historial);
        assertEquals(estadoOriginal, historial.getEstadoAnterior());
        assertEquals(EstadoPaquete.NOVEDAD_EN_BODEGA, historial.getEstadoNuevo());
    }

    @Test
    @DisplayName("Debe crear HistorialEstado correctamente para novedad tipo DAÑADO")
    void registrarNovedad_TipoDañado_DebeCrearHistorialCompleto() {
        // Given
        String observaciones = "Paquete con rotura en esquina superior";
        String urlEvidencia = "https://s3.amazonaws.com/evidencia/paquete-123.jpg";
        
        // When
        HistorialEstado historial = paquete.registrarNovedad(
            TipoNovedad.DAÑADO, 
            observaciones, 
            usuarioId, 
            urlEvidencia
        );
        
        // Then
        assertNotNull(historial.getId());
        assertEquals(paquete.getId(), historial.getPaqueteId());
        assertEquals(EstadoPaquete.RECIBIDO_EN_SEDE, historial.getEstadoAnterior());
        assertEquals(EstadoPaquete.NOVEDAD_EN_BODEGA, historial.getEstadoNuevo());
        assertEquals(observaciones, historial.getObservaciones());
        assertEquals(usuarioId, historial.getUsuarioId());
        assertEquals(urlEvidencia, historial.getUrlEvidencia());
        assertNotNull(historial.getFechaTransicionUtc());
    }

    @Test
    @DisplayName("Debe permitir novedad tipo EXTRAVIADO sin evidencia")
    void registrarNovedad_TipoExtraviado_NoRequiereEvidencia() {
        // When
        HistorialEstado historial = paquete.registrarNovedad(
            TipoNovedad.EXTRAVIADO, 
            "Paquete no encontrado en inventario", 
            usuarioId, 
            null
        );
        
        // Then
        assertNotNull(historial);
        assertNull(historial.getUrlEvidencia());
        assertEquals(EstadoPaquete.NOVEDAD_EN_BODEGA, paquete.getEstado());
    }

    @Test
    @DisplayName("Debe permitir registrar novedad desde estado EN_CLASIFICACION")
    void registrarNovedad_DesdeEnClasificacion_DebePermitir() {
        // Given
        paquete.setEstado(EstadoPaquete.EN_CLASIFICACION);
        
        // When
        HistorialEstado historial = paquete.registrarNovedad(
            TipoNovedad.EXTRAVIADO, 
            "Test", 
            usuarioId, 
            null
        );
        
        // Then
        assertNotNull(historial);
        assertEquals(EstadoPaquete.EN_CLASIFICACION, historial.getEstadoAnterior());
        assertEquals(EstadoPaquete.NOVEDAD_EN_BODEGA, paquete.getEstado());
    }
}
