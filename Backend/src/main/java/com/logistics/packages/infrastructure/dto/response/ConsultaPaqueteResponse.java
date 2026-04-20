package com.logistics.packages.infrastructure.dto.response;

import com.logistics.packages.domain.valueobject.EstadoPaquete;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.UUID;

@Getter
@AllArgsConstructor
public class ConsultaPaqueteResponse {
    private UUID idRoute; // This should come from the Paquete entity
    private UUID idPaquete;
    private EstadoPaquete estado;
}
