# Credenciales AWS S3 — Configuración y Uso

## Variables de entorno

| Variable | Propósito | ¿Requerida? |
|----------|-----------|-------------|
| `AWS_ACCESS_KEY_ID` | Access Key de AWS/IAM | Sí (producción) |
| `AWS_SECRET_ACCESS_KEY` | Secret Key de AWS/IAM | Sí (producción) |
| `AWS_REGION` | Región AWS (default: `us-east-2`) | No |
| `AWS_S3_BUCKET_NAME` | Nombre del bucket S3 | Sí |

Spring Cloud AWS lee automáticamente `AWS_ACCESS_KEY_ID` y `AWS_SECRET_ACCESS_KEY` del entorno.

---

## Perfiles de ejecución

### Local (Spring Profile: `local`)

Credenciales **fijas** para LocalStack (sin validez real):

```yaml
# application-local.yml
spring:
  cloud:
    aws:
      credentials:
        access-key: test
        secret-key: test
```

No requieren configuración adicional. LocalStack ignora la validez de las credenciales por defecto.

### Producción (Spring Profile: `aws`)

Credenciales IAM reales con `S3Client` + `S3Presigner`:

```yaml
# application-aws.yml
spring:
  cloud:
    aws:
      region:
        static: us-east-2
      s3:
        region: us-east-2
        path-style-access-enabled: false
```

Las credenciales se obtienen de las variables de entorno `AWS_ACCESS_KEY_ID` y `AWS_SECRET_ACCESS_KEY`, o del perfil de AWS configurado en la instancia (EC2 instance profile, ECS task role, etc.).

---

## Política IAM mínima requerida (producción)

```json
{
  "Version": "2012-10-17",
  "Statement": [
    {
      "Effect": "Allow",
      "Action": [
        "s3:PutObject",
        "s3:GetObject",
        "s3:DeleteObject",
        "s3:ListBucket"
      ],
      "Resource": [
        "arn:aws:s3:::logistics-packages-prod",
        "arn:aws:s3:::logistics-packages-prod/*"
      ]
    }
  ]
}
```

| Acción | ¿Por qué? | ¿Requerida? |
|--------|-----------|-------------|
| `s3:PutObject` | Subir evidencia (`guardar()`) | Sí |
| `s3:GetObject` | Leer archivos (`obtenerBytes()`) | Sí |
| `s3:DeleteObject` | Eliminar archivos (`eliminar()`) | Sí |
| `s3:ListBucket` | Listar objetos por prefijo (`listObjectsV2`) | Sí |

Para URLs firmadas se usa `S3Presigner`, que requiere permisos `s3:GetObject` en la política del **solicitante**, no de quien descarga (la URL firmada tiene permisos embebidos).

**No requiere** `s3:CreateBucket` ni `s3:HeadBucket` en producción (`auto-create-bucket=false`).

---

## Configuración en diferentes entornos

### Desarrollo Local (sin Docker)

No aplica. S3 requiere LocalStack o AWS real.

### Desarrollo Local (con LocalStack via Docker Compose)

```yaml
# docker-compose.yml (fragmento)
services:
  localstack:
    image: localstack/localstack:3.4.0
    ports:
      - "4566:4566"
    environment:
      - SERVICES=sqs,s3
      - AWS_ACCESS_KEY_ID=test
      - AWS_SECRET_ACCESS_KEY=test
      - AWS_DEFAULT_REGION=us-east-2
      - S3_SKIP_SIGNATURE_VALIDATION=1  # Opcional: simplifica dev
```

### Pipeline CI/CD (GitHub Actions / Jenkins)

```yaml
# .github/workflows/ci.yml (fragmento)
- name: Run integration tests
  run: ./gradlew test
  env:
    AWS_ACCESS_KEY_ID: ${{ secrets.AWS_ACCESS_KEY_ID }}
    AWS_SECRET_ACCESS_KEY: ${{ secrets.AWS_SECRET_ACCESS_KEY }}
    AWS_REGION: us-east-2
```

### Producción (ECS / EC2 / EKS)

Usar **IAM Roles** en lugar de Access Keys estáticas:

```yaml
# application-aws.yml no necesita credentials block
# El SDK obtiene las credenciales del IAM Role automáticamente
spring:
  cloud:
    aws:
      region:
        static: us-east-2
```

---

## Resolución de problemas

| Síntoma | Causa probable | Solución |
|---------|---------------|----------|
| `The AWS Access Key Id you provided does not exist` | Credenciales inválidas | Verificar `AWS_ACCESS_KEY_ID` |
| `Access Denied` al listar objetos | Falta `s3:ListBucket` en policy | Agregar `s3:ListBucket` al bucket |
| `Access Denied` al subir archivo | Falta `s3:PutObject` en policy | Agregar `s3:PutObject` al `arn:*` del bucket |
| `NoSuchBucket` | Bucket no existe | Crear bucket manualmente, o setear `auto-create-bucket=true` (solo dev/test) |
| Connection refused (LocalStack) | LocalStack no está corriendo | `docker compose up -d localstack` |
| SignatureDoesNotMatch (LocalStack) | Firma inválida | Setear `S3_SKIP_SIGNATURE_VALIDATION=1` en el contenedor LocalStack |
