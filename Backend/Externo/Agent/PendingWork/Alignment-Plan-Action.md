### Plan de Acción: Integración SQS M1 ↔ M2

> **Última actualización:** 2026-05-23  
> **PRs ejecutados:** PR1 (`@Builder` en controller), PR2 (renombre + docs), PR3 (eliminación componentes deprecados), PR4+PR5 (alineación infraestructura SQS), PR6 (`bugfix/m2-outbound-contracts`), PR7 (`bugfix/m2-inbound-contracts`), PR8 (`bugfix/domain-immutability-and-states`), PR9 (`feature/m3-async-sqs-migration` + Bloque 2)

---

#### 1. Alineación de Infraestructura y Perfiles SQS

| Estado | Prioridad |
|--------|-----------|
| ✅ **Completado** | ⚠️ **Alta** |

* **Ramas:** `feature/sqs-infrastructure-alignment` (PR4) + `fix/m2-queue-name-arn` (PR5)
* **Objetivo:** Sincronizar la configuración de colas con M2 y preparar el entorno de AWS real (Tarea 5 preliminar).
* **Dependencias:** Ninguna.
* **Tareas completadas:**
  * Eliminado el uso de `${DEV_PREFIX}` en `application.yml` y `application-local.yml`.
  * Colas homologadas con M2: `solicitudes-ruta-queue`, `logistics-eventos-paquete` (según ARN de M2) y `respuestas-ruta-queue`.
  * Creado el perfil `application-aws.yml` sin credenciales hardcodeadas (`DefaultCredentialsProvider`).
  * Añadido `@Profile({"default", "local", "aws"})` a los 3 listeners SQS de M1 (`RutaEventSqsListener`, `RutaSqsListener`, `PaqueteListoClasificacionSqsListener`).
  * Corregido anidamiento del endpoint LocalStack bajo `spring.cloud.aws.sqs.endpoint`.
* **Bloqueantes:** Ninguno.
* **Riesgo:** Bajo. Cambios exclusivamente en configuración.

---

#### 2. Resolución de Contratos de Salida (M1 → M2)

| Estado | Prioridad |
|--------|-----------|
| ✅ **Completado** | 🔴 **Crítica** |

* **Rama:** `bugfix/m2-outbound-contracts`
* **Commits:** `3932d8f` (DTO), `3b52285` (Adapter)
* **Objetivo:** Garantizar que los mensajes que M1 escribe en `solicitudes-ruta-queue` sean legibles para M2 (Tarea 2 y 3).
* **Dependencias:** Debería ejecutarse antes que las pruebas de humo en AWS (workstream 5).
* **Tareas completadas:**
  * ~~**CRÍTICO** — Modificar `RutaSqsAdapter.mapDireccion()` para concatenar el objeto `DireccionDto` a String plano~~ — **Resuelto:** M2 acepta objeto `DireccionDto`, no requiere cambio.
  * **CRÍTICO** — `fechaLimiteEntrega` cambiado de `OffsetDateTime` a `Instant` en `SolicitudRutaPayload` y convertido con `.toInstant()` en `RutaSqsAdapter.calcularFechaLimite()`.
  * **ALTA** — Enums `TipoMercancia` y `MetodoPago` mapeados a `String` explícitamente con `.name()` en `RutaSqsAdapter.mapToPayload()`.
* **Bloqueantes:** Ninguno.
* **Riesgo:** Medio. Cambios en el contrato de salida; requiere coordinación con M2 para validar el nuevo formato.

---

#### 3. Resolución de Contratos de Entrada (M2 → M1)

| Estado | Prioridad |
|--------|-----------|
| ✅ **Completado** | 🔴 **Crítica** |

