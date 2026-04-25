package com.logistics.packages.application.ports;

import com.logistics.packages.domain.model.Notificacion;

/**
 * Puerto de salida para el envío de notificaciones.
 * MOD1-UC-007: FR-002 - Notificar al remitente y destinatario al registrar novedades.
 * 
 * Este puerto abstrae el mecanismo de envío de notificaciones (SMS, Email, etc.)
 * siguiendo el principio de Inversión de Dependencias.
 */
public interface NotificacionPort {
    
    /**
     * Envía una notificación SMS.
     * 
     * @param telefono Número de teléfono del destinatario
     * @param mensaje Contenido del mensaje
     * @return Notificacion con el resultado del envío
     */
    Notificacion enviarSms(String telefono, String mensaje);
    
    /**
     * Envía una notificación por correo electrónico.
     * 
     * @param email Dirección de correo electrónico del destinatario
     * @param asunto Asunto del correo
     * @param mensaje Contenido del mensaje
     * @return Notificacion con el resultado del envío
     */
    Notificacion enviarEmail(String email, String asunto, String mensaje);
    
    /**
     * Envía una notificación usando la entidad Notificacion.
     * Este método permite reintentar notificaciones fallidas.
     * 
     * @param notificacion La notificación a enviar
     * @return Notificacion actualizada con el resultado del envío
     */
    Notificacion enviar(Notificacion notificacion);
}
