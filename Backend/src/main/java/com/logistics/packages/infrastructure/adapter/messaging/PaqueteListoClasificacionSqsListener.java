package com.logistics.packages.infrastructure.adapter.messaging;

import com.logistics.packages.application.usecase.ClasificarPaqueteUseCase;
import io.awspring.cloud.sqs.annotation.SqsListener;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaqueteListoClasificacionSqsListener {

    private final ClasificarPaqueteUseCase clasificarPaqueteUseCase;

    @SqsListener("${aws.sqs.clasificacion-queue:paquete-listo-clasificar-queue}")
    public void procesarPaqueteListoParaClasificacion(Map<String, String> payload) {
        UUID paqueteId = UUID.fromString(payload.get("paqueteId"));
        log.info("Evento recibido: paquete listo para clasificación - {}", paqueteId);

        try {
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
