package com.logistics.packages.application.usecase;

import com.logistics.packages.application.repository.HistorialEstadoRepository;
import com.logistics.packages.application.repository.PaqueteRepository;
import com.logistics.packages.domain.exception.PaqueteNotFoundException;
import com.logistics.packages.domain.model.HistorialEstado;
import com.logistics.packages.domain.model.Paquete;
import com.logistics.packages.domain.valueobject.EstadoPaquete;
import com.logistics.packages.domain.valueobject.TipoNovedad;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Tests unitarios para ConsultarHistorialPaqueteUseCase.
 * MOD1-UC-007: Verificación de consulta del historial inmutable de paquetes.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ConsultarHistorialPaqueteUseCase")
class ConsultarHistorialPaqueteUseCaseTest {
    
    @Mock
    private PaqueteRepository paqueteRepository;
    
    @Mock
    private HistorialEstadoRepository historialEstadoRepository;
    
    @InjectMocks
    private ConsultarHistorialPaqueteUseCase consultarHistorialPaqueteUseCase;
    
    private UUID paqueteId;
    private Paquete paquete;
    
    @BeforeEach
    void setUp() {
        paqueteId = UUID.randomUUID();
        paquete = Paquete.builder()
                .id(paqueteId)
                .estado(EstadoPaquete.EN_TRANSITO)
                .build();
    }
    
    @Test
    @DisplayName("Retorna lista ordenada cronológicamente para paquete existente")
    void consultarHistorial_PaqueteExistente_RetornaListaOrdenada() {
        // Given
        UUID usuarioId = UUID.randomUUID();
        
        HistorialEstado h1 = HistorialEstado.builder()
                .id(UUID.randomUUID())
                .paqueteId(paqueteId)
                .estadoAnterior(EstadoPaquete.RECIBIDO_EN_SEDE)
                .estadoNuevo(EstadoPaquete.EN_CLASIFICACION)
                .observaciones("Ingreso en bodega")
                .usuarioId(usuarioId)
                .fechaTransicionUtc(LocalDateTime.now(ZoneOffset.UTC).minusHours(2))
                .build();
        
        HistorialEstado h2 = HistorialEstado.builder()
                .id(UUID.randomUUID())
                .paqueteId(paqueteId)
                .estadoAnterior(EstadoPaquete.EN_CLASIFICACION)
                .estadoNuevo(EstadoPaquete.LISTO_PARA_DESPACHO)
                .observaciones("Clasificación completada")
                .usuarioId(usuarioId)
                .fechaTransicionUtc(LocalDateTime.now(ZoneOffset.UTC).minusHours(1))
                .build();
        
        HistorialEstado h3 = HistorialEstado.builder()
                .id(UUID.randomUUID())
                .paqueteId(paqueteId)
                .estadoAnterior(EstadoPaquete.LISTO_PARA_DESPACHO)
                .estadoNuevo(EstadoPaquete.EN_TRANSITO)
                .observaciones("Ruta iniciada")
                .usuarioId(UUID.fromString("00000000-0000-0000-0000-000000000002"))
                .fechaTransicionUtc(LocalDateTime.now(ZoneOffset.UTC))
                .build();
        
        when(paqueteRepository.findById(paqueteId)).thenReturn(Optional.of(paquete));
        when(historialEstadoRepository.obtenerHistorialPorPaqueteId(paqueteId))
                .thenReturn(List.of(h1, h2, h3));
        
        // When
        List<HistorialEstado> resultado = consultarHistorialPaqueteUseCase.consultarHistorial(paqueteId);
        
        // Then
        assertNotNull(resultado);
        assertEquals(3, resultado.size());
        
        // Verificar orden cronológico
        assertTrue(resultado.get(0).getFechaTransicionUtc().isBefore(resultado.get(1).getFechaTransicionUtc()));
        assertTrue(resultado.get(1).getFechaTransicionUtc().isBefore(resultado.get(2).getFechaTransicionUtc()));
        
        // Verificar transiciones
        assertEquals(EstadoPaquete.EN_CLASIFICACION, resultado.get(0).getEstadoNuevo());
        assertEquals(EstadoPaquete.LISTO_PARA_DESPACHO, resultado.get(1).getEstadoNuevo());
        assertEquals(EstadoPaquete.EN_TRANSITO, resultado.get(2).getEstadoNuevo());
        
        verify(paqueteRepository).findById(paqueteId);
        verify(historialEstadoRepository).obtenerHistorialPorPaqueteId(paqueteId);
    }
    
