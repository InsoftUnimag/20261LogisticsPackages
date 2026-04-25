package com.logistics.packages.application.usecase;

import com.logistics.packages.application.command.NovedadCommand;
import com.logistics.packages.application.response.NovedadRegistradaResponse;
import com.logistics.packages.domain.exception.PaqueteNoEncontradoException;
import com.logistics.packages.domain.model.HistorialEstado;
import com.logistics.packages.domain.model.NovedadBodega;
import com.logistics.packages.domain.model.Paquete;
import com.logistics.packages.domain.repository.HistorialRepository;
import com.logistics.packages.domain.repository.NovedadRepository;
import com.logistics.packages.domain.repository.PaqueteRepository;
import com.logistics.packages.domain.service.EvidenciaStorageService;
import com.logistics.packages.domain.service.NovedadEventPublisher;
import com.logistics.packages.domain.service.ValidadorTransicionEstado;
import com.logistics.packages.domain.valueobject.Evidencia;
import com.logistics.packages.domain.valueobject.EstadoPaquete;
import com.logistics.packages.domain.valueobject.TipoArchivo;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;

/**
 * Caso de uso para registrar una novedad en bodega.
 * Orquesta la lógica de negocio, almacenamiento de evidencias y notificaciones.
 * MOD1-IP-006: Actualizar Estado de Paquete por Novedad
 * 
 * Aplica principios SOLID:
 * - Single Responsibility: Solo se encarga de registrar novedades
 * - Dependency Inversion: Depende de abstracciones (interfaces), no de implementaciones
 */
public class RegistrarNovedadUseCase {
    
    private final PaqueteRepository paqueteRepository;
    private final HistorialRepository historialRepository;
    private final NovedadRepository novedadRepository;
    private final EvidenciaStorageService evidenciaStorage;
    private final NovedadEventPublisher eventPublisher;
    private final ValidadorTransicionEstado validador;
    
    /**
     * Constructor con inyección de dependencias.
     * Las dependencias son inyectadas por el framework (Spring).
     */
    public RegistrarNovedadUseCase(PaqueteRepository paqueteRepository,
                                  HistorialRepository historialRepository,
                                  NovedadRepository novedadRepository,
                                  EvidenciaStorageService evidenciaStorage,
                                  NovedadEventPublisher eventPublisher,
                                  ValidadorTransicionEstado validador) {
        this.paqueteRepository = paqueteRepository;
        this.historialRepository = historialRepository;
        this.novedadRepository = novedadRepository;
        this.evidenciaStorage = evidenciaStorage;
        this.eventPublisher = eventPublisher;
        this.validador = validador;
    }
    
    /**
     * Ejecuta el caso de uso de registrar una novedad.
     * 
     * @param command Comando con los datos de la novedad
     * @return Respuesta con información del registro
     * @throws PaqueteNoEncontradoException si el paquete no existe
     * @throws com.logistics.packages.domain.exception.PaqueteEnTransitoException si el paquete ya está en tránsito
     * @throws com.logistics.packages.domain.exception.EvidenciaRequeridaException si falta evidencia obligatoria
     */
    public NovedadRegistradaResponse ejecutar(NovedadCommand command) {
        
        // 1. Obtener el paquete (lanza excepción si no existe)
        Paquete paquete = paqueteRepository.findById(command.getPaqueteId())
            .orElseThrow(() -> new PaqueteNoEncontradoException(command.getPaqueteId()));
        
        // 2. Validar que la transición sea permitida
        validador.validarTransicion(paquete.getEstado(), EstadoPaquete.NOVEDAD_EN_BODEGA);
        
        // 3. Procesar y almacenar evidencias en S3 (si existen)
        List<Evidencia> evidencias = procesarEvidencias(command, paquete.getId());
        
        // 4. Crear la entidad de dominio NovedadBodega (valida regla de evidencia obligatoria)
        NovedadBodega novedad = new NovedadBodega(
            command.getPaqueteId(),
            command.getTipoNovedad(),
            command.getDescripcion(),
            command.getUsuarioResponsable(),
            evidencias
        );
        
        // 5. Registrar la novedad en el paquete (actualiza estado y crea historial)
        HistorialEstado entrada = paquete.registrarNovedad(
            command.getTipoNovedad(),
            command.getDescripcion(),
            command.getUsuarioResponsable(),
            evidencias
        );
        
        // 6. Persistir cambios en el orden correcto
        paqueteRepository.save(paquete);
        historialRepository.save(entrada);
        novedadRepository.save(novedad);
        
        // 7. FR-005: Publicar evento para notificar al Controlador de Novedades
        eventPublisher.publicarNovedadRegistrada(novedad);
        
        // 8. Retornar respuesta
        return new NovedadRegistradaResponse(
            paquete.getId(),
            paquete.getEstado(),
            entrada.getId(),
            entrada.getFechaTransicion()
        );
    }
    
    /**
     * Procesa y almacena los archivos de evidencia en S3/MinIO.
     * 
     * @param command Comando con archivos
     * @param paqueteId ID del paquete
     * @return Lista de evidencias procesadas y almacenadas
     */
    private List<Evidencia> procesarEvidencias(NovedadCommand command, java.util.UUID paqueteId) {
        List<Evidencia> evidencias = new ArrayList<>();
        
        if (command.getArchivosEvidencia() != null && !command.getArchivosEvidencia().isEmpty()) {
            for (var archivoCmd : command.getArchivosEvidencia()) {
                
                // Crear adaptador para el archivo
                EvidenciaStorageService.ArchivoEvidencia archivo = new ArchivoEvidenciaAdapter(archivoCmd);
                
                // Almacenar en S3 y obtener URL
                String url = evidenciaStorage.almacenar(archivo, paqueteId);
                
                // Determinar tipo de archivo
                TipoArchivo tipoArchivo = determinarTipoArchivo(archivoCmd.getContentType());
                
                // Crear evidencia del dominio
                Evidencia evidencia = new Evidencia(
                    archivoCmd.getNombre(),
                    url,
                    tipoArchivo,
                    archivoCmd.getTamanio(),
                    LocalDateTime.now(ZoneOffset.UTC)
                );
                
                evidencias.add(evidencia);
            }
        }
        
        return evidencias;
    }
    
    /**
     * Determina el tipo de archivo basado en el content type.
     */
    private TipoArchivo determinarTipoArchivo(String contentType) {
        if (contentType != null) {
            if (contentType.startsWith("image/")) {
                return TipoArchivo.FOTO;
            } else if (contentType.startsWith("video/")) {
                return TipoArchivo.VIDEO;
            }
        }
        // Por defecto asumimos que es foto
        return TipoArchivo.FOTO;
    }
    
    /**
     * Adaptador interno para convertir el comando en la interfaz esperada por el servicio.
     */
    private static class ArchivoEvidenciaAdapter implements EvidenciaStorageService.ArchivoEvidencia {
        
        private final NovedadCommand.ArchivoEvidenciaCommand comando;
        
        public ArchivoEvidenciaAdapter(NovedadCommand.ArchivoEvidenciaCommand comando) {
            this.comando = comando;
        }
        
        @Override
        public String getNombre() {
            return comando.getNombre();
        }
        
        @Override
        public String getContentType() {
            return comando.getContentType();
        }
        
        @Override
        public long getTamanio() {
            return comando.getTamanio();
        }
        
        @Override
        public byte[] getContenido() {
            return comando.getContenido();
        }
    }
}
