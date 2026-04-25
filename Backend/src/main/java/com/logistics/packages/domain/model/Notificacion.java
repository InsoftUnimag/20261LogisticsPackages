package com.logistics.packages.domain.model;

import lombok.*;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

/**
 * Entidad que representa una notificación enviada a usuarios (remitente o destinatario).
 * MOD1-UC-007: FR-002 - Notificar al remitente y destinatario al registrar cualquier novedad.
 * 
 * Esta entidad mantiene un registro de las notificaciones enviadas para trazabilidad
 * y permite reintentos en caso de fallos.
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
public class Notificacion {
    
    /**
     * ID único de la notificación
     */
    @Builder.Default
    private UUID id = UUID.randomUUID();
    
    /**
     * ID del paquete asociado a la notificación
     */
    private UUID paqueteId;
    
    /**
     * Destinatario de la notificación (teléfono o correo electrónico)
     */
    private String destinatario;
    
    /**
     * Tipo de notificación (SMS, EMAIL)
     */
    private TipoNotificacion tipo;
    
    /**
     * Mensaje de la notificación
     */
    private String mensaje;
    
    /**
     * Estado de la notificación (PENDIENTE, ENVIADO, FALLIDO)
     */
    @Builder.Default
    private EstadoNotificacion estado = EstadoNotificacion.PENDIENTE;
    
    /**
     * Timestamp UTC del momento de creación
     */
    @Builder.Default
    private LocalDateTime fechaCreacionUtc = LocalDateTime.now(ZoneOffset.UTC);
    
    /**
     * Timestamp UTC del momento de envío (si aplica)
     */
    private LocalDateTime fechaEnvioUtc;
    
    /**
     * Número de intentos de envío
     */
    @Builder.Default
    private Integer intentos = 0;
    
    /**
     * Mensaje de error (si aplica)
     */
    private String mensajeError;
    
    /**
     * Marca la notificación como enviada exitosamente.
     */
    public void marcarComoEnviada() {
        this.estado = EstadoNotificacion.ENVIADO;
        this.fechaEnvioUtc = LocalDateTime.now(ZoneOffset.UTC);
        this.intentos++;
    }
    
    /**
     * Marca la notificación como fallida con un mensaje de error.
     * 
     * @param mensajeError Mensaje de error del fallo
     */
    public void marcarComoFallida(String mensajeError) {
        this.estado = EstadoNotificacion.FALLIDO;
        this.mensajeError = mensajeError;
        this.intentos++;
    }
    
    /**
     * Enum para tipos de notificación
     */
    public enum TipoNotificacion {
        SMS,
        EMAIL
    }
    
    /**
     * Enum para estados de notificación
     */
    public enum EstadoNotificacion {
        PENDIENTE,
        ENVIADO,
        FALLIDO
    }
}
