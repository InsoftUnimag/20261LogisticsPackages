package com.logistics.packages.infrastructure.adapter.messaging;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.logistics.packages.infrastructure.dto.request.SolicitudRutaPayload;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Serialización de SolicitudRutaPayload al contrato M2")
class SolicitudRutaPayloadSerializationTest {

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
    }

    @Test
    @DisplayName("Debe serializar payload completo al JSON exacto que espera M2")
    void debeSerializarPayloadCompleto() throws JsonProcessingException {
        SolicitudRutaPayload payload = SolicitudRutaPayload.builder()
                .tipoEvento("SOLICITAR_RUTA")
                .paqueteId(UUID.fromString("123e4567-e89b-12d3-a456-426614174000"))
                .pesoKg(25.5)
                .volumenM3(0.15)
                .direccion(SolicitudRutaPayload.DireccionDto.builder()
                        .direccion("Carrera 15 # 88-22")
                        .ciudad("Bogotá")
                        .pais("Colombia")
                        .build())
                .latitud(4.7110)
                .longitud(-74.0721)
                .fechaLimiteEntrega("2026-05-24T18:54:32Z")
                .tipoMercancia("ESTANDAR")
                .metodoPago("PREPAGO")
                .build();

        String json = objectMapper.writeValueAsString(payload);

        String expectedJson = "{" +
                "\"tipo_evento\":\"SOLICITAR_RUTA\"," +
                "\"paquete_id\":\"123e4567-e89b-12d3-a456-426614174000\"," +
                "\"peso_kg\":25.5," +
                "\"volumen_m3\":0.15," +
                "\"direccion\":{" +
                "\"direccion\":\"Carrera 15 # 88-22\"," +
                "\"ciudad\":\"Bogotá\"," +
                "\"pais\":\"Colombia\"" +
                "}," +
                "\"latitud\":4.7110," +
                "\"longitud\":-74.0721," +
                "\"fecha_limite_entrega\":\"2026-05-24T18:54:32Z\"," +
                "\"tipo_mercancia\":\"ESTANDAR\"," +
                "\"metodo_pago\":\"PREPAGO\"" +
                "}";

        assertEquals(objectMapper.readTree(expectedJson), objectMapper.readTree(json),
                "El JSON generado debe coincidir exactamente con el contrato M2");
    }

    @Test
    @DisplayName("Debe serializar con campos nulos correctamente")
    void debeSerializarCamposNulos() throws JsonProcessingException {
        SolicitudRutaPayload payload = SolicitudRutaPayload.builder()
                .tipoEvento("SOLICITAR_RUTA")
                .paqueteId(UUID.fromString("123e4567-e89b-12d3-a456-426614174000"))
                .build();

        String json = objectMapper.writeValueAsString(payload);

        assertTrue(json.contains("\"tipo_evento\":\"SOLICITAR_RUTA\""));
        assertTrue(json.contains("\"paquete_id\":\"123e4567-e89b-12d3-a456-426614174000\""));
        assertTrue(json.contains("\"peso_kg\":null"));
        assertTrue(json.contains("\"direccion\":null"));
        assertTrue(json.contains("\"fecha_limite_entrega\":null"));
        assertTrue(json.contains("\"metodo_pago\":null"));
    }

    @Test
    @DisplayName("TipoMercancia y MetodoPago deben usar valores exactos del enum en string")
    void debeUsarValoresDeEnumEnString() throws JsonProcessingException {
        SolicitudRutaPayload payload = SolicitudRutaPayload.builder()
                .tipoEvento("SOLICITAR_RUTA")
                .paqueteId(UUID.randomUUID())
                .tipoMercancia("PELIGROSO")
                .metodoPago("CONTRA_ENTREGA")
                .build();

        String json = objectMapper.writeValueAsString(payload);

        assertTrue(json.contains("\"tipo_mercancia\":\"PELIGROSO\""));
        assertTrue(json.contains("\"metodo_pago\":\"CONTRA_ENTREGA\""));
    }
}
