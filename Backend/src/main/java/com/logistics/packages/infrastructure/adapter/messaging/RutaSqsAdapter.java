package com.logistics.packages.infrastructure.adapter.messaging;

import com.logistics.packages.application.ports.RutaQueuePort;
import com.logistics.packages.domain.model.Paquete;
import com.logistics.packages.domain.valueobject.Direccion;
import com.logistics.packages.infrastructure.dto.request.SolicitudRutaPayload;
import com.logistics.packages.infrastructure.exception.SqsCommunicationException;
import io.awspring.cloud.sqs.operations.SqsTemplate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;

@Slf4j
@Component
@RequiredArgsConstructor
public class RutaSqsAdapter implements RutaQueuePort {

    private final SqsTemplate sqsTemplate;

    @Value("${aws.sqs.ruta-request-queue:solicitudes-ruta-queue}")
    private String queueName;

    @Override
    public void enviarSolicitud(Paquete paquete) {
        SolicitudRutaPayload payload = mapToPayload(paquete);
        try {
            log.info("Enviando solicitud de ruta para paquete: {}", paquete.getId());
            sqsTemplate.send(to -> to.queue(queueName).payload(payload));
            log.info("Solicitud de ruta enviada exitosamente a SQS: {}", queueName);
        } catch (Exception e) {
            log.error("Error enviando solicitud de ruta para paquete {}: {}",
                    paquete.getId(), e.getMessage(), e);
            throw new SqsCommunicationException("Error al enviar solicitud de ruta", e);
        }
    }

    private SolicitudRutaPayload mapToPayload(Paquete paquete) {
        return SolicitudRutaPayload.builder()
                .tipoEvento("SOLICITAR_RUTA")
                .paqueteId(paquete.getId())
                .pesoKg(paquete.getPeso() != null ? paquete.getPeso().getKilogramos() : null)
                .volumenM3(paquete.getVolumenM3())
                .direccion(mapDireccion(paquete.getDireccionDestino()))
                .latitud(paquete.getCoordenadas() != null ? paquete.getCoordenadas().latitud() : null)
                .longitud(paquete.getCoordenadas() != null ? paquete.getCoordenadas().longitud() : null)
                .fechaLimiteEntrega(calcularFechaLimite(paquete.getFechaIngresoUtc()))
                .tipoMercancia(paquete.getTipoMercancia())
                .metodoPago(paquete.getMetodoPago())
                .build();
    }

    private SolicitudRutaPayload.DireccionDto mapDireccion(Direccion direccion) {
        if (direccion == null) return null;
        return SolicitudRutaPayload.DireccionDto.builder()
                .direccion(direccion.getDireccion())
                .ciudad(direccion.getCiudad())
                .pais(direccion.getPais())
                .build();
    }

    private OffsetDateTime calcularFechaLimite(java.time.LocalDateTime fechaIngresoUtc) {
        if (fechaIngresoUtc == null) return null;
        return fechaIngresoUtc.plusDays(7).atOffset(ZoneOffset.UTC);
    }
}
