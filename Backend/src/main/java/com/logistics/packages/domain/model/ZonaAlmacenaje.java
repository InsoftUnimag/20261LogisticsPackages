package com.logistics.packages.domain.model;

import com.logistics.packages.domain.exception.ZonaSaturadaException;
import com.logistics.packages.domain.exception.ZonaIncompatibleException;
import com.logistics.packages.domain.valueobject.CategoriaZona;
import com.logistics.packages.domain.valueobject.EstadoZona;
import com.logistics.packages.domain.valueobject.TipoMercancia;
import lombok.Getter;
import lombok.Setter;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Builder;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ZonaAlmacenaje {

    private UUID id;
    private String nombre;
    private String codigo;
    private CategoriaZona categoria;
    private BigDecimal capacidadMaxKg;
    private BigDecimal capacidadMaxM3;
    private Integer capacidadMaxPaquetes;
    private BigDecimal pesoActualKg;
    private BigDecimal volumenActualM3;
    private Integer contadorPaquetes;
    private EstadoZona estado;
    private String ubicacionFisica;
    private UUID idSede;
    private UUID zonaContingenciaId;
    private Long version;

    /**
     * FR-006: Verifica si la zona puede albergar un paquete según su tipo de mercancía.
     * Valida la compatibilidad entre el tipo de mercancía del paquete y la categoría de la zona.
     * 
     * @param paquete El paquete a verificar
     * @return true si la zona es compatible con el tipo de mercancía, false en caso contrario
     */
    public boolean puedeAlbergar(Paquete paquete) {
        if (paquete.getTipoMercancia() == null) {
            return false;
        }

        // Las zonas de RETENCION aceptan cualquier tipo de mercancía
        if (this.categoria == CategoriaZona.RETENCION) {
            return true;
        }

        return switch (paquete.getTipoMercancia()) {
            case PELIGROSO -> this.categoria == CategoriaZona.ALTO_RIESGO;
            case FRAGIL -> this.categoria == CategoriaZona.DELICADA || this.categoria == CategoriaZona.ALTO_RIESGO;
            case ESTANDAR -> this.categoria == CategoriaZona.NORMAL ||
                    this.categoria == CategoriaZona.DELICADA ||
                    this.categoria == CategoriaZona.ALTO_RIESGO;
            default -> false;
        };
    }

    /**
     * FR-007: Agrega un paquete a la zona, actualizando atómicamente los tres contadores.
     * FR-008: Verifica capacidades y lanza ZonaSaturadaException si se excede algún límite.
     * 
     * @param paquete El paquete a agregar
     * @throws ZonaSaturadaException si la zona ha alcanzado su capacidad máxima
     * @throws ZonaIncompatibleException si el tipo de mercancía no es compatible con la zona
     */
    public void agregarPaquete(Paquete paquete) {
        // Verificar compatibilidad
        if (!puedeAlbergar(paquete)) {
            throw new ZonaIncompatibleException(
                paquete.getId(), 
                paquete.getTipoMercancia(), 
                this.id, 
                this.categoria
            );
        }

        BigDecimal pesoPaquete = paquete.getPeso() != null ? 
            BigDecimal.valueOf(paquete.getPeso().getKilogramos()) : BigDecimal.ZERO;
        BigDecimal volumenPaquete = paquete.getVolumenM3() != null ? 
            BigDecimal.valueOf(paquete.getVolumenM3()) : BigDecimal.ZERO;

        // Verificar capacidades antes de agregar
        BigDecimal nuevoPeso = (this.pesoActualKg != null ? this.pesoActualKg : BigDecimal.ZERO).add(pesoPaquete);
        BigDecimal nuevoVolumen = (this.volumenActualM3 != null ? this.volumenActualM3 : BigDecimal.ZERO).add(volumenPaquete);
        int nuevaCantidad = (this.contadorPaquetes != null ? this.contadorPaquetes : 0) + 1;

        // Verificar exceso de capacidad
        if (this.capacidadMaxKg != null && nuevoPeso.compareTo(this.capacidadMaxKg) > 0) {
            throw new ZonaSaturadaException(this.id, this.nombre, "peso", this.zonaContingenciaId);
        }
        if (this.capacidadMaxM3 != null && nuevoVolumen.compareTo(this.capacidadMaxM3) > 0) {
            throw new ZonaSaturadaException(this.id, this.nombre, "volumen", this.zonaContingenciaId);
        }
        if (this.capacidadMaxPaquetes != null && nuevaCantidad > this.capacidadMaxPaquetes) {
            throw new ZonaSaturadaException(this.id, this.nombre, "cantidad de paquetes", this.zonaContingenciaId);
        }

        // Actualización atómica de los tres contadores
        this.pesoActualKg = nuevoPeso;
        this.volumenActualM3 = nuevoVolumen;
        this.contadorPaquetes = nuevaCantidad;

        // Actualizar estado de la zona según ocupación
        actualizarEstado();
    }

    /**
     * Actualiza el estado de la zona basándose en los niveles de ocupación.
     * DISPONIBLE: < 70% de capacidad
     * PARCIAL: 70% - 90% de capacidad
     * SATURADO: > 90% de capacidad
     */
    private void actualizarEstado() {
        double porcentajeOcupacionPeso = this.capacidadMaxKg != null && this.capacidadMaxKg.compareTo(BigDecimal.ZERO) > 0 ?
            this.pesoActualKg.divide(this.capacidadMaxKg, 2, RoundingMode.HALF_UP).doubleValue() : 0.0;
        
        double porcentajeOcupacionVolumen = this.capacidadMaxM3 != null && this.capacidadMaxM3.compareTo(BigDecimal.ZERO) > 0 ?
            this.volumenActualM3.divide(this.capacidadMaxM3, 2, RoundingMode.HALF_UP).doubleValue() : 0.0;
        
        double porcentajeOcupacionPaquetes = this.capacidadMaxPaquetes != null && this.capacidadMaxPaquetes > 0 ?
            (double) this.contadorPaquetes / this.capacidadMaxPaquetes : 0.0;

        double mayorOcupacion = Math.max(porcentajeOcupacionPeso, 
                                Math.max(porcentajeOcupacionVolumen, porcentajeOcupacionPaquetes));

        if (mayorOcupacion >= 0.9) {
            this.estado = EstadoZona.SATURADO;
        } else if (mayorOcupacion >= 0.7) {
            this.estado = EstadoZona.PARCIAL;
        } else {
            this.estado = EstadoZona.DISPONIBLE;
        }
    }

    /**
     * Verifica si la zona tiene capacidad disponible para un paquete específico.
     * 
     * @param paquete El paquete a verificar
     * @return true si hay capacidad disponible, false en caso contrario
     */
    public boolean tieneCapacidadPara(Paquete paquete) {
        BigDecimal pesoPaquete = paquete.getPeso() != null ? 
            BigDecimal.valueOf(paquete.getPeso().getKilogramos()) : BigDecimal.ZERO;
        BigDecimal volumenPaquete = paquete.getVolumenM3() != null ? 
            BigDecimal.valueOf(paquete.getVolumenM3()) : BigDecimal.ZERO;

        BigDecimal nuevoPeso = (this.pesoActualKg != null ? this.pesoActualKg : BigDecimal.ZERO).add(pesoPaquete);
        BigDecimal nuevoVolumen = (this.volumenActualM3 != null ? this.volumenActualM3 : BigDecimal.ZERO).add(volumenPaquete);
        int nuevaCantidad = (this.contadorPaquetes != null ? this.contadorPaquetes : 0) + 1;

        boolean capacidadPeso = this.capacidadMaxKg == null || nuevoPeso.compareTo(this.capacidadMaxKg) <= 0;
        boolean capacidadVolumen = this.capacidadMaxM3 == null || nuevoVolumen.compareTo(this.capacidadMaxM3) <= 0;
        boolean capacidadPaquetes = this.capacidadMaxPaquetes == null || nuevaCantidad <= this.capacidadMaxPaquetes;

        return capacidadPeso && capacidadVolumen && capacidadPaquetes;
    }
}
