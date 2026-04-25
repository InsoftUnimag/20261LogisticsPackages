package com.logistics.packages.application.usecase;

import com.logistics.packages.application.ports.PaqueteRepository;
import com.logistics.packages.domain.event.SolicitudRutaEvent;
import com.logistics.packages.domain.exception.PaqueteNotFoundException;
import com.logistics.packages.domain.model.Paquete;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Caso de uso para procesar el pesaje de un paquete (MOD1-UC-002)
 * T205, T206: Orquesta la actualización del paquete con los datos del pesaje.
 * Integración MOD1-IP-003: Después del pesaje exitoso, se dispara la solicitud de ruta
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class ProcesarPesajeUseCase {

    private final PaqueteRepository paqueteRepository;
    private final SolicitarRutaUseCase solicitarRutaUseCase;

    /**
     * Procesa el pesaje de un paquete existente.
     * 
     * @param command Comando con los datos del pesaje
     * @return Respuesta con el precio calculado y alertas
     * @throws PaqueteNotFoundException si el paquete no existe
     */
    public PesajeResponse procesarPesaje(PesajeCommand command) {
        // Buscar el paquete existente
        Paquete paquete = paqueteRepository.buscarPorId(command.getPaqueteId())
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

        // Persistir los cambios
        paqueteRepository.guardar(paquete);

        // MOD1-IP-003: Después del pesaje exitoso, disparar solicitud de ruta
        log.info("Pesaje completado exitosamente para paquete {}. Disparando solicitud de ruta", paquete.getId());
        try {
            SolicitudRutaEvent evento = SolicitudRutaEvent.of(paquete.getId());
            solicitarRutaUseCase.handle(evento);
        } catch (Exception e) {
            log.error("Error al solicitar ruta para paquete {}: {}", paquete.getId(), e.getMessage(), e);
            // No fallar el pesaje si la solicitud de ruta falla
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
