package com.logistics.packages.application.usecase;

import com.logistics.packages.application.ports.EventoProcesadoRepository;
import com.logistics.packages.application.repository.ArchivoStoragePort;
import com.logistics.packages.application.repository.HistorialEstadoRepository;
import com.logistics.packages.application.repository.PaqueteRepository;
import com.logistics.packages.domain.event.SolicitudRutaEvent;
import com.logistics.packages.domain.exception.PaqueteNotFoundException;
import com.logistics.packages.domain.model.EventoProcesado;
import com.logistics.packages.domain.model.HistorialEstado;
import com.logistics.packages.domain.model.Paquete;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Caso de uso para procesar el pesaje de un paquete (MOD1-UC-002)
 * T205, T206: Orquesta la actualización del paquete con los datos del pesaje.
 * Integración MOD1-IP-003: Después del pesaje exitoso, se dispara la solicitud de ruta
 * 
 * BE-1: Incluye guardia de idempotencia para evitar duplicados de SQS
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class ProcesarPesajeUseCase {

    private final PaqueteRepository paqueteRepository;
    private final HistorialEstadoRepository historialEstadoRepository;
    private final ArchivoStoragePort archivoStoragePort;
    private final SolicitarRutaUseCase solicitarRutaUseCase;
    private final EventoProcesadoRepository eventoProcesadoRepository;

    /**
     * Procesa el pesaje de un paquete existente.
     * 
     * @param command Comando con los datos del pesaje
     * @return Respuesta con el precio calculado y alertas
     * @throws PaqueteNotFoundException si el paquete no existe
     */
    public PesajeResponse procesarPesaje(PesajeCommand command) {
        // Buscar el paquete existente
        Paquete paquete = paqueteRepository.findById(command.getPaqueteId())
                .orElseThrow(() -> new PaqueteNotFoundException(command.getPaqueteId()));

        // Procesar el pesaje en el dominio (validaciones y cálculos)
        paquete.procesarPesaje(
                command.getPeso(),
                command.getDimensiones(),
                command.getTipoMercancia(),
                command.isFormaIrregular()
        );

        // Calcular el precio de envío
        paquete.calcularPrecioEnvio(
                command.getTarifaBase(),
                command.getTarifaPorKg(),
                command.getTarifaPorKm(),
                command.getRecargoTipoMercancia(),
                command.getRecargoCategoriaCarga()
        );

        // Subir evidencia fotográfica si se proporcionó
        String urlEvidencia = null;
        if (command.getEvidencia() != null && !command.getEvidencia().isEmpty()) {
            urlEvidencia = archivoStoragePort.guardar(
                "pesaje",
                command.getPaqueteId().toString(),
                command.getEvidencia()
            );
        }

        // Persistir los cambios
        paqueteRepository.save(paquete);

        // Registrar en el historial la transición de pesaje
        HistorialEstado historialPesaje = new HistorialEstado(
                command.getPaqueteId(),
                null,
                paquete.getEstado(),
                "Pesaje procesado",
                null,
                urlEvidencia
        );
        historialEstadoRepository.guardar(historialPesaje);

        // MOD1-IP-003: Después del pesaje exitoso, disparar solicitud de ruta (CON GUARDIA DE IDEMPOTENCIA - BE-1)
        log.info("Pesaje completado exitosamente para paquete {}. Verificando idempotencia antes de disparar solicitud de ruta", paquete.getId());
        
        String eventoIdSolicitud = "SOLICITUD_RUTA_" + paquete.getId();
        
        // Verificar si ya fue procesado este evento
        if (eventoProcesadoRepository.yaFueProcesado(eventoIdSolicitud)) {
            log.info("Evento de solicitud de ruta ya fue procesado para paquete {}. Omitiendo segundo disparo.", paquete.getId());
        } else {
            try {
                SolicitudRutaEvent evento = SolicitudRutaEvent.of(paquete.getId());
                solicitarRutaUseCase.handle(evento);
                
                // Registrar el evento como procesado para garantizar idempotencia
                EventoProcesado eventoProcesado = new EventoProcesado(eventoIdSolicitud, paquete.getId(), "SOLICITUD_RUTA");
                eventoProcesadoRepository.guardar(eventoProcesado);
                
                log.info("Solicitud de ruta emitida y registrada como procesada para paquete {}", paquete.getId());
            } catch (Exception e) {
                log.error("Error al solicitar ruta para paquete {}: {}", paquete.getId(), e.getMessage(), e);
                // No fallar el pesaje si la solicitud de ruta falla
            }
        }

        // Construir la respuesta con el precio y las alertas
        return PesajeResponse.builder()
                .paqueteId(paquete.getId())
                .peso(paquete.getPeso().getKilogramos())
                .volumenM3(paquete.getVolumenM3())
                .pesoVolumetrico(paquete.getPesoVolumetrico())
                .pesoFacturable(paquete.getPesoFacturable())
                .categoriaCarga(paquete.getCategoriaCarga())
                .precioEnvio(paquete.getPrecioEnvio().getValor())
                .alertaCargaEspecial(paquete.isAlertaCargaEspecial())
                .alertaDensidadAtipica(paquete.isAlertaDensidadAtipica())
                .build();
    }
}
