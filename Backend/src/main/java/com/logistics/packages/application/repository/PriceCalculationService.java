package com.logistics.packages.application.repository;

import com.logistics.packages.domain.model.Paquete;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;

@Repository
public interface PriceCalculationService {
    BigDecimal calculatePrice(Paquete paquete);
}
