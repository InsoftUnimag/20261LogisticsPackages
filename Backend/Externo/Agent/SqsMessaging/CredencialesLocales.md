# Credenciales y Configuración Local — Colas SQS

## Mapeo General de las 4 Colas Reales

| # | Cola | ARN | Dueño | Rol M1 | Permiso |
|---|------|-----|-------|--------|---------|
| 1 | `solicitudes-ruta-queue` | `arn:aws:sqs:us-east-2:214654654786:solicitudes-ruta-queue` | M1 (214654654786) | **Produce** (M1→M2) | `sqs:SendMessage` |
| 2 | `eventos-financieros-paquete-queue` | `arn:aws:sqs:us-east-2:214654654786:eventos-financieros-paquete-queue` | M1 (214654654786) | **Produce** (M1→M3) | `sqs:SendMessage` (M1) / `sqs:ReceiveMessage`, `sqs:DeleteMessage` (M3 183678668012) |
| 3 | `logistics-eventos-paquete` | `arn:aws:sqs:us-east-2:383941187903:logistics-eventos-paquete` | M2 (383941187903) | **Consume** (M2→M1) | `sqs:ReceiveMessage`, `sqs:DeleteMessage`, `sqs:ChangeMessageVisibility` |
| 4 | `respuestas-ruta-queue` | `arn:aws:sqs:us-east-2:383941187903:respuestas-ruta-queue` | M2 (383941187903) | **Consume** (M2→M1) | `sqs:ReceiveMessage`, `sqs:DeleteMessage`, `sqs:ChangeMessageVisibility` |

---

## Cola 1: `solicitudes-ruta-queue`

### Identificación
| Campo | Valor |
|-------|-------|
| **Nombre lógico** | `solicitudes-ruta-queue` |
| **Variable de entorno** | `aws.sqs.ruta-request-queue` |
| **Default en código** | `solicitudes-ruta-queue` (hardcoded en `@Value` de `RutaSqsAdapter`) |
| **Perfil activo** | `default`, `local`, `aws` |

### Permisos IAM Mínimos (Cuenta M1)
```json
{
    "Effect": "Allow",
    "Action": "sqs:SendMessage",
    "Resource": "arn:aws:sqs:us-east-2:214654654786:solicitudes-ruta-queue"
}
```

### Config LocalStack
```yaml
# docker-compose.yml (ya configurado)
services:
  localstack:
    image: localstack/localstack:3.0
    environment:
      - SERVICES=sqs,s3
    ports:
      - "4566:4566"
```

```bash
# Crear cola en LocalStack
aws --endpoint-url=http://localhost:4566 sqs create-queue --queue-name solicitudes-ruta-queue
```

### Payload Saliente (M1 produce)
```json
{
    "tipo_evento": "SOLICITAR_RUTA",
    "paquete_id": "uuid",
    "peso_kg": 10.5,
    "volumen_m3": 0.05,
    "direccion": {
        "direccion": "Calle 123 #45-67",
        "ciudad": "Bogotá",
        "pais": "Colombia"
    },
    "latitud": 4.711,
    "longitud": -74.072,
    "fecha_limite_entrega": "2026-06-05T15:00:00Z",
    "tipo_mercancia": "PAQUETE_NORMAL",
    "metodo_pago": "PAGO_CONTRA_ENTREGA"
}
```

**DTO**: `SolicitudRutaPayload` → `RutaSqsAdapter.enviarSolicitud()`

---

## Cola 2: `eventos-financieros-paquete-queue`

### Identificación
| Campo | Valor |
|-------|-------|
| **Nombre lógico** | `eventos-financieros-paquete-queue` |
| **Variable de entorno** | `app.sqs.eventos-financieros-queue` |
| **Default en código** | `eventos-financieros-paquete-queue` (hardcoded en `@Value` de `FinanzasEventSqsAdapter`) |
| **Perfil activo** | `default`, `local`, `aws` |

### Permisos IAM Mínimos

