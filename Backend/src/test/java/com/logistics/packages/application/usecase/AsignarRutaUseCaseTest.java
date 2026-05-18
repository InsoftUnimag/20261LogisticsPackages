package com.logistics.packages.application.usecase;

import com.logistics.packages.application.repository.PaqueteRepository;
import com.logistics.packages.domain.exception.PaqueteNotFoundException;
import com.logistics.packages.domain.model.Paquete;
import com.logistics.packages.domain.valueobject.EstadoPaquete;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

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
    }

    @Test
    @DisplayName("Debe asignar ruta correctamente cuando recibe comando válido")
    void debeAsignarRutaCorrectamente() {
        AsignarRutaCommand command = new AsignarRutaCommand(
                paqueteId, rutaId, OffsetDateTime.now());

        when(paqueteRepository.findById(paqueteId)).thenReturn(Optional.of(paquete));
        when(paqueteRepository.save(any(Paquete.class))).thenReturn(paquete);

        asignarRutaUseCase.asignarRuta(command);

        verify(paqueteRepository).findById(paqueteId);

        ArgumentCaptor<Paquete> paqueteCaptor = ArgumentCaptor.forClass(Paquete.class);
        verify(paqueteRepository).save(paqueteCaptor.capture());

        Paquete paqueteGuardado = paqueteCaptor.getValue();
        assertEquals(rutaId, paqueteGuardado.getRutaId());
        assertEquals(EstadoPaquete.LISTO_PARA_DESPACHO, paqueteGuardado.getEstado());
    }

    @Test
    @DisplayName("Debe lanzar excepción cuando el paquete no existe")
    void debeLanzarExcepcionCuandoPaqueteNoExiste() {
        UUID paqueteInexistente = UUID.randomUUID();
        AsignarRutaCommand command = new AsignarRutaCommand(
                paqueteInexistente, rutaId, OffsetDateTime.now());

        when(paqueteRepository.findById(paqueteInexistente)).thenReturn(Optional.empty());

        assertThrows(PaqueteNotFoundException.class, () -> asignarRutaUseCase.asignarRuta(command));

        verify(paqueteRepository).findById(paqueteInexistente);
        verify(paqueteRepository, never()).save(any());
    }

    @Test
    @DisplayName("Debe lanzar excepción si el paquete ya tiene ruta asignada")
    void debeLanzarExcepcionSiYaTieneRuta() {
        UUID primeraRuta = UUID.randomUUID();
        paquete.asignarRuta(primeraRuta);

        AsignarRutaCommand command = new AsignarRutaCommand(
                paqueteId, rutaId, OffsetDateTime.now());

        when(paqueteRepository.findById(paqueteId)).thenReturn(Optional.of(paquete));

        assertThrows(IllegalStateException.class, () -> asignarRutaUseCase.asignarRuta(command));

        verify(paqueteRepository).findById(paqueteId);
        verify(paqueteRepository, never()).save(any());
        assertEquals(primeraRuta, paquete.getRutaId());
    }
}
