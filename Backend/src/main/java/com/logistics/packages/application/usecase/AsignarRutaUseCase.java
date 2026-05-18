package com.logistics.packages.application.usecase;

import com.logistics.packages.application.repository.PaqueteRepository;
import com.logistics.packages.domain.exception.PaqueteNotFoundException;
import com.logistics.packages.domain.model.Paquete;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class AsignarRutaUseCase {

    private final PaqueteRepository paqueteRepository;

    public void asignarRuta(AsignarRutaCommand command) {
        log.info("Procesando asignación de ruta para paquete: {} / ruta: {}",
                command.paqueteId(), command.rutaId());

        Paquete paquete = paqueteRepository.findById(command.paqueteId())
                .orElseThrow(() -> new PaqueteNotFoundException(command.paqueteId()));

        paquete.asignarRuta(command.rutaId());

        paqueteRepository.save(paquete);

        log.info("Ruta {} asignada al paquete {}. Nuevo estado: {}",
                command.rutaId(), command.paqueteId(), paquete.getEstado());
    }
}
