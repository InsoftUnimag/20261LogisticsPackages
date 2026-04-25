package com.logistics.packages.application.ports;

import java.util.UUID;

public interface RutaEventPublisher {
    void publicarSolicitudRuta(UUID paqueteId);
}
