package com.logistics.packages.infrastructure.controller;

import com.logistics.packages.application.command.NovedadCommand;
import com.logistics.packages.application.response.NovedadRegistradaResponse;
import com.logistics.packages.application.usecase.RegistrarNovedadUseCase;
import com.logistics.packages.infrastructure.dto.request.RegistrarNovedadRequest;
import com.logistics.packages.infrastructure.dto.response.NovedadResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Controlador REST para gestión de novedades en bodega.
 * Adaptador de entrada en la Arquitectura Hexagonal.
 * MOD1-IP-006: Actualizar Estado de Paquete por Novedad
 */
@RestController
@RequestMapping("/api/novedades")
@CrossOrigin(origins = "*")
public class NovedadController {
    
    private final RegistrarNovedadUseCase registrarNovedadUseCase;
    
    public NovedadController(RegistrarNovedadUseCase registrarNovedadUseCase) {
        this.registrarNovedadUseCase = registrarNovedadUseCase;
    }
    
    /**
     * Endpoint para registrar una novedad en bodega.
     * Acepta datos en formato multipart/form-data para soportar archivos.
     * 
     * POST /api/novedades/registrar
     * 
     * @param request Datos de la novedad (JSON o form-data)
     * @param evidencias Archivos multimedia opcionales (obligatorio para tipo DAÑADO)
     * @return Respuesta con información del registro
     */
    @PostMapping(value = "/registrar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<NovedadResponse> registrarNovedad(
            @Valid @ModelAttribute RegistrarNovedadRequest request,
            @RequestParam(value = "evidencias", required = false) List<MultipartFile> evidencias) {
        
        // Convertir archivos MultipartFile a comando
        List<NovedadCommand.ArchivoEvidenciaCommand> archivosCmd = convertirArchivos(evidencias);
        
        // Crear comando
        NovedadCommand command = new NovedadCommand(
            request.getPaqueteId(),
            request.getTipoNovedad(),
            request.getDescripcion(),
            request.getUsuarioResponsable(),
            archivosCmd
        );
        
        // Ejecutar caso de uso
        NovedadRegistradaResponse response = registrarNovedadUseCase.ejecutar(command);
        
        // Convertir respuesta del dominio a DTO
        NovedadResponse dto = new NovedadResponse(
            response.getPaqueteId(),
            response.getEstadoActual(),
            response.getHistorialEntradaId(),
            response.getFechaTransicion(),
            response.getMensaje()
        );
        
        return ResponseEntity.status(HttpStatus.CREATED).body(dto);
    }
    
    /**
     * Endpoint alternativo que acepta JSON puro (sin archivos).
     * Útil para novedades de tipo EXTRAVIADO que no requieren evidencia.
     * 
     * POST /api/novedades
     */
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<NovedadResponse> registrarNovedadJson(
            @Valid @RequestBody RegistrarNovedadRequest request) {
        
        // Crear comando sin archivos
        NovedadCommand command = new NovedadCommand(
            request.getPaqueteId(),
            request.getTipoNovedad(),
            request.getDescripcion(),
            request.getUsuarioResponsable(),
            List.of()
        );
        
        // Ejecutar caso de uso
        NovedadRegistradaResponse response = registrarNovedadUseCase.ejecutar(command);
        
        // Convertir respuesta
        NovedadResponse dto = new NovedadResponse(
            response.getPaqueteId(),
            response.getEstadoActual(),
            response.getHistorialEntradaId(),
            response.getFechaTransicion(),
            response.getMensaje()
        );
        
        return ResponseEntity.status(HttpStatus.CREATED).body(dto);
    }
    
    /**
     * Convierte archivos MultipartFile a comandos de archivo.
     */
    private List<NovedadCommand.ArchivoEvidenciaCommand> convertirArchivos(List<MultipartFile> archivos) {
        if (archivos == null || archivos.isEmpty()) {
            return List.of();
        }
        
        List<NovedadCommand.ArchivoEvidenciaCommand> result = new ArrayList<>();
        
        for (MultipartFile archivo : archivos) {
            if (!archivo.isEmpty()) {
                try {
                    NovedadCommand.ArchivoEvidenciaCommand cmd = new NovedadCommand.ArchivoEvidenciaCommand(
                        archivo.getOriginalFilename(),
                        archivo.getContentType(),
                        archivo.getSize(),
                        archivo.getBytes()
                    );
                    result.add(cmd);
                } catch (IOException e) {
                    throw new RuntimeException("Error al leer archivo: " + archivo.getOriginalFilename(), e);
                }
            }
        }
        
        return result;
    }
}
