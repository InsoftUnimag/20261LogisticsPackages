### Plan de Acción: Integración SQS M1 ↔ M2

> **Última actualización:** 2026-05-23  
> **PRs ejecutados:** PR1 (`@Builder` en controller), PR2 (renombre + docs), PR3 (eliminación componentes deprecados), PR4+PR5 (alineación infraestructura SQS), PR6 (`bugfix/m2-outbound-contracts`)

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
| ❌ **No iniciado** | 🔴 **Crítica** |

* **Rama propuesta:** `fix/m2-inbound-contracts`
* **Objetivo:** Evitar fallos de deserialización (`DateTimeParseException` o `IllegalArgumentException`) cuando M1 consuma de `eventos-paquete-queue` (Tarea 2 y 3).
* **Dependencias:** Debería ejecutarse antes que las pruebas de humo en AWS (workstream 5).
* **Tareas a completar:**
  * **CRÍTICO** — Cambiar el tipo de dato de `fecha_hora_evento` de `OffsetDateTime` a `Instant` en los 6 DTOs de eventos dentro de `infrastructure/dto/event/`.
  * **ALTA** — Actualizar `EventoPaqueteM2Mapper` para que tome ese `Instant`, lo convierta internamente a `OffsetDateTime` y se lo pase correctamente al caso de uso (y utilizar este campo no solo para el ID del evento).
  * **ALTA** — Implementar una lógica de validación o *fallback* en el Mapper para castear con seguridad los valores de texto (`String`) que llegan de M2 a los enums locales `MotivoParadaFallida` y `TipoNovedadGrave`.
* **Bloqueantes:** Ninguno.
* **Riesgo:** Alto. Fallos de deserialización en runtime pueden detener el procesamiento de eventos de M2.

---

#### 4. Purificación del Dominio y Actualización UC 007

| Estado | Prioridad |
|--------|-----------|
| ⚠️ **Progreso parcial** | ⚠️ **Alta** |

* **Rama propuesta:** `fix/domain-immutability-and-states`
* **Objetivo:** Proteger el núcleo del negocio y corregir las inconsistencias detectadas en la gestión de novedades (Tarea 4).
* **Avance:** Renombre de `ConsultaPaqueteResponse` → `GestionNovedadPaqueteResponse` completado en PR2. Documentación corregida en PR2. Resto de tareas pendiente.
* **Tareas a completar:**
  * **CRÍTICO** — Eliminar el `@Setter` a nivel de clase en `domain/model/Paquete.java`.
  * **ALTA** — Refactorizar los casos de uso para que utilicen exclusivamente los métodos de transición de negocio (ej. `transitarAEnRuta()`, `entregarPaquete()`).
  * **MEDIA** — Modificar `Paquete.registrarNovedad()` para que evalúe y asigne el subtipo correcto de novedad en lugar de fijar siempre `NOVEDAD_EN_BODEGA`.
  * **MEDIA** — Eliminar las anotaciones de `jakarta.validation` de la entidad `Persona`.
* **Bloqueantes:** El refactor de `@Setter` requiere revisar todos los lugares que modifican `Paquete` directamente.
* **Riesgo:** Alto. Cambios en el core del dominio afectan a todos los casos de uso. Requiere suite completa de tests.

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
| 3 | Contratos Entrada (M2→M1) | 🔴 Crítica | — | ❌ No iniciado |
| 4 | Purificación del Dominio | ⚠️ Alta | — | ⚠️ Parcial |
| 1 | Infraestructura/Perfiles SQS | ⚠️ Alta | — | ✅ **Completado** |
| 5 | Pruebas de Humo AWS | ⚠️ Media | 1, 2, 3 | ❌ No iniciado |

> [!NOTE]
> Los workstreams 2 y 3 son bloqueantes para la comunicación funcional con M2. El workstream 4 (dominio) es independiente y puede ejecutarse en paralelo.
