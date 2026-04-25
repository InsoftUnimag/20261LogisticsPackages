package com.logistics.packages.application.usecase;

import com.logistics.packages.application.repository.PaqueteRepository;
import com.logistics.packages.domain.exception.PaqueteNotFoundException;
import com.logistics.packages.domain.model.Paquete;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Caso de uso para procesar el pesaje de un paquete (MOD1-UC-002)
 * T205, T206: Orquesta la actualización del paquete con los datos del pesaje.
 */
@Service
@RequiredArgsConstructor
@Transactional
public class ProcesarPesajeUseCase {

    private final PaqueteRepository paqueteRepository;

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

        // Persistir los cambios
        paqueteRepository.save(paquete);

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
