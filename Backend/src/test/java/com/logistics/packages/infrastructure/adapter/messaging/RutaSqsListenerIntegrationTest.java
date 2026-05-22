package com.logistics.packages.infrastructure.adapter.messaging;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.logistics.packages.application.repository.PaqueteRepository;
import com.logistics.packages.domain.model.Paquete;
import com.logistics.packages.domain.valueobject.EstadoPaquete;
import com.logistics.packages.infrastructure.dto.response.RespuestaRutaPayload;
import io.awspring.cloud.sqs.operations.SqsTemplate;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.CreateQueueRequest;
import software.amazon.awssdk.services.sqs.model.CreateQueueResponse;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test de integración para RutaSqsListener (T309)
 * Verifica que el listener consume mensajes de RUTA_ASIGNADA desde SQS,
 * invoca el use case de asignación y persiste correctamente en DB.
 *
 * Requiere: LocalStack con SQS, PostgreSQL, TestContainers
 */
@SpringBootTest
@ActiveProfiles("test")
@Import(TestcontainersConfigurationForIT.class)
@Tag("integration")
@DisplayName("RutaSqsListener Integration Test (T309)")
class RutaSqsListenerIntegrationTest {

    @Autowired
    private SqsTemplate sqsTemplate;

    @Autowired
    private PaqueteRepository paqueteRepository;

    @Autowired
    private SqsClient sqsClient;

    private String responseQueueUrl;
    private ObjectMapper objectMapper;
    private UUID paqueteId;
    private UUID rutaId;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        paqueteId = UUID.randomUUID();
        rutaId = UUID.randomUUID();

        // Crear la cola de respuestas en LocalStack
        CreateQueueResponse response = sqsClient.createQueue(
                CreateQueueRequest.builder()
                        .queueName("test-respuestas-ruta-queue")
                        .build()
        );
        responseQueueUrl = response.queueUrl();

        // Crear el paquete en la BD para que el listener pueda encontrarlo
        Paquete paquete = Paquete.builder()
                .id(paqueteId)
                .estado(EstadoPaquete.RECIBIDO_EN_SEDE)
                .build();
        paquete.prePersist();
        paqueteRepository.save(paquete);
    }

    @Test
    @DisplayName("T309: RutaSqsListener debe consumir RUTA_ASIGNADA y asignar la ruta al paquete")
    void debeConsumirRutaAsignadaYPersistir() {
        // Given: Un mensaje RUTA_ASIGNADA en la cola
        RespuestaRutaPayload respuesta = RespuestaRutaPayload.builder()
                .tipoEvento("RUTA_ASIGNADA")
                .paqueteId(paqueteId)
                .rutaId(rutaId)
                .fechaHoraEvento(OffsetDateTime.now(ZoneOffset.UTC))
                .build();

        // When: Enviar el mensaje a la cola de respuestas
        sqsTemplate.send(to -> to.queue("test-respuestas-ruta-queue").payload(respuesta));

        // Then: Esperar a que el listener procese el mensaje
        Awaitility.await()
                .atMost(5, TimeUnit.SECONDS)
                .pollInterval(100, TimeUnit.MILLISECONDS)
                .untilAsserted(() -> {
                    Optional<Paquete> paqueteActualizado = paqueteRepository.findById(paqueteId);
                    assertTrue(paqueteActualizado.isPresent(), "El paquete debe existir en BD");

                    Paquete paquete = paqueteActualizado.get();
                    assertEquals(rutaId, paquete.getRutaId(), "La ruta debe estar asignada");
                    assertEquals(EstadoPaquete.LISTO_PARA_DESPACHO, paquete.getEstado(),
                            "El estado debe cambiar a LISTO_PARA_DESPACHO");
                });
    }

    @Test
    @DisplayName("T309: RutaSqsListener debe ignorar mensajes sin rutaId")
    void debeIgnorarMensajesSinRutaId() {
        // Given: Un mensaje RUTA_ASIGNADA sin ruta_id
        RespuestaRutaPayload respuesta = RespuestaRutaPayload.builder()
                .tipoEvento("RUTA_ASIGNADA")
                .paqueteId(paqueteId)
                .rutaId(null)  // Sin ruta ID
                .fechaHoraEvento(OffsetDateTime.now(ZoneOffset.UTC))
                .build();

        // When: Enviar el mensaje
        sqsTemplate.send(to -> to.queue("test-respuestas-ruta-queue").payload(respuesta));

        // Then: El paquete no debe tener ruta asignada
        Awaitility.await()
                .atMost(3, TimeUnit.SECONDS)
                .pollInterval(100, TimeUnit.MILLISECONDS)
                .untilAsserted(() -> {
                    Optional<Paquete> paquete = paqueteRepository.findById(paqueteId);
                    assertTrue(paquete.isPresent());
                    assertNull(paquete.get().getRutaId(), "La ruta no debe asignarse sin ruta_id");
                });
    }

    @Test
    @DisplayName("T309: RutaSqsListener debe ignorar tipos de evento inesperados")
    void debeIgnorarTiposEventoIninesperados() {
        // Given: Un mensaje con tipo de evento incorrecto
        RespuestaRutaPayload respuesta = RespuestaRutaPayload.builder()
                .tipoEvento("RUTA_RECHAZADA")  // Tipo no esperado
                .paqueteId(paqueteId)
                .rutaId(rutaId)
                .fechaHoraEvento(OffsetDateTime.now(ZoneOffset.UTC))
                .build();

        // When: Enviar el mensaje
        sqsTemplate.send(to -> to.queue("test-respuestas-ruta-queue").payload(respuesta));

        // Then: El paquete no debe tener ruta asignada
        Awaitility.await()
                .atMost(3, TimeUnit.SECONDS)
                .pollInterval(100, TimeUnit.MILLISECONDS)
                .untilAsserted(() -> {
                    Optional<Paquete> paquete = paqueteRepository.findById(paqueteId);
                    assertTrue(paquete.isPresent());
                    assertNull(paquete.get().getRutaId(), "La ruta no debe asignarse con tipo de evento incorrecto");
                });
    }

    @Test
    @DisplayName("T309: RutaSqsListener debe manejar paquetes inexistentes sin fallar")
    void debeHandlearPaqueteInexistente() {
        // Given: Un mensaje para un paquete que no existe
        UUID paqueteInexistente = UUID.randomUUID();
        RespuestaRutaPayload respuesta = RespuestaRutaPayload.builder()
                .tipoEvento("RUTA_ASIGNADA")
                .paqueteId(paqueteInexistente)
                .rutaId(rutaId)
                .fechaHoraEvento(OffsetDateTime.now(ZoneOffset.UTC))
                .build();

        // When: Enviar el mensaje (no debe lanzar excepción)
        assertDoesNotThrow(() -> {
            sqsTemplate.send(to -> to.queue("test-respuestas-ruta-queue").payload(respuesta));
            Thread.sleep(1000);  // Dar tiempo al listener
        });

        // Then: El paquete original debe seguir sin ruta
        Optional<Paquete> paquete = paqueteRepository.findById(paqueteId);
        assertTrue(paquete.isPresent());
        assertNull(paquete.get().getRutaId());
    }
}
