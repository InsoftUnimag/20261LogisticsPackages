package com.logistics.packages.infrastructure.controller;

import com.logistics.packages.application.repository.ConsultarPaqueteIn;
import com.logistics.packages.application.repository.PaqueteRepository;
import com.logistics.packages.application.repository.RegistrarAdmisionIn;
import com.logistics.packages.application.usecase.RegistroAdmisionCommand;
import com.logistics.packages.domain.model.Paquete;
import com.logistics.packages.domain.valueobject.EstadoPaquete;
import com.logistics.packages.infrastructure.dto.request.CoordenadasUpdateRequest;
import com.logistics.packages.infrastructure.dto.request.RegistroAdmisionRequest;
import com.logistics.packages.infrastructure.dto.response.ConsultaPaqueteResponse;
import com.logistics.packages.infrastructure.dto.response.PaqueteListadoResponse;
import com.logistics.packages.infrastructure.dto.response.RegistroAdmisionResponse;
import com.logistics.packages.infrastructure.dto.response.TrackingPaqueteResponse;
import com.logistics.packages.infrastructure.dto.response.HistorialEstadoItemDto;
import com.logistics.packages.domain.model.HistorialEstado;
import com.logistics.packages.application.repository.HistorialEstadoRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.security.core.context.SecurityContextHolder;
import com.logistics.packages.application.ports.UsuarioRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Tag(name = "Admisión", description = "Registro y consulta de paquetes")
@RestController
@RequestMapping("/api/paquetes")
@AllArgsConstructor
public class AdmisionController {

    private final RegistrarAdmisionIn registrarAdmisionIn;
    private final ConsultarPaqueteIn consultarPaqueteIn;
    private final PaqueteRepository paqueteRepository;
    private final HistorialEstadoRepository historialEstadoRepository;
    private final UsuarioRepository usuarioRepository;

    @Operation(summary = "Registrar admisión de paquete", description = "Registra un nuevo paquete en el sistema con datos del remitente, destinatario, tipo de mercancía y método de pago. Retorna el ID asignado al paquete")
    @ApiResponse(responseCode = "200", description = "Paquete registrado exitosamente")
    @ApiResponse(responseCode = "400", description = "Error de validación en los datos de entrada")
    @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    @PostMapping(value = "/admision", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<RegistroAdmisionResponse> registrarAdmision(
            @Valid @ModelAttribute RegistroAdmisionRequest request,
            @RequestParam(required = false) MultipartFile evidencia) {
        // FIX Bug 7: Mapear explícitamente PersonaRequest → Persona
        com.logistics.packages.domain.model.Persona remitente = request.getRemitente() != null ?
                com.logistics.packages.domain.model.Persona.builder()
                        .tipoDocumento(request.getRemitente().getTipoDocumento())
                        .numeroDocumento(request.getRemitente().getNumeroDocumento())
                        .nombreCompleto(request.getRemitente().getNombreCompleto())
                        .telefono(request.getRemitente().getTelefono())
                        .correoElectronico(request.getRemitente().getCorreoElectronico())
                        .build()
                : null;

        // FIX Bug 8: Asignar direccionDestino como dirección del destinatario
        com.logistics.packages.domain.model.Persona destinatario = request.getDestinatario() != null ?
                com.logistics.packages.domain.model.Persona.builder()
                        .tipoDocumento(request.getDestinatario().getTipoDocumento())
                        .numeroDocumento(request.getDestinatario().getNumeroDocumento())
                        .nombreCompleto(request.getDestinatario().getNombreCompleto())
                        .telefono(request.getDestinatario().getTelefono())
                        .correoElectronico(request.getDestinatario().getCorreoElectronico())
                        .direccion(request.getDireccionDestino())
                        .build()
                : null;

        // Obtener usuarioId del contexto de seguridad (usuario autenticado)
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        UUID usuarioId = usuarioRepository.findByUsername(username)
                .map(usuario -> usuario.getId())
                .orElse(null);

        RegistroAdmisionCommand command = RegistroAdmisionCommand.builder()
                .sedeId(request.getSedeId())
                .direccionDestino(request.getDireccionDestino())
                .valorDeclarado(request.getValorDeclarado())
                .metodoPago(request.getMetodoPago())
                .remitente(remitente)
                .destinatario(destinatario)
                .tipoMercancia(request.getTipoMercancia())
                .indicadorFormaIrregular(request.getIndicadorFormaIrregular())
                .peso(request.getPeso())
                .largo(request.getLargo())
                .ancho(request.getAncho())
                .alto(request.getAlto())
                .usuarioId(usuarioId)
                .evidencia(evidencia)
                .build();

        UUID paqueteId = registrarAdmisionIn.registrarAdmision(command);
        
        // BE-4: Obtener el paquete guardado para enriquecer la respuesta
        Optional<Paquete> paqueteOpt = paqueteRepository.findById(paqueteId);
        
         if (paqueteOpt.isPresent()) {
             Paquete paquete = paqueteOpt.get();
             return ResponseEntity.ok(RegistroAdmisionResponse.builder()
                     .paqueteId(paquete.getId())
                     .etiquetaDigital(paquete.getEtiquetaDigital())
                     .estadoGps(paquete.getEstadoGps().toString())
                     .estado(paquete.getEstado().toString())
                     .distanciaEstimadaKm(paquete.getDistanciaEstimadaKm())
                     .build());
         }

         return ResponseEntity.ok(RegistroAdmisionResponse.builder()
                 .paqueteId(paqueteId)
                 .build());
    }

    @Operation(summary = "Listar paquetes con paginación y filtros", description = "Obtiene una lista paginada de paquetes con filtros opcionales por estado y rango de fechas de ingreso. Los resultados se ordenan por fecha de ingreso descendente.")
    @ApiResponse(responseCode = "200", description = "Lista de paquetes obtenida exitosamente")
    @GetMapping
    public ResponseEntity<Page<PaqueteListadoResponse>> listarPaquetes(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) EstadoPaquete estado,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaDesde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaHasta) {
        LocalDateTime desde = fechaDesde != null ? fechaDesde.atStartOfDay() : null;
        LocalDateTime hasta = fechaHasta != null ? fechaHasta.plusDays(1).atStartOfDay() : null;
        Page<Paquete> paquetes = paqueteRepository.findAll(estado, desde, hasta, PageRequest.of(page, size));
        return ResponseEntity.ok(paquetes.map(PaqueteListadoResponse::fromDomain));
    }

