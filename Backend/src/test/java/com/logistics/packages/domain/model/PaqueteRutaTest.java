package com.logistics.packages.domain.model;

import com.logistics.packages.domain.exception.EstadoTransicionInvalidaException;
import com.logistics.packages.domain.exception.EvidenciaRequeridaException;
import com.logistics.packages.domain.valueobject.EstadoPaquete;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests unitarios para las transiciones de estado de ruta en Paquete.
 * MOD1-UC-007: T704 [P] - Test del dominio para transiciones de estado de ruta.
 */
@DisplayName("Paquete - Transiciones de Estado de Ruta")
class PaqueteRutaTest {
    
    private Paquete paquete;
    private UUID moduloId;
    
    @BeforeEach
    void setUp() {
        paquete = Paquete.builder()
                .id(UUID.randomUUID())
                .estado(EstadoPaquete.LISTO_PARA_DESPACHO)
                .build();
        
        moduloId = UUID.randomUUID();
    }
    
    @Test
    @DisplayName("transitarAEnRuta() cambia el estado a EN_TRANSITO y genera un HistorialEstado")
    void testTransitarAEnRuta() {
        // When
        HistorialEstado historial = paquete.transitarAEnRuta("Ruta iniciada", moduloId);
        
        // Then
        assertEquals(EstadoPaquete.EN_TRANSITO, paquete.getEstado());
        assertNotNull(historial);
        assertEquals(EstadoPaquete.LISTO_PARA_DESPACHO, historial.getEstadoAnterior());
        assertEquals(EstadoPaquete.EN_TRANSITO, historial.getEstadoNuevo());
        assertEquals("Ruta iniciada", historial.getObservaciones());
    }
    
    @Test
    @DisplayName("transitarAParadaDeEntrega() desde EN_TRANSITO cambia el estado correctamente")
    void testTransitarAParadaDeEntrega() {
        // Given
        paquete.setEstado(EstadoPaquete.EN_TRANSITO);
        
        // When
        HistorialEstado historial = paquete.transitarAParadaDeEntrega("Llegando al destino", moduloId);
        
        // Then
        assertEquals(EstadoPaquete.EN_PARADA_DE_ENTREGA, paquete.getEstado());
        assertNotNull(historial);
        assertEquals(EstadoPaquete.EN_TRANSITO, historial.getEstadoAnterior());
        assertEquals(EstadoPaquete.EN_PARADA_DE_ENTREGA, historial.getEstadoNuevo());
    }
    
    @Test
    @DisplayName("entregarPaquete() cambia el estado a ENTREGADO y asocia evidencia")
    void testEntregarPaquete() {
        // Given
        paquete.setEstado(EstadoPaquete.EN_PARADA_DE_ENTREGA);
        String urlEvidencia = "https://storage.com/pod/123.jpg";
        String nombreFirmante = "Juan Pérez";
        
        // When
        HistorialEstado historial = paquete.entregarPaquete(
                urlEvidencia, nombreFirmante, "Entrega exitosa", moduloId);
        
        // Then
        assertEquals(EstadoPaquete.ENTREGADO, paquete.getEstado());
        assertEquals(urlEvidencia, paquete.getUrlEvidenciaEntrega());
        assertEquals(nombreFirmante, paquete.getNombreFirmante());
        assertNotNull(paquete.getFechaEntregaUtc());
        assertNotNull(historial);
    }
    
    @Test
    @DisplayName("entregarPaquete() lanza excepción si no se proporciona evidencia")
    void testEntregarPaqueteSinEvidencia() {
        // Given
        paquete.setEstado(EstadoPaquete.EN_PARADA_DE_ENTREGA);
        
        // When & Then
        assertThrows(EvidenciaRequeridaException.class, () -> 
                paquete.entregarPaquete(null, "Juan Pérez", "Sin evidencia", moduloId));
    }
    
    @Test
    @DisplayName("No se puede transitar a un estado anterior desde ENTREGADO")
    void testNoRetrocederDesdeEntregado() {
        // Given
        paquete.setEstado(EstadoPaquete.ENTREGADO);
        
        // When & Then
        assertThrows(EstadoTransicionInvalidaException.class, () -> 
                paquete.transitarAEnRuta("Intento inválido", moduloId));
    }
    
    @Test
    @DisplayName("registrarDevolucionEnRuta() cambia el estado correctamente")
    void testRegistrarDevolucionEnRuta() {
        // Given
        paquete.setEstado(EstadoPaquete.EN_TRANSITO);
        String motivo = "Dirección incorrecta";
        
        // When
        HistorialEstado historial = paquete.registrarDevolucionEnRuta(motivo, moduloId);
        
        // Then
        assertEquals(EstadoPaquete.DEVOLUCION_EN_RUTA, paquete.getEstado());
        assertEquals(motivo, historial.getObservaciones());
    }
    
    @Test
    @DisplayName("registrarExtraviadoEnRuta() cambia el estado correctamente")
    void testRegistrarExtraviadoEnRuta() {
        // Given
        paquete.setEstado(EstadoPaquete.EN_TRANSITO);
        
        // When
        HistorialEstado historial = paquete.registrarExtraviadoEnRuta("Paquete extraviado", moduloId);
        
        // Then
        assertEquals(EstadoPaquete.EXTRAVIADO_EN_RUTA, paquete.getEstado());
        assertNotNull(historial);
    }
    
    @Test
    @DisplayName("registrarDañadoEnRuta() requiere evidencia obligatoria")
    void testRegistrarDañadoEnRutaConEvidencia() {
        // Given
        paquete.setEstado(EstadoPaquete.EN_TRANSITO);
        String urlEvidencia = "https://storage.com/damage/456.jpg";
        
        // When
        HistorialEstado historial = paquete.registrarDañadoEnRuta(
                "Caja aplastada", urlEvidencia, moduloId);
        
        // Then
        assertEquals(EstadoPaquete.DAÑADO_EN_RUTA, paquete.getEstado());
        assertEquals(urlEvidencia, historial.getUrlEvidencia());
    }
    
    @Test
    @DisplayName("registrarDañadoEnRuta() lanza excepción sin evidencia")
    void testRegistrarDañadoEnRutaSinEvidencia() {
        // Given
        paquete.setEstado(EstadoPaquete.EN_TRANSITO);
        
        // When & Then
        assertThrows(EvidenciaRequeridaException.class, () -> 
                paquete.registrarDañadoEnRuta("Daños", null, moduloId));
    }
}
