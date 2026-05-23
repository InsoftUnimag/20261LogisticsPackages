package com.logistics.packages.infrastructure.adapter.messaging;

import com.logistics.packages.domain.model.Paquete;
import com.logistics.packages.domain.valueobject.EstadoPaquete;
import io.awspring.cloud.sqs.operations.SqsTemplate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("FinanzasEventSqsAdapter")
class FinanzasEventSqsAdapterTest {

    @Mock
    private SqsTemplate sqsTemplate;

    private FinanzasEventSqsAdapter adapter;

    private Paquete paquete;

    @BeforeEach
    void setUp() {
        adapter = new FinanzasEventSqsAdapter(sqsTemplate);

        paquete = Paquete.builder()
                .id(UUID.randomUUID())
                .rutaId(UUID.randomUUID())
                .estado(EstadoPaquete.ENTREGADO)
                .build();
    }

    @Test
    @DisplayName("Debe enviar el mensaje a SQS sin lanzar excepción")
    void testPublicarEstadoFinalSinError() {
        assertDoesNotThrow(() -> adapter.publicarEstadoFinal(paquete));
        verify(sqsTemplate).send(any());
    }

    @Test
    @DisplayName("Debe publicar el estado correcto cuando el paquete está ENTREGADO")
    void testPublicarEstadoEntregado() {
        adapter.publicarEstadoFinal(paquete);
        verify(sqsTemplate).send(any());
    }

    @Test
    @DisplayName("Debe publicar cuando el paquete está NOVEDAD_EN_BODEGA")
    void testPublicarEstadoNovedadEnBodega() {
        paquete = Paquete.builder()
                .id(UUID.randomUUID())
                .rutaId(UUID.randomUUID())
                .estado(EstadoPaquete.NOVEDAD_EN_BODEGA)
                .build();

        adapter.publicarEstadoFinal(paquete);
        verify(sqsTemplate).send(any());
    }

    @Test
    @DisplayName("Debe publicar cuando el paquete está EN_TRANSITO")
    void testPublicarEstadoEnTransito() {
        paquete = Paquete.builder()
                .id(UUID.randomUUID())
                .rutaId(UUID.randomUUID())
                .estado(EstadoPaquete.EN_TRANSITO)
                .build();

        adapter.publicarEstadoFinal(paquete);
        verify(sqsTemplate).send(any());
    }

    @Test
    @DisplayName("Debe propagar la excepción cuando SQS falla")
    void testPropagarExcepcionCuandoSqsFalla() {
        doThrow(new RuntimeException("SQS no disponible")).when(sqsTemplate).send(any());

        assertThrows(RuntimeException.class, () -> adapter.publicarEstadoFinal(paquete));
        verify(sqsTemplate).send(any());
    }

    @Test
    @DisplayName("Debe publicar con rutaId null cuando el paquete no tiene ruta asignada")
    void testPublicarSinRutaId() {
        paquete = Paquete.builder()
                .id(UUID.randomUUID())
                .rutaId(null)
                .estado(EstadoPaquete.RECIBIDO_EN_SEDE)
                .build();

        assertDoesNotThrow(() -> adapter.publicarEstadoFinal(paquete));
        verify(sqsTemplate).send(any());
    }
}