M1 necesita permiso para enviar mensajes:
```json
{
    "Effect": "Allow",
    "Action": "sqs:SendMessage",
    "Resource": "arn:aws:sqs:us-east-2:214654654786:eventos-financieros-paquete-queue"
}
```

Además, M1 debe configurar una política de recursos en la cola para que M3 (183678668012) pueda consumir:
```json
{
    "Effect": "Allow",
    "Principal": {
        "AWS": "arn:aws:iam::183678668012:root"
    },
    "Action": [
        "sqs:ReceiveMessage",
        "sqs:DeleteMessage",
        "sqs:ChangeMessageVisibility"
    ],
    "Resource": "arn:aws:sqs:us-east-2:214654654786:eventos-financieros-paquete-queue"
}
```

### Config LocalStack
```bash
aws --endpoint-url=http://localhost:4566 sqs create-queue --queue-name eventos-financieros-paquete-queue
```

### Payload Saliente (M1 produce)
```json
{
    "id_paquete": "uuid",
    "id_ruta": "uuid",
    "estado": "ENTREGADO"
}
```

**Estados que disparan la publicación**: `ENTREGADO`, `DAÑADO_EN_RUTA`, `EXTRAVIADO_EN_RUTA`, `DEVOLUCION_EN_RUTA`, `NOVEDAD_EN_BODEGA`.

**DTO**: `EventoFinancieroPaqueteDto` → `FinanzasEventSqsAdapter.publicarEstadoFinal()`

---

## Cola 3: `logistics-eventos-paquete`

### Identificación
| Campo | Valor |
|-------|-------|
| **Nombre lógico** | `logistics-eventos-paquete` |
| **Variable de entorno** | `app.sqs.eventos-paquete-queue` |
| **Default en código** | `logistics-eventos-paquete` (hardcoded en `@SqsListener` de `RutaEventSqsListener`) |
| **Perfil `aws`** | `arn:aws:sqs:us-east-2:383941187903:logistics-eventos-paquete` |
| **Perfil activo** | `default`, `local`, `aws` |

### Permisos IAM Mínimos (Cuenta M1)
Requiere política de recursos en la cola (configurada por M2 en su cuenta 383941187903):
```json
{
    "Effect": "Allow",
    "Principal": {
        "AWS": "arn:aws:iam::214654654786:root"
    },
    "Action": [
        "sqs:ReceiveMessage",
        "sqs:DeleteMessage",
        "sqs:ChangeMessageVisibility"
    ],
    "Resource": "arn:aws:sqs:us-east-2:383941187903:logistics-eventos-paquete"
}
```

### Config LocalStack (simulando M2)
```bash
aws --endpoint-url=http://localhost:4566 sqs create-queue --queue-name logistics-eventos-paquete
```

### Payload Entrante (M1 consume) — Polimórfico

**Base** (todos los eventos incluyen):
```json
{
    "tipo_evento": "PAQUETE_EN_TRANSITO",
    "paquete_id": "uuid",
    "ruta_id": "uuid",
    "fecha_hora_evento": "2026-05-29T10:30:00Z"
}
```

**Subtipo `PAQUETE_ENTREGADO`** — incluye `evidencia`:
```json
{
    "tipo_evento": "PAQUETE_ENTREGADO",
    "paquete_id": "uuid",
    "ruta_id": "uuid",
    "fecha_hora_evento": "2026-05-29T10:30:00Z",
    "evidencia": {
        "url_foto": "https://s3.amazonaws.com/evidencias/foto.jpg",
        "url_firma": "https://s3.amazonaws.com/evidencias/firma.jpg"
    }
}
```

**Subtipo `PARADA_FALLIDA`** — incluye `motivo`:
```json
{
    "tipo_evento": "PARADA_FALLIDA",
    "paquete_id": "uuid",
    "ruta_id": "uuid",
    "fecha_hora_evento": "2026-05-29T10:30:00Z",
    "motivo": "CLIENTE_AUSENTE"
}
```
Valores de `motivo`: `CLIENTE_AUSENTE`, `DIRECCION_INCORRECTA`, `RECHAZADO_POR_CLIENTE`, `ZONA_DIFICIL_ACCESO`.

