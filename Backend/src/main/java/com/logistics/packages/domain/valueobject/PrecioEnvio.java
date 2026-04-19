package com.logistics.packages.domain.valueobject;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@AllArgsConstructor
public class PrecioEnvio {
    private BigDecimal valor;
}
