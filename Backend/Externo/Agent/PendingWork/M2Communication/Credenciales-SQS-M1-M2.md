# Configuración de Credenciales SQS para Comunicación M1 ↔ M2

**Fecha:** 2026-05-18
**Proyecto:** 20261LogisticsRoutes (M2)
**Versión:** 1.0

---

## 1. Resumen Ejecutivo

El Módulo 2 (Routes) actúa como receptor y emisor en la comunicación con el Módulo 1 (Packages). A continuación se documentan las credenciales SQS usadas para ambos sentidos de la comunicación, organizadas por perfil de entorno.

---

## 2. Credenciales AWS Base (Ambos Sentidos de Comunicación)

### 2.1 Perfil DEV — `application-dev.yml:32-40`

```yaml
spring:
  cloud:
    aws:
      credentials:
        access-key: test
        secret-key: test
      region:
        static: us-east-1
      sqs:
        endpoint: http://localhost:4566
```

| Propiedad | Valor | Descripción |
|-----------|-------|-------------|
| `access-key` | `test` | Clave hardcodeada para LocalStack |
| `secret-key` | `test` | Clave hardcodeada para LocalStack |
| `region` | `us-east-1` | Región AWS |
| `endpoint` | `http://localhost:4566` | Endpoint de LocalStack (SQS real en dev local) |

### 2.2 Perfil PROD — `application-prod.yml:30-32`

```yaml
spring.cloud.aws:
  region:
    static: ${AWS_REGION:us-east-1}
  # Sin access-key/secret-key: se resuelve con IAM role o variables de entorno AWS_*
```

| Propiedad | Valor | Descripción |
|-----------|-------|-------------|
| `region` | `${AWS_REGION:us-east-1}` | Variable de entorno |
| `access-key` | Sin hardcodeo | Resuelve desde IAM Role en EC2/ECS |
| `secret-key` | Sin hardcodeo | Resuelve desde IAM Role en EC2/ECS |

### 2.3 Perfil TEST — `application-test.yml:28-36`

```yaml
spring:
  cloud:
    aws:
      credentials:
        access-key: test
        secret-key: test
      region:
        static: us-east-1
      sqs:
        endpoint: http://localhost:4566
```

| Propiedad | Valor | Descripción |
|-----------|-------|-------------|
| `access-key` | `test` | Clave hardcodeada para LocalStack |
| `secret-key` | `test` | Clave hardcodeada para LocalStack |
| `region` | `us-east-1` | Región AWS |
| `endpoint` | `http://localhost:4566` | Endpoint de LocalStack |

---

## 3. Colas SQS y Dirección de Comunicación

### 3.1 Flujo: M1 → M2 (M1 escribe / M2 lee)

| Dato | DEV | PROD |
|------|-----|------|
| **Property** | `app.sqs.solicitudes-ruta-queue` | `${SQS_SOLICITUDES_RUTA_QUEUE}` |
| **Nombre cola** | `solicitudes-ruta-queue` | Variable de entorno |
| **M1 rol** | Producer (envía SOLICITAR_RUTA) | Producer |
| **M2 rol** | Consumer (`SolicitarRutaConsumer` con `@SqsListener`) | Consumer |
| **Clase consumidor en M2** | `com.logistics.routes.infrastructure.adapter.in.messaging.SolicitarRutaConsumer` | — |
| **Perfil activo en M2** | `aws` (`@Profile("aws")`) | `aws` |

### 3.2 Flujo: M2 → M1 (M2 escribe / M1 lee)

| Dato | DEV | PROD |
|------|-----|------|
| **Property** | `app.sqs.eventos-paquete-queue` | `${SQS_EVENTOS_PAQUETE_QUEUE}` |
| **Nombre cola (DEV)** | `logistics-eventos-paquete` (según ARN de M2: `arn:aws:sqs:us-east-2:383941187903:logistics-eventos-paquete`) | Variable de entorno |
| **M2 rol** | Producer (`SqsIntegracionModulo1Adapter`) | Producer |
| **M1 rol** | Consumer (`RutaEventSqsListener`) | Consumer |
| **Clase producer en M2** | `com.logistics.routes.infrastructure.adapter.out.messaging.SqsIntegracionModulo1Adapter` | — |
| **Eventos publicados** | `PAQUETE_EN_TRANSITO`, `PAQUETE_ENTREGADO`, `PARADA_FALLIDA`, `NOVEDAD_GRAVE`, `PARADAS_SIN_GESTIONAR`, `PAQUETE_EXCLUIDO_DESPACHO` | — |
| **Perfil activo en M2** | `aws` (`@Profile("aws")`) | `aws` |

