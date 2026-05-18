package com.logistics.packages.application.ports;

import com.logistics.packages.domain.model.Paquete;

public interface RutaQueuePort {

    void enviarSolicitud(Paquete paquete);
}
