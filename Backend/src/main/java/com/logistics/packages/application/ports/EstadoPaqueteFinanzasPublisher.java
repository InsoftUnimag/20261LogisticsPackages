package com.logistics.packages.application.ports;

import com.logistics.packages.domain.model.Paquete;

public interface EstadoPaqueteFinanzasPublisher {

    void publicarEstadoFinal(Paquete paquete);
}
