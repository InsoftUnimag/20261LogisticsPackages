package com.logistics.packages.application.repository;

import com.logistics.packages.domain.model.Paquete;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public interface PriceCalculationService {
    BigDecimal calculatePrice(Paquete paquete, Double distanciaEstimadaKm);
}
