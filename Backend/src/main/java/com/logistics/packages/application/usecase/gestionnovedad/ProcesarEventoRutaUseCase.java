package com.logistics.packages.application.usecase.gestionnovedad;

import com.logistics.packages.application.ports.EstadoPaqueteFinanzasPublisher;
import com.logistics.packages.application.ports.EventoProcesadoRepository;
import com.logistics.packages.application.ports.NotificacionPort;
import com.logistics.packages.application.repository.HistorialEstadoRepository;
import com.logistics.packages.application.repository.PaqueteRepository;
import com.logistics.packages.domain.exception.EventoDuplicadoException;
import com.logistics.packages.domain.exception.PaqueteNotFoundException;
import com.logistics.packages.domain.model.EventoProcesado;
import com.logistics.packages.domain.model.HistorialEstado;
import com.logistics.packages.domain.model.Paquete;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Caso de uso para procesar eventos provenientes del Módulo de Gestión de Rutas.
 * MOD1-UC-007: FR-001, FR-002, FR-008 - Procesamiento de eventos asíncronos con idempotencia.
 * 
 * Este caso de uso orquesta:
 * - Validación de idempotencia
 * - Actualización del estado del paquete
 * - Registro en el historial
 * - Envío de notificaciones
 * 
 * Principios aplicados: Single Responsibility, Dependency Inversion
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ProcesarEventoRutaUseCase {
    
    private final PaqueteRepository paqueteRepository;
    private final HistorialEstadoRepository historialEstadoRepository;
    private final EventoProcesadoRepository eventoProcesadoRepository;
    private final NotificacionPort notificacionPort;
    private final EstadoPaqueteFinanzasPublisher estadoPaqueteFinanzasPublisher;
    
    // ID del módulo de rutas para el registro de historial
    private static final UUID MODULO_RUTAS_ID = UUID.fromString("00000000-0000-0000-0000-000000000002");
    
    /**
     * Procesa un evento de ruta de manera idempotente.
     * 
     * @param eventoDto DTO con la información del evento
     * @throws EventoDuplicadoException si el evento ya fue procesado
     * @throws PaqueteNotFoundException si el paquete no existe
     */
    @Transactional
    public void procesar(EventoRutaDto eventoDto) {
        log.info("Procesando evento de ruta: {} para paquete: {}", 
                eventoDto.getEventoId(), eventoDto.getPaqueteId());
        
        // FR-008: Verificar idempotencia
        if (eventoProcesadoRepository.yaFueProcesado(eventoDto.getEventoId())) {
            log.warn("Evento duplicado detectado: {}", eventoDto.getEventoId());
            throw new EventoDuplicadoException(eventoDto.getEventoId());
        }
        
        // Buscar el paquete
        Paquete paquete = paqueteRepository.findById(eventoDto.getPaqueteId())
                .orElseThrow(() -> new PaqueteNotFoundException(eventoDto.getPaqueteId()));
        
        // Procesar según el tipo de evento
        HistorialEstado historial = procesarSegunTipoEvento(paquete, eventoDto);
        
         // Guardar el paquete actualizado
         paqueteRepository.save(paquete);
         
         // Guardar el registro en el historial
         historialEstadoRepository.guardar(historial);
         
         // Marcar el evento como procesado
         EventoProcesado eventoProcesado = new EventoProcesado(
                 eventoDto.getEventoId(),
                 eventoDto.getPaqueteId(),
                 eventoDto.getTipoEvento().name()
         );
         eventoProcesadoRepository.guardar(eventoProcesado);
         
         // FR-002: Enviar notificaciones
         enviarNotificaciones(paquete, eventoDto);

         // Publicar estado actualizado a la cola de Finanzas (M3)
         // SOLO si el estado es definitivo (final)
         if (esEstadoFinal(paquete.getEstado())) {
             estadoPaqueteFinanzasPublisher.publicarEstadoFinal(paquete);
             log.info("Estado final publicado a M3 para cálculo de liquidación: {}", paquete.getEstado());
         }
         
         log.info("Evento procesado exitosamente: {}", eventoDto.getEventoId());
    }
    
    /**
     * Procesa el evento según su tipo y actualiza el paquete.
     * 
     * @param paquete El paquete a actualizar
     * @param eventoDto DTO con la información del evento
     * @return HistorialEstado Registro de la transición
     */
    private HistorialEstado procesarSegunTipoEvento(Paquete paquete, EventoRutaDto eventoDto) {
        switch (eventoDto.getTipoEvento()) {
            case EN_TRANSITO:
                return paquete.transitarAEnRuta(eventoDto.getObservaciones(), MODULO_RUTAS_ID);
                
            case EN_PARADA_DE_ENTREGA:
                return paquete.transitarAParadaDeEntrega(eventoDto.getObservaciones(), MODULO_RUTAS_ID);
                
            case ENTREGADO:
                return paquete.entregarPaquete(
                        eventoDto.getUrlEvidencia(),
                        eventoDto.getNombreFirmante(),
                        eventoDto.getObservaciones(),
                        MODULO_RUTAS_ID
                );
                
            case DEVOLUCION:
                return paquete.registrarDevolucionEnRuta(
                        eventoDto.getMotivo() != null ? eventoDto.getMotivo() : eventoDto.getObservaciones(),
                        MODULO_RUTAS_ID
                );
                
            case EXTRAVIADO:
                return paquete.registrarExtraviadoEnRuta(eventoDto.getObservaciones(), MODULO_RUTAS_ID);
                
            case DAÑADO:
                return paquete.registrarDañadoEnRuta(
                        eventoDto.getObservaciones(),
                        eventoDto.getUrlEvidencia(),
                        MODULO_RUTAS_ID
                );
                
            default:
                throw new IllegalArgumentException("Tipo de evento no soportado: " + eventoDto.getTipoEvento());
        }
    }
    
    /**
     * Envía notificaciones al remitente y destinatario sobre el cambio de estado.
     * Si el envío falla, se registra el error pero no se detiene el proceso.
     * 
     * @param paquete El paquete actualizado
     * @param eventoDto DTO con la información del evento
     */
    private void enviarNotificaciones(Paquete paquete, EventoRutaDto eventoDto) {
        try {
            String mensaje = construirMensajeNotificacion(paquete, eventoDto);
            
            // Notificar al remitente por SMS
            if (paquete.getRemitente() != null && paquete.getRemitente().getTelefono() != null) {
                try {
                    notificacionPort.enviarSms(paquete.getRemitente().getTelefono(), mensaje);
                    log.info("Notificación SMS enviada al remitente: {}", paquete.getRemitente().getTelefono());
                } catch (Exception e) {
                    log.error("Error al enviar SMS al remitente: {}", e.getMessage());
                }
            }
            
            // Notificar al destinatario por SMS y Email
            if (paquete.getDestinatario() != null) {
                if (paquete.getDestinatario().getTelefono() != null) {
                    try {
                        notificacionPort.enviarSms(paquete.getDestinatario().getTelefono(), mensaje);
                        log.info("Notificación SMS enviada al destinatario: {}", paquete.getDestinatario().getTelefono());
                    } catch (Exception e) {
                        log.error("Error al enviar SMS al destinatario: {}", e.getMessage());
                    }
                }
                
                if (paquete.getDestinatario().getCorreoElectronico() != null) {
                    try {
                        String asunto = "Actualización de estado de tu paquete";
                        notificacionPort.enviarEmail(paquete.getDestinatario().getCorreoElectronico(), asunto, mensaje);
                        log.info("Notificación Email enviada al destinatario: {}", paquete.getDestinatario().getCorreoElectronico());
                    } catch (Exception e) {
                        log.error("Error al enviar Email al destinatario: {}", e.getMessage());
                    }
                }
            }
        } catch (Exception e) {
            log.error("Error general al enviar notificaciones para paquete {}: {}", 
                    paquete.getId(), e.getMessage());
        }
    }
    
    /**
     * Construye el mensaje de notificación según el tipo de evento.
     * 
     * @param paquete El paquete
     * @param eventoDto DTO con la información del evento
     * @return Mensaje de notificación
     */
    private String construirMensajeNotificacion(Paquete paquete, EventoRutaDto eventoDto) {
        String idPaquete = paquete.getId().toString().substring(0, 8);
        
        switch (eventoDto.getTipoEvento()) {
            case EN_TRANSITO:
                return String.format("Su paquete %s está en tránsito. Pronto llegará a su destino.", idPaquete);
                
            case EN_PARADA_DE_ENTREGA:
                return String.format("El transportador está llegando con su paquete %s. Prepárese para recibirlo.", idPaquete);
                
            case ENTREGADO:
                return String.format("Su paquete %s ha sido entregado exitosamente. Recibido por: %s", 
                        idPaquete, eventoDto.getNombreFirmante());
                
            case DEVOLUCION:
                return String.format("Su paquete %s está en devolución. Motivo: %s", 
                        idPaquete, eventoDto.getMotivo() != null ? eventoDto.getMotivo() : "No especificado");
                
            case EXTRAVIADO:
                return String.format("Lamentamos informarle que su paquete %s ha sido reportado como extraviado. " +
                        "Nuestro equipo está trabajando para localizarlo.", idPaquete);
                
            case DAÑADO:
                return String.format("Su paquete %s presenta daños. Nuestro equipo se pondrá en contacto para resolver la situación.", 
                        idPaquete);
                
            default:
                return String.format("Su paquete %s ha sido actualizado.", idPaquete);
        }
    }
    
    /**
     * Determina si un estado del paquete es final (definitivo).
     * Los estados finales son aquellos donde el paquete ha completado su ciclo de vida.
     * 
     * @param estado El estado del paquete a evaluar
     * @return true si el estado es final, false en caso contrario
     */
    private boolean esEstadoFinal(com.logistics.packages.domain.valueobject.EstadoPaquete estado) {
        // Estados finales: paquete ha completado su ciclo de vida
        return estado == com.logistics.packages.domain.valueobject.EstadoPaquete.ENTREGADO ||
               estado == com.logistics.packages.domain.valueobject.EstadoPaquete.DAÑADO_EN_RUTA ||
               estado == com.logistics.packages.domain.valueobject.EstadoPaquete.EXTRAVIADO_EN_RUTA ||
               estado == com.logistics.packages.domain.valueobject.EstadoPaquete.DEVOLUCION_EN_RUTA ||
               estado == com.logistics.packages.domain.valueobject.EstadoPaquete.NOVEDAD_EN_BODEGA;
    }
}
