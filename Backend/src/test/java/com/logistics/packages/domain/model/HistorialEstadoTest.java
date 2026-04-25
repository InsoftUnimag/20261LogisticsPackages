package com.logistics.packages.domain.model;

import com.logistics.packages.domain.valueobject.EstadoPaquete;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests unitarios para la entidad HistorialEstado.
 * MOD1-UC-006: T605 - Validación de la creación de registros de historial.
 */
@DisplayName("HistorialEstado - Creación y Validación")
class HistorialEstadoTest {

    @Test
    @DisplayName("Debe crear HistorialEstado con todos los campos correctamente asignados")
    void crearHistorialEstado_DatosCompletos_DebeFuncionar() {
        // Given
        UUID paqueteId = UUID.randomUUID();
        EstadoPaquete estadoAnterior = EstadoPaquete.RECIBIDO_EN_SEDE;
        EstadoPaquete estadoNuevo = EstadoPaquete.NOVEDAD_EN_BODEGA;
        String observaciones = "Paquete dañado durante clasificación";
        UUID usuarioId = UUID.randomUUID();
        String urlEvidencia = "https://s3.amazonaws.com/evidencia/123.jpg";
        
        // When
        HistorialEstado historial = new HistorialEstado(
            paqueteId, 
            estadoAnterior, 
            estadoNuevo, 
            observaciones, 
            usuarioId, 
            urlEvidencia
        );
        
        // Then
        assertNotNull(historial.getId(), "El ID debe ser generado automáticamente");
        assertEquals(paqueteId, historial.getPaqueteId());
        assertEquals(estadoAnterior, historial.getEstadoAnterior());
        assertEquals(estadoNuevo, historial.getEstadoNuevo());
        assertEquals(observaciones, historial.getObservaciones());
        assertEquals(usuarioId, historial.getUsuarioId());
        assertEquals(urlEvidencia, historial.getUrlEvidencia());
        assertNotNull(historial.getFechaTransicionUtc(), "La fecha debe ser generada automáticamente");
    }

    @Test
    @DisplayName("Debe crear HistorialEstado sin evidencia cuando no es requerida")
    void crearHistorialEstado_SinEvidencia_DebeFuncionar() {
        // Given
        UUID paqueteId = UUID.randomUUID();
        UUID usuarioId = UUID.randomUUID();
        
        // When
        HistorialEstado historial = new HistorialEstado(
            paqueteId,
            EstadoPaquete.EN_CLASIFICACION,
            EstadoPaquete.NOVEDAD_EN_BODEGA,
            "Paquete extraviado",
            usuarioId,
            null
        );
        
        // Then
        assertNotNull(historial);
        assertNull(historial.getUrlEvidencia());
        assertNotNull(historial.getId());
        assertNotNull(historial.getFechaTransicionUtc());
    }

    @Test
    @DisplayName("Debe registrar correctamente el usuario responsable de la transición")
    void crearHistorialEstado_UsuarioResponsable_DebeRegistrarse() {
        // Given
        UUID usuarioId = UUID.randomUUID();
        UUID paqueteId = UUID.randomUUID();
        
        // When
        HistorialEstado historial = new HistorialEstado(
            paqueteId,
            EstadoPaquete.RECIBIDO_EN_SEDE,
            EstadoPaquete.NOVEDAD_EN_BODEGA,
            "Test",
            usuarioId,
            null
        );
        
        // Then
        assertEquals(usuarioId, historial.getUsuarioId());
    }

    @Test
    @DisplayName("Debe generar timestamp UTC automáticamente al crear el historial")
    void crearHistorialEstado_FechaTransicion_DebeSerGeneradaAutomaticamente() {
        // Given
        UUID paqueteId = UUID.randomUUID();
        UUID usuarioId = UUID.randomUUID();
        
        // When
        HistorialEstado historial = new HistorialEstado(
            paqueteId,
            EstadoPaquete.RECIBIDO_EN_SEDE,
            EstadoPaquete.NOVEDAD_EN_BODEGA,
            "Test",
            usuarioId,
            null
        );
        
        // Then
        assertNotNull(historial.getFechaTransicionUtc());
    }
}
