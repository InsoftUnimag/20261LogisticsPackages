package com.logistics.packages.application.usecase;

import com.logistics.packages.infrastructure.dto.request.DatosFisicosDiscrepanciaDto;
import java.util.Optional;
import java.util.UUID;

/**
 * Comando para preparar un paquete para almacenaje.
 * MOD1-IP-004: Incluye soporte para discrepancias físicas y registro de usuario.
 * 
 * @param paqueteId ID del paquete a almacenar
 * @param zonaAlmacenamientoId ID de la zona asignada
 * @param usuarioId ID del almacenista que realiza la operación
 * @param datosDiscrepancia Datos físicos corregidos si hay discrepancia (opcional)
 */
public record PrepararAlmacenajeCommand(
    UUID paqueteId, 
    UUID zonaAlmacenamientoId,
    UUID usuarioId,
    Optional<DatosFisicosDiscrepanciaDto> datosDiscrepancia
) {
    
    public boolean tieneDiscrepancias() {
        return datosDiscrepancia != null && datosDiscrepancia.isPresent();
    }
}
