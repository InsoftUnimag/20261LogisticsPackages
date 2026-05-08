package com.logistics.packages.infrastructure.adapter.messaging;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.logistics.packages.application.usecase.ClasificarPaqueteUseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.UUID;

/**
 * Listener para eventos de paquetes listos para clasificación por zona de destino.
 * MOD1-IP-005: T511 - Phase 4
 * 
 * Escucha la cola de paquetes listos para clasificar (publicada por MOD1-UC-004)
 * y sugiere automáticamente la zona de destino.
 * FR-009: Invocación automática de clasificación
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PaqueteListoClasificacionListener {

    private final ClasificarPaqueteUseCase clasificarPaqueteUseCase;
    private final ObjectMapper objectMapper;

    /**
     * Procesa eventos de paquetes listos para clasificación.
     * 
     * @param message El mensaje JSON con el ID del paquete
     */
    @RabbitListener(queues = "${app.messaging.clasificacion.queue:paquete_listo_para_clasificar_queue}")
    public void procesarPaqueteListoParaClasificacion(String message) {
        log.info("Evento recibido: paquete listo para clasificación - {}", message);
        
        try {
            // Deserializar el mensaje de forma segura usando TypeReference
            Map<String, String> payload = objectMapper.readValue(message, new TypeReference<Map<String, String>>() {});
            UUID paqueteId = UUID.fromString(payload.get("paqueteId"));
            
            log.info("Procesando clasificación automática para paquete: {}", paqueteId);
            
            // Calcular y sugerir la zona de destino
            var sugerencia = clasificarPaqueteUseCase.sugerirZonaParaPaquete(paqueteId);
            
            log.info("Zona sugerida: {} ({}) para paquete {}", 
                    sugerencia.getNombreZona(), 
                    sugerencia.getCodigoZona(), 
                    paqueteId);
            
            // La UI puede consultar esta sugerencia mediante el endpoint GET /api/clasificacion/sugerencia/{paqueteId}
            // o podría confirmarse automáticamente si el flujo no requiere intervención manual
            
        } catch (JsonProcessingException e) {
            log.error("Error deserializando evento de clasificación: {}", e.getMessage(), e);
            throw new RuntimeException("Error al deserializar evento de clasificación", e);
        } catch (Exception e) {
            log.error("Error procesando clasificación de paquete: {}", e.getMessage(), e);
            throw e;
        }
    }
}