### 3.3 Flujo: M2 → M1 — RUTA_ASIGNADA (Pendiente de implementación)

| Dato | Estado |
|------|--------|
| **Property** | `aws.sqs.ruta-response-queue` en M1 |
| **Nombre cola** | `respuestas-ruta-queue` |
| **M2 rol** | **NO IMPLEMENTADO** — M2 no tiene producer hacia esta cola |
| **M1 rol** | Consumer (`RutaSqsListener`) escuchando |
| **Evento esperado** | `RUTA_ASIGNADA` con `paquete_id`, `ruta_id`, `fecha_hora_evento` |

---

## 4. Variables de Entorno Requeridas para Producción

### 4.1 M2 (Routes) — application-prod.yml

| Variable de Entorno | Descripción |
|--------------------|-------------|
| `AWS_REGION` | Región AWS (default: `us-east-1`) |
| `SQS_SOLICITUDES_RUTA_QUEUE` | Nombre de la cola de solicitudes de ruta |
| `SQS_EVENTOS_PAQUETE_QUEUE` | Nombre de la cola de eventos de paquete |
| `SQS_CIERRE_RUTA_QUEUE` | Nombre de la cola de cierre de ruta (hacia M3) |
| `DB_URL` | URL de conexión PostgreSQL |
| `DB_USERNAME` | Usuario de base de datos |
| `DB_PASSWORD` | Contraseña de base de datos |
| `REDIS_HOST` | Host de Redis |
| `REDIS_PASSWORD` | Contraseña de Redis (opcional) |
| `JWT_SECRET` | Secreto para firma JWT |

### 4.2 Credenciales AWS en Producción

| Método | Descripción |
|--------|-------------|
| **IAM Role (EC2/ECS)** | Recomendado. Las credenciales se resuelven automáticamente desde el rol adjunto a la instancia. |
| **Variables de entorno** | Alternativa: `AWS_ACCESS_KEY_ID`, `AWS_SECRET_ACCESS_KEY`, `AWS_SESSION_TOKEN` |

> **Nota:** No se deben hardcodear `access-key` ni `secret-key` en producción.

---

## 5. Configuración de Integración End-to-End

```
┌──────────────────────────────────────────────────────────────┐
│  MÓDULO 1 — LogisticsPackages (Packages)                     │
│                                                              │
│  Producer:  RutaSqsAdapter                                   │
│             Cola: solicitudes-ruta-queue                     │
│             Eventos: SOLICITAR_RUTA                          │
│                                                              │
│  Consumer:  RutaEventSqsListener                             │
│             Cola: logistics-eventos-paquete                  │
│             Eventos: 6 tipos de eventos M2                   │
│                                                              │
│  Consumer:  RutaSqsListener                                  │
│             Cola: respuestas-ruta-queue                      │
│             Eventos: RUTA_ASIGNADA ← SIN IMPLEMENTAR EN M2   │
└──────────────────────────────────────────────────────────────┘

┌──────────────────────────────────────────────────────────────┐
│  MÓDULO 2 — LogisticsRoutes (Routes)                         │
│                                                              │
│  Consumer:  SolicitarRutaConsumer (@SqsListener)             │
│             Cola: solicitudes-ruta-queue                     │
│             Eventos: SOLICITAR_RUTA                          │
│             Perfil: aws                                      │
│                                                              │
│  Producer:  SqsIntegracionModulo1Adapter                     │
│             Cola: logistics-eventos-paquete                  │
│             Eventos: 6 tipos de eventos                      │
│             Perfil: aws                                      │
└──────────────────────────────────────────────────────────────┘
```

---

## 6. Notas de Seguridad

| # | Nota |
|----|------|
| 1 | En dev y test se usan credenciales hardcodeadas (`test/test`)指向 LocalStack. Nunca usar en producción. |
| 2 | En producción, M2 debe tener permisos IAM para: `sqs:SendMessage` y `sqs:ReceiveMessage` en las colas `solicitudes-ruta-queue` y `eventos-paquete-queue`. |
| 3 | El `endpoint: http://localhost:4566` solo aplica en perfiles dev y test. En prod se usa el endpoint real de AWS SQS. |
| 4 | M2 tiene un adapter stub `InMemoryIntegracionModulo1Adapter` para perfiles no-aws (dev/test) que solo hace logging en lugar de publicar a SQS. |

---

*Documento generado automáticamente por el Agente de Auditoría de Integración SQS.*