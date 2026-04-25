package com.logistics.packages.application.repository;

import java.util.UUID;

public interface RutaEventPublisher {
    void publicarSolicitudRuta(UUID paqueteId);
}