    @Test
    @DisplayName("Retorna lista vacía cuando no hay historial para paquete válido")
    void consultarHistorial_PaqueteExistenteSinHistorial_RetornaListaVacia() {
        // Given
        when(paqueteRepository.findById(paqueteId)).thenReturn(Optional.of(paquete));
        when(historialEstadoRepository.obtenerHistorialPorPaqueteId(paqueteId))
                .thenReturn(List.of());
        
        // When
        List<HistorialEstado> resultado = consultarHistorialPaqueteUseCase.consultarHistorial(paqueteId);
        
        // Then
        assertNotNull(resultado);
        assertTrue(resultado.isEmpty());
        
        verify(paqueteRepository).findById(paqueteId);
        verify(historialEstadoRepository).obtenerHistorialPorPaqueteId(paqueteId);
    }
    
    @Test
    @DisplayName("Lanza PaqueteNotFoundException cuando el paquete no existe")
    void consultarHistorial_PaqueteNoExiste_LanzaExcepcion() {
        // Given
        when(paqueteRepository.findById(paqueteId)).thenReturn(Optional.empty());
        
        // When & Then
        assertThrows(PaqueteNotFoundException.class, () ->
                consultarHistorialPaqueteUseCase.consultarHistorial(paqueteId));
        
        // Verificar que no se consultó el historial si el paquete no existe
        verify(paqueteRepository).findById(paqueteId);
        verify(historialEstadoRepository, never()).obtenerHistorialPorPaqueteId(any());
    }
    
    @Test
    @DisplayName("Incluye registros con tipo novedad cuando aplica")
    void consultarHistorial_ConNovedades_IncluirTipoNovedad() {
        // Given
        UUID usuarioId = UUID.randomUUID();
        
        // Transición con novedad de tipo DAÑADO
        HistorialEstado hNovedad = HistorialEstado.builder()
                .id(UUID.randomUUID())
                .paqueteId(paqueteId)
                .estadoAnterior(EstadoPaquete.RECIBIDO_EN_SEDE)
                .estadoNuevo(EstadoPaquete.NOVEDAD_EN_BODEGA)
                .observaciones("DAÑADO - Paquete con rotura en esquina")
                .usuarioId(usuarioId)
                .urlEvidencia("https://s3.amazonaws.com/novedades/paquete-123.jpg")
                .tipoNovedad(TipoNovedad.DAÑADO)
                .fechaTransicionUtc(LocalDateTime.now(ZoneOffset.UTC))
                .build();
        
        when(paqueteRepository.findById(paqueteId)).thenReturn(Optional.of(paquete));
        when(historialEstadoRepository.obtenerHistorialPorPaqueteId(paqueteId))
                .thenReturn(List.of(hNovedad));
        
        // When
        List<HistorialEstado> resultado = consultarHistorialPaqueteUseCase.consultarHistorial(paqueteId);
        
        // Then
        assertNotNull(resultado);
        assertEquals(1, resultado.size());
        assertEquals(TipoNovedad.DAÑADO, resultado.get(0).getTipoNovedad());
        assertEquals("https://s3.amazonaws.com/novedades/paquete-123.jpg", 
                resultado.get(0).getUrlEvidencia());
        assertEquals(EstadoPaquete.NOVEDAD_EN_BODEGA, resultado.get(0).getEstadoNuevo());
    }
    
    @Test
    @DisplayName("Preserva la información completa del historial incluyendo timestamps")
    void consultarHistorial_PreservaInformacionCompleta() {
        // Given
        UUID usuarioId = UUID.randomUUID();
        LocalDateTime fechaTransicion = LocalDateTime.now(ZoneOffset.UTC);
        String observaciones = "Paquete en tránsito hacia destino final";
        
        HistorialEstado historial = HistorialEstado.builder()
                .id(UUID.randomUUID())
                .paqueteId(paqueteId)
                .estadoAnterior(EstadoPaquete.LISTO_PARA_DESPACHO)
                .estadoNuevo(EstadoPaquete.EN_TRANSITO)
                .observaciones(observaciones)
                .usuarioId(usuarioId)
                .fechaTransicionUtc(fechaTransicion)
                .build();
        
        when(paqueteRepository.findById(paqueteId)).thenReturn(Optional.of(paquete));
        when(historialEstadoRepository.obtenerHistorialPorPaqueteId(paqueteId))
                .thenReturn(List.of(historial));
        
        // When
        List<HistorialEstado> resultado = consultarHistorialPaqueteUseCase.consultarHistorial(paqueteId);
        
        // Then
        assertEquals(1, resultado.size());
        HistorialEstado retrieved = resultado.get(0);
        assertEquals(historial.getId(), retrieved.getId());
        assertEquals(paqueteId, retrieved.getPaqueteId());
        assertEquals(observaciones, retrieved.getObservaciones());
        assertEquals(usuarioId, retrieved.getUsuarioId());
        assertEquals(fechaTransicion, retrieved.getFechaTransicionUtc());
    }
}
