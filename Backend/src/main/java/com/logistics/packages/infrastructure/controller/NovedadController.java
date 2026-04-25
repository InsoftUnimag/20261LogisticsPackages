package com.logistics.packages.infrastructure.controller;

import com.logistics.packages.application.usecase.novedad.RegistrarNovedadCommand;
import com.logistics.packages.application.usecase.novedad.RegistrarNovedadUseCase;
import com.logistics.packages.application.usecase.novedad.RegistroNovedadResponse;
import com.logistics.packages.infrastructure.dto.request.RegistroNovedadRequest;
import com.logistics.packages.infrastructure.dto.response.RegistroNovedadResponseDto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

/**
 * Controlador REST para gestionar novedades de paquetes.
 * MOD1-UC-006: Endpoint POST /api/paquetes/{id}/novedades
 */
@RestController
@RequestMapping("/api/paquetes")
@RequiredArgsConstructor
@Slf4j
public class NovedadController {
    
    private final RegistrarNovedadUseCase registrarNovedadUseCase;

    /**
     * Registra una novedad en un paquete (dañado o extraviado).
     * Acepta multipart/form-data para incluir el archivo de evidencia.
     * 
     * @param paqueteId ID del paquete
     * @param request Datos de la novedad
     * @param evidencia Archivo de evidencia (obligatorio para tipo DAÑADO)
     * @return ResponseEntity con el resultado del registro
     */
    @PostMapping(value = "/{paqueteId}/novedades", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<RegistroNovedadResponseDto> registrarNovedad(
            @PathVariable UUID paqueteId,
            @Valid @ModelAttribute RegistroNovedadRequest request,
            @RequestParam(required = false) MultipartFile evidencia) {
        
        log.info("Recibida solicitud de registro de novedad para paquete: {}, tipo: {}", 
            paqueteId, request.getTipoNovedad());
        
        // Crear el comando
        RegistrarNovedadCommand command = new RegistrarNovedadCommand(
            paqueteId,
            request.getTipoNovedad(),
            request.getObservaciones(),
            request.getUsuarioId(),
            evidencia
        );
        
        // Ejecutar el caso de uso
        RegistroNovedadResponse response = registrarNovedadUseCase.registrarNovedad(command);
        
        // Construir la respuesta HTTP
        RegistroNovedadResponseDto responseDto = new RegistroNovedadResponseDto(
            response.getPaqueteId(),
            response.getEstadoActual(),
            response.getHistorialId(),
            "Novedad registrada exitosamente"
        );
        
        log.info("Novedad registrada exitosamente para paquete: {}, historial ID: {}", 
            paqueteId, response.getHistorialId());
        
        return ResponseEntity.status(HttpStatus.CREATED).body(responseDto);
    }
}