* **Rama:** `bugfix/m2-inbound-contracts`
* **Commits:** `1320045` (DTOs), `e5d7295` (mapper), `49fd62c` (tests), `54d0b84` (fallback motivos)
* **Objetivo:** Evitar fallos de deserialización (`DateTimeParseException` o `IllegalArgumentException`) cuando M1 consuma de `eventos-paquete-queue` (Tarea 2 y 3).
* **Dependencias:** Debería ejecutarse antes que las pruebas de humo en AWS (workstream 5).
* **Tareas completadas:**
  * **CRÍTICO** — Cambiado `fecha_hora_evento` de `OffsetDateTime` a `Instant` en los 7 DTOs (padre + 6 hijos). Eliminado `@JsonFormat` obsoleto.
  * **ALTA** — `EventoPaqueteM2Mapper` actualizado: recibe `Instant`, convierte a `OffsetDateTime` con `ZoneOffset.UTC` y asigna al campo `fechaHoraEvento` de `EventoRutaDto` (antes solo se usaba para generar el ID).
  * **ALTA** — Implementado fallback defensivo con `try-catch` en el Mapper para `motivo` (`ParadaFallida`) y `tipoNovedad` (`NovedadGrave`): si el string de M2 no coincide con el enum de M1, se loguea `warn` y se asigna valor por defecto (`MOTIVO_DESCONOCIDO` o `DEVOLUCION`).
* **Bloqueantes:** Ninguno.
* **Riesgo:** Resuelto. La deserialización ahora es compatible con el formato `Instant` de M2 y los enums tienen fallback seguro.

---

#### 4. Purificación del Dominio y Actualización UC 007

| Estado | Prioridad |
|--------|-----------|
| ✅ **Completado** | ⚠️ **Alta** |

* **Rama:** `bugfix/domain-immutability-and-states`
* **Commits:** `466551d` (domain core), `f11d11f` (app/infra), `f8eb8ff` (tests)
* **Objetivo:** Proteger el núcleo del negocio y corregir las inconsistencias detectadas en la gestión de novedades (Tarea 4).
* **Tareas completadas:**
  * **CRÍTICO** — Eliminado `@Setter` a nivel de clase y field `etiquetaDigital` en `Paquete.java`. Creadas fábricas `crearNuevo()` y `reconstruir()`.
  * **CRÍTICO** — `Paquete.reconstruir()` público para hidratación desde infraestructura; `@NoArgsConstructor` privado.
  * **ALTA** — Refactorizados casos de uso: `RegistrarAdmisionUseCase` genera UUID, `PriceCalculationService` recibe `distanciaKm` explícito.
  * **MEDIA** — `registrarNovedad()` recibe `NovedadBodega` VO preservando `tipoNovedad` en `HistorialEstado`.
  * **MEDIA** — Eliminado `@Embeddable` de `Persona.java`.
  * **MEDIA** — Flyway V7: columna `tipo_novedad` en `historial_estados`.
  * **Todos los tests** refactorizados sin `new Paquete()`, `setEstado()` ni `setTipoMercancia()`.
* **Evidencia:** 185 tests, 7 fallos (solo Docker/LocalStack no disponible).
* **Riesgo:** Resuelto. Compilación y tests unitarios verificados.

---

#### 4b. Migración M3 (Finanzas) — Síncrono a Asíncrono vía SQS

| Estado | Prioridad |
|--------|-----------|
| ✅ **Completado** | ⚠️ **Alta** |

