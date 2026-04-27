package com.logistics.packages.application.repository;

import com.logistics.packages.domain.exception.DistanciaRequeridaException;
import com.logistics.packages.domain.model.Paquete;
import com.logistics.packages.domain.valueobject.CategoriaCarga;
import com.logistics.packages.domain.valueobject.TipoMercancia;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
@Slf4j
public class PriceCalculationServiceImpl implements PriceCalculationService {

    private static final BigDecimal TARIFA_BASE = new BigDecimal("5000.00");
    private static final BigDecimal TARIFA_POR_KG = new BigDecimal("1500.00");
    private static final BigDecimal TARIFA_POR_KM = new BigDecimal("50.00");
    private static final BigDecimal RECARGO_MERCANCIA_PELIGROSA = new BigDecimal("5000.00");
    private static final BigDecimal RECARGO_MERCANCIA_FRAGIL = new BigDecimal("3000.00");
    private static final BigDecimal RECARGO_CARGA_ESPECIAL = new BigDecimal("8000.00");
    private static final BigDecimal RECARGO_FORMA_IRREGULAR = new BigDecimal("2000.00");
    private static final BigDecimal RECARGO_DENSIDAD_ATIPICA = new BigDecimal("1500.00");
    private static final BigDecimal IVA = new BigDecimal("0.19");

    @Override
    public BigDecimal calculatePrice(Paquete paquete) {
        log.debug("Calculando precio de envío para paquete {}", paquete.getId());

        if (paquete.getPesoFacturable() == null) {
            throw new IllegalArgumentException(
                "El paquete debe tener peso facturable calculado antes de determinar el precio."
            );
        }

        if (paquete.getDistanciaEstimadaKm() == null) {
            throw new DistanciaRequeridaException();
        }

        BigDecimal precio = TARIFA_BASE;

        BigDecimal costoPeso = new BigDecimal(paquete.getPesoFacturable()).multiply(TARIFA_POR_KG);
        precio = precio.add(costoPeso);

        BigDecimal costoDistancia = new BigDecimal(paquete.getDistanciaEstimadaKm()).multiply(TARIFA_POR_KM);
        precio = precio.add(costoDistancia);

        precio = precio.add(calcularRecargoTipoMercancia(paquete.getTipoMercancia()));

        precio = precio.add(calcularRecargoCategoriaCarga(paquete.getCategoriaCarga()));

        if (Boolean.TRUE.equals(paquete.getIndicadorFormaIrregular())) {
            precio = precio.add(RECARGO_FORMA_IRREGULAR);
            log.debug("Aplicando recargo por forma irregular");
        }

        if (paquete.isAlertaDensidadAtipica()) {
            precio = precio.add(RECARGO_DENSIDAD_ATIPICA);
            log.debug("Aplicando recargo por densidad atípica");
        }

        BigDecimal precioConIva = precio.multiply(BigDecimal.ONE.add(IVA));
        
        log.info("Precio calculado para paquete {}: {} (sin IVA: {})", 
                paquete.getId(), precioConIva, precio);

        return precioConIva;
    }

    public BigDecimal calcularSubtotal(Paquete paquete) {
        if (paquete.getPesoFacturable() == null || paquete.getDistanciaEstimadaKm() == null) {
            return null;
        }

        BigDecimal subtotal = TARIFA_BASE;
        subtotal = subtotal.add(new BigDecimal(paquete.getPesoFacturable()).multiply(TARIFA_POR_KG));
        subtotal = subtotal.add(new BigDecimal(paquete.getDistanciaEstimadaKm()).multiply(TARIFA_POR_KM));
        subtotal = subtotal.add(calcularRecargoTipoMercancia(paquete.getTipoMercancia()));
        subtotal = subtotal.add(calcularRecargoCategoriaCarga(paquete.getCategoriaCarga()));

        if (Boolean.TRUE.equals(paquete.getIndicadorFormaIrregular())) {
            subtotal = subtotal.add(RECARGO_FORMA_IRREGULAR);
        }

        if (paquete.isAlertaDensidadAtipica()) {
            subtotal = subtotal.add(RECARGO_DENSIDAD_ATIPICA);
        }

        return subtotal;
    }

    private BigDecimal calcularRecargoTipoMercancia(TipoMercancia tipoMercancia) {
        if (tipoMercancia == null) {
            return BigDecimal.ZERO;
        }
        return switch (tipoMercancia) {
            case PELIGROSO -> RECARGO_MERCANCIA_PELIGROSA;
            case FRAGIL -> RECARGO_MERCANCIA_FRAGIL;
            default -> BigDecimal.ZERO;
        };
    }

    private BigDecimal calcularRecargoCategoriaCarga(CategoriaCarga categoriaCarga) {
        if (categoriaCarga == null) {
            return BigDecimal.ZERO;
        }
        return switch (categoriaCarga) {
            case CARGA_ESPECIAL -> RECARGO_CARGA_ESPECIAL;
            default -> BigDecimal.ZERO;
        };
    }
}