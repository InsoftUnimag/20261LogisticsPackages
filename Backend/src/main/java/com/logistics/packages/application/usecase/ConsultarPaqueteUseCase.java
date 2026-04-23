package com.logistics.packages.application.admision.usecase;

import com.logistics.packages.application.admision.repositories.ConsultarPaqueteIn;
import com.logistics.packages.application.admision.repositories.PaqueteRepository;
import com.logistics.packages.domain.model.Paquete;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
@AllArgsConstructor
public class ConsultarPaqueteUseCase implements ConsultarPaqueteIn {

    private final PaqueteRepository paqueteRepository;

    @Override
    public Optional<Paquete> consultarPaquete(UUID paqueteId) {
        return paqueteRepository.findById(paqueteId);
    }
}
