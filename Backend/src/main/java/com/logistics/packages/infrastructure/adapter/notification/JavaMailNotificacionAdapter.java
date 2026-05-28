package com.logistics.packages.infrastructure.adapter.notification;

import com.logistics.packages.application.ports.NotificacionPort;
import com.logistics.packages.domain.model.Notificacion;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

/**
 * Adaptador real para envío de notificaciones usando Gmail SMTP y JavaMailSender.
 * MOD1-UC-007: FR-002 - Envío real de emails vía Gmail.
 * 
 * Configuración requerida en application-local.yml:
 * spring:
 *   mail:
 *     host: smtp.gmail.com
 *     port: 587
 *     username: tu-email@gmail.com
 *     password: tu-app-password (NO contraseña de Google)
 *     properties:
 *       mail.smtp.starttls.enabled: true
 *       mail.smtp.starttls.required: true
 * 
 * NOTA: Para Gmail necesitas:
 * 1. Activar 2FA en tu cuenta de Google
 * 2. Generar una contraseña de aplicación (no es tu contraseña de Gmail)
 * 3. Ver: https://myaccount.google.com/apppasswords
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class JavaMailNotificacionAdapter implements NotificacionPort {
    
    private final JavaMailSender mailSender;
    
    @Value("${spring.mail.username:noreply@hermesexpress.com}")
    private String fromEmail;
    
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    
    @Override
    public Notificacion enviarSms(String telefono, String mensaje) {
        String timestamp = LocalDateTime.now().format(formatter);
        String id = UUID.randomUUID().toString();
        
        log.info("═══════════════════════════════════════════════════════════");
        log.info("📱 SMS SIMULADO [{}, {}]", timestamp, id);
        log.info("───────────────────────────────────────────────────────────");
        log.info("Destinatario: {}", telefono);
        log.info("Mensaje: {}", mensaje);
        log.info("NOTA: Para SMS real, integrar con Twilio o AWS SNS");
        log.info("═══════════════════════════════════════════════════════════");
        
        Notificacion notificacion = Notificacion.builder()
                .id(UUID.fromString(id))
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
        String timestamp = LocalDateTime.now().format(formatter);
        String id = UUID.randomUUID().toString();
        
        Notificacion notificacion = Notificacion.builder()
                .id(UUID.fromString(id))
                .destinatario(email)
                .tipo(Notificacion.TipoNotificacion.EMAIL)
                .mensaje(asunto + "\n\n" + mensaje)
                .estado(Notificacion.EstadoNotificacion.PENDIENTE)
                .build();
        
        try {
            SimpleMailMessage mailMessage = new SimpleMailMessage();
            mailMessage.setFrom(fromEmail);
            mailMessage.setTo(email);
            mailMessage.setSubject(asunto);
            mailMessage.setText(mensaje);
            
            mailSender.send(mailMessage);
            
            log.info("═══════════════════════════════════════════════════════════");
            log.info("📧 EMAIL ENVIADO VIA GMAIL [{}, {}]", timestamp, id);
            log.info("───────────────────────────────────────────────────────────");
            log.info("De: {}", fromEmail);
            log.info("Para: {}", email);
            log.info("Asunto: {}", asunto);
            log.info("Estado: ENVIADO");
            log.info("═══════════════════════════════════════════════════════════");
            
            notificacion.marcarComoEnviada();
            
        } catch (Exception e) {
            log.error("❌ ERROR al enviar email a {}: {}", email, e.getMessage());
            log.error("Verifica que Gmail SMTP esté configurado correctamente en application-local.yml");
            notificacion.marcarComoFallida(e.getMessage());
        }
        
        return notificacion;
    }
    
    @Override
    public Notificacion enviar(Notificacion notificacion) {
        if (notificacion.getTipo() == Notificacion.TipoNotificacion.SMS) {
            return enviarSms(notificacion.getDestinatario(), notificacion.getMensaje());
        } else {
            return enviarEmail(notificacion.getDestinatario(), "Notificación HERMES EXPRESS", notificacion.getMensaje());
        }
    }
}
