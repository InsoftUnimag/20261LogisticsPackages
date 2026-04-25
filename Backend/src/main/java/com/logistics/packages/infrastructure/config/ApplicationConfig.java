package com.logistics.packages.infrastructure.config;

import com.logistics.packages.application.usecase.RegistrarNovedadUseCase;
import com.logistics.packages.domain.repository.HistorialRepository;
import com.logistics.packages.domain.repository.NovedadRepository;
import com.logistics.packages.domain.repository.PaqueteRepository;
import com.logistics.packages.domain.service.EvidenciaStorageService;
import com.logistics.packages.domain.service.NovedadEventPublisher;
import com.logistics.packages.domain.service.ValidadorTransicionEstado;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * Configuración de beans de la aplicación.
 * Inyección de dependencias y configuración de casos de uso.
 * MOD1-IP-006: Actualizar Estado de Paquete por Novedad
 */
@Configuration
@EnableTransactionManagement
public class ApplicationConfig {
    
    /**
     * Bean del caso de uso de registrar novedad.
     * Aquí se realiza la inyección manual de todas las dependencias.
     */
    @Bean
    public RegistrarNovedadUseCase registrarNovedadUseCase(
            PaqueteRepository paqueteRepository,
            HistorialRepository historialRepository,
            NovedadRepository novedadRepository,
            EvidenciaStorageService evidenciaStorage,
            NovedadEventPublisher eventPublisher,
            ValidadorTransicionEstado validador) {
        
        return new RegistrarNovedadUseCase(
            paqueteRepository,
            historialRepository,
            novedadRepository,
            evidenciaStorage,
            eventPublisher,
            validador
        );
    }
    
    /**
     * Bean del validador de transiciones de estado.
     * Servicio de dominio sin dependencias externas.
     */
    @Bean
    public ValidadorTransicionEstado validadorTransicionEstado() {
        return new ValidadorTransicionEstado();
    }
}
