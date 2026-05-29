package com.logistics.packages.application.usecase;

import com.logistics.packages.application.repository.PaqueteRepository;
import com.logistics.packages.application.ports.ZonaDestinoRepository;
import com.logistics.packages.application.repository.ArchivoStoragePort;
import com.logistics.packages.application.repository.HistorialEstadoRepository;
import com.logistics.packages.domain.exception.PaqueteNotFoundException;
import com.logistics.packages.domain.exception.ZonaDestinoNotFoundException;
import com.logistics.packages.domain.exception.ZonaDestinoSaturadaException;
import com.logistics.packages.domain.exception.ZonaNoAptaException;
import com.logistics.packages.domain.model.Paquete;
import com.logistics.packages.domain.model.ZonaDestino;
import com.logistics.packages.domain.service.CalculoZonaDestinoService;
import com.logistics.packages.domain.valueobject.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Tests unitarios para ClasificarPaqueteUseCase
 * MOD1-IP-005: T508, T509
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ClasificarPaqueteUseCase - Tests Unitarios")
class ClasificarPaqueteUseCaseTest {

    @Mock
    private PaqueteRepository paqueteRepository;

    @Mock
    private ZonaDestinoRepository zonaDestinoRepository;

    @Mock
    private CalculoZonaDestinoService calculoZonaService;

    @Mock
    private HistorialEstadoRepository historialEstadoRepository;

    @Mock
    private ArchivoStoragePort archivoStoragePort;

    private ClasificarPaqueteUseCase clasificarUseCase;

    private Paquete paquete;
    private ZonaDestino zonaDestino;

    @BeforeEach
    void setUp() {
        clasificarUseCase = new ClasificarPaqueteUseCase(
                paqueteRepository, 
                zonaDestinoRepository, 
                calculoZonaService,
                historialEstadoRepository,
                archivoStoragePort
        );

        Coordenadas coordenadas = new Coordenadas(4.70, -74.05);
        paquete = Paquete.builder()
                .id(UUID.randomUUID())
                .estado(EstadoPaquete.EN_CLASIFICACION)
                .coordenadas(coordenadas)
                .tipoMercancia(TipoMercancia.ESTANDAR)
                .build();

        zonaDestino = ZonaDestino.builder()
                .id(UUID.randomUUID())
                .nombre("Zona Norte")
                .codigo("ZD-NORTE")
                .categoria(CategoriaZona.NORMAL)
                .capacidadMaxPaquetes(100)
                .contadorPaquetes(50)
                .build();
    }

    @Test
    @DisplayName("[T508] Debe sugerir zona para paquete exitosamente")
    void debeSugerirZonaParaPaqueteExitosamente() {
        // Given
        when(paqueteRepository.findById(paquete.getId())).thenReturn(Optional.of(paquete));
        when(calculoZonaService.calcularZona(paquete)).thenReturn(zonaDestino);

        // When
        ClasificacionSugeridaResponse response = clasificarUseCase.sugerirZonaParaPaquete(paquete.getId());

        // Then
        assertNotNull(response);
        assertEquals(paquete.getId(), response.getPaqueteId());
        assertEquals(zonaDestino.getId(), response.getZonaDestinoId());
        assertEquals("Zona Norte", response.getNombreZona());
        
        verify(paqueteRepository).findById(paquete.getId());
        verify(calculoZonaService).calcularZona(paquete);
    }

