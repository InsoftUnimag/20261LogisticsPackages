package com.logistics.packages.application.usecase.novedad;

import com.logistics.packages.application.ports.EstadoPaqueteFinanzasPublisher;
import com.logistics.packages.application.repository.ArchivoStoragePort;
import com.logistics.packages.application.repository.HistorialEstadoRepository;
import com.logistics.packages.application.repository.NovedadEventPublisher;
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
import org.springframework.web.multipart.MultipartFile;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Tests unitarios para el caso de uso RegistrarNovedadUseCase.
 * MOD1-UC-006: T608, T609 - Validación del flujo completo de registro de novedad.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("RegistrarNovedadUseCase - Casos de Uso")
class RegistrarNovedadUseCaseTest {

    @Mock
    private PaqueteRepository paqueteRepository;

    @Mock
    private HistorialEstadoRepository historialRepository;

    @Mock
    private ArchivoStoragePort archivoStoragePort;

    @Mock
    private NovedadEventPublisher novedadEventPublisher;

    @Mock
    private EstadoPaqueteFinanzasPublisher estadoPaqueteFinanzasPublisher;

    @InjectMocks
    private RegistrarNovedadUseCase registrarNovedadUseCase;

    private UUID paqueteId;
    private UUID usuarioId;
    private Paquete paquete;
    private MultipartFile archivo;

    @BeforeEach
    void setUp() {
        paqueteId = UUID.randomUUID();
        usuarioId = UUID.randomUUID();
        
        paquete = Paquete.builder()
            .id(paqueteId)
            .estado(EstadoPaquete.RECIBIDO_EN_SEDE)
            .build();

        archivo = mock(MultipartFile.class);
    }

