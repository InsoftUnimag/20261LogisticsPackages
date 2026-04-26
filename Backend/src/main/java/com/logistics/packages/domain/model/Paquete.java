package com.logistics.packages.domain.model;

import com.logistics.packages.domain.exception.EstadoTransicionInvalidaException;
import com.logistics.packages.domain.exception.EvidenciaRequeridaException;
import com.logistics.packages.domain.valueobject.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Paquete {
    private UUID id;
    private LocalDateTime fechaIngresoUtc;
    private EstadoPaquete estado;
    private String sedeId;
    private Direccion direccionDestino;
    private Coordenadas coordenadas;
    private EstadoGps estadoGps;
    private BigDecimal valorDeclarado;
    private MetodoPago metodoPago;
    private Persona remitente;
    private Persona destinatario;

    
    // Atributos Físicos y Tarifarios (MOD1-UC-002)
    private Peso peso;
    private Dimensiones dimensiones;
    private Double volumenM3;
    private Double pesoVolumetrico;
    private Double pesoFacturable;
    private TipoMercancia tipoMercancia;
    private CategoriaCarga categoriaCarga;
    private Boolean indicadorFormaIrregular;
    private PrecioEnvio precioEnvio;
    private Double distanciaEstimadaKm;
    private UUID rutaId;
    private UUID zonaAlmacenamientoId;
    private UUID zonaDestinoId;

    @Builder.Default
    private boolean alertaCargaEspecial = false;
    @Builder.Default
    private boolean alertaDensidadAtipica = false;
    
    // Atributos para evidencia de entrega (MOD1-UC-007)
    private String urlEvidenciaEntrega;  // URL de la foto POD (Proof of Delivery)
    private String nombreFirmante;  // Nombre de quien recibe el paquete
    private LocalDateTime fechaEntregaUtc;  // Timestamp de entrega

    public void prePersist() {
        this.id = UUID.randomUUID();
        this.fechaIngresoUtc = LocalDateTime.now(ZoneOffset.UTC);
        this.estado = EstadoPaquete.RECIBIDO_EN_SEDE;
        this.estadoGps = EstadoGps.PENDIENTE;
    }

    public void asignarCoordenadas(Coordenadas coordenadas) {
        this.coordenadas = coordenadas;
        this.estadoGps = EstadoGps.RESUELTO;
    }

    public void asignarPrecio(BigDecimal precio) {
        this.precioEnvio = new PrecioEnvio(precio);
    }

    /**
     * Procesa el pesaje del paquete con los nuevos datos físicos.
     * FR-001, FR-003: Validaciones en los Value Objects Peso y Dimensiones
     * FR-004, FR-005, FR-006: Cálculos de volumen, peso volumétrico y facturable
     * 
     * @param peso Peso del paquete
     * @param dimensiones Dimensiones del paquete
     * @param tipoMercancia Tipo de mercancía
     * @param irregular Indicador de forma irregular
     */
    public void procesarPesaje(Peso peso, Dimensiones dimensiones, TipoMercancia tipoMercancia, boolean irregular) {
        this.peso = peso;
        this.dimensiones = dimensiones;
        this.tipoMercancia = tipoMercancia;
        this.indicadorFormaIrregular = irregular;

        // Cálculos según las reglas de negocio
        this.volumenM3 = calcularVolumen();
        this.pesoVolumetrico = calcularPesoVolumetrico();
        this.pesoFacturable = determinarPesoFacturable();
        this.categoriaCarga = determinarCategoriaCarga();
        verificarDensidadAtipica();
    }

    /**
     * FR-004: Calcula el volumen en metros cúbicos
     * Volumen = (L × A × H) / 1,000,000
     */
    private Double calcularVolumen() {
        if (this.dimensiones != null) {
            return this.dimensiones.calcularVolumenM3();
        }
        return null;
    }

    /**
     * FR-005: Calcula el peso volumétrico
     * Peso Volumétrico = Volumen (m³) × 250 kg/m³
     */
    private Double calcularPesoVolumetrico() {
        if (this.volumenM3 != null) {
            return this.volumenM3 * 250;
        }
        return null;
    }

    /**
     * FR-006: Determina el peso facturable
     * Peso Facturable = MAX(Peso Real, Peso Volumétrico)
     */
    private Double determinarPesoFacturable() {
        if (this.peso != null && this.pesoVolumetrico != null) {
            return Math.max(this.peso.getKilogramos(), this.pesoVolumetrico);
        }
        return null;
    }

    /**
     * FR-002: Determina la categoría de carga
     * Carga Especial si: Peso > 50 kg O Volumen > 0.5 m³
     */
    private CategoriaCarga determinarCategoriaCarga() {
        if (this.peso != null && this.peso.getKilogramos() > 50) {
            this.alertaCargaEspecial = true;
            return CategoriaCarga.CARGA_ESPECIAL;
        }
        if (this.volumenM3 != null && this.volumenM3 > 0.5) {
            this.alertaCargaEspecial = true;
            return CategoriaCarga.CARGA_ESPECIAL;
        }
        return CategoriaCarga.NORMAL;
    }

    /**
     * FR-010: Verifica si hay una densidad atípica
     * Densidad Atípica si: |Peso Real - Peso Volumétrico| / Peso Real > 30%
     */
    private void verificarDensidadAtipica() {
        if (this.peso != null && this.pesoVolumetrico != null) {
            double diferencia = Math.abs(this.peso.getKilogramos() - this.pesoVolumetrico);
            double porcentajeDiferencia = (diferencia / this.peso.getKilogramos());
            if (porcentajeDiferencia > 0.3) {
                this.alertaDensidadAtipica = true;
            }
        }
    }

    public void calcularPrecioEnvio(BigDecimal tarifaBase, BigDecimal tarifaPorKg, BigDecimal tarifaPorKm, BigDecimal recargoTipoMercancia, BigDecimal recargoCategoriaCarga) {
        BigDecimal precio = tarifaBase;
        precio = precio.add(new BigDecimal(this.pesoFacturable).multiply(tarifaPorKg));
        precio = precio.add(new BigDecimal(this.distanciaEstimadaKm).multiply(tarifaPorKm));
        precio = precio.add(recargoTipoMercancia);
        precio = precio.add(recargoCategoriaCarga);
        this.precioEnvio = new PrecioEnvio(precio);
    }

    public void cambiarEstado(EstadoPaquete nuevoEstado) {
        this.estado = nuevoEstado;
    }

    /**
     * FR-003: Asigna una ruta al paquete y cambia su estado a LISTO_PARA_DESPACHO
     * T302 [P] - MOD1-IP-003: No permite reasignar si ya tiene ruta
     * 
     * @param rutaId El ID de la ruta asignada por el Módulo de Gestión de Rutas
     * @throws IllegalArgumentException si el rutaId es nulo
     * @throws IllegalStateException si el paquete ya tiene una ruta asignada
     */
    public void asignarRuta(UUID rutaId) {
        if (rutaId == null) {
            throw new IllegalArgumentException("El ID de ruta no puede ser nulo.");
        }
        if (this.rutaId != null) {
            throw new IllegalStateException("El paquete ya tiene una ruta asignada.");
        }
        this.rutaId = rutaId;
        this.estado = EstadoPaquete.LISTO_PARA_DESPACHO;
    }

    public void asignarZonaAlmacenamiento(UUID zonaAlmacenamientoId) {
        this.zonaAlmacenamientoId = zonaAlmacenamientoId;
        this.estado = EstadoPaquete.EN_CLASIFICACION;
    }

    /**
     * MOD1-IP-005: Asigna una zona de destino al paquete y cambia su estado a LISTO_PARA_DESPACHO.
     * FR-001, FR-002: Registra la clasificación lógica de zona de destino.
     * 
     * @param zonaDestinoId El ID de la zona de destino asignada
     * @throws IllegalArgumentException si el zonaDestinoId es nulo
     * @throws IllegalStateException si el paquete no está en estado EN_CLASIFICACION
     */
    public void asignarZonaDestino(UUID zonaDestinoId) {
        if (zonaDestinoId == null) {
            throw new IllegalArgumentException("El ID de zona de destino no puede ser nulo.");
        }
        if (this.estado != EstadoPaquete.EN_CLASIFICACION) {
            throw new IllegalStateException(
                "El paquete debe estar en estado EN_CLASIFICACION para asignar zona de destino. Estado actual: " + this.estado
            );
        }
        
        this.zonaDestinoId = zonaDestinoId;
        this.estado = EstadoPaquete.LISTO_PARA_DESPACHO;
    }

    /**
     * FR-004: Permite actualizar los datos físicos del paquete cuando se detectan discrepancias.
     * Este método actualiza el peso, dimensiones y recalcula todos los valores derivados.
     * Se debe registrar en el historial la corrección realizada.
     * 
     * @param nuevoPeso Nuevo peso del paquete
     * @param nuevasDimensiones Nuevas dimensiones del paquete
     * @throws IllegalArgumentException si el peso o dimensiones son nulos
     */
    public void actualizarDatosFisicos(Peso nuevoPeso, Dimensiones nuevasDimensiones) {
        if (nuevoPeso == null) {
            throw new IllegalArgumentException("El peso no puede ser nulo al actualizar datos físicos.");
        }
        if (nuevasDimensiones == null) {
            throw new IllegalArgumentException("Las dimensiones no pueden ser nulas al actualizar datos físicos.");
        }

        // Actualizar datos básicos
        this.peso = nuevoPeso;
        this.dimensiones = nuevasDimensiones;

        // Recalcular todos los valores derivados
        this.volumenM3 = calcularVolumen();
        this.pesoVolumetrico = calcularPesoVolumetrico();
        this.pesoFacturable = determinarPesoFacturable();
        this.categoriaCarga = determinarCategoriaCarga();
        verificarDensidadAtipica();
    }

    /**
     * MOD1-UC-006: Registra una novedad en el paquete (dañado o extraviado).
     * FR-004: Requiere evidencia fotográfica obligatoria para tipo DAÑADO
     * FR-006: Solo permite novedades en estados RECIBIDO_EN_SEDE o EN_CLASIFICACION
     * 
     * @param tipo Tipo de novedad (DAÑADO o EXTRAVIADO)
     * @param observaciones Notas descriptivas de la novedad
     * @param usuarioId ID del almacenista responsable del registro
     * @param urlEvidencia URL de la evidencia multimedia (obligatoria para DAÑADO)
     * @return HistorialEstado Registro inmutable de la transición de estado
     * @throws EstadoTransicionInvalidaException si el paquete no está en un estado válido
     * @throws EvidenciaRequeridaException si es tipo DAÑADO y no se proporciona evidencia
     */
    public HistorialEstado registrarNovedad(TipoNovedad tipo, String observaciones, UUID usuarioId, String urlEvidencia) {
        // FR-006: Validar que el estado permita registrar novedades desde bodega
        if (!this.estado.permiteNovedadEnBodega()) {
            throw new EstadoTransicionInvalidaException(this.id, this.estado);
        }
        
        // FR-004: Validar evidencia obligatoria para tipo DAÑADO
        if (tipo == TipoNovedad.DAÑADO && (urlEvidencia == null || urlEvidencia.isBlank())) {
            throw new EvidenciaRequeridaException(this.id);
        }

        // Registrar el estado anterior
        EstadoPaquete estadoAnterior = this.estado;
        
        // Actualizar el estado del paquete
        this.estado = EstadoPaquete.NOVEDAD_EN_BODEGA;

        // Crear y retornar el registro de historial
        return new HistorialEstado(this.id, estadoAnterior, this.estado, observaciones, usuarioId, urlEvidencia);
    }
    
    /**
     * MOD1-UC-007: Transita el paquete al estado EN_TRANSITO.
     * Este método es invocado cuando el Módulo 2 notifica que la ruta ha iniciado.
     * 
     * @param observaciones Notas sobre la transición
     * @param moduloId ID del sistema o módulo responsable
     * @return HistorialEstado Registro de la transición
     * @throws EstadoTransicionInvalidaException si el estado actual no permite esta transición
     */
    public HistorialEstado transitarAEnRuta(String observaciones, UUID moduloId) {
        if (!EstadoPaquete.EN_TRANSITO.esTransicionValidaDesde(this.estado)) {
            throw new EstadoTransicionInvalidaException(this.id, this.estado);
        }
        
        EstadoPaquete estadoAnterior = this.estado;
        this.estado = EstadoPaquete.EN_TRANSITO;
        
        return new HistorialEstado(this.id, estadoAnterior, this.estado, observaciones, moduloId, null);
    }
    
    /**
     * MOD1-UC-007: Transita el paquete al estado EN_PARADA_DE_ENTREGA.
     * Este método es invocado cuando el Módulo 2 notifica que el transportador llegó al destino.
     * 
     * @param observaciones Notas sobre la transición
     * @param moduloId ID del sistema o módulo responsable
     * @return HistorialEstado Registro de la transición
     * @throws EstadoTransicionInvalidaException si el estado actual no permite esta transición
     */
    public HistorialEstado transitarAParadaDeEntrega(String observaciones, UUID moduloId) {
        if (!EstadoPaquete.EN_PARADA_DE_ENTREGA.esTransicionValidaDesde(this.estado)) {
            throw new EstadoTransicionInvalidaException(this.id, this.estado);
        }
        
        EstadoPaquete estadoAnterior = this.estado;
        this.estado = EstadoPaquete.EN_PARADA_DE_ENTREGA;
        
        return new HistorialEstado(this.id, estadoAnterior, this.estado, observaciones, moduloId, null);
    }
    
    /**
     * MOD1-UC-007: Registra la entrega exitosa del paquete.
     * Este método requiere evidencia de entrega (POD) y actualiza el estado a ENTREGADO.
     * 
     * @param urlEvidenciaEntrega URL de la foto/firma de entrega (POD)
     * @param nombreFirmante Nombre de quien recibe el paquete
     * @param observaciones Notas sobre la entrega
     * @param moduloId ID del sistema o módulo responsable
     * @return HistorialEstado Registro de la transición
     * @throws EstadoTransicionInvalidaException si el estado actual no permite esta transición
     * @throws EvidenciaRequeridaException si no se proporciona evidencia
     */
    public HistorialEstado entregarPaquete(String urlEvidenciaEntrega, String nombreFirmante, 
                                          String observaciones, UUID moduloId) {
        if (!EstadoPaquete.ENTREGADO.esTransicionValidaDesde(this.estado)) {
            throw new EstadoTransicionInvalidaException(this.id, this.estado);
        }
        
        if (urlEvidenciaEntrega == null || urlEvidenciaEntrega.isBlank()) {
            throw new EvidenciaRequeridaException(this.id);
        }
        
        EstadoPaquete estadoAnterior = this.estado;
        this.estado = EstadoPaquete.ENTREGADO;
        this.urlEvidenciaEntrega = urlEvidenciaEntrega;
        this.nombreFirmante = nombreFirmante;
        this.fechaEntregaUtc = LocalDateTime.now(ZoneOffset.UTC);
        
        return new HistorialEstado(this.id, estadoAnterior, this.estado, observaciones, moduloId, urlEvidenciaEntrega);
    }
    
    /**
     * MOD1-UC-007: Registra una devolución del paquete desde ruta.
     * FR-009: Solo puede registrarse desde el Módulo de Gestión de Rutas.
     * 
     * @param motivo Motivo de la devolución
     * @param moduloId ID del sistema o módulo responsable
     * @return HistorialEstado Registro de la transición
     * @throws EstadoTransicionInvalidaException si el estado actual no permite esta transición
     */
    public HistorialEstado registrarDevolucionEnRuta(String motivo, UUID moduloId) {
        if (!EstadoPaquete.DEVOLUCION_EN_RUTA.esTransicionValidaDesde(this.estado)) {
            throw new EstadoTransicionInvalidaException(this.id, this.estado);
        }
        
        EstadoPaquete estadoAnterior = this.estado;
        this.estado = EstadoPaquete.DEVOLUCION_EN_RUTA;
        
        return new HistorialEstado(this.id, estadoAnterior, this.estado, motivo, moduloId, null);
    }
    
    /**
     * MOD1-UC-007: Registra el extravío del paquete en ruta.
     * 
     * @param observaciones Descripción del incidente
     * @param moduloId ID del sistema o módulo responsable
     * @return HistorialEstado Registro de la transición
     * @throws EstadoTransicionInvalidaException si el estado actual no permite esta transición
     */
    public HistorialEstado registrarExtraviadoEnRuta(String observaciones, UUID moduloId) {
        if (!EstadoPaquete.EXTRAVIADO_EN_RUTA.esTransicionValidaDesde(this.estado)) {
            throw new EstadoTransicionInvalidaException(this.id, this.estado);
        }
        
        EstadoPaquete estadoAnterior = this.estado;
        this.estado = EstadoPaquete.EXTRAVIADO_EN_RUTA;
        
        return new HistorialEstado(this.id, estadoAnterior, this.estado, observaciones, moduloId, null);
    }
    
    /**
     * MOD1-UC-007: Registra daños en el paquete detectados en ruta.
     * Requiere evidencia fotográfica obligatoria.
     * 
     * @param descripcion Descripción de los daños
     * @param urlEvidencia URL de la evidencia fotográfica
     * @param moduloId ID del sistema o módulo responsable
     * @return HistorialEstado Registro de la transición
     * @throws EstadoTransicionInvalidaException si el estado actual no permite esta transición
     * @throws EvidenciaRequeridaException si no se proporciona evidencia
     */
    public HistorialEstado registrarDañadoEnRuta(String descripcion, String urlEvidencia, UUID moduloId) {
        if (!EstadoPaquete.DAÑADO_EN_RUTA.esTransicionValidaDesde(this.estado)) {
            throw new EstadoTransicionInvalidaException(this.id, this.estado);
        }
        
        if (urlEvidencia == null || urlEvidencia.isBlank()) {
            throw new EvidenciaRequeridaException(this.id);
        }
        
        EstadoPaquete estadoAnterior = this.estado;
        this.estado = EstadoPaquete.DAÑADO_EN_RUTA;
        
        return new HistorialEstado(this.id, estadoAnterior, this.estado, descripcion, moduloId, urlEvidencia);
    }
}
