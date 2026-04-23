# Implementation Plan: Procesar Pesaje y Dimensiones (MOD1-IP-002)

**Date**: 2026-04-12
**Spec**: [Procesar Pesaje y Dimensiones](../Specs/MOD1-UC-002-Procesar-Pesaje-Y-Dimensiones.md)

## Summary

Como Empleado de Envío y Recepción, necesito capturar el peso y las dimensiones del paquete para calcular el precio de envío, determinar la categoría de carga y asignar los atributos físicos necesarios para que el `Módulo de Gestión de Rutas` pueda seleccionar el vehículo adecuado. Esta funcionalidad es una extensión del proceso de admisión y actualiza el `Paquete` existente con los datos físicos y tarifarios.

## Technical Context

| Campo | Valor |
|---|---|
| **Language/Version** | Java 21 (Backend) / JavaScript (React para Frontend) |
| **Primary Dependencies** | Spring Web, Spring Data JPA, Spring Validation |
| **Storage** | PostgreSQL (actualización transaccional del `Paquete`) |
| **Testing** | JUnit 5, Mockito |
| **Target Platform** | Servidor Linux para Backend API y Web browser para la UI |
| **Project Type** | Extensión de Single Web Application (Backend / Web) |
| **Performance Goals** | Cálculos de peso y precio < 10s |
| **Constraints** | El peso no puede exceder los 70 kg. Las dimensiones y el peso deben ser > 0. |
| **Scale/Scope** | Integrado en el flujo de admisión de paquetes. |
| **Framework** | Spring Boot 3.x (Backend), React (Frontend) |
| **Arquitectura** | Hexagonal (Ports & Adapters) — Backend aislado |

## Project Structure

> Se extiende la estructura hexagonal existente. Los cambios se centran en el dominio del `Paquete` y un nuevo caso de uso para el pesaje.

```text
frontend/
└── src/
    ├── components/
    │   └── form/
    │       └── PesajePaqueteForm.jsx      [NUEVO — Componente React para pesaje]
    └── services/
        └── PesajeApiService.js            [NUEVO — HTTP Fetchs a Spring Boot]

backend/
└── src
    ├── main
    │   ├── java
    │   │   └── com
    │   │       └── logistics
    │   │           └── packages
    │   │               ├── application
    │   │               │   ├── usecase
    │   │               │   │   ├── ProcesarPesajeUseCase.java   [NUEVO]
    │   │               │   │   └── PesajeCommand.java           [NUEVO]
    │   │               ├── domain
    │   │               │   ├── model
    │   │               │   │   └── Paquete.java                 [MODIFICADO - Añadir atributos físicos]
    │   │               │   └── valueobject
    │   │               │       ├── Peso.java                    [NUEVO]
    │   │               │       ├── Dimensiones.java             [NUEVO]
    │   │               │       └── Tarifa.java                  [NUEVO]
    │   │               └── infrastructure
    │   │                   ├── controller
    │   │                   │   └── PesajeController.java        [NUEVO]
    │   │                   ├── dto
    │   │                   │   ├── request
    │   │                   │   │   └── PesajeRequest.java       [NUEVO]
    │   │                   │   └── response
    │   │                   │       └── PesajeResponse.java      [NUEVO]
    │   │                   └── persistence
    │   │                       └── paquete
    │   │                           └── PaqueteJpaAdapter.java   [MODIFICADO - método de actualización]
    └── test
        └── java
            └── com
                └── logistics
                    └── packages
                        ├── application
                        │   └── usecase
                        │       └── ProcesarPesajeUseCaseTest.java [NUEVO]
                        └── domain
                            └── model
                                └── PaquetePesajeTest.java     [NUEVO]
```

---

## Phase 1: Dominio del Pesaje — Lógica y Cálculos

**Purpose**: Implementar las reglas de negocio para el cálculo de peso volumétrico, peso facturable y precio, manteniendo el dominio aislado.

### Tests del dominio (TDD)

- [ ] T201 [P] Test unitario `PaquetePesajeTest`:
  - `calcularVolumen()`: verificar que `(l*a*h)/1,000,000` funciona.
  - `calcularPesoVolumetrico()`: verificar que `volumen_m3 * 250` es correcto.
  - `determinarPesoFacturable()`: verificar que se elige el mayor entre peso real y peso volumétrico.
  - `asignarCategoriaCarga()`: verificar que se asigna `Carga Especial` si `peso > 50kg` o `volumen > 0.5m³`.
- [ ] T202 [P] Test unitario `PaquetePesajeTest` — Validaciones:
  - Lanzar excepción si el peso es > 70 kg.
  - Lanzar excepción si el peso o las dimensiones son <= 0.

### Implementación del dominio

