package com.logistics.packages.application.usecase;

import com.logistics.packages.domain.model.Persona;
import com.logistics.packages.domain.valueobject.Coordenadas;
import com.logistics.packages.domain.valueobject.Direccion;
import com.logistics.packages.domain.valueobject.MetodoPago;
import com.logistics.packages.domain.valueobject.TipoMercancia;
import lombok.Builder;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.UUID;

@Builder
public record RegistroAdmisionCommand(
        UUID sedeId,
        Direccion direccionDestino,
        BigDecimal valorDeclarado,
        MetodoPago metodoPago,
        Persona remitente,
        Persona destinatario,
        TipoMercancia tipoMercancia,
        Boolean indicadorFormaIrregular,
        Double peso,
        Double largo,
        Double ancho,
        Double alto,
        Coordenadas coordenadasManuales,
        UUID usuarioId,
        MultipartFile evidencia
) {
}
