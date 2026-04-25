package com.logistics.packages.infrastructure.adapter.notification;

import com.logistics.packages.application.ports.NotificacionPort;
import com.logistics.packages.domain.model.Notificacion;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Adaptador Mock para el envío de notificaciones.
 * MOD1-UC-007: FR-002 - Implementación de envío de notificaciones.
 * 
 * Este es un adaptador de demostración que simula el envío de notificaciones.
 * En producción, se debe reemplazar por una implementación real que integre
 * con servicios como AWS SNS, Twilio, SendGrid, etc.
 * 
 * Principio aplicado: Dependency Inversion
 */
@Component
@Slf4j
public class MockNotificacionAdapter implements NotificacionPort {
    
    @Override
    public Notificacion enviarSms(String telefono, String mensaje) {
        log.info("===== MOCK SMS =====");
        log.info("Destinatario: {}", telefono);
        log.info("Mensaje: {}", mensaje);
        log.info("====================");
        
        Notificacion notificacion = Notificacion.builder()
                .id(UUID.randomUUID())
                .destinatario(telefono)
                .tipo(Notificacion.TipoNotificacion.SMS)
                .mensaje(mensaje)
                .estado(Notificacion.EstadoNotificacion.ENVIADO)
                .build();
        
        notificacion.marcarComoEnviada();
        return notificacion;
    }
    
    @Override
    public Notificacion enviarEmail(String email, String asunto, String mensaje) {
        log.info("===== MOCK EMAIL =====");
        log.info("Destinatario: {}", email);
        log.info("Asunto: {}", asunto);
        log.info("Mensaje: {}", mensaje);
        log.info("======================");
        
        Notificacion notificacion = Notificacion.builder()
                .id(UUID.randomUUID())
                .destinatario(email)
                .tipo(Notificacion.TipoNotificacion.EMAIL)
                .mensaje(asunto + "\n\n" + mensaje)
                .estado(Notificacion.EstadoNotificacion.ENVIADO)
                .build();
        
        notificacion.marcarComoEnviada();
        return notificacion;
    }
    
    @Override
    public Notificacion enviar(Notificacion notificacion) {
        if (notificacion.getTipo() == Notificacion.TipoNotificacion.SMS) {
            return enviarSms(notificacion.getDestinatario(), notificacion.getMensaje());
        } else {
            return enviarEmail(notificacion.getDestinatario(), "Notificación", notificacion.getMensaje());
        }
    }
}
