package com.logistics.packages.infrastructure.adapter.out.messaging;

import com.logistics.packages.application.ports.out.RutaEventPublisher;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class RutaEventAdapter implements RutaEventPublisher {

    @Override
    public void publicarSolicitudRuta(UUID paqueteId) {
        // Placeholder implementation
        // In a real implementation, this would publish an event to a message queue
        System.out.println("Publishing route request for package: " + paqueteId);
    }
}