     @Operation(summary = "Consultar paquete por ID", description = "Obtiene la información de un paquete incluyendo su ruta, estado y datos físicos (peso, dimensiones)")
     @ApiResponse(responseCode = "200", description = "Paquete encontrado")
     @ApiResponse(responseCode = "404", description = "Paquete no encontrado")
     @ApiResponse(responseCode = "500", description = "Error interno del servidor")
     @GetMapping("/{idPaquete}")
     public ResponseEntity<ConsultaPaqueteResponse> consultarPaquete(@PathVariable UUID idPaquete) {
         Optional<Paquete> paqueteOpt = consultarPaqueteIn.consultarPaquete(idPaquete);

         return paqueteOpt
                 .map(paquete -> {
                     Double pesoKg = paquete.getPeso() != null ? paquete.getPeso().getKilogramos() : null;
                     Double largoCm = paquete.getDimensiones() != null ? paquete.getDimensiones().getLargoCm() : null;
                     Double anchoCm = paquete.getDimensiones() != null ? paquete.getDimensiones().getAnchoCm() : null;
                     Double altoCm = paquete.getDimensiones() != null ? paquete.getDimensiones().getAltoCm() : null;
                     
                     return ResponseEntity.ok(ConsultaPaqueteResponse.builder()
                             .rutaId(paquete.getRutaId())
                             .idPaquete(paquete.getId())
                             .estado(paquete.getEstado())
                             .fechaEntregaSujetaConfirmacion(paquete.getRutaId() == null)
                             .pesoKg(pesoKg)
                             .largoCm(largoCm)
                             .anchoCm(anchoCm)
                             .altoCm(altoCm)
                             .build());
                 })
                 .orElse(ResponseEntity.notFound().build());
     }

