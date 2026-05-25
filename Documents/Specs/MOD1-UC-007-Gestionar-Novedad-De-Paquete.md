# Feature Specification: Gestionar Novedad de Paquete (MOD1-UC-007)

**Created**: 2026-02-28

## User Scenarios & Testing

### User Story 7 — Registro y procesamiento de novedades del ciclo de vida del paquete (P1)

Como Controlador de Novedades, necesito recibir y procesar los estados y novedades que provienen del Módulo 2 (Gestión de Rutas) cuando un paquete está en ruta (`En Tránsito`, `En Parada de Entrega`, `Entregado`) o presenta novedades operativas en campo (`Extraviado`, `Devolución`, `Dañado`), así como las novedades detectadas en bodega, registrando las anomalías con evidencia adjunta, para mantener actualizada la trazabilidad y notificar al Módulo de Gestión de Finanzas de forma asíncrona mediante eventos SQS.

**Why this priority**: Es el soporte legal ante penalizaciones y cobros de pólizas, y el cierre contable del ciclo. Este caso de uso es el receptor principal de los estados que provienen del Módulo 2 mientras el paquete está en ruta. Sin el registro formal con evidencia, el Módulo de Gestión de Finanzas no puede ejecutar los ajustes financieros correspondientes.

**Importante sobre comunicación con otros módulos**:
- La comunicación con el `Módulo de Gestión de Finanzas` es **asíncrona mediante eventos SQS**. Después de persistir cualquier cambio de estado relevante para el cierre contable, el módulo de gestión de paquetes publica un evento en la cola `eventos-financieros-paquete-queue` con un payload mínimo de 3 campos (`id_paquete`, `id_ruta`, `estado`). El módulo de finanzas consume el evento y ejecuta los ajustes financieros correspondientes.

**Independent Test**: Registrar una novedad de tipo `Dañado` con fotografía en un paquete activo, y verificar que el sistema notifique al remitente y destinatario, vincule la novedad al UUID en el historial y publique automáticamente el evento asíncrono en la cola `eventos-financieros-paquete-queue` con el payload mínimo (`id_paquete`, `id_ruta`, `estado`).

**Acceptance Scenarios**:

1. **Recepción de estado "En Tránsito" desde Módulo 2**
   - **Given** el Módulo de Gestión de Rutas notifica que una ruta pasó a estado `En Tránsito`.
   - **When** el sistema recibe el evento del Módulo 2.
   - **Then** el sistema actualiza el estado del paquete a `En Tránsito`, registra la transición en el historial con fecha/hora e identificador del controlador de novedades, y envía notificación al destinatario informando que su paquete está en camino.

2. **Recepción de estado "En Parada de Entrega" desde Módulo 2**
   - **Given** el Módulo de Gestión de Rutas notifica que el transportador está en el sitio de entrega.
   - **When** el sistema recibe el evento del Módulo 2.
   - **Then** el sistema actualiza el estado del paquete a `En Parada de Entrega`, registra la transición en el historial con fecha/hora e identificador del controlador de novedades y envía notificación al destinatario informando que el transportador está llegando.

3. **Recepción de estado "Entregado" desde Módulo 2 con notificación asíncrona a Finanzas**
   - **Given** el Módulo de Gestión de Rutas notifica que el paquete fue entregado exitosamente con firma y evidencia (POD).
   - **When** el Controlador de Novedades procesa el evento y persiste el cambio de estado.
   - **Then** el sistema actualiza el estado a `Entregado`, vincula la evidencia (firma y foto POD), **publica un evento asíncrono** en la cola `eventos-financieros-paquete-queue` con el payload `{id_paquete, id_ruta, estado}` para que el Módulo de Gestión de Finanzas procese el cierre contable.

4. **Registro de daño con evidencia desde campo o bodega**
   - **Given** un paquete con daños físicos visibles (reportado desde Módulo 2 en ruta o desde bodega por el Almacenista a través de [Actualizar Estado de Paquete por Novedad (MOD1-UC-006)](./MOD1-UC-006-Actualizar-Estado-De-Paquete-Por-Novedad.md)).
   - **When** el Controlador selecciona tipo `Dañado` y adjunta la evidencia fotográfica.
   - **Then** el sistema actualiza el estado del paquete a `Novedad en Bodega - Dañado`, vincula la evidencia, notifica a remitente y destinatario, y **publica un evento asíncrono** en la cola `eventos-financieros-paquete-queue` con el payload mínimo para que el Módulo de Gestión de Finanzas procese la penalización y el cobro de póliza correspondiente.

5. **Declaración de extravío**
   - **Given** un paquete no encontrado físicamente (en bodega o reportado por el Módulo 2 en ruta).
   - **When** el Controlador registra la novedad como `Extraviado`.
   - **Then** el sistema actualiza el estado a `Novedad en Bodega - Extraviado`, registra la incidencia, notifica a remitente y destinatario, y **publica un evento asíncrono** en la cola `eventos-financieros-paquete-queue` con el payload mínimo para que el Módulo de Gestión de Finanzas procese la indemnización correspondiente.

