package com.logistics.packages.application.usecase;

import com.logistics.packages.application.ports.ClasificacionEventPublisher;
import com.logistics.packages.application.ports.PaqueteRepository;
import com.logistics.packages.application.ports.ZonaAlmacenajeRepository;
import com.logistics.packages.domain.exception.PaqueteNotFoundException;
import com.logistics.packages.domain.exception.ZonaSaturadaException;
import com.logistics.packages.domain.model.Paquete;
import com.logistics.packages.domain.model.ZonaAlmacenaje;
import com.logistics.packages.domain.valueobject.Dimensiones;
import com.logistics.packages.domain.valueobject.Peso;
import com.logistics.packages.infrastructure.dto.request.AsignarZonaRequest;
import com.logistics.packages.infrastructure.dto.response.AsignacionZonaResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Caso de uso para preparar un paquete para almacenaje.
 * MOD1-IP-004: Preparar Paquete para Almacenaje
 * 
 * Responsabilidades:
 * - FR-001: Asignar zona de almacenamiento al paquete por UUID
 * - FR-003: Actualizar estado a EN_CLASIFICACION
 * - FR-004: Permitir actualización de datos físicos por discrepancia
 * - FR-006: Bloquear asignación de mercancía incompatible
 * - FR-007: Actualizar contadores de zona atómicamente
 * - FR-008: Manejar zona saturada sugiriendo zona de contingencia
 * - FR-009: Invocar clasificación automáticamente
 */
@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class PrepararAlmacenajeUseCase {

    private final PaqueteRepository paqueteRepository;
    private final ZonaAlmacenajeRepository zonaRepository;
    private final ClasificacionEventPublisher clasificacionEventPublisher;

    /**
     * Prepara un paquete para almacenaje asignándolo a una zona específica.
     * 
     * @param request Solicitud con datos de asignación
     * @return Respuesta con resultado de la operación
     * @throws PaqueteNotFoundException si el paquete no existe
     * @throws ZonaSaturadaException si la zona no tiene capacidad
     */
    public AsignacionZonaResponse prepararParaAlmacenaje(AsignarZonaRequest request) {
        log.info("Iniciando preparación para almacenaje. Paquete: {}, Zona: {}", 
            request.getPaqueteId(), request.getZonaId());

        // 1. Obtener el paquete
        Paquete paquete = paqueteRepository.buscarPorId(request.getPaqueteId())
            .orElseThrow(() -> new PaqueteNotFoundException(request.getPaqueteId()));

        // 2. FR-004: Actualizar datos físicos si hay discrepancias
        boolean datosActualizados = false;
        if (request.tieneDiscrepancias()) {
            log.info("Detectada discrepancia física en paquete {}. Actualizando datos.", 
                paquete.getId());
            
            Peso nuevoPeso = new Peso(request.getDatosDiscrepancia().getPesoKg());
            Dimensiones nuevasDimensiones = new Dimensiones(
                request.getDatosDiscrepancia().getLargoCm(),
                request.getDatosDiscrepancia().getAnchoCm(),
                request.getDatosDiscrepancia().getAltoCm()
            );
            
            paquete.actualizarDatosFisicos(nuevoPeso, nuevasDimensiones);
            datosActualizados = true;
            
            log.info("Datos físicos actualizados para paquete {}. Nuevo peso: {} kg, Nuevo volumen: {} m³",
                paquete.getId(), nuevoPeso.getKilogramos(), paquete.getVolumenM3());
            
            // TODO: Registrar la discrepancia en el historial del paquete
            // historialService.registrarDiscrepancia(paquete.getId(), request.getDatosDiscrepancia());
        }

        // 3. Obtener la zona
        ZonaAlmacenaje zona = zonaRepository.findById(request.getZonaId())
            .orElseThrow(() -> new IllegalArgumentException(
                "La zona con ID " + request.getZonaId() + " no existe."));

        // 4. FR-006, FR-007, FR-008: Validar y agregar paquete a la zona
        // Esto valida compatibilidad, capacidad y actualiza contadores atómicamente
        try {
            zona.agregarPaquete(paquete);
            log.info("Paquete {} agregado a zona {}. Peso actual: {} kg, Volumen actual: {} m³, Paquetes: {}",
                paquete.getId(), zona.getNombre(), zona.getPesoActualKg(), 
                zona.getVolumenActualM3(), zona.getContadorPaquetes());
        } catch (ZonaSaturadaException e) {
            log.warn("Zona {} saturada. Sugiriendo zona de contingencia: {}", 
                zona.getNombre(), e.getZonaContingenciaId());
            
            // Si hay zona de contingencia, retornar información para que el usuario decida
            return AsignacionZonaResponse.builder()
                .paqueteId(paquete.getId())
                .zonaId(zona.getId())
                .nombreZona(zona.getNombre())
                .estadoPaquete(paquete.getEstado())
                .datosActualizados(datosActualizados)
                .usandoZonaContingencia(true)
                .zonaContingenciaId(e.getZonaContingenciaId())
                .mensaje("La zona " + zona.getNombre() + " ha alcanzado su capacidad máxima de " + 
                    e.getTipoCapacidadExcedida() + ". Se sugiere usar la zona de contingencia.")
                .build();
        }

        // 5. FR-003: Asignar zona al paquete y cambiar estado a EN_CLASIFICACION
        paquete.asignarZonaAlmacenamiento(zona.getId());
        
        // 6. Guardar cambios
        paqueteRepository.guardar(paquete);
        zonaRepository.save(zona);
        
        log.info("Paquete {} asignado a zona {}. Estado: {}", 
            paquete.getId(), zona.getNombre(), paquete.getEstado());

        // 7. FR-009: Publicar evento para invocar clasificación automáticamente
        clasificacionEventPublisher.publicarPaqueteListoParaClasificar(paquete.getId());
        log.info("Evento de clasificación publicado para paquete {}", paquete.getId());

        // 8. Retornar respuesta
        return AsignacionZonaResponse.builder()
            .paqueteId(paquete.getId())
            .zonaId(zona.getId())
            .nombreZona(zona.getNombre())
            .estadoPaquete(paquete.getEstado())
            .datosActualizados(datosActualizados)
            .usandoZonaContingencia(false)
            .mensaje("Paquete asignado exitosamente a la zona " + zona.getNombre())
            .build();
    }

    /**
     * Obtiene zonas compatibles con capacidad disponible para un paquete.
     * FR-002: Sugerir zona apropiada según tipo de mercancía
     * 
     * @param paqueteId ID del paquete
     * @param sedeId ID de la sede
     * @return Lista de zonas sugeridas
     */
    public java.util.List<ZonaAlmacenaje> obtenerZonasSugeridas(UUID paqueteId, UUID sedeId) {
        Paquete paquete = paqueteRepository.buscarPorId(paqueteId)
            .orElseThrow(() -> new PaqueteNotFoundException(paqueteId));

        if (paquete.getTipoMercancia() == null) {
            throw new IllegalStateException(
                "El paquete debe tener un tipo de mercancía antes de sugerir zonas.");
        }

        return zonaRepository.findZonasCompatiblesConCapacidad(
            paquete.getTipoMercancia(), 
            sedeId
        );
    }
}
