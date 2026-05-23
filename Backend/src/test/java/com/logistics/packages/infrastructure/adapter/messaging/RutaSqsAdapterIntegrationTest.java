package com.logistics.packages.infrastructure.adapter.messaging;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.logistics.packages.domain.model.Paquete;
import com.logistics.packages.domain.valueobject.*;
import com.logistics.packages.infrastructure.dto.request.SolicitudRutaPayload;
import io.awspring.cloud.sqs.operations.SqsTemplate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.messaging.Message;
import org.springframework.test.context.ActiveProfiles;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.CreateQueueRequest;
import software.amazon.awssdk.services.sqs.model.CreateQueueResponse;
import software.amazon.awssdk.services.sqs.model.ReceiveMessageRequest;
import software.amazon.awssdk.services.sqs.model.ReceiveMessageResponse;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test de integración para RutaSqsAdapter (T308)
 * Verifica que el payload se serializa correctamente al formato esperado por M2 (snake_case)
 * y se envía a la cola SQS correctamente.
 *
 * Requiere: LocalStack con SQS, TestContainers
 */
@SpringBootTest
@ActiveProfiles("test")
@Import(TestcontainersConfigurationForIT.class)
@Tag("integration")
@DisplayName("RutaSqsAdapter Integration Test (T308)")
class RutaSqsAdapterIntegrationTest {

    @Autowired
    private RutaSqsAdapter rutaSqsAdapter;

    @Autowired
    private SqsTemplate sqsTemplate;

    @Autowired
    private SqsClient sqsClient;

    private String testQueueUrl;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        // Crear la cola de prueba en LocalStack
        CreateQueueResponse response = sqsClient.createQueue(
                CreateQueueRequest.builder()
                        .queueName("test-solicitar-ruta-queue")
                        .build()
        );
        testQueueUrl = response.queueUrl();
    }

    @Test
    @DisplayName("T308: Debe serializar y enviar el payload con contrato snake_case de M2")
    void debeSerializarYEnviarPayloadSnakeCase() throws Exception {
        // Given: Un paquete con todos los datos requeridos
        UUID paqueteId = UUID.randomUUID();
        Paquete paquete = Paquete.builder()
                .id(paqueteId)
                .fechaIngresoUtc(LocalDateTime.now(ZoneOffset.UTC))
                .peso(new Peso(25.5))
                .dimensiones(new Dimensiones(100.0, 50.0, 30.0))
                .volumenM3(0.15)
                .direccionDestino(new Direccion("Carrera 15 # 88-22", "Bogotá", "Cundinamarca", "Colombia"))
                .coordenadas(new Coordenadas(4.7110, -74.0721))
                .tipoMercancia(TipoMercancia.ESTANDAR)
                .metodoPago(MetodoPago.PREPAGO)
                .build();

        // When: Enviar la solicitud de ruta
        rutaSqsAdapter.enviarSolicitud(paquete);

        // Then: Verificar que el mensaje llegó a la cola con los campos correctos
        Thread.sleep(1000); // Esperar a que el mensaje sea enviado

        ReceiveMessageResponse response = sqsClient.receiveMessage(
                ReceiveMessageRequest.builder()
                        .queueUrl(testQueueUrl)
                        .maxNumberOfMessages(1)
                        .build()
        );

        assertNotNull(response.messages(), "Debe haber recibido al menos un mensaje");
        assertFalse(response.messages().isEmpty(), "La cola no debe estar vacía");

        String messageBody = response.messages().get(0).body();
        SolicitudRutaPayload payload = objectMapper.readValue(messageBody, SolicitudRutaPayload.class);

        // Validar contrato snake_case de M2
        assertEquals("SOLICITAR_RUTA", payload.getTipoEvento());
        assertEquals(paqueteId, payload.getPaqueteId());
        assertEquals(25.5, payload.getPesoKg());
        assertEquals(0.15, payload.getVolumenM3());
        assertNotNull(payload.getDireccion());
        assertEquals("Carrera 15 # 88-22", payload.getDireccion().getDireccion());
        assertEquals("Bogotá", payload.getDireccion().getCiudad());
        assertEquals("Colombia", payload.getDireccion().getPais());
        assertEquals(4.7110, payload.getLatitud());
        assertEquals(-74.0721, payload.getLongitud());
        assertEquals("ESTANDAR", payload.getTipoMercancia());
        assertEquals("PREPAGO", payload.getMetodoPago());
        assertNotNull(payload.getFechaLimiteEntrega());

        // Validar que la fecha límite es 7 días después de la de ingreso
        assertEquals(
                paquete.getFechaIngresoUtc().plusDays(7).atOffset(ZoneOffset.UTC).toInstant(),
                payload.getFechaLimiteEntrega()
        );
    }

    @Test
    @DisplayName("T308: Debe manejar paquetes con datos opcionales nulos")
    void debeHandlearDatosOpcionalNulos() throws Exception {
        // Given: Un paquete con datos mínimos
        UUID paqueteId = UUID.randomUUID();
        Paquete paquete = Paquete.builder()
                .id(paqueteId)
                .fechaIngresoUtc(LocalDateTime.now(ZoneOffset.UTC))
                .estado(EstadoPaquete.RECIBIDO_EN_SEDE)
                .build();

        // When: Enviar la solicitud
        rutaSqsAdapter.enviarSolicitud(paquete);

        // Then: Verificar que el mensaje llegó
        Thread.sleep(1000);

        ReceiveMessageResponse response = sqsClient.receiveMessage(
                ReceiveMessageRequest.builder()
                        .queueUrl(testQueueUrl)
                        .maxNumberOfMessages(1)
                        .build()
        );

        String messageBody = response.messages().get(0).body();
        SolicitudRutaPayload payload = objectMapper.readValue(messageBody, SolicitudRutaPayload.class);

        assertEquals("SOLICITAR_RUTA", payload.getTipoEvento());
        assertEquals(paqueteId, payload.getPaqueteId());
        assertNull(payload.getPesoKg());
        assertNull(payload.getDireccion());
        assertNull(payload.getLatitud());
    }
}
