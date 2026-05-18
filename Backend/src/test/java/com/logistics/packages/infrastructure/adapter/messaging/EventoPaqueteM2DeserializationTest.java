package com.logistics.packages.infrastructure.adapter.messaging;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.logistics.packages.infrastructure.dto.event.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Deserialización Jackson de eventos M2 (contrato polimórfico)")
class EventoPaqueteM2DeserializationTest {

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
    }

    @Test
    @DisplayName("PAQUETE_EN_TRANSITO se deserializa al subtipo correcto")
    void testDeserializarPaqueteEnTransito() throws Exception {
        String json = """
                {
                    "tipo_evento": "PAQUETE_EN_TRANSITO",
                    "paquete_id": "123e4567-e89b-12d3-a456-426614174000",
                    "ruta_id": "223e4567-e89b-12d3-a456-426614174001",
                    "fecha_hora_evento": "2026-03-08T14:30:00Z"
                }
                """;

        EventoPaqueteM2Dto dto = objectMapper.readValue(json, EventoPaqueteM2Dto.class);

        assertInstanceOf(PaqueteEnTransitoEvento.class, dto);
        assertEquals("PAQUETE_EN_TRANSITO", dto.getTipoEvento());
        assertEquals(UUID.fromString("123e4567-e89b-12d3-a456-426614174000"), dto.getPaqueteId());
        assertEquals(UUID.fromString("223e4567-e89b-12d3-a456-426614174001"), dto.getRutaId());
    }

    @Test
    @DisplayName("PAQUETE_ENTREGADO con evidencia de POD (foto + firma)")
    void testDeserializarPaqueteEntregadoConPod() throws Exception {
        String json = """
                {
                    "tipo_evento": "PAQUETE_ENTREGADO",
                    "paquete_id": "123e4567-e89b-12d3-a456-426614174000",
                    "ruta_id": "223e4567-e89b-12d3-a456-426614174001",
                    "fecha_hora_evento": "2026-03-08T15:00:00Z",
                    "evidencia": {
                        "url_foto": "https://storage.com/pod/foto123.jpg",
                        "url_firma": "https://storage.com/pod/firma456.png"
                    }
                }
                """;

        EventoPaqueteM2Dto dto = objectMapper.readValue(json, EventoPaqueteM2Dto.class);

        assertInstanceOf(PaqueteEntregadoEvento.class, dto);
        PaqueteEntregadoEvento entregado = (PaqueteEntregadoEvento) dto;
        assertNotNull(entregado.getEvidencia());
        assertEquals("https://storage.com/pod/foto123.jpg", entregado.getEvidencia().getUrlFoto());
        assertEquals("https://storage.com/pod/firma456.png", entregado.getEvidencia().getUrlFirma());
    }

    @Test
    @DisplayName("PARADA_FALLIDA con cada motivo posible")
    void testDeserializarParadaFallida() throws Exception {
        for (ParadaFallidaEvento.MotivoParadaFallida motivo : ParadaFallidaEvento.MotivoParadaFallida.values()) {
            String json = String.format("""
                    {
                        "tipo_evento": "PARADA_FALLIDA",
                        "paquete_id": "123e4567-e89b-12d3-a456-426614174000",
                        "ruta_id": "223e4567-e89b-12d3-a456-426614174001",
                        "fecha_hora_evento": "2026-03-08T12:00:00Z",
                        "motivo": "%s"
                    }
                    """, motivo.name());

            EventoPaqueteM2Dto dto = objectMapper.readValue(json, EventoPaqueteM2Dto.class);

            assertInstanceOf(ParadaFallidaEvento.class, dto, "Fallo con motivo: " + motivo);
            assertEquals(motivo, ((ParadaFallidaEvento) dto).getMotivo());
        }
    }

    @Test
    @DisplayName("NOVEDAD_GRAVE muta a DAÑADO_EN_RUTA, EXTRAVIADO, DEVOLUCION en aplicación")
    void testDeserializarNovedadGrave() throws Exception {
        String json = """
                {
                    "tipo_evento": "NOVEDAD_GRAVE",
                    "paquete_id": "123e4567-e89b-12d3-a456-426614174000",
                    "ruta_id": "223e4567-e89b-12d3-a456-426614174001",
                    "fecha_hora_evento": "2026-03-08T16:00:00Z",
                    "tipo_novedad": "DAÑADO_EN_RUTA"
                }
                """;

        EventoPaqueteM2Dto dto = objectMapper.readValue(json, EventoPaqueteM2Dto.class);

        assertInstanceOf(NovedadGraveEvento.class, dto);
        assertEquals(NovedadGraveEvento.TipoNovedadGrave.DAÑADO_EN_RUTA, ((NovedadGraveEvento) dto).getTipoNovedad());
    }

    @Test
    @DisplayName("PARADAS_SIN_GESTIONAR con lista de paquetes")
    void testDeserializarParadasSinGestionar() throws Exception {
        String json = """
                {
                    "tipo_evento": "PARADAS_SIN_GESTIONAR",
                    "ruta_id": "223e4567-e89b-12d3-a456-426614174001",
                    "tipo_cierre": "AUTOMATICO",
                    "fecha_hora_evento": "2026-03-10T23:00:00Z",
                    "paquetes": [
                        {"paquete_id": "123e4567-e89b-12d3-a456-426614174000"},
                        {"paquete_id": "323e4567-e89b-12d3-a456-426614174002"}
                    ]
                }
                """;

        EventoPaqueteM2Dto dto = objectMapper.readValue(json, EventoPaqueteM2Dto.class);

        assertInstanceOf(ParadasSinGestionarEvento.class, dto);
        ParadasSinGestionarEvento paradas = (ParadasSinGestionarEvento) dto;
        assertEquals("AUTOMATICO", paradas.getTipoCierre());
        assertNotNull(paradas.getPaquetes());
        assertEquals(2, paradas.getPaquetes().size());
    }

    @Test
    @DisplayName("PAQUETE_EXCLUIDO_DESPACHO se deserializa correctamente")
    void testDeserializarPaqueteExcluidoDespacho() throws Exception {
        String json = """
                {
                    "tipo_evento": "PAQUETE_EXCLUIDO_DESPACHO",
                    "paquete_id": "123e4567-e89b-12d3-a456-426614174000",
                    "ruta_id": "223e4567-e89b-12d3-a456-426614174001",
                    "fecha_hora_evento": "2026-03-08T10:00:00Z"
                }
                """;

        EventoPaqueteM2Dto dto = objectMapper.readValue(json, EventoPaqueteM2Dto.class);

        assertInstanceOf(PaqueteExcluidoDespachoEvento.class, dto);
        assertEquals("PAQUETE_EXCLUIDO_DESPACHO", dto.getTipoEvento());
    }
}
