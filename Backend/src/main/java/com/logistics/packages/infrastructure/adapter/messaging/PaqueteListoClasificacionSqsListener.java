package com.logistics.packages.infrastructure.adapter.messaging;

import com.logistics.packages.application.repository.PaqueteRepository;
import com.logistics.packages.application.usecase.ClasificarPaqueteUseCase;
import com.logistics.packages.domain.valueobject.EstadoGps;
import io.awspring.cloud.sqs.annotation.SqsListener;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.UUID;

/**
 * Listener SQS para procesar paquetes listos para clasificación.
 * MOD1-IP-005: B2 - Blindado contra paquetes sin coordenadas asignadas
 */
@Slf4j
@Component
@Profile({"default", "local", "aws"})
@RequiredArgsConstructor
public class PaqueteListoClasificacionSqsListener {

    private final ClasificarPaqueteUseCase clasificarPaqueteUseCase;
    private final PaqueteRepository paqueteRepository;

    @SqsListener("${aws.sqs.clasificacion-queue:paquete-listo-clasificar-queue}")
    public void procesarPaqueteListoParaClasificacion(Map<String, String> payload) {
        UUID paqueteId = UUID.fromString(payload.get("paqueteId"));
        log.info("Evento recibido: paquete listo para clasificación - {}", paqueteId);

        try {
            // B2: Validación previa: verificar que el paquete tenga coordenadas resueltas
            var paquete = paqueteRepository.findById(paqueteId)
                    .orElseThrow(() -> new IllegalArgumentException("Paquete no encontrado: " + paqueteId));
            
            if (paquete.getEstadoGps() != EstadoGps.RESUELTO) {
                log.warn("Paquete {} no tiene coordenadas resueltas. Estado GPS: {}. Evento será rechazado.",
                        paqueteId, paquete.getEstadoGps());
                throw new IllegalStateException(
                    "El paquete " + paqueteId + " aún no tiene coordenadas geográficas resueltas. " +
                    "No es posible calcular la zona de destino hasta que se complete el geocodificador."
                );
            }
            
            // Proceder con la sugerencia de clasificación
            var sugerencia = clasificarPaqueteUseCase.sugerirZonaParaPaquete(paqueteId);
            log.info("Zona sugerida: {} ({}) para paquete {}",
                    sugerencia.getNombreZona(),
                    sugerencia.getCodigoZona(),
                    paqueteId);
        } catch (Exception e) {
            log.error("Error procesando clasificación de paquete {}: {}", paqueteId, e.getMessage(), e);
            throw e;
        }
    }
}
