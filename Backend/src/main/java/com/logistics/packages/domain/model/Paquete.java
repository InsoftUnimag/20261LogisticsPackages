package com.logistics.packages.domain.model;

import com.logistics.packages.domain.exception.PaqueteEnTransitoException;
import com.logistics.packages.domain.valueobject.EstadoPaquete;
import com.logistics.packages.domain.valueobject.Evidencia;
import com.logistics.packages.domain.valueobject.TipoNovedad;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Entidad raíz de agregado que representa un paquete en el sistema logístico.
 * Contiene la lógica de negocio para transiciones de estado y registro de novedades.
 * MOD1-IP-006: Actualizar Estado de Paquete por Novedad
 */
public class Paquete {
    
    private final UUID id;
    private EstadoPaquete estado;
    private final List<HistorialEstado> historial;
    
    /**
     * Constructor para crear un nuevo paquete.
     * El paquete inicia en estado RECIBIDO_EN_SEDE.
     */
    public Paquete(UUID id) {
        if (id == null) {
            throw new IllegalArgumentException("El ID del paquete no puede ser nulo");
        }
        this.id = id;
        this.estado = EstadoPaquete.RECIBIDO_EN_SEDE;
        this.historial = new ArrayList<>();
    }
    
    /**
     * Constructor para reconstitución desde persistencia.
     */
    public Paquete(UUID id, EstadoPaquete estado, List<HistorialEstado> historial) {
        this.id = id;
        this.estado = estado;
        this.historial = historial != null ? new ArrayList<>(historial) : new ArrayList<>();
    }
    
    /**
     * Registra una novedad en bodega y actualiza el estado del paquete.
     * FR-006: Bloquear actualizaciones si ya está en tránsito o posterior.
     * 
     * @param tipoNovedad Tipo de novedad (DAÑADO o EXTRAVIADO)
     * @param descripcion Descripción de la novedad
     * @param usuarioResponsable Usuario que registra la novedad
     * @param evidencias Lista de evidencias multimedia
     * @return La entrada de historial creada
     * @throws PaqueteEnTransitoException si el paquete ya está en tránsito o posterior
     */
    public HistorialEstado registrarNovedad(TipoNovedad tipoNovedad, String descripcion,
                                           String usuarioResponsable, List<Evidencia> evidencias) {
        
        // FR-006: Bloquear si ya está en tránsito o posterior
        if (this.estado.esPosteriorA(EstadoPaquete.LISTO_PARA_DESPACHO)) {
            throw new PaqueteEnTransitoException(
                "No se puede registrar novedad en paquete con estado: " + this.estado
            );
        }
        
        EstadoPaquete estadoAnterior = this.estado;
        EstadoPaquete nuevoEstado = EstadoPaquete.NOVEDAD_EN_BODEGA;
        
        // Crear entrada en historial
        HistorialEstado entrada = new HistorialEstado(
            this.id, 
            estadoAnterior, 
            nuevoEstado, 
            usuarioResponsable, 
            descripcion, 
            evidencias
        );
        
        // Actualizar estado y agregar al historial
        this.historial.add(entrada);
        this.estado = nuevoEstado;
        
        return entrada;
    }
    
    /**
     * Cambia el estado del paquete y registra la transición en el historial.
     * Método genérico para cualquier transición de estado.
     * 
     * @param nuevoEstado Nuevo estado del paquete
     * @param usuarioResponsable Usuario que realiza el cambio
     * @param notas Notas opcionales sobre la transición
     * @return La entrada de historial creada
     */
    public HistorialEstado cambiarEstado(EstadoPaquete nuevoEstado, String usuarioResponsable, String notas) {
        EstadoPaquete estadoAnterior = this.estado;
        
        HistorialEstado entrada = new HistorialEstado(
            this.id,
            estadoAnterior,
            nuevoEstado,
            usuarioResponsable,
            notas,
            List.of()
        );
        
        this.historial.add(entrada);
        this.estado = nuevoEstado;
        
        return entrada;
    }
    
    public UUID getId() {
        return id;
    }
    
    public EstadoPaquete getEstado() {
        return estado;
    }
    
    /**
     * @return Copia inmutable del historial de estados
     */
    public List<HistorialEstado> getHistorial() {
        return List.copyOf(this.historial);
    }
    
    /**
     * @return true si el paquete tiene novedad registrada (estado NOVEDAD_EN_BODEGA)
     */
    public boolean tieneNovedad() {
        return this.estado == EstadoPaquete.NOVEDAD_EN_BODEGA;
    }
    
    /**
     * @return true si el paquete está en tránsito o posterior
     */
    public boolean estaEnTransitoOPosterior() {
        return this.estado.esPosteriorA(EstadoPaquete.LISTO_PARA_DESPACHO);
    }
    
    /**
     * @return true si el paquete puede ser modificado desde bodega
     */
    public boolean puedeModificarseDesdeBodega() {
        return !estaEnTransitoOPosterior();
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Paquete paquete = (Paquete) o;
        return Objects.equals(id, paquete.id);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
    
    @Override
    public String toString() {
        return "Paquete{" +
               "id=" + id +
               ", estado=" + estado +
               ", historial=" + historial.size() + " entradas" +
               '}';
    }
}
