package com.logistics.packages.application.usecase;

import java.util.UUID;

public record PrepararAlmacenajeCommand(UUID paqueteId, UUID zonaAlmacenamientoId) {
}
