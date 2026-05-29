package com.logistics.packages.infrastructure.controller;

import com.logistics.packages.application.ports.SedeRepository;
import com.logistics.packages.application.usecase.PesajeCommand;
import com.logistics.packages.application.usecase.PesajeResponse;
import com.logistics.packages.application.usecase.ProcesarPesajeUseCase;
import com.logistics.packages.application.usecase.SolicitarRutaUseCase;
import com.logistics.packages.domain.event.SolicitudRutaEvent;
import com.logistics.packages.domain.model.Sede;
import com.logistics.packages.domain.valueobject.Dimensiones;
import com.logistics.packages.domain.valueobject.Peso;
import com.logistics.packages.domain.valueobject.TipoMercancia;
import com.logistics.packages.infrastructure.config.TarifasConfigProperties;
import com.logistics.packages.infrastructure.dto.request.PesajeRequest;
import com.logistics.packages.infrastructure.dto.response.PesajeResponseDto;
import com.logistics.packages.application.repository.PaqueteRepository;
import com.logistics.packages.domain.model.Paquete;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * Controlador REST para el procesamiento de pesaje de paquetes (MOD1-UC-002)
 * T208: Expone el endpoint POST /api/paquetes/pesaje
 * 
 * BE-3: Las tarifas se leen desde TarifasConfigProperties, no del cliente
 */
@Tag(name = "Pesaje", description = "Procesamiento de pesaje y dimensiones de paquetes")
@Slf4j
@RestController
@RequestMapping("/api/paquetes")
@RequiredArgsConstructor
public class PesajeController {

    private final ProcesarPesajeUseCase procesarPesajeUseCase;
    private final SolicitarRutaUseCase solicitarRutaUseCase;
    private final TarifasConfigProperties tarifasConfig;
    private final SedeRepository sedeRepository;
    private final PaqueteRepository paqueteRepository;

    /**
     * Procesa el pesaje de un paquete existente.
     * 
     * @param request Datos del pesaje con validaciones
     * @return Respuesta con el precio calculado y alertas
     */
    @Operation(summary = "Procesar pesaje de paquete", description = "Procesa el pesaje y dimensiones de un paquete existente. Calcula peso volumétrico, peso facturable, categoría de carga, precio de envío y genera alertas si aplica")
    @ApiResponse(responseCode = "200", description = "Pesaje procesado exitosamente con precio calculado")
    @ApiResponse(responseCode = "400", description = "Error de validación en los datos de pesaje")
    @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    @PostMapping(value = "/pesaje", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<PesajeResponseDto> procesarPesaje(
            @Valid @ModelAttribute PesajeRequest request,
            @RequestParam(required = false) MultipartFile evidencia) {
        log.info("Procesando pesaje para paquete ID: {}", request.getPaqueteId());
        
        try {
            // Convertir DTO a Value Objects del dominio
            Peso peso = new Peso(request.getPeso());
            Dimensiones dimensiones = new Dimensiones(
                    request.getLargoCm(),
                    request.getAnchoCm(),
                    request.getAltoCm()
            );
            
            // Crear el comando con tarifas desde la configuración (BE-3)
            TipoMercancia tipoMercancia = request.getTipoMercancia() != null ? request.getTipoMercancia() : TipoMercancia.ESTANDAR;
            
             // BE-3: Obtener tarifas de la sede si existen, si no usar globales
             Paquete paquete = paqueteRepository.findById(request.getPaqueteId())
                     .orElseThrow(() -> new IllegalArgumentException("Paquete no encontrado"));
             
             java.math.BigDecimal tarifaBase = tarifasConfig.getBase();
             java.math.BigDecimal tarifaPorKg = tarifasConfig.getPorKg();
             java.math.BigDecimal tarifaPorKm = tarifasConfig.getPorKm();
             
             if (paquete.getSedeId() != null) {
                 try {
                     Sede sede = sedeRepository.findById(paquete.getSedeId())
                             .orElse(null);
                     if (sede != null) {
                         tarifaBase = sede.getTarifaBase() != null ? sede.getTarifaBase() : tarifaBase;
                         tarifaPorKg = sede.getTarifaPorKg() != null ? sede.getTarifaPorKg() : tarifaPorKg;
                         tarifaPorKm = sede.getTarifaPorKm() != null ? sede.getTarifaPorKm() : tarifaPorKm;
                         log.info("Usando tarifas de sede {} para paquete {}", paquete.getSedeId(), request.getPaqueteId());
                     }
                 } catch (Exception e) {
                     log.warn("Error al obtener tarifas de sede: {}, usando tarifas globales", e.getMessage());
                 }
             }
             
             // BE-3: Seleccionar el recargo según el tipo de mercancía
             java.math.BigDecimal recargoMercancia;
             switch (tipoMercancia) {
                 case FRAGIL -> recargoMercancia = tarifasConfig.getRecargoFragil();
                 case PELIGROSO -> recargoMercancia = tarifasConfig.getRecargoPeligroso();
                 default -> recargoMercancia = java.math.BigDecimal.ZERO;
             }
             
             PesajeCommand command = PesajeCommand.builder()
                     .paqueteId(request.getPaqueteId())
                     .peso(peso)
                     .dimensiones(dimensiones)
                     .tipoMercancia(tipoMercancia)
                     .formaIrregular(request.getFormaIrregular() != null ? request.getFormaIrregular() : false)
                     .tarifaBase(tarifaBase)
                     .tarifaPorKg(tarifaPorKg)
                     .tarifaPorKm(tarifaPorKm)
                     .recargoTipoMercancia(recargoMercancia)
                     .recargoCategoriaCarga(tarifasConfig.getRecargoCargaEspecial())
                     .evidencia(evidencia)
                     .build();
            
            // Ejecutar el caso de uso
            PesajeResponse response = procesarPesajeUseCase.procesarPesaje(command);
            
            // Convertir la respuesta a DTO
            PesajeResponseDto responseDto = PesajeResponseDto.builder()
                    .paqueteId(response.getPaqueteId())
                    .peso(response.getPeso())
                    .volumenM3(response.getVolumenM3())
                    .pesoVolumetrico(response.getPesoVolumetrico())
                    .pesoFacturable(response.getPesoFacturable())
                    .categoriaCarga(response.getCategoriaCarga())
                    .precioEnvio(response.getPrecioEnvio())
                    .build();
            
            // Agregar alertas si aplican
            responseDto.agregarAlertaSiAplica(
                    response.isAlertaCargaEspecial(),
                    response.isAlertaDensidadAtipica()
            );
            
            log.info("Pesaje procesado exitosamente. Precio: {}, Alertas: {}", 
                    response.getPrecioEnvio(), responseDto.getAlertas().size());
            
            // MOD1-UC-002 Fix: Solicitar ruta al backend del Módulo 2 después de confirmar pesaje
            SolicitudRutaEvent evento = SolicitudRutaEvent.of(request.getPaqueteId());
            solicitarRutaUseCase.handle(evento);
            log.info("Solicitud de ruta disparada para paquete: {}", request.getPaqueteId());
            
            return ResponseEntity.ok(responseDto);
            
        } catch (IllegalArgumentException e) {
            log.error("Error de validación en pesaje: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Error procesando pesaje para paquete {}: {}", request.getPaqueteId(), e.getMessage());
            throw e;
        }
    }
    
    /**
     * Health check endpoint
     */
    @Operation(summary = "Health check de pesaje", description = "Endpoint de verificación del servicio de pesaje")
    @ApiResponse(responseCode = "200", description = "Servicio operativo")
    @GetMapping("/pesaje/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Servicio de pesaje operativo");
    }
}