- [ ] T203 [P] Modificar `Paquete.java` para incluir los nuevos atributos físicos y los métodos de cálculo:
```java
// backend/domain/model/Paquete.java
public class Paquete {
    // ... atributos existentes ...

    // Atributos Físicos y Tarifarios (MOD1-UC-002)
    private Peso peso;
    private Dimensiones dimensiones;
    private Double volumenM3;
    private Double pesoVolumetrico;
    private Double pesoFacturable;
    private TipoMercancia tipoMercancia;
    private CategoriaCarga categoriaCarga;
    private Boolean indicadorFormaIrregular;
    private PrecioEnvio precioEnvio; // Inmutable tras el pesaje

    public void procesarPesaje(Peso peso, Dimensiones dimensiones, TipoMercancia tipoMercancia, boolean irregular) {
        if (peso.getKilogramos() > 70) {
            throw new IllegalArgumentException("El peso no puede exceder los 70 kg.");
        }
        if (peso.getKilogramos() <= 0 || dimensiones.getLargoCm() <= 0 || dimensiones.getAnchoCm() <= 0 || dimensiones.getAltoCm() <= 0) {
            throw new IllegalArgumentException("El peso y las dimensiones deben ser mayores a cero.");
        }

        this.peso = peso;
        this.dimensiones = dimensiones;
        this.tipoMercancia = tipoMercancia;
        this.indicadorFormaIrregular = irregular;

        this.volumenM3 = calcularVolumen();
        this.pesoVolumetrico = calcularPesoVolumetrico();
        this.pesoFacturable = determinarPesoFacturable();
        this.categoriaCarga = determinarCategoriaCarga();
    }

    private double calcularVolumen() {
        return (dimensiones.getLargoCm() * dimensiones.getAnchoCm() * dimensiones.getAltoCm()) / 1_000_000.0;
    }

    private double calcularPesoVolumetrico() {
        return this.volumenM3 * 250;
    }

    private double determinarPesoFacturable() {
        return Math.max(this.peso.getKilogramos(), this.pesoVolumetrico);
    }
    
    private CategoriaCarga determinarCategoriaCarga() {
        if (this.peso.getKilogramos() > 50 || this.volumenM3 > 0.5) {
            return CategoriaCarga.CARGA_ESPECIAL;
        }
        return CategoriaCarga.NORMAL;
    }
    
    public void calcularPrecio(Tarifa tarifa) {
        // Lógica de cálculo de precio según FR-007
        // this.precioEnvio = ...
    }
}
```
- [ ] T204 [P] Crear los Value Objects `Peso.java` y `Dimensiones.java`.

---

## Phase 2: Servicio de Aplicación — ProcesarPesajeUseCase (US2)

**Goal**: Orquestar la actualización del `Paquete` con los datos del pesaje y calcular el precio final.

### Tests del servicio (TDD)

- [ ] T205 [P] [US2] `ProcesarPesajeUseCaseTest` — Pesaje exitoso:
  - Dado: Un `Paquete` existente y un `PesajeCommand` válido.
  - Cuando: Se ejecuta `procesarPesaje()`.
  - Entonces: Se llama a `paqueteRepository.actualizar()` con el `Paquete` modificado y se retorna el precio calculado.
- [ ] T206 [P] [US2] `ProcesarPesajeUseCaseTest` — Alerta de densidad atípica:
  - Dado: Un `PesajeCommand` donde `|peso_real - peso_volumetrico| / peso_real > 0.3`.
  - Cuando: Se ejecuta `procesarPesaje()`.
  - Entonces: Se retorna una respuesta que incluye una alerta de `DENSIDAD_ATIPICA`.

### Implementación del servicio

- [ ] T207 [P] [US2] Implementar `ProcesarPesajeUseCase`:
```java
// backend/application/usecase/ProcesarPesajeUseCase.java
@Service
@Transactional
public class ProcesarPesajeUseCase {

    private final PaqueteRepository paqueteRepository;
    private final TarifaRepository tarifaRepository; // Para obtener las tarifas

    public ProcesarPesajeUseCase(PaqueteRepository paqueteRepository, TarifaRepository tarifaRepository) {
        this.paqueteRepository = paqueteRepository;
        this.tarifaRepository = tarifaRepository;
    }

    public PesajeResponse procesarPesaje(PesajeCommand command) {
        Paquete paquete = paqueteRepository.findById(command.getPaqueteId())
            .orElseThrow(() -> new PaqueteNotFoundException(command.getPaqueteId()));

        paquete.procesarPesaje(command.getPeso(), command.getDimensiones(), command.getTipoMercancia(), command.isIrregular());
        
        Tarifa tarifa = tarifaRepository.findBySede(paquete.getSedeId());
        paquete.calcularPrecio(tarifa);

        paqueteRepository.actualizar(paquete);

        PesajeResponse response = new PesajeResponse(paquete.getPrecioEnvio());
        if (Math.abs(paquete.getPeso().getKilogramos() - paquete.getPesoVolumetrico()) / paquete.getPeso().getKilogramos() > 0.3) {
            response.addAlerta("DENSIDAD_ATIPICA");
        }
        
        return response;
    }
}
```

---

## Phase 3: Adaptadores de Entrada (REST y React)

### Adaptador REST (Backend)

- [ ] T208 [US2] Crear `PesajeController` que reciba un `PesajeRequest` y llame al `ProcesarPesajeUseCase`.
- [ ] T209 [US2] Implementar validaciones en `PesajeRequest` para `FR-001` y `FR-003`.

### Aplicación UI (Frontend)

- [ ] T210 [US2] Crear `PesajePaqueteForm.jsx` que permita al usuario ingresar peso, dimensiones y tipo de mercancía.
- [ ] T211 [US2] El formulario debe mostrar alertas (`Carga Especial`, `Densidad atípica`) basadas en la respuesta del backend y requerir confirmación del usuario.
- [ ] T212 [US2] Incluir un toggle para `Dimensiones irregulares` (FR-008).

---

## Dependencies & Execution Order

- **Dependencia**: Esta funcionalidad depende de que un `Paquete` haya sido creado previamente por `MOD1-UC-001`.
- **Orden**:
    1.  **Phase 1 (Dominio)**: Implementar la lógica de negocio en el modelo.
    2.  **Phase 2 (Servicio)**: Crear el caso de uso que orquesta la actualización.
    3.  **Phase 3 (Adaptadores)**: Exponer la funcionalidad a través de una API REST y consumirla desde el frontend.

## Notes

- Las tarifas se asumen configurables y accesibles a través de un `TarifaRepository`.
- La `distanciaEstimadaKm` necesaria para el cálculo del precio se asume que ya está en el objeto `Paquete` desde el geocoding de la admisión.
