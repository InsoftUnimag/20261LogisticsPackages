package com.logistics.packages.infrastructure.dto.response;

import com.logistics.packages.domain.valueobject.CategoriaZona;
import com.logistics.packages.domain.valueobject.EstadoPaquete;
import com.logistics.packages.domain.valueobject.TipoMercancia;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * DTO de respuesta para la asignación de zona de almacenamiento.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AsignacionZonaResponse {
    
    @Schema(description = "ID del paquete asignado")
    private UUID paqueteId;
    @Schema(description = "ID de la zona de almacenamiento asignada")
    private UUID zonaId;
    @Schema(description = "Nombre de la zona de almacenamiento")
    private String nombreZona;
    @Schema(description = "Categoría de la zona")
    private CategoriaZona categoria;
    @Schema(description = "Tipo de mercancía del paquete")
    private TipoMercancia tipoMercancia;
    @Schema(description = "Estado actual del paquete tras la asignación")
    private EstadoPaquete estadoPaquete;
    @Schema(description = "Indica si los datos físicos fueron actualizados por discrepancias")
    private boolean datosActualizados;
    @Schema(description = "Mensaje informativo de la operación")
    private String mensaje;
    
    @Schema(description = "Peso actual de la zona en kg")
    private Double pesoActualKg;
    @Schema(description = "Capacidad máxima de peso en kg")
    private Double capacidadMaxKg;
    @Schema(description = "Volumen actual de la zona en m³")
    private Double volumenActualM3;
    @Schema(description = "Capacidad máxima de volumen en m³")
    private Double capacidadMaxM3;
    @Schema(description = "Cantidad actual de paquetes en la zona")
    private Integer contadorPaquetes;
    @Schema(description = "Capacidad máxima de paquetes")
    private Integer capacidadMaxPaquetes;
    @Schema(description = "Indica si la zona está saturada")
    private boolean zonaSaturada;
    
    @Schema(description = "ID de la zona de contingencia usada (si aplica)")
    private UUID zonaContingenciaId;
    @Schema(description = "Indica si se está usando una zona de contingencia")
    private boolean usandoZonaContingencia;
}
