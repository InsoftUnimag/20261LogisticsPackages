package com.logistics.packages.application.usecase.novedad;

import com.logistics.packages.domain.valueobject.TipoNovedad;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

/**
 * Comando para registrar una novedad en un paquete.
 * MOD1-UC-006: Encapsula los datos necesarios para el caso de uso.
 */
@Getter
@AllArgsConstructor
public class RegistrarNovedadCommand {
    
    /**
     * ID del paquete donde se registrará la novedad
     */
    private final UUID paqueteId;
    
    /**
     * Tipo de novedad (DAÑADO o EXTRAVIADO)
     */
    private final TipoNovedad tipoNovedad;
    
    /**
     * Observaciones descriptivas de la novedad
     */
    private final String observaciones;
    
    /**
     * ID del usuario (almacenista) responsable del registro
     */
    private final UUID usuarioId;
    
    /**
     * Archivo de evidencia multimedia (obligatorio para tipo DAÑADO)
     */
    private final MultipartFile evidencia;
}
