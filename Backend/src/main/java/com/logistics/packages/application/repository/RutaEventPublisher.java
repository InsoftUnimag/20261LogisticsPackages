package com.logistics.packages.application.admision.repositories;

import java.util.UUID;

public interface RutaEventPublisher {
    void publicarSolicitudRuta(UUID paqueteId);
}
