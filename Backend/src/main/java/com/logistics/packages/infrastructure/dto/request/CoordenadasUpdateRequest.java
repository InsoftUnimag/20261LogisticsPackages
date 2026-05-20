package com.logistics.packages.infrastructure.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para actualizar coordenadas de un paquete (fallback GPS manual).
 * Utilizado en el endpoint PATCH /api/paquetes/{paqueteId}/coordenadas
 * 
 * FE-5: Contingencia GPS
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CoordenadasUpdateRequest {
    
    @Min(value = -90, message = "Latitud debe estar entre -90 y 90")
    @Max(value = 90, message = "Latitud debe estar entre -90 y 90")
    private double latitud;
    
    @Min(value = -180, message = "Longitud debe estar entre -180 y 180")
    @Max(value = 180, message = "Longitud debe estar entre -180 y 180")
    private double longitud;
}