6. **Procesamiento de devolución**
   - **Given** un paquete retornado por el Módulo de Gestión de Rutas (dirección incorrecta, cliente ausente, zona de difícil acceso / orden público, rechazado por el cliente).
   - **When** el Controlador registra la novedad como `Devolución`.
   - **Then** el sistema actualiza el estado a `Novedad en Bodega - Devolución`, notifica a remitente y destinatario informando que el paquete será retornado a la sede, y **publica un evento asíncrono** en la cola `eventos-financieros-paquete-queue` con el payload mínimo para que el Módulo de Gestión de Finanzas procese el pago de logística inversa.

### Edge Cases

- **¿Qué ocurre si la publicación del evento SQS hacia Finanzas falla?** El sistema no silencia la excepción (`no try-catch`). El error se propaga a través del adaptador, forzando el rollback transaccional de la base de datos. Es preferible no actualizar el estado del paquete si no se puede notificar a Finanzas, para preservar la consistencia contable del negocio.
- **¿Cómo maneja el sistema la recepción de un evento duplicado proveniente del Módulo 2 para el mismo paquete?** El sistema valida el timestamp y UUID del evento. Si detecta que el evento ya fue procesado, lo descarta sin generar una segunda transición en el historial, previniendo inconsistencias en la trazabilidad.
- **¿Qué ocurre si la notificación al remitente o destinatario falla al registrar una novedad?** El sistema registra el fallo de notificación en el log de eventos y reintenta el envío. La novedad queda registrada en el historial con independencia del resultado de la notificación.

---

## Requirements

### Functional Requirements

- **FR-001**: Recibir y procesar automáticamente los eventos del Módulo de Gestión de Rutas cuando un paquete pasa a `En Tránsito`, `En Parada de Entrega`, `Entregado` o presenta novedades operativas en campo (`Extraviado`, `Devolución`, `Dañado`).
- **FR-002**: Notificar al remitente (vía teléfono) y al destinatario (vía teléfono y correo electrónico) al registrar cualquier novedad.
- **FR-003**: El estado por defecto de un paquete devuelto tras la inspección en bodega es `En Clasificación`.
- **FR-004**: Restringir el cambio del tipo de novedad.
- **FR-005**: Publicar un evento asíncrono en la cola SQS `eventos-financieros-paquete-queue` después de persistir cualquier cambio de estado relevante para Finanzas. El payload JSON del evento debe contener única y exclusivamente 3 campos en snake_case:
  - `id_paquete`: UUID del paquete
  - `id_ruta`: Identificador de la ruta
  - `estado`: Estado actual del paquete (por ejemplo `ENTREGADO`, `NOVEDAD_EN_BODEGA`)
- **FR-006**: La publicación del evento debe ocurrir **dentro de la misma transacción** que la persistencia del paquete. Cualquier fallo o excepción en la publicación SQS no se silencia (`no try-catch`), forzando el rollback de la base de datos para preservar la consistencia contable del negocio.
- **FR-007**: El payload delega al Módulo de Gestión de Finanzas (M3) la responsabilidad única de consumir datos adicionales del paquete si los requiere. M3 recibe solo `id_paquete`, `id_ruta`, `estado` y debe consultar por su cuenta cualquier detalle complementario.
- **FR-008**: Detectar y prevenir el procesamiento de eventos duplicados del Módulo 2 mediante validación de timestamp y UUID.
- **FR-009**: Una devolución de ruta solo se puede realizar desde el Módulo de Gestión de Rutas. No se permiten devoluciones originadas desde bodega.

### Key Entities

- **Novedad**: tipo (Dañado / Extraviado / Devolución), origen (Bodega / Ruta), descripción, evidencias adjuntas, fecha/hora, identificador del Controlador responsable, motivo específico.
- **Historial de Estados**: registro inmutable de todas las transiciones del paquete, incluyendo re-ingresos por devolución, con fecha/hora, usuario/módulo responsable.
- **Estado Final**: determina la acción financiera en el Módulo de Gestión de Finanzas.
- **Evento de Notificación a Finanzas**: Mensaje JSON publicado en la cola SQS `eventos-financieros-paquete-queue` después de cada cambio de estado relevante. El payload contiene 3 campos en snake_case (`id_paquete`, `id_ruta`, `estado`). La comunicación con el Módulo de Gestión de Finanzas es **asíncrona**, al igual que la comunicación con el Módulo de Gestión de Rutas.

---

## Success Criteria

- **SC-001**: El 100% de las novedades quedan vinculadas al UUID y visibles en el historial con origen claramente identificado (Bodega / Ruta).
- **SC-002**: El 100% de las novedades de tipo `Dañado` tienen evidencia adjunta antes de guardarse.
- **SC-003**: El evento SQS se publica en la cola `eventos-financieros-paquete-queue` en menos de 500ms tras la persistencia del paquete para el 95% de las operaciones.
- **SC-004**: El 100% de los eventos del Módulo 2 son procesados y registrados en el historial dentro de los 10 segundos posteriores a su recepción.
- **SC-005**: Si la publicación SQS falla, la excepción se propaga al caso de uso y la transacción `@Transactional` se revierte, garantizando que nunca se persista un estado sin su correspondiente notificación a Finanzas.