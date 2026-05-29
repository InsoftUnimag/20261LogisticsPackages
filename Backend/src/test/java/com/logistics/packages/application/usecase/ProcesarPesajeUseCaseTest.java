package com.logistics.packages.application.usecase;

import com.logistics.packages.application.ports.EventoProcesadoRepository;
import com.logistics.packages.application.repository.ArchivoStoragePort;
import com.logistics.packages.application.repository.HistorialEstadoRepository;
import com.logistics.packages.application.repository.PaqueteRepository;
import com.logistics.packages.domain.event.SolicitudRutaEvent;
import com.logistics.packages.domain.exception.PaqueteNotFoundException;
import com.logistics.packages.domain.model.EventoProcesado;
import com.logistics.packages.domain.model.Paquete;
import com.logistics.packages.domain.valueobject.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Tests unitarios para ProcesarPesajeUseCase (MOD1-UC-002)
 * T205, T206: Validación de la orquestación del pesaje
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ProcesarPesajeUseCase - Procesamiento de Pesaje")
class ProcesarPesajeUseCaseTest {

    @Mock
    private PaqueteRepository paqueteRepository;

    @Mock
    private SolicitarRutaUseCase solicitarRutaUseCase;

    @Mock
    private EventoProcesadoRepository eventoProcesadoRepository;

    @Mock
    private ArchivoStoragePort archivoStoragePort;

    @Mock
    private HistorialEstadoRepository historialEstadoRepository;

    @InjectMocks
    private ProcesarPesajeUseCase procesarPesajeUseCase;

    private UUID paqueteId;
    private Paquete paqueteExistente;

