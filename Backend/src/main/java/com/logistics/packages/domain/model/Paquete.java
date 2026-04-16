package com.logistics.packages.domain.model;

import com.logistics.packages.domain.valueobject.Coordenadas;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

@Getter
public class Paquete {
    private final UUID id;
    private final LocalDateTime fechaIngresoUtc;
    private final String sedeId;
    
    @Setter
    private EstadoPaquete estado;
    
    private final String direccionDestino;
    
    @Setter
    private Coordenadas coordenadas;
    
    @Setter
    private EstadoGps estadoGps;
    
    private final BigDecimal valorDeclarado;
    private final MetodoPago metodoPago;
    private final Persona remitente;
    private final Persona destinatario;

    // Atributos físicos (UC-002) - se inicializarán después
    @Setter
    private Double peso;
    @Setter
    private Double largo;
    @Setter
    private Double ancho;
    @Setter
    private Double alto;

    @Builder
    public Paquete(String sedeId, String direccionDestino, BigDecimal valorDeclarado, 
                   MetodoPago metodoPago, Persona remitente, Persona destinatario) {
        this.id = UUID.randomUUID();
        this.fechaIngresoUtc = LocalDateTime.now(ZoneOffset.UTC);
        this.estado = EstadoPaquete.RECIBIDO_EN_SEDE;
        this.sedeId = sedeId;
        this.direccionDestino = direccionDestino;
        this.valorDeclarado = valorDeclarado;
        this.metodoPago = metodoPago;
        this.remitente = remitente;
        this.destinatario = destinatario;
        this.estadoGps = EstadoGps.PENDIENTE;
    }

    public void asignarCoordenadas(Coordenadas coordenadas) {
        this.coordenadas = coordenadas;
        this.estadoGps = EstadoGps.RESUELTO;
    }
}