* **Rama:** `feature/m3-async-sqs-migration`
* **Objetivo:** Migrar la comunicación con M3 (Finanzas) del endpoint REST síncrono (`GET /route/{idRoute}/package/{idPaquete}`) a una cola SQS asíncrona `eventos-financieros-paquete-queue` (Tarea 4).
* **Dependencias:** Workstream 4 (purificación del dominio) como prerrequisito.
* **Tareas completadas:**
  * Eliminados componentes síncronos legacy: `ConsultaFinancieraController`, `ConsultarEstadoPaqueteUseCase`, `GestionNovedadPaqueteResponse`.
  * Modificado `springdoc.paths-to-match=/api/**` en `application.properties`.
  * Creado puerto `EstadoPaqueteFinanzasPublisher` (interfaz `publicarEstadoFinal(Paquete paquete)`).
  * Creado DTO `EventoFinancieroPaqueteDto` con `@JsonProperty` snake_case (`id_paquete`, `id_ruta`, `estado`).
  * Creado adaptador `FinanzasEventSqsAdapter` que implementa el puerto usando `SqsTemplate`.
  * Integrado publisher en `ProcesarEventoRutaUseCase` (eventos M2) y `RegistrarNovedadUseCase` (novedades M1).
  * Tests: `FinanzasEventSqsAdapterTest` (6 escenarios: ENTREGADO, NOVEDAD_EN_BODEGA, EN_TRANSITO, rutaId null, propagación excepción SQS) + verificación en `ProcesarEventoRutaUseCaseTest` + `RegistrarNovedadUseCaseTest` actualizado.
* **Bloque 2 (Garantía Transaccional):**
  * `FinanzasEventSqsAdapter` corregido: el `catch` ahora relanza la excepción (`throw e`) para que el `@Transactional` del caso de uso realice rollback si SQS falla.
  * Nuevo test `testPropagarExcepcionCuandoSqsFalla` que verifica que la excepción se propaga.
  * `RegistrarNovedadUseCaseTest` actualizado con `@Mock EstadoPaqueteFinanzasPublisher` + aserciones `verify`/`never` en 5 tests (resuelve NPE por dependencia no mockeada).
* **Payload SQS:** Mínimo (3 campos) — M3 consulta detalles adicionales por su cuenta.
* **Riesgo:** Bajo. Publisher invocado después de notificaciones; la excepción SQS ahora se propaga correctamente para rollback transaccional.

---

#### 5. Pruebas de Humo End-to-End en AWS

| Estado | Prioridad |
|--------|-----------|
| ❌ **No iniciado** | ⚠️ **Media** (depende de workstreams 1-3) |

* **Rama propuesta:** `feature/aws-smoke-testing-and-sync`
* **Objetivo:** Validar la comunicación SQS bidireccional en el entorno real (Tarea 5).
* **Dependencias:** Workstreams 1 (perfiles), 2 (contratos salida) y 3 (contratos entrada) deben estar completados o al menos estables.
* **Tareas a completar:**
  * Levantar la aplicación utilizando el perfil `application-aws.yml`.
  * Ejecutar los pasos de prueba manual definidos en el archivo de simulación (si aplica).
  * Generar un ticket técnico o PR cruzado solicitando al equipo del M2 que implementen el *producer* faltante para la cola `respuestas-ruta-queue` (flujo `RUTA_ASIGNADA`), ya que esto bloqueará el avance de los paquetes hacia el estado `LISTO_PARA_DESPACHO` de forma asíncrona.
* **Bloqueantes:** Workstreams 1, 2 y 3.
* **Riesgo:** Medio. Dependencia externa del equipo M2 para completar el flujo.

---

### Resumen de Prioridades

| # | Workstream | Prioridad | Depende de | Estado |
|---|------------|-----------|------------|--------|
| 2 | Contratos Salida (M1→M2) | 🔴 Crítica | — | ✅ **Completado** |
| 3 | Contratos Entrada (M2→M1) | 🔴 Crítica | — | ✅ **Completado** |
| 4 | Purificación del Dominio | ⚠️ Alta | — | ✅ **Completado** |
| 1 | Infraestructura/Perfiles SQS | ⚠️ Alta | — | ✅ **Completado** |
| 4b | Migración M3 (Finanzas) Síncrono→Asíncrono | ⚠️ Alta | 4 | ✅ **Completado** |
| 5 | Pruebas de Humo AWS | ⚠️ Media | 1, 2, 3 | ❌ No iniciado |

> [!NOTE]
> Los workstreams 1-4 y 4b están completados, desbloqueando el workstream 5 (pruebas de humo AWS).
