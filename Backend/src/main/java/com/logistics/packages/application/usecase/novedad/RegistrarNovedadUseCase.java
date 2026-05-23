package com.logistics.packages.application.usecase.novedad;

import com.logistics.packages.application.ports.EstadoPaqueteFinanzasPublisher;
import com.logistics.packages.application.repository.ArchivoStoragePort;
import com.logistics.packages.application.repository.HistorialEstadoRepository;
import com.logistics.packages.application.repository.NovedadEventPublisher;
import com.logistics.packages.application.repository.PaqueteRepository;
import com.logistics.packages.domain.exception.PaqueteNotFoundException;
import com.logistics.packages.domain.model.HistorialEstado;
import com.logistics.packages.domain.model.Paquete;
import com.logistics.packages.domain.valueobject.NovedadBodega;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Caso de uso para registrar novedades en paquetes (dañados o extraviados).
 * MOD1-UC-006: Orquesta el flujo completo de registro de novedad.
 * 
 * Responsabilidades:
 * 1. Validar existencia del paquete
 * 2. Guardar evidencia multimedia (si aplica)
 * 3. Registrar la novedad en el dominio del paquete
 * 4. Persistir cambios en paquete e historial
 * 5. Publicar evento para notificar al Controlador de Novedades
 */
@Service
@RequiredArgsConstructor
public class RegistrarNovedadUseCase {

    private final PaqueteRepository paqueteRepository;
    private final HistorialEstadoRepository historialRepository;
    private final ArchivoStoragePort archivoStoragePort;
    private final NovedadEventPublisher novedadEventPublisher;
    private final EstadoPaqueteFinanzasPublisher estadoPaqueteFinanzasPublisher;

    /**
     * Ejecuta el registro de una novedad en un paquete.
     * 
     * @param command Comando con los datos de la novedad a registrar
     * @return RegistroNovedadResponse con información del resultado
     * @throws PaqueteNotFoundException si el paquete no existe
     * @throws EstadoTransicionInvalidaException si el paquete no está en estado válido
     * @throws EvidenciaRequeridaException si falta evidencia obligatoria para tipo DAÑADO
     */
    @Transactional
    public RegistroNovedadResponse registrarNovedad(RegistrarNovedadCommand command) {
        // 1. Validar existencia del paquete
        Paquete paquete = paqueteRepository.findById(command.getPaqueteId())
            .orElseThrow(() -> new PaqueteNotFoundException(command.getPaqueteId()));

        // 2. Guardar evidencia multimedia si se proporcionó
        String urlEvidencia = null;
        if (command.getEvidencia() != null && !command.getEvidencia().isEmpty()) {
            urlEvidencia = archivoStoragePort.guardar(
                "novedades",
                command.getPaqueteId().toString(),
                command.getEvidencia()
            );
        }

        // 3. Registrar la novedad en el dominio (aplica las validaciones de negocio)
        NovedadBodega novedad = new NovedadBodega(
            command.getTipoNovedad(),
            command.getObservaciones(),
            command.getUsuarioId(),
            urlEvidencia
        );
        HistorialEstado historial = paquete.registrarNovedad(novedad);

        // 4. Persistir cambios en base de datos
        paqueteRepository.save(paquete);
        historialRepository.guardar(historial);

        // 5. Publicar evento para notificar al Controlador de Novedades (FR-005)
        novedadEventPublisher.publicarNovedadRegistrada(paquete.getId(), historial.getId());

        // Publicar estado actualizado a la cola de Finanzas (M3)
        estadoPaqueteFinanzasPublisher.publicarEstadoFinal(paquete);

        // 6. Retornar respuesta
        return new RegistroNovedadResponse(
            paquete.getId(),
            paquete.getEstado(),
            historial.getId()
        );
    }
}
