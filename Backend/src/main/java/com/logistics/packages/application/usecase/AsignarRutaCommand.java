package com.logistics.packages.application.usecase;

import java.time.OffsetDateTime;
import java.util.UUID;

public record AsignarRutaCommand(
        UUID paqueteId,
        UUID rutaId,
        OffsetDateTime fechaHoraEvento
) {
}