    @Operation(summary = "Actualizar coordenadas de un paquete", description = "Permite actualizar manualmente las coordenadas de un paquete cuando el GPS está en estado PENDIENTE. FE-5: Contingencia GPS")
    @ApiResponse(responseCode = "200", description = "Coordenadas actualizadas exitosamente")
    @ApiResponse(responseCode = "404", description = "Paquete no encontrado")
    @ApiResponse(responseCode = "400", description = "Datos de coordenadas inválidos")
    @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    @PatchMapping("/{paqueteId}/coordenadas")
    public ResponseEntity<?> actualizarCoordenadas(
            @PathVariable UUID paqueteId,
            @Valid @RequestBody CoordenadasUpdateRequest request) {
        
        Paquete paquete = paqueteRepository.findById(paqueteId)
                .orElseThrow(() -> new com.logistics.packages.domain.exception.PaqueteNotFoundException(paqueteId));
        
        // Crear y asignar las nuevas coordenadas (validaciones del VO se aplican automáticamente)
        com.logistics.packages.domain.valueobject.Coordenadas nuevasCoordenadas = 
                new com.logistics.packages.domain.valueobject.Coordenadas(
                        request.getLatitud(), 
                        request.getLongitud());
        
        paquete.asignarCoordenadas(nuevasCoordenadas);
        paqueteRepository.save(paquete);
        
        return ResponseEntity.ok(Map.of(
                "mensaje", "Coordenadas actualizadas correctamente",
                "paqueteId", paquete.getId().toString(),
                "estadoGps", paquete.getEstadoGps().toString(),
                "coordenadas", Map.of(
                        "latitud", nuevasCoordenadas.latitud(),
                        "longitud", nuevasCoordenadas.longitud()
                )
        ));
    }

    @Operation(summary = "Actualizar datos físicos de un paquete", description = "Actualiza solo peso y dimensiones de un paquete sin afectar su asignación de zona. MOD1-UC-004")
    @ApiResponse(responseCode = "200", description = "Datos físicos actualizados correctamente")
    @ApiResponse(responseCode = "404", description = "Paquete no encontrado")
    @ApiResponse(responseCode = "400", description = "Datos inválidos")
    @PatchMapping("/{paqueteId}/datos-fisicos")
    public ResponseEntity<?> actualizarDatosFisicos(
            @PathVariable UUID paqueteId,
            @RequestBody Map<String, Double> request) {
        
        Paquete paquete = paqueteRepository.findById(paqueteId)
                .orElseThrow(() -> new com.logistics.packages.domain.exception.PaqueteNotFoundException(paqueteId));
        
        Double pesoKg = request.get("pesoKg");
        Double largoCm = request.get("largoCm");
        Double anchoCm = request.get("anchoCm");
        Double altoCm = request.get("altoCm");
        
        if (pesoKg != null && largoCm != null && anchoCm != null && altoCm != null) {
            com.logistics.packages.domain.valueobject.Peso nuevoPeso = new com.logistics.packages.domain.valueobject.Peso(pesoKg);
            com.logistics.packages.domain.valueobject.Dimensiones nuevasDimensiones = 
                    new com.logistics.packages.domain.valueobject.Dimensiones(largoCm, anchoCm, altoCm);
            
            paquete.actualizarDatosFisicos(nuevoPeso, nuevasDimensiones);
            paqueteRepository.save(paquete);
        }
        
        return ResponseEntity.ok(Map.of(
                "mensaje", "Datos físicos actualizados correctamente",
                "paqueteId", paquete.getId().toString(),
                "pesoKg", paquete.getPeso().getKilogramos(),
                "largoCm", paquete.getDimensiones().getLargoCm(),
                "anchoCm", paquete.getDimensiones().getAnchoCm(),
                "altoCm", paquete.getDimensiones().getAltoCm()
        ));
    }

    @Operation(summary = "Consultar tracking en tiempo real de un paquete", description = "Obtiene el estado actual y el historial completo de transiciones de un paquete. FT-3: feature/tracking-paquete-en-ruta")
    @ApiResponse(responseCode = "200", description = "Tracking consultado exitosamente")
    @ApiResponse(responseCode = "404", description = "Paquete no encontrado")
    @ApiResponse(responseCode = "500", description = "Error interno del servidor")
     @GetMapping("/{paqueteId}/tracking")
     public ResponseEntity<TrackingPaqueteResponse> obtenerTracking(@PathVariable UUID paqueteId) {
         Paquete paquete = paqueteRepository.findById(paqueteId)
                 .orElseThrow(() -> new com.logistics.packages.domain.exception.PaqueteNotFoundException(paqueteId));
         
         // Obtener el historial de transiciones
         List<HistorialEstado> historial = historialEstadoRepository.obtenerHistorialPorPaqueteId(paqueteId);
          List<HistorialEstadoItemDto> historialDto = historial.stream()
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
                          .build())
                  .toList();
         
