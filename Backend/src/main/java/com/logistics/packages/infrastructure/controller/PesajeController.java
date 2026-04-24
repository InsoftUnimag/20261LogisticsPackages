package com.logistics.packages.infrastructure.controller;

import com.logistics.packages.application.usecase.PesajeCommand;
import com.logistics.packages.application.usecase.PesajeResponse;
import com.logistics.packages.application.usecase.ProcesarPesajeUseCase;
import com.logistics.packages.domain.valueobject.Dimensiones;
import com.logistics.packages.domain.valueobject.Peso;
import com.logistics.packages.infrastructure.dto.request.PesajeRequest;
import com.logistics.packages.infrastructure.dto.response.PesajeResponseDto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controlador REST para el procesamiento de pesaje de paquetes (MOD1-UC-002)
 * T208: Expone el endpoint POST /api/paquetes/pesaje
 */
@Slf4j
@RestController
@RequestMapping("/api/paquetes")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class PesajeController {

    private final ProcesarPesajeUseCase procesarPesajeUseCase;

    /**
     * Procesa el pesaje de un paquete existente.
     * 
     * @param request Datos del pesaje con validaciones
     * @return Respuesta con el precio calculado y alertas
     */
    @PostMapping("/pesaje")
    public ResponseEntity<PesajeResponseDto> procesarPesaje(@Valid @RequestBody PesajeRequest request) {
        log.info("Procesando pesaje para paquete ID: {}", request.getPaqueteId());
        
        try {
            // Convertir DTO a Value Objects del dominio
            Peso peso = new Peso(request.getPeso());
            Dimensiones dimensiones = new Dimensiones(
                    request.getLargoCm(),
                    request.getAnchoCm(),
                    request.getAltoCm()
            );
            
            // Crear el comando
            PesajeCommand command = PesajeCommand.builder()
                    .paqueteId(request.getPaqueteId())
                    .peso(peso)
                    .dimensiones(dimensiones)
                    .tipoMercancia(request.getTipoMercancia())
                    .formaIrregular(request.getFormaIrregular())
                    .tarifaBase(request.getTarifaBase())
                    .tarifaPorKg(request.getTarifaPorKg())
                    .tarifaPorKm(request.getTarifaPorKm())
                    .recargoTipoMercancia(request.getRecargoTipoMercancia())
                    .recargoCategoriaCarga(request.getRecargoCategoriaCarga())
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
    @GetMapping("/pesaje/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Servicio de pesaje operativo");
    }
}
