package com.logistics.packages.application.ports.out;

import java.util.UUID;

public interface RutaEventPublisher {
    void publicarSolicitudRuta(UUID paqueteId);
}