    @Test
    @DisplayName("T608 - Debe registrar novedad tipo DAÑADO exitosamente con evidencia")
    void registrarNovedad_TipoDañadoConEvidencia_DebeRegistrarExitosamente() {
        // Given
        String urlEvidencia = "https://s3.amazonaws.com/novedades/paquete-123.jpg";
        when(paqueteRepository.findById(paqueteId)).thenReturn(Optional.of(paquete));
        when(archivo.isEmpty()).thenReturn(false);
        when(archivoStoragePort.guardar(eq("novedades"), eq(paqueteId.toString()), eq(archivo)))
            .thenReturn(urlEvidencia);
        when(paqueteRepository.save(any(Paquete.class))).thenReturn(paquete);
        when(historialRepository.guardar(any(HistorialEstado.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));

        RegistrarNovedadCommand command = new RegistrarNovedadCommand(
            paqueteId,
            TipoNovedad.DAÑADO,
            "Paquete con rotura en esquina",
            usuarioId,
            archivo
        );

        // When
        RegistroNovedadResponse response = registrarNovedadUseCase.registrarNovedad(command);

        // Then
        assertNotNull(response);
        assertEquals(paqueteId, response.getPaqueteId());
        assertEquals(EstadoPaquete.NOVEDAD_EN_BODEGA, response.getEstadoActual());
        assertNotNull(response.getHistorialId());

        // Verify interactions
        verify(paqueteRepository).findById(paqueteId);
        verify(archivoStoragePort).guardar(eq("novedades"), eq(paqueteId.toString()), eq(archivo));
        verify(paqueteRepository).save(paquete);
        verify(historialRepository).guardar(any(HistorialEstado.class));
        verify(novedadEventPublisher).publicarNovedadRegistrada(eq(paqueteId), any(UUID.class));
        verify(estadoPaqueteFinanzasPublisher).publicarEstadoFinal(paquete);
    }

    @Test
    @DisplayName("T608 - Debe registrar novedad tipo EXTRAVIADO sin evidencia")
    void registrarNovedad_TipoExtraviadoSinEvidencia_DebeRegistrarExitosamente() {
        // Given
        when(paqueteRepository.findById(paqueteId)).thenReturn(Optional.of(paquete));
        when(paqueteRepository.save(any(Paquete.class))).thenReturn(paquete);
        when(historialRepository.guardar(any(HistorialEstado.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));

        RegistrarNovedadCommand command = new RegistrarNovedadCommand(
            paqueteId,
            TipoNovedad.EXTRAVIADO,
            "Paquete no encontrado en inventario",
            usuarioId,
            null
        );

        // When
        RegistroNovedadResponse response = registrarNovedadUseCase.registrarNovedad(command);

        // Then
        assertNotNull(response);
        assertEquals(paqueteId, response.getPaqueteId());
        assertEquals(EstadoPaquete.NOVEDAD_EN_BODEGA, response.getEstadoActual());

        // Verify NO se llamó al storage (no hay evidencia)
        verify(archivoStoragePort, never()).guardar(any(), any(), any());
        verify(paqueteRepository).save(paquete);
        verify(historialRepository).guardar(any(HistorialEstado.class));
        verify(novedadEventPublisher).publicarNovedadRegistrada(eq(paqueteId), any(UUID.class));
        verify(estadoPaqueteFinanzasPublisher).publicarEstadoFinal(paquete);
    }

    @Test
    @DisplayName("T609 - Debe lanzar excepción cuando el paquete no existe")
    void registrarNovedad_PaqueteNoExiste_DebeLanzarExcepcion() {
        // Given
        when(paqueteRepository.findById(paqueteId)).thenReturn(Optional.empty());

        RegistrarNovedadCommand command = new RegistrarNovedadCommand(
            paqueteId,
            TipoNovedad.DAÑADO,
            "Test",
            usuarioId,
            archivo
        );

        // When & Then
        assertThrows(PaqueteNotFoundException.class, 
            () -> registrarNovedadUseCase.registrarNovedad(command));

        // Verify no se ejecutaron operaciones posteriores
        verify(paqueteRepository).findById(paqueteId);
        verify(archivoStoragePort, never()).guardar(any(), any(), any());
        verify(paqueteRepository, never()).save(any());
        verify(historialRepository, never()).guardar(any());
        verify(novedadEventPublisher, never()).publicarNovedadRegistrada(any(), any());
        verify(estadoPaqueteFinanzasPublisher, never()).publicarEstadoFinal(any());
    }

    @Test
    @DisplayName("T609 - Debe manejar falla al guardar archivo en S3")
    void registrarNovedad_ErrorAlGuardarArchivo_DebeLanzarExcepcion() {
        // Given
        when(paqueteRepository.findById(paqueteId)).thenReturn(Optional.of(paquete));
        when(archivo.isEmpty()).thenReturn(false);
        when(archivoStoragePort.guardar(any(), any(), any()))
            .thenThrow(new RuntimeException("Error al almacenar en S3"));

        RegistrarNovedadCommand command = new RegistrarNovedadCommand(
            paqueteId,
            TipoNovedad.DAÑADO,
            "Test",
            usuarioId,
            archivo
        );

        // When & Then
        assertThrows(RuntimeException.class,
            () -> registrarNovedadUseCase.registrarNovedad(command));

        // Verify la transacción se debe revertir
        verify(paqueteRepository).findById(paqueteId);
        verify(archivoStoragePort).guardar(any(), any(), any());
        verify(paqueteRepository, never()).save(any());
        verify(historialRepository, never()).guardar(any());
        verify(novedadEventPublisher, never()).publicarNovedadRegistrada(any(), any());
        verify(estadoPaqueteFinanzasPublisher, never()).publicarEstadoFinal(any());
    }

    @Test
    @DisplayName("Debe publicar evento después de persistir exitosamente")
    void registrarNovedad_PersistenciaExitosa_DebePublicarEvento() {
        // Given
        when(paqueteRepository.findById(paqueteId)).thenReturn(Optional.of(paquete));
        when(paqueteRepository.save(any(Paquete.class))).thenReturn(paquete);
        when(historialRepository.guardar(any(HistorialEstado.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));

        RegistrarNovedadCommand command = new RegistrarNovedadCommand(
            paqueteId,
            TipoNovedad.EXTRAVIADO,
            "Test",
            usuarioId,
            null
        );

        // When
        registrarNovedadUseCase.registrarNovedad(command);

        // Then - Verificar orden de ejecución
        var inOrder = inOrder(paqueteRepository, historialRepository, novedadEventPublisher, estadoPaqueteFinanzasPublisher);
        inOrder.verify(paqueteRepository).findById(paqueteId);
        inOrder.verify(paqueteRepository).save(paquete);
        inOrder.verify(historialRepository).guardar(any(HistorialEstado.class));
        inOrder.verify(novedadEventPublisher).publicarNovedadRegistrada(eq(paqueteId), any(UUID.class));
        inOrder.verify(estadoPaqueteFinanzasPublisher).publicarEstadoFinal(paquete);
    }
}