**Subtipo `NOVEDAD_GRAVE`** — incluye `tipo_novedad`:
```json
{
    "tipo_evento": "NOVEDAD_GRAVE",
    "paquete_id": "uuid",
    "ruta_id": "uuid",
    "fecha_hora_evento": "2026-05-29T10:30:00Z",
    "tipo_novedad": "DAÑADO_EN_RUTA"
}
```
Valores de `tipo_novedad`: `DAÑADO_EN_RUTA`, `EXTRAVIADO`, `DEVOLUCION`.

**Subtipo `PARADAS_SIN_GESTIONAR`** — incluye lista de paquetes:
```json
{
    "tipo_evento": "PARADAS_SIN_GESTIONAR",
    "ruta_id": "uuid",
    "fecha_hora_evento": "2026-05-29T10:30:00Z",
    "tipo_cierre": "CIERRE_TURNO",
    "paquetes": [
        { "paquete_id": "uuid" },
        { "paquete_id": "uuid" }
    ]
}
```

**Subtipo `PAQUETE_EXCLUIDO_DESPACHO`** — sin campos adicionales:
```json
{
    "tipo_evento": "PAQUETE_EXCLUIDO_DESPACHO",
    "paquete_id": "uuid",
    "ruta_id": "uuid",
    "fecha_hora_evento": "2026-05-29T10:30:00Z"
}
```

**Mapper**: `EventoPaqueteM2Mapper.mapToEventoRuta()` → `List<EventoRutaDto>` → `ProcesarEventoRutaUseCase.procesar()`.

---

## Cola 4: `respuestas-ruta-queue`

### Identificación
| Campo | Valor |
|-------|-------|
| **Nombre lógico** | `respuestas-ruta-queue` |
| **Variable de entorno** | `aws.sqs.ruta-response-queue` |
| **Default en código** | `respuestas-ruta-queue` (hardcoded en `@SqsListener` de `RutaSqsListener`) |
| **Perfil `aws`** | `arn:aws:sqs:us-east-2:383941187903:respuestas-ruta-queue` |
| **Perfil activo** | `default`, `local`, `aws` |

### Permisos IAM Mínimos (Cuenta M1)
Requiere política de recursos en la cola (configurada por M2):
```json
{
    "Effect": "Allow",
    "Principal": {
        "AWS": "arn:aws:iam::214654654786:root"
    },
    "Action": [
        "sqs:ReceiveMessage",
        "sqs:DeleteMessage",
        "sqs:ChangeMessageVisibility"
    ],
    "Resource": "arn:aws:sqs:us-east-2:383941187903:respuestas-ruta-queue"
}
```

### Config LocalStack (simulando M2)
```bash
aws --endpoint-url=http://localhost:4566 sqs create-queue --queue-name respuestas-ruta-queue
```

### Payload Entrante (M1 consume)
```json
{
    "tipo_evento": "RUTA_ASIGNADA",
    "paquete_id": "uuid",
    "ruta_id": "uuid",
    "fecha_hora_evento": "2026-05-29T10:30:00-05:00"
}
```

**Validaciones en `RutaSqsListener`:**
- `tipo_evento` debe ser exactamente `RUTA_ASIGNADA` (si no, se descarta con warn).
- `ruta_id` no debe ser `null` (si no, se descarta con warn).

**DTO**: `RespuestaRutaPayload` → `RutaSqsListener.recibirRespuesta()` → `AsignarRutaUseCase.asignarRuta()`.

---

## Configuración por Perfil

### Perfil `default` / `local` (application.yml)
```yaml
spring:
  cloud:
    aws:
      sqs:
        endpoint: http://localhost:4566

aws:
  sqs:
    ruta-request-queue: solicitudes-ruta-queue
    ruta-response-queue: respuestas-ruta-queue

app:
  sqs:
    eventos-paquete-queue: logistics-eventos-paquete
```