    @Test
    @DisplayName("[T508] Debe lanzar excepción si el paquete no existe al sugerir")
    void debeLanzarExcepcionSiPaqueteNoExisteAlSugerir() {
        // Given
        UUID paqueteId = UUID.randomUUID();
        when(paqueteRepository.findById(paqueteId)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(PaqueteNotFoundException.class,
                () -> clasificarUseCase.sugerirZonaParaPaquete(paqueteId));
        
        verify(paqueteRepository).findById(paqueteId);
        verify(calculoZonaService, never()).calcularZona(any());
    }

    @Test
    @DisplayName("[T509] Debe confirmar clasificación exitosamente")
    void debeConfirmarClasificacionExitosamente() {
        // Given
        when(paqueteRepository.findById(paquete.getId())).thenReturn(Optional.of(paquete));
        when(zonaDestinoRepository.findById(zonaDestino.getId())).thenReturn(Optional.of(zonaDestino));
        when(paqueteRepository.save(any(Paquete.class))).thenReturn(paquete);

        // When
        clasificarUseCase.confirmarClasificacion(paquete.getId(), zonaDestino.getId(), null);

        // Then
        assertEquals(zonaDestino.getId(), paquete.getZonaDestinoId());
        assertEquals(EstadoPaquete.LISTO_PARA_DESPACHO, paquete.getEstado());
        assertEquals(51, zonaDestino.getContadorPaquetes());
        
        verify(paqueteRepository).findById(paquete.getId());
        verify(zonaDestinoRepository).findById(zonaDestino.getId());
        verify(paqueteRepository).save(paquete);
        verify(zonaDestinoRepository).save(zonaDestino);
    }

    @Test
    @DisplayName("[T509] Debe lanzar excepción si el paquete no existe al confirmar")
    void debeLanzarExcepcionSiPaqueteNoExisteAlConfirmar() {
        // Given
        UUID paqueteId = UUID.randomUUID();
        when(paqueteRepository.findById(paqueteId)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(PaqueteNotFoundException.class,
                () -> clasificarUseCase.confirmarClasificacion(paqueteId, zonaDestino.getId(), null));
        
        verify(paqueteRepository).findById(paqueteId);
        verify(zonaDestinoRepository, never()).findById(any());
    }

    @Test
    @DisplayName("[T509] Debe lanzar excepción si la zona no existe al confirmar")
    void debeLanzarExcepcionSiZonaNoExisteAlConfirmar() {
        // Given
        UUID zonaId = UUID.randomUUID();
        when(paqueteRepository.findById(paquete.getId())).thenReturn(Optional.of(paquete));
        when(zonaDestinoRepository.findById(zonaId)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(ZonaDestinoNotFoundException.class,
                () -> clasificarUseCase.confirmarClasificacion(paquete.getId(), zonaId, null));
        
        verify(paqueteRepository).findById(paquete.getId());
        verify(zonaDestinoRepository).findById(zonaId);
        verify(paqueteRepository, never()).save(any());
    }

    @Test
    @DisplayName("Debe lanzar excepción si la zona no es apta para el tipo de mercancía")
    void debeLanzarExcepcionSiZonaNoAptaParaTipoMercancia() {
        // Given
        paquete = Paquete.builder()
                .id(paquete.getId())
                .estado(EstadoPaquete.EN_CLASIFICACION)
                .coordenadas(paquete.getCoordenadas())
                .tipoMercancia(TipoMercancia.PELIGROSO)
                .build();
        zonaDestino.setCategoria(CategoriaZona.NORMAL); // NORMAL no puede con PELIGROSO

        when(paqueteRepository.findById(paquete.getId())).thenReturn(Optional.of(paquete));
        when(zonaDestinoRepository.findById(zonaDestino.getId())).thenReturn(Optional.of(zonaDestino));

        // When & Then
        assertThrows(ZonaNoAptaException.class,
                () -> clasificarUseCase.confirmarClasificacion(paquete.getId(), zonaDestino.getId(), null));
        
        verify(paqueteRepository).findById(paquete.getId());
        verify(zonaDestinoRepository).findById(zonaDestino.getId());
        verify(paqueteRepository, never()).save(any());
    }

    @Test
    @DisplayName("Debe lanzar excepción si la zona ha alcanzado su capacidad máxima")
    void debeLanzarExcepcionSiZonaSaturada() {
        // Given
        zonaDestino.setCapacidadMaxPaquetes(100);
        zonaDestino.setContadorPaquetes(100); // Saturada

        when(paqueteRepository.findById(paquete.getId())).thenReturn(Optional.of(paquete));
        when(zonaDestinoRepository.findById(zonaDestino.getId())).thenReturn(Optional.of(zonaDestino));

        // When & Then
        assertThrows(ZonaDestinoSaturadaException.class,
                () -> clasificarUseCase.confirmarClasificacion(paquete.getId(), zonaDestino.getId(), null));
        
        verify(paqueteRepository).findById(paquete.getId());
        verify(zonaDestinoRepository).findById(zonaDestino.getId());
        verify(paqueteRepository, never()).save(any());
    }
}