         // Construir la respuesta
         String remitenteNombre = paquete.getRemitente() != null ? paquete.getRemitente().getNombreCompleto() : null;
         String destinatarioNombre = paquete.getDestinatario() != null ? paquete.getDestinatario().getNombreCompleto() : null;
         String ciudadDestino = paquete.getDireccionDestino() != null ? paquete.getDireccionDestino().getCiudad() : null;
         String departamentoDestino = paquete.getDireccionDestino() != null ? paquete.getDireccionDestino().getDepartamento() : null;
         
         TrackingPaqueteResponse response = TrackingPaqueteResponse.builder()
                 .paqueteId(paquete.getId())
                 .etiquetaDigital(paquete.getEtiquetaDigital())
                 .estado(paquete.getEstado().toString())
                 .remitenteNombre(remitenteNombre)
                 .destinatarioNombre(destinatarioNombre)
                 .ciudadDestino(ciudadDestino)
                 .departamentoDestino(departamentoDestino)
                 .distanciaEstimadaKm(paquete.getDistanciaEstimadaKm())
                 .rutaId(paquete.getRutaId())
                 .fechaEntregaUtc(paquete.getFechaEntregaUtc())
                 .nombreFirmante(paquete.getNombreFirmante())
                 .urlEvidenciaEntrega(paquete.getUrlEvidenciaEntrega())
                 .historial(historialDto)
                 .build();
         
         return ResponseEntity.ok(response);
     }

     /**
      * MOD1-IP-009: Endpoint para consulta sincrónica desde Módulo de Facturación (M3).
      * Permite que M3 obtenga el estado actual de un paquete asociado a una ruta.
      * Spec de M3: GET /route/{idRoute}/package/{idPaquete} → { "idPaquete": "...", "estado": "ENTREGADO" }
      * 
      * @param rutaId ID de la ruta (validación de pertenencia)
      * @param paqueteId ID del paquete
      * @return ResponseEntity con { idPaquete, idRuta, estadoActual } o 404
      */
     @Operation(summary = "Consultar estado de paquete por ruta (M3)", description = "Endpoint sincrónico para que el Módulo de Facturación consulte el estado actual de un paquete. Contrato: GET /route/{idRoute}/package/{idPaquete}")
     @ApiResponse(responseCode = "200", description = "Estado del paquete obtenido exitosamente")
     @ApiResponse(responseCode = "404", description = "Paquete o ruta no encontrados")
     @ApiResponse(responseCode = "500", description = "Error interno del servidor")
     @GetMapping("/route/{rutaId}/package/{paqueteId}")
     public ResponseEntity<Map<String, Object>> consultarEstadoPaquetePorRuta(
             @PathVariable UUID rutaId,
             @PathVariable UUID paqueteId) {
         
         log.info("Consulta de estado de paquete desde M3: rutaId={}, paqueteId={}", rutaId, paqueteId);
         
         Paquete paquete = paqueteRepository.findById(paqueteId)
                 .orElseThrow(() -> new com.logistics.packages.domain.exception.PaqueteNotFoundException(paqueteId));
         
         // Validar que el paquete pertenezca a la ruta consultada
         if (paquete.getRutaId() == null || !paquete.getRutaId().equals(rutaId)) {
             log.warn("Intento de consulta de paquete {} con ruta incorrecta. Ruta esperada: {}, Ruta del paquete: {}",
                     paqueteId, rutaId, paquete.getRutaId());
             return ResponseEntity.notFound().build();
         }
         
         Map<String, Object> response = new LinkedHashMap<>();
         response.put("idPaquete", paquete.getId());
         response.put("idRuta", rutaId);
         response.put("estadoActual", paquete.getEstado().name());
         
         log.info("Estado consultado exitosamente: paqueteId={}, estado={}", paqueteId, paquete.getEstado());
         
         return ResponseEntity.ok(response);
     }
}