### Perfil `aws` (application-aws.yml)
```yaml
app:
  sqs:
    eventos-paquete-queue: arn:aws:sqs:us-east-2:383941187903:logistics-eventos-paquete

aws:
  sqs:
    ruta-response-queue: arn:aws:sqs:us-east-2:383941187903:respuestas-ruta-queue
```

> **Nota**: Las colas propiedad de M1 (`solicitudes-ruta-queue`, `eventos-financieros-paquete-queue`) no requieren ARN en el perfil `aws` porque M1 puede resolver el nombre corto dentro de su propia cuenta. Sin embargo, `eventos-financieros-paquete-queue` necesita política de recursos que otorgue acceso cross-account a M3 (183678668012).

### Perfil `test` (application-test.yml)
```yaml
# SQS auto-config excluido para tests unitarios
spring:
  autoconfigure:
    exclude: org.springframework.cloud.aws.autoconfigure.sqs.SqsAutoConfiguration
```

---

## Credenciales AWS

### Desarrollo Local (LocalStack)
```yaml
# application-local.yml
spring:
  cloud:
    aws:
      credentials:
        access-key: test
        secret-key: test
      sqs:
        endpoint: http://localhost:4566
```

### Producción (AWS)
```properties
# Variables de entorno (no commited)
SPRING_CLOUD_AWS_REGION_STATIC=us-east-2
SPRING_CLOUD_AWS_CREDENTIALS_ACCESS-KEY=<access-key>
SPRING_CLOUD_AWS_CREDENTIALS_SECRET-KEY=<secret-key>
SPRING_CLOUD_AWS_SQS_ENDPOINT=https://sqs.us-east-2.amazonaws.com
```

---

## Política IAM para M1 (Identity-based)

```json
{
    "Version": "2012-10-17",
    "Statement": [
        {
            "Sid": "SendToOwnQueues",
            "Effect": "Allow",
            "Action": "sqs:SendMessage",
            "Resource": [
                "arn:aws:sqs:us-east-2:214654654786:solicitudes-ruta-queue",
                "arn:aws:sqs:us-east-2:214654654786:eventos-financieros-paquete-queue"
            ]
        },
        {
            "Sid": "ReceiveFromM2Queues",
            "Effect": "Allow",
            "Action": [
                "sqs:ReceiveMessage",
                "sqs:DeleteMessage",
                "sqs:ChangeMessageVisibility"
            ],
            "Resource": [
                "arn:aws:sqs:us-east-2:383941187903:logistics-eventos-paquete",
                "arn:aws:sqs:us-east-2:383941187903:respuestas-ruta-queue"
            ]
        }
    ]
}
```

## Política de Recursos para `eventos-financieros-paquete-queue` (Cross-Account M3)

M1 debe adjuntar esta política a su cola para permitir que M3 (183678668012) consuma:

```json
{
    "Version": "2012-10-17",
    "Statement": [
        {
            "Sid": "Allow-M3-Consume",
            "Effect": "Allow",
            "Principal": {
                "AWS": "arn:aws:iam::183678668012:root"
            },
            "Action": [
                "sqs:ReceiveMessage",
                "sqs:DeleteMessage",
                "sqs:ChangeMessageVisibility"
            ],
            "Resource": "arn:aws:sqs:us-east-2:214654654786:eventos-financieros-paquete-queue"
        }
    ]
}
```

## Resumen de Cuentas AWS

| Módulo | Account ID | Rol |
|--------|-----------|-----|
| M1 (Logística) | `214654654786` | Dueño de `solicitudes-ruta-queue` y `eventos-financieros-paquete-queue`. Consume colas de M2. |
| M2 (Rutas) | `383941187903` | Dueño de `logistics-eventos-paquete` y `respuestas-ruta-queue`. Consume `solicitudes-ruta-queue`. |
| M3 (Finanzas) | `183678668012` | Consume `eventos-financieros-paquete-queue` (cross-account desde M1). |
