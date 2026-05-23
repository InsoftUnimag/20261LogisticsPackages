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
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Tag(name = "Admisión", description = "Registro y consulta de paquetes")
@RestController
@RequestMapping("/api/paquetes")
@AllArgsConstructor
public class AdmisionController {

    private final RegistrarAdmisionIn registrarAdmisionIn;
    private final ConsultarPaqueteIn consultarPaqueteIn;
    private final PaqueteRepository paqueteRepository;

    @Operation(summary = "Registrar admisión de paquete", description = "Registra un nuevo paquete en el sistema con datos del remitente, destinatario, tipo de mercancía y método de pago. Retorna el ID asignado al paquete")
    @ApiResponse(responseCode = "200", description = "Paquete registrado exitosamente")
    @ApiResponse(responseCode = "400", description = "Error de validación en los datos de entrada")
    @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    @PostMapping("/admision")
    public ResponseEntity<RegistroAdmisionResponse> registrarAdmision(@Valid @RequestBody RegistroAdmisionRequest request) {
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

    @Operation(summary = "Consultar paquete por ID", description = "Obtiene la información básica de un paquete (ID de ruta, ID de paquete y estado actual) dado su ID único")
    @ApiResponse(responseCode = "200", description = "Paquete encontrado")
    @ApiResponse(responseCode = "404", description = "Paquete no encontrado")
    @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    @GetMapping("/{idPaquete}")
    public ResponseEntity<ConsultaPaqueteResponse> consultarPaquete(@PathVariable UUID idPaquete) {
        Optional<Paquete> paqueteOpt = consultarPaqueteIn.consultarPaquete(idPaquete);

        return paqueteOpt
                .map(paquete -> ResponseEntity.ok(new ConsultaPaqueteResponse(
                        paquete.getRutaId(),
                        paquete.getId(),
                        paquete.getEstado())))
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
}
