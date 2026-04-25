package com.logistics.packages.domain.model;

import com.logistics.packages.domain.valueobject.CategoriaZona;
import com.logistics.packages.domain.valueobject.Coordenadas;
import com.logistics.packages.domain.valueobject.TipoMercancia;
import lombok.*;

import java.util.UUID;

/**
 * Entidad de dominio ZonaDestino
 * MOD1-IP-005: Representa una agrupación lógica geográfica para clasificación de paquetes
 * 
 * Responsabilidades:
 * - FR-001: Agrupar paquetes por proximidad geográfica
 * - FR-003: Validar compatibilidad de mercancía con la categoría de zona
 * - FR-004: Validar capacidad máxima antes de confirmar clasificación
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ZonaDestino {

    private UUID id;
    private String nombre;
    private String codigo;
    private CategoriaZona categoria;
    
    // Límites geográficos de la zona
    private Double latitudMin;
    private Double latitudMax;
    private Double longitudMin;
    private Double longitudMax;
    
    // Capacidad y control
    private Integer capacidadMaxPaquetes;
    private Integer contadorPaquetes;
    private UUID idSede;

    /**
     * FR-003: Verifica si la zona es apta para un tipo de mercancía específico.
     * Valida la compatibilidad entre el tipo de mercancía y la categoría de la zona.
     * 
     * @param tipoMercancia El tipo de mercancía a verificar
     * @return true si la zona es apta para el tipo de mercancía, false en caso contrario
     */
    public boolean esAptaPara(TipoMercancia tipoMercancia) {
        if (tipoMercancia == null) {
            return false;
        }
        
        switch (tipoMercancia) {
            case PELIGROSO:
                // Solo zonas de alto riesgo pueden albergar mercancía peligrosa
                return this.categoria == CategoriaZona.ALTO_RIESGO;
            case FRAGIL:
                // Zonas delicadas y alto riesgo pueden albergar mercancía frágil
                return this.categoria == CategoriaZona.DELICADA || 
                       this.categoria == CategoriaZona.ALTO_RIESGO;
            case ESTANDAR:
                // Mercancía estándar puede ir a cualquier zona excepto retención
                return this.categoria == CategoriaZona.NORMAL || 
                       this.categoria == CategoriaZona.DELICADA || 
                       this.categoria == CategoriaZona.ALTO_RIESGO;
            default:
                return false;
        }
    }

    /**
     * FR-004: Verifica si la zona tiene capacidad disponible.
     * 
     * @return true si hay capacidad disponible, false si está saturada
     */
    public boolean tieneCapacidadDisponible() {
        if (this.capacidadMaxPaquetes == null) {
            return true; // Sin límite configurado
        }
        
        int contador = this.contadorPaquetes != null ? this.contadorPaquetes : 0;
        return contador < this.capacidadMaxPaquetes;
    }

    /**
     * Incrementa el contador de paquetes de la zona.
     * Debe ser llamado después de confirmar la clasificación.
     */
    public void incrementarContador() {
        if (this.contadorPaquetes == null) {
            this.contadorPaquetes = 0;
        }
        this.contadorPaquetes++;
    }

    /**
     * FR-001: Determina si unas coordenadas geográficas pertenecen a esta zona de destino.
     * 
     * @param coordenadas Las coordenadas a verificar
     * @return true si las coordenadas están dentro de los límites de la zona
     */
    public boolean contieneCoordenas(Coordenadas coordenadas) {
        if (coordenadas == null || 
            this.latitudMin == null || this.latitudMax == null ||
            this.longitudMin == null || this.longitudMax == null) {
            return false;
        }
        
        boolean dentroDeLatitud = coordenadas.latitud() >= this.latitudMin && 
                                  coordenadas.latitud() <= this.latitudMax;
        boolean dentroDeLongitud = coordenadas.longitud() >= this.longitudMin && 
                                   coordenadas.longitud() <= this.longitudMax;
        
        return dentroDeLatitud && dentroDeLongitud;
    }
}
