package com.logistics.packages.infrastructure.controller;

import com.logistics.packages.application.repository.HistorialEstadoRepository;
import com.logistics.packages.application.usecase.ConsultarHistorialPaqueteUseCase;
import com.logistics.packages.application.usecase.novedad.RegistrarNovedadCommand;
import com.logistics.packages.application.usecase.novedad.RegistrarNovedadUseCase;
import com.logistics.packages.application.usecase.novedad.RegistroNovedadResponse;
import com.logistics.packages.domain.model.HistorialEstado;
import com.logistics.packages.infrastructure.dto.request.RegistroNovedadRequest;
import com.logistics.packages.infrastructure.dto.response.HistorialEstadoItemDto;
import com.logistics.packages.infrastructure.dto.response.RegistroNovedadResponseDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import com.logistics.packages.application.ports.UsuarioRepository;

import java.util.List;
import java.util.UUID;

/**
 * Controlador REST para gestionar novedades de paquetes.
 * MOD1-UC-006: Endpoint POST /api/paquetes/{id}/novedades
 * MOD1-UC-007: Endpoint GET /api/paquetes/{id}/historial
 */
@Tag(name = "Novedades", description = "Registro de novedades (dañado o extraviado) en paquetes")
@RestController
@RequestMapping("/api/paquetes")
@RequiredArgsConstructor
@Slf4j
public class NovedadController {
    
    private final RegistrarNovedadUseCase registrarNovedadUseCase;
    private final ConsultarHistorialPaqueteUseCase consultarHistorialPaqueteUseCase;
    private final HistorialEstadoRepository historialEstadoRepository;
    private final UsuarioRepository usuarioRepository;