    @BeforeEach
    void setUp() {
        paqueteId = UUID.randomUUID();
        paqueteExistente = Paquete.builder()
                .id(paqueteId)
                .sedeId(UUID.fromString("550e8400-e29b-41d4-a716-446655440000"))
                .distanciaEstimadaKm(50.0)
                .build();

        // Configurar el mock de EventoProcesadoRepository para garantizar idempotencia
        // Usar lenient() para evitar UnnecessaryStubbingException en tests que lanzan excepciones antes de invocar estos stubs
        lenient().when(eventoProcesadoRepository.yaFueProcesado(anyString())).thenReturn(false);
        lenient().when(eventoProcesadoRepository.guardar(any(EventoProcesado.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
    }

    // T205 - Pesaje exitoso

    @Test
    @DisplayName("US2: Debe procesar el pesaje exitosamente y retornar el precio calculado")
    void debeProcesarPesajeExitosamente() {
        // Given
        Peso peso = new Peso(25.0);
        Dimensiones dimensiones = new Dimensiones(50.0, 40.0, 30.0);
        
        PesajeCommand command = PesajeCommand.builder()
                .paqueteId(paqueteId)
                .peso(peso)
                .dimensiones(dimensiones)
                .tipoMercancia(TipoMercancia.ESTANDAR)
                .formaIrregular(false)
                .tarifaBase(new BigDecimal("10.00"))
                .tarifaPorKg(new BigDecimal("2.00"))
                .tarifaPorKm(new BigDecimal("0.50"))
                .recargoTipoMercancia(new BigDecimal("0.00"))
                .recargoCategoriaCarga(new BigDecimal("0.00"))
                .build();

        when(paqueteRepository.findById(paqueteId)).thenReturn(Optional.of(paqueteExistente));
        when(paqueteRepository.save(any(Paquete.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        PesajeResponse response = procesarPesajeUseCase.procesarPesaje(command);

        // Then
        assertNotNull(response);
        assertEquals(paqueteId, response.getPaqueteId());
        assertEquals(25.0, response.getPeso());
        assertNotNull(response.getVolumenM3());
        assertNotNull(response.getPesoVolumetrico());
        assertNotNull(response.getPesoFacturable());
        assertNotNull(response.getPrecioEnvio());
        assertEquals(CategoriaCarga.NORMAL, response.getCategoriaCarga());

        // Verificar que se llamó a save y al evento de ruta
        ArgumentCaptor<Paquete> paqueteCaptor = ArgumentCaptor.forClass(Paquete.class);
        verify(paqueteRepository).save(paqueteCaptor.capture());
        verify(solicitarRutaUseCase).handle(any(SolicitudRutaEvent.class));
        
        Paquete paqueteGuardado = paqueteCaptor.getValue();
        assertNotNull(paqueteGuardado.getPeso());
        assertNotNull(paqueteGuardado.getDimensiones());
        assertEquals(TipoMercancia.ESTANDAR, paqueteGuardado.getTipoMercancia());
    }

    @Test
    @DisplayName("US2: Debe calcular el precio correcto basado en las tarifas")
    void debeCalcularPrecioCorrectamente() {
        // Given: Peso facturable 15kg, distancia 50km
        Peso peso = new Peso(10.0);
        Dimensiones dimensiones = new Dimensiones(50.0, 40.0, 30.0); // Vol 0.06 m³ → PV 15 kg
        
        PesajeCommand command = PesajeCommand.builder()
                .paqueteId(paqueteId)
                .peso(peso)
                .dimensiones(dimensiones)
                .tipoMercancia(TipoMercancia.ESTANDAR)
                .formaIrregular(false)
                .tarifaBase(new BigDecimal("10.00"))
                .tarifaPorKg(new BigDecimal("2.00"))
                .tarifaPorKm(new BigDecimal("0.50"))
                .recargoTipoMercancia(new BigDecimal("5.00"))
                .recargoCategoriaCarga(new BigDecimal("0.00"))
                .build();

        when(paqueteRepository.findById(paqueteId)).thenReturn(Optional.of(paqueteExistente));
        when(paqueteRepository.save(any(Paquete.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        PesajeResponse response = procesarPesajeUseCase.procesarPesaje(command);

        // Then
        // Precio = 10 (base) + 15*2 (peso) + 50*0.5 (distancia) + 5 (tipo) + 0 (categoría) = 70
        assertEquals(0, new BigDecimal("70.00").compareTo(response.getPrecioEnvio()));
    }

    @Test
    @DisplayName("US2: Debe generar alerta de carga especial cuando peso > 50 kg")
    void debeGenerarAlertaCargaEspecial() {
        // Given
        Peso peso = new Peso(55.0);
        Dimensiones dimensiones = new Dimensiones(30.0, 20.0, 10.0);
        
        PesajeCommand command = PesajeCommand.builder()
                .paqueteId(paqueteId)
                .peso(peso)
                .dimensiones(dimensiones)
                .tipoMercancia(TipoMercancia.ESTANDAR)
                .formaIrregular(false)
                .tarifaBase(new BigDecimal("10.00"))
                .tarifaPorKg(new BigDecimal("2.00"))
                .tarifaPorKm(new BigDecimal("0.50"))
                .recargoTipoMercancia(new BigDecimal("0.00"))
                .recargoCategoriaCarga(new BigDecimal("10.00"))
                .build();

        when(paqueteRepository.findById(paqueteId)).thenReturn(Optional.of(paqueteExistente));
        when(paqueteRepository.save(any(Paquete.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        PesajeResponse response = procesarPesajeUseCase.procesarPesaje(command);

        // Then
        assertEquals(CategoriaCarga.CARGA_ESPECIAL, response.getCategoriaCarga());
        assertTrue(response.isAlertaCargaEspecial());
    }

    // T206 - Alerta de densidad atípica

    @Test
    @DisplayName("US2: Debe generar alerta de densidad atípica cuando diferencia > 30%")
    void debeGenerarAlertaDensidadAtipica() {
        // Given: Peso real 10kg, Peso volumétrico 200kg (diferencia > 30%)
        Peso peso = new Peso(10.0);
        Dimensiones dimensiones = new Dimensiones(100.0, 100.0, 80.0); // 0.8 m³ × 250 = 200 kg
        
        PesajeCommand command = PesajeCommand.builder()
                .paqueteId(paqueteId)
                .peso(peso)
                .dimensiones(dimensiones)
                .tipoMercancia(TipoMercancia.ESTANDAR)
                .formaIrregular(false)
                .tarifaBase(new BigDecimal("10.00"))
                .tarifaPorKg(new BigDecimal("2.00"))
                .tarifaPorKm(new BigDecimal("0.50"))
                .recargoTipoMercancia(new BigDecimal("0.00"))
                .recargoCategoriaCarga(new BigDecimal("10.00"))
                .build();

        when(paqueteRepository.findById(paqueteId)).thenReturn(Optional.of(paqueteExistente));
        when(paqueteRepository.save(any(Paquete.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        PesajeResponse response = procesarPesajeUseCase.procesarPesaje(command);

        // Then
        assertTrue(response.isAlertaDensidadAtipica());
    }

    @Test
    @DisplayName("US2: No debe generar alerta de densidad atípica cuando diferencia <= 30%")
    void noDebeGenerarAlertaDensidadAtipica() {
        // Given: Peso real 20kg, Peso volumétrico 25kg (diferencia 25%)
        Peso peso = new Peso(20.0);
        Dimensiones dimensiones = new Dimensiones(50.0, 50.0, 40.0); // 0.1 m³ × 250 = 25 kg
        
        PesajeCommand command = PesajeCommand.builder()
                .paqueteId(paqueteId)
                .peso(peso)
                .dimensiones(dimensiones)
                .tipoMercancia(TipoMercancia.ESTANDAR)
                .formaIrregular(false)
                .tarifaBase(new BigDecimal("10.00"))
                .tarifaPorKg(new BigDecimal("2.00"))
                .tarifaPorKm(new BigDecimal("0.50"))
                .recargoTipoMercancia(new BigDecimal("0.00"))
                .recargoCategoriaCarga(new BigDecimal("0.00"))
                .build();

        when(paqueteRepository.findById(paqueteId)).thenReturn(Optional.of(paqueteExistente));
        when(paqueteRepository.save(any(Paquete.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        PesajeResponse response = procesarPesajeUseCase.procesarPesaje(command);

        // Then
        assertFalse(response.isAlertaDensidadAtipica());
    }

    @Test
    @DisplayName("Debe lanzar PaqueteNotFoundException si el paquete no existe")
    void debeLanzarExcepcionSiPaqueteNoExiste() {
        // Given
        UUID paqueteIdInexistente = UUID.randomUUID();
        PesajeCommand command = PesajeCommand.builder()
                .paqueteId(paqueteIdInexistente)
                .peso(new Peso(10.0))
                .dimensiones(new Dimensiones(50.0, 40.0, 30.0))
                .tipoMercancia(TipoMercancia.ESTANDAR)
                .formaIrregular(false)
                .tarifaBase(new BigDecimal("10.00"))
                .tarifaPorKg(new BigDecimal("2.00"))
                .tarifaPorKm(new BigDecimal("0.50"))
                .recargoTipoMercancia(new BigDecimal("0.00"))
                .recargoCategoriaCarga(new BigDecimal("0.00"))
                .build();

        when(paqueteRepository.findById(paqueteIdInexistente)).thenReturn(Optional.empty());

        // When/Then
        assertThrows(PaqueteNotFoundException.class, () -> {
            procesarPesajeUseCase.procesarPesaje(command);
        });

        verify(paqueteRepository, never()).save(any());
    }

    @Test
    @DisplayName("Debe asignar correctamente el indicador de forma irregular")
    void debeAsignarIndicadorFormaIrregular() {
        // Given
        PesajeCommand command = PesajeCommand.builder()
                .paqueteId(paqueteId)
                .peso(new Peso(15.0))
                .dimensiones(new Dimensiones(50.0, 40.0, 30.0))
                .tipoMercancia(TipoMercancia.FRAGIL)
                .formaIrregular(true)
                .tarifaBase(new BigDecimal("10.00"))
                .tarifaPorKg(new BigDecimal("2.00"))
                .tarifaPorKm(new BigDecimal("0.50"))
                .recargoTipoMercancia(new BigDecimal("5.00"))
                .recargoCategoriaCarga(new BigDecimal("0.00"))
                .build();

        when(paqueteRepository.findById(paqueteId)).thenReturn(Optional.of(paqueteExistente));
        when(paqueteRepository.save(any(Paquete.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        procesarPesajeUseCase.procesarPesaje(command);

        // Then
        ArgumentCaptor<Paquete> paqueteCaptor = ArgumentCaptor.forClass(Paquete.class);
        verify(paqueteRepository).save(paqueteCaptor.capture());
        
        Paquete paqueteGuardado = paqueteCaptor.getValue();
        assertTrue(paqueteGuardado.getIndicadorFormaIrregular());
        assertEquals(TipoMercancia.FRAGIL, paqueteGuardado.getTipoMercancia());
    }
}
