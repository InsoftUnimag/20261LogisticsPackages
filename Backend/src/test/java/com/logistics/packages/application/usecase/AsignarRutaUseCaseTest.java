package com.logistics.packages.application.usecase;

import com.logistics.packages.application.ports.PaqueteRepository;
import com.logistics.packages.domain.exception.PaqueteNotFoundException;
import com.logistics.packages.domain.model.Paquete;
import com.logistics.packages.domain.valueobject.EstadoPaquete;
import com.logistics.packages.infrastructure.dto.response.RespuestaRutaPayload;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * T305 [P] [US3] - Tests unitarios para AsignarRutaUseCase
 * MOD1-IP-003 - Phase 2
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Asignar Ruta Use Case")
class AsignarRutaUseCaseTest {

    @Mock
    private PaqueteRepository paqueteRepository;

    @InjectMocks
    private AsignarRutaUseCase asignarRutaUseCase;

    private Paquete paquete;
    private UUID paqueteId;
    private UUID rutaId;

    @BeforeEach
    void setUp() {
        paqueteId = UUID.randomUUID();
        rutaId = UUID.randomUUID();
        
        paquete = Paquete.builder()
                .id(paqueteId)
                .estado(EstadoPaquete.RECIBIDO_EN_SEDE)
                .build();
        paquete.prePersist();
    }

    @Test
    @DisplayName("Debe asignar ruta correctamente cuando recibe respuesta exitosa")
    void debeAsignarRutaCorrectamente() {
        // Given: Una respuesta con un rutaId
        RespuestaRutaPayload respuesta = RespuestaRutaPayload.builder()
                .paqueteId(paqueteId)
                .rutaId(rutaId)
                .estado("asignada")
                .tiempoEstimadoDias(7)
                .build();
        
        when(paqueteRepository.buscarPorId(paqueteId)).thenReturn(Optional.of(paquete));
        when(paqueteRepository.guardar(any(Paquete.class))).thenReturn(paquete);

        // When: Se ejecuta el caso de uso
        asignarRutaUseCase.asignarRuta(respuesta);

        // Then: Se busca el paquete, se le asigna la ruta y se guarda
        verify(paqueteRepository).buscarPorId(paqueteId);
        
        ArgumentCaptor<Paquete> paqueteCaptor = ArgumentCaptor.forClass(Paquete.class);
        verify(paqueteRepository).guardar(paqueteCaptor.capture());
        
        Paquete paqueteGuardado = paqueteCaptor.getValue();
        assertEquals(rutaId, paqueteGuardado.getRutaId());
        assertEquals(EstadoPaquete.LISTO_PARA_DESPACHO, paqueteGuardado.getEstado());
    }

    @Test
    @DisplayName("Debe lanzar excepción cuando el paquete no existe")
    void debeLanzarExcepcionCuandoPaqueteNoExiste() {
        // Given: El paquete no existe
        UUID paqueteInexistente = UUID.randomUUID();
        RespuestaRutaPayload respuesta = RespuestaRutaPayload.builder()
                .paqueteId(paqueteInexistente)
                .rutaId(rutaId)
                .estado("asignada")
                .build();
        
        when(paqueteRepository.buscarPorId(paqueteInexistente)).thenReturn(Optional.empty());

        // When & Then: Debe lanzar excepción
        assertThrows(PaqueteNotFoundException.class, () -> asignarRutaUseCase.asignarRuta(respuesta));
        
        verify(paqueteRepository).buscarPorId(paqueteInexistente);
        verify(paqueteRepository, never()).guardar(any());
    }

    @Test
    @DisplayName("Debe manejar respuesta pendiente sin asignar ruta")
    void debeOmitirAsignacionSiEstadoPendiente() {
        // Given: Una respuesta con estado "pendiente" (sin rutaId)
        RespuestaRutaPayload respuesta = RespuestaRutaPayload.builder()
                .paqueteId(paqueteId)
                .rutaId(null)
                .estado("pendiente")
                .mensaje("Esperando disponibilidad de vehículos")
                .build();
        
        when(paqueteRepository.buscarPorId(paqueteId)).thenReturn(Optional.of(paquete));

        // When: Se ejecuta el caso de uso
        asignarRutaUseCase.asignarRuta(respuesta);

        // Then: Se busca el paquete pero no se asigna ruta ni se guarda
        verify(paqueteRepository).buscarPorId(paqueteId);
        verify(paqueteRepository, never()).guardar(any());
        assertNull(paquete.getRutaId());
    }

    @Test
    @DisplayName("Debe lanzar excepción si el paquete ya tiene ruta asignada")
    void debeLanzarExcepcionSiYaTieneRuta() {
        // Given: Un paquete que ya tiene ruta asignada
        UUID primeraRuta = UUID.randomUUID();
        paquete.asignarRuta(primeraRuta);
        
        RespuestaRutaPayload respuesta = RespuestaRutaPayload.builder()
                .paqueteId(paqueteId)
                .rutaId(rutaId)
                .estado("asignada")
                .build();
        
        when(paqueteRepository.buscarPorId(paqueteId)).thenReturn(Optional.of(paquete));

        // When & Then: Debe lanzar excepción por intento de reasignación
        assertThrows(IllegalStateException.class, () -> asignarRutaUseCase.asignarRuta(respuesta));
        
        verify(paqueteRepository).buscarPorId(paqueteId);
        verify(paqueteRepository, never()).guardar(any());
        assertEquals(primeraRuta, paquete.getRutaId()); // Mantiene la primera ruta
    }
}