    /**
     * Registra una novedad en un paquete (dañado o extraviado).
     * Acepta multipart/form-data para incluir el archivo de evidencia.
     * 
     * @param paqueteId ID del paquete
     * @param request Datos de la novedad
     * @param evidencia Archivo de evidencia (obligatorio para tipo DAÑADO)
     * @return ResponseEntity con el resultado del registro
     */
    @Operation(summary = "Registrar novedad en paquete", description = "Registra una novedad (DAÑADO o EXTRAVIADO) en un paquete. Acepta archivo de evidencia en formato multipart/form-data (obligatorio para tipo DAÑADO)")
    @ApiResponse(responseCode = "201", description = "Novedad registrada exitosamente")
    @ApiResponse(responseCode = "400", description = "Error de validación: tipo de novedad inválido o evidencia faltante")
    @ApiResponse(responseCode = "404", description = "Paquete no encontrado")
    @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    @PostMapping(value = "/{paqueteId}/novedades", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<RegistroNovedadResponseDto> registrarNovedad(
            @PathVariable UUID paqueteId,
            @Valid @ModelAttribute RegistroNovedadRequest request,
            @RequestParam(required = false) MultipartFile evidencia) {
        
        log.info("Recibida solicitud de registro de novedad para paquete: {}, tipo: {}", 
            paqueteId, request.getTipoNovedad());
        
        // Obtener usuarioId del contexto de seguridad (usuario autenticado)
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        UUID usuarioId = usuarioRepository.findByUsername(username)
                .map(usuario -> usuario.getId())
                .orElse(null);
        
        // Crear el comando
        RegistrarNovedadCommand command = new RegistrarNovedadCommand(
            paqueteId,
            request.getTipoNovedad(),
            request.getObservaciones(),
            usuarioId,
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

    /**
     * Consulta el historial inmutable de transiciones de estado de un paquete.
     * Expone todas las novedades y cambios de estado ordenados cronológicamente.
     * 
     * @param paqueteId ID del paquete cuyo historial se desea consultar
     * @return ResponseEntity con la lista de registros de historial
     */
    @Operation(summary = "Consultar historial de transiciones de un paquete", description = "Retorna el historial inmutable de todas las transiciones de estado de un paquete, ordenado cronológicamente desde el más antiguo hasta el más reciente")
    @ApiResponse(responseCode = "200", description = "Historial consultado exitosamente")
    @ApiResponse(responseCode = "404", description = "Paquete no encontrado")
    @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    @GetMapping(value = "/{paqueteId}/historial")
    public ResponseEntity<List<HistorialEstadoItemDto>> consultarHistorial(
            @PathVariable UUID paqueteId) {
        
        log.info("Recibida solicitud de consulta de historial para paquete: {}", paqueteId);
        
        // Ejecutar el caso de uso (lanza PaqueteNotFoundException si no existe)
        List<HistorialEstado> historial = consultarHistorialPaqueteUseCase.consultarHistorial(paqueteId);
        
        // Convertir a DTOs para la respuesta HTTP
        List<HistorialEstadoItemDto> dtos = historial.stream()
                .map(h -> HistorialEstadoItemDto.builder()
                        .id(h.getId())
                        .paqueteId(h.getPaqueteId())
                        .estadoAnterior(h.getEstadoAnterior() != null ? h.getEstadoAnterior().name() : null)
                        .estadoNuevo(h.getEstadoNuevo().name())
                        .observaciones(h.getObservaciones())
                        .usuarioId(h.getUsuarioId())
                        .urlEvidencia(h.getUrlEvidencia())
                        .tipoNovedad(h.getTipoNovedad() != null ? h.getTipoNovedad().name() : null)
                        .fechaTransicionUtc(h.getFechaTransicionUtc())
                        .estadoNovedad(h.getEstadoNovedad())
                        .build())
                .toList();
        
        log.info("Historial consultado exitosamente: {} transiciones para paquete: {}", 
                dtos.size(), paqueteId);
        
        return ResponseEntity.ok(dtos);
    }

    /**
     * Actualiza el estado de una novedad (notificar cliente o cerrar).
     * MOD1-UC-007: Exposición de acciones de novedad para el frontend.
     * FE-4: Persiste el cambio en la base de datos para que persista al recargar.
     * 
     * @param paqueteId ID del paquete
     * @param historialId ID del registro de historial/novedad
     * @param action Acción a realizar: "notify" o "close"
     * @return ResponseEntity con resultado de la operación
     */
    @Operation(summary = "Actualizar estado de novedad", description = "Actualiza el estado de una novedad (NOTIFICADO o CERRADO)")
    @ApiResponse(responseCode = "200", description = "Estado actualizado exitosamente")
    @ApiResponse(responseCode = "404", description = "Paquete o novedad no encontrados")
    @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    @PatchMapping("/{paqueteId}/novedades/{historialId}/estado")
    public ResponseEntity<HistorialEstadoItemDto> actualizarEstadoNovedad(
            @PathVariable java.util.UUID paqueteId,
            @PathVariable java.util.UUID historialId,
            @RequestParam String action) {
        
        log.info("Actualizando estado de novedad {} para paquete {}, action: {}", 
                historialId, paqueteId, action);
        
        List<HistorialEstado> historial = historialEstadoRepository.obtenerHistorialPorPaqueteId(paqueteId);
        HistorialEstado novedad = historial.stream()
                .filter(h -> h.getId().equals(historialId) && h.getTipoNovedad() != null)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Novedad no encontrada: " + historialId));
        
        // FE-4: Aplicar lógica según la acción y actualizar en base de datos
        String nuevoEstado = "PENDIENTE";
        if ("notify".equals(action)) {
            nuevoEstado = "NOTIFICADO";
            log.info("Novedad {} notificada al cliente", historialId);
        } else if ("close".equals(action)) {
            nuevoEstado = "CERRADO";
            log.info("Novedad {} cerrada", historialId);
        } else {
            throw new IllegalArgumentException("Acción inválida: " + action);
        }
        
        // Actualizar la novedad con el nuevo estado y guardar en BD
        novedad.setEstadoNovedad(nuevoEstado);
        historialEstadoRepository.guardar(novedad);
        log.info("Estado de novedad {} actualizado a {} en base de datos", historialId, nuevoEstado);
        
        // Construir la respuesta con el estado actualizado
        HistorialEstadoItemDto responseDto = HistorialEstadoItemDto.builder()
                .id(novedad.getId())
                .paqueteId(novedad.getPaqueteId())
                .estadoAnterior(novedad.getEstadoAnterior() != null ? novedad.getEstadoAnterior().name() : null)
                .estadoNuevo(novedad.getEstadoNuevo().name())
                .observaciones(novedad.getObservaciones())
                .usuarioId(novedad.getUsuarioId())
                .urlEvidencia(novedad.getUrlEvidencia())
                .tipoNovedad(novedad.getTipoNovedad() != null ? novedad.getTipoNovedad().name() : null)
                .fechaTransicionUtc(novedad.getFechaTransicionUtc())
                .estadoNovedad(nuevoEstado)
                .build();
        
        return ResponseEntity.ok(responseDto);
    }

    /**
     * Lista todas las novedades activas del sistema.
     * MOD1-UC-007: Control de novedades - obtener lista de todas las novedades pendientes.
     * 
     * @return ResponseEntity con la lista de todas las novedades
     */
    @GetMapping("/novedades")
    public ResponseEntity<List<HistorialEstadoItemDto>> listarNovedades() {
        log.info("Recibida solicitud de listado de todas las novedades");
        
        List<HistorialEstado> novedades = historialEstadoRepository.obtenerTodasLasNovedades();
        
        List<HistorialEstadoItemDto> dtos = novedades.stream()
                .map(h -> HistorialEstadoItemDto.builder()
                        .id(h.getId())
                        .paqueteId(h.getPaqueteId())
                        .estadoAnterior(h.getEstadoAnterior() != null ? h.getEstadoAnterior().name() : null)
                        .estadoNuevo(h.getEstadoNuevo().name())
                        .observaciones(h.getObservaciones())
                        .usuarioId(h.getUsuarioId())
                        .urlEvidencia(h.getUrlEvidencia())
                        .tipoNovedad(h.getTipoNovedad() != null ? h.getTipoNovedad().name() : null)
                        .fechaTransicionUtc(h.getFechaTransicionUtc())
                        .estadoNovedad(h.getEstadoNovedad())
                        .build())
                .toList();
        
        log.info("Listado de novedades completado: {} novedades encontrados", dtos.size());
        return ResponseEntity.ok(dtos);
    }
}
