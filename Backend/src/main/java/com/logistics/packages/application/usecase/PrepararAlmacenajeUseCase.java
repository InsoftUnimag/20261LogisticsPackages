package com.logistics.packages.application.usecase;

import com.logistics.packages.application.repository.HistorialEstadoRepository;
import com.logistics.packages.application.repository.PaqueteRepository;
import com.logistics.packages.application.repository.PrepararAlmacenajeIn;
import com.logistics.packages.application.repository.ZonaAlmacenajeRepository;
import com.logistics.packages.application.usecase.ClasificarPaqueteUseCase;
import com.logistics.packages.domain.exception.PaqueteNotFoundException;
import com.logistics.packages.domain.exception.ZonaAlmacenajeNotFoundException;
import com.logistics.packages.domain.exception.ZonaIncompatibleException;
import com.logistics.packages.domain.exception.ZonaSaturadaException;
import com.logistics.packages.domain.model.HistorialEstado;
import com.logistics.packages.domain.model.Paquete;
import com.logistics.packages.domain.model.ZonaAlmacenaje;
import com.logistics.packages.domain.valueobject.Dimensiones;
import com.logistics.packages.domain.valueobject.EstadoPaquete;
import com.logistics.packages.domain.valueobject.Peso;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Caso de uso para preparar un paquete para almacenaje.
 * MOD1-IP-004: T412
 * 
 * Responsabilidades:
 * - FR-001: Asignar paquete a zona por UUID
 * - FR-004: Actualizar datos físicos por discrepancia y registrar en historial
 * - FR-007: Actualizar atómicamente los tres contadores de zona
 * - FR-009: Publicar evento de clasificación al confirmar
 */
@Service
@Transactional
@AllArgsConstructor
@Slf4j
public class PrepararAlmacenajeUseCase implements PrepararAlmacenajeIn {

    private final PaqueteRepository paqueteRepository;
    private final ZonaAlmacenajeRepository zonaAlmacenajeRepository;
    private final ClasificarPaqueteUseCase clasificarPaqueteUseCase;
    private final HistorialEstadoRepository historialEstadoRepository;
    
    // ID del sistema para registros de transiciones automáticas
    private static final UUID SISTEMA_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");

    @Override
    public void prepararAlmacenaje(PrepararAlmacenajeCommand command) {
        log.info("Iniciando preparación de almacenaje para paquete: {}", command.paqueteId());
        
        // 1. Cargar el paquete
        Paquete paquete = paqueteRepository.findById(command.paqueteId())
                .orElseThrow(() -> new PaqueteNotFoundException(command.paqueteId()));

        // 2. FR-004: Actualizar datos físicos si hay discrepancia
        if (command.tieneDiscrepancias()) {
            var datos = command.datosDiscrepancia().get();
            Peso nuevoPeso = new Peso(datos.getPesoKg());
            Dimensiones nuevasDimensiones = new Dimensiones(
                    datos.getLargoCm(),
                    datos.getAnchoCm(),
                    datos.getAltoCm()
            );
            
            paquete.actualizarDatosFisicos(nuevoPeso, nuevasDimensiones);
            
            // Registrar la corrección en el historial
            HistorialEstado historialDiscrepancia = new HistorialEstado(
                    paquete.getId(),
                    paquete.getEstado(),
                    paquete.getEstado(), // Estado no cambia, solo se corrigen datos
                    "Corrección de datos físicos: " + datos.getObservaciones(),
                    command.usuarioId(),
                    null // No hay evidencia para correcciones
            );
            historialEstadoRepository.guardar(historialDiscrepancia);
            log.info("Datos físicos actualizados y registrados en historial para paquete: {}", paquete.getId());
        }

        // 3. Cargar la zona
        ZonaAlmacenaje zona = zonaAlmacenajeRepository.findById(command.zonaAlmacenamientoId())
                .orElseThrow(() -> new ZonaAlmacenajeNotFoundException(command.zonaAlmacenamientoId()));

        // 4. Validar compatibilidad
        if (!zona.puedeAlbergar(paquete)) {
            throw new ZonaIncompatibleException(paquete.getId(), paquete.getTipoMercancia(), zona.getId(), zona.getCategoria());
        }

        // 5. Validar capacidad
        if (!zona.tieneCapacidadPara(paquete)) {
            throw new ZonaSaturadaException(zona.getId(), zona.getNombre(), "capacidad", zona.getZonaContingenciaId());
        }

        // 6. FR-007: Asignar zona y actualizar contadores atómicamente
        paquete.asignarZonaAlmacenamiento(zona.getId());
        zona.agregarPaquete(paquete);

        // 7. Persistir cambios
        paqueteRepository.save(paquete);
        zonaAlmacenajeRepository.save(zona);
        
        // Registrar en el historial la transición: RECIBIDO_EN_SEDE → EN_CLASIFICACION
        HistorialEstado historialTransicion = new HistorialEstado(
                paquete.getId(),
                EstadoPaquete.RECIBIDO_EN_SEDE,
                EstadoPaquete.EN_CLASIFICACION,
                "Paquete asignado a zona de almacenaje: " + zona.getNombre(),
                command.usuarioId(),
                null // Sin evidencia para transición de almacenaje
        );
        historialEstadoRepository.guardar(historialTransicion);
        
        log.info("Paquete {} asignado a zona {} con estado EN_CLASIFICACION", 
                paquete.getId(), zona.getNombre());

        // 8. FR-009: Invocar clasificación directamente (reemplaza SQS interno)
        ClasificacionSugeridaResponse sugerencia = clasificarPaqueteUseCase.sugerirZonaParaPaquete(paquete.getId());
        log.info("Clasificación sugerida para paquete {}: zona {}", paquete.getId(), sugerencia.getNombreZona());
    }
}
