package com.logistics.packages.application.usecase.novedad;

import com.logistics.packages.application.ports.EstadoPaqueteFinanzasPublisher;
import com.logistics.packages.application.ports.NotificacionPort;
import com.logistics.packages.application.repository.ArchivoStoragePort;
import com.logistics.packages.application.repository.HistorialEstadoRepository;
import com.logistics.packages.application.repository.NovedadEventPublisher;
import com.logistics.packages.application.repository.PaqueteRepository;
import com.logistics.packages.domain.exception.PaqueteNotFoundException;
import com.logistics.packages.domain.model.HistorialEstado;
import com.logistics.packages.domain.model.Paquete;
import com.logistics.packages.domain.valueobject.NovedadBodega;
import com.logistics.packages.domain.valueobject.TipoNovedad;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Caso de uso para registrar novedades en paquetes (dañados o extraviados).
 * MOD1-UC-006: Orquesta el flujo completo de registro de novedad.
 * 
 * Responsabilidades:
 * 1. Validar existencia del paquete
 * 2. Guardar evidencia multimedia (si aplica)
 * 3. Registrar la novedad en el dominio del paquete
 * 4. Persistir cambios en paquete e historial
 * 5. Publicar evento para notificar al Controlador de Novedades
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class RegistrarNovedadUseCase {

    private final PaqueteRepository paqueteRepository;
    private final HistorialEstadoRepository historialRepository;
    private final ArchivoStoragePort archivoStoragePort;
    private final NovedadEventPublisher novedadEventPublisher;
    private final EstadoPaqueteFinanzasPublisher estadoPaqueteFinanzasPublisher;
    private final NotificacionPort notificacionPort;

    /**
     * Ejecuta el registro de una novedad en un paquete.
     * 
     * @param command Comando con los datos de la novedad a registrar
     * @return RegistroNovedadResponse con información del resultado
     * @throws PaqueteNotFoundException si el paquete no existe
     * @throws EstadoTransicionInvalidaException si el paquete no está en estado válido
     * @throws EvidenciaRequeridaException si falta evidencia obligatoria para tipo DAÑADO
     */
    @Transactional
    public RegistroNovedadResponse registrarNovedad(RegistrarNovedadCommand command) {
        // 1. Validar existencia del paquete
        Paquete paquete = paqueteRepository.findById(command.getPaqueteId())
            .orElseThrow(() -> new PaqueteNotFoundException(command.getPaqueteId()));

        // 2. Guardar evidencia multimedia si se proporcionó
        String urlEvidencia = null;
        if (command.getEvidencia() != null && !command.getEvidencia().isEmpty()) {
            urlEvidencia = archivoStoragePort.guardar(
                "novedades",
                command.getPaqueteId().toString(),
                command.getEvidencia()
            );
        }

        // 3. Registrar la novedad en el dominio (aplica las validaciones de negocio)
        NovedadBodega novedad = new NovedadBodega(
            command.getTipoNovedad(),
            command.getObservaciones(),
            command.getUsuarioId(),
            urlEvidencia
        );
        HistorialEstado historial = paquete.registrarNovedad(novedad);

        // 4. Persistir cambios en base de datos
        paqueteRepository.save(paquete);
        historialRepository.guardar(historial);

        // 5. Publicar evento para notificar al Controlador de Novedades (FR-005)
        novedadEventPublisher.publicarNovedadRegistrada(paquete.getId(), historial.getId());

        // FR-002: Enviar notificaciones al remitente y destinatario
        enviarNotificacionesBodega(paquete, novedad.getTipo());

        // Publicar estado actualizado a la cola de Finanzas (M3)
        estadoPaqueteFinanzasPublisher.publicarEstadoFinal(paquete);

        // 6. Retornar respuesta
        return new RegistroNovedadResponse(
            paquete.getId(),
            paquete.getEstado(),
            historial.getId()
        );
    }

    /**
     * FR-002: Envía notificaciones al remitente y destinatario sobre la novedad registrada.
     * Si el envío de notificaciones falla, se registra en logs pero NO hace rollback de la transacción principal.
     * 
     * @param paquete El paquete con novedad registrada
     * @param tipoNovedad Tipo de novedad (DAÑADO o EXTRAVIADO)
     */
    private void enviarNotificacionesBodega(Paquete paquete, TipoNovedad tipoNovedad) {
        try {
            String mensaje = construirMensajeNotificacion(paquete, tipoNovedad);
            
            // Notificar al remitente por SMS
            if (paquete.getRemitente() != null && paquete.getRemitente().getTelefono() != null) {
                try {
                    notificacionPort.enviarSms(paquete.getRemitente().getTelefono(), mensaje);
                    log.info("Notificación SMS enviada al remitente sobre novedad en bodega: {}", 
                            paquete.getRemitente().getTelefono());
                } catch (Exception e) {
                    log.error("Error al enviar SMS al remitente sobre novedad en bodega: {}", e.getMessage());
                }
            }
            
            // Notificar al destinatario por SMS y Email
            if (paquete.getDestinatario() != null) {
                if (paquete.getDestinatario().getTelefono() != null) {
                    try {
                        notificacionPort.enviarSms(paquete.getDestinatario().getTelefono(), mensaje);
                        log.info("Notificación SMS enviada al destinatario sobre novedad en bodega: {}", 
                                paquete.getDestinatario().getTelefono());
                    } catch (Exception e) {
                        log.error("Error al enviar SMS al destinatario sobre novedad en bodega: {}", e.getMessage());
                    }
                }
                
                if (paquete.getDestinatario().getCorreoElectronico() != null) {
                    try {
                        String asunto = "Novedad en tu paquete - " + tipoNovedad.name();
                        notificacionPort.enviarEmail(paquete.getDestinatario().getCorreoElectronico(), asunto, mensaje);
                        log.info("Notificación Email enviada al destinatario sobre novedad en bodega: {}", 
                                paquete.getDestinatario().getCorreoElectronico());
                    } catch (Exception e) {
                        log.error("Error al enviar Email al destinatario sobre novedad en bodega: {}", e.getMessage());
                    }
                }
            }
        } catch (Exception e) {
            log.error("Error general al enviar notificaciones sobre novedad en bodega para paquete {}: {}", 
                    paquete.getId(), e.getMessage());
        }
    }

    /**
     * Construye el mensaje de notificación según el tipo de novedad en bodega.
     * 
     * @param paquete El paquete con novedad
     * @param tipoNovedad Tipo de novedad
     * @return Mensaje de notificación
     */
    private String construirMensajeNotificacion(Paquete paquete, TipoNovedad tipoNovedad) {
        String idPaquete = paquete.getId().toString().substring(0, 8);
        
        switch (tipoNovedad) {
            case DAÑADO:
                return String.format("Novedad crítica: Tu paquete %s presenta daños. " +
                        "Nuestro equipo se pondrá en contacto para resolver la situación.", idPaquete);
            case EXTRAVIADO:
                return String.format("Novedad crítica: Tu paquete %s ha sido reportado como extraviado en bodega. " +
                        "Nuestro equipo está trabajando para localizarlo.", idPaquete);
            default:
                return String.format("Se ha reportado una novedad en tu paquete %s. " +
                        "Nuestro equipo se pondrá en contacto.", idPaquete);
        }
    }
}
