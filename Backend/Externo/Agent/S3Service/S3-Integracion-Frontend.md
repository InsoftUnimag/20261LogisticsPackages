# Integración con Frontend — Evidencia Fotográfica S3

Los 3 endpoints de registro ahora aceptan `multipart/form-data` en lugar de JSON.
El campo `evidencia` es **opcional** en todos los casos.

---

## 1. Registro de Admisión

### Antes (JSON)
```http
POST /api/paquetes/admision
Content-Type: application/json

{
  "sedeId": "550e8400-e29b-41d4-a716-446655440000",
  "remitente": { ... },
  "destinatario": { ... },
  "direccionDestino": { ... },
  "valorDeclarado": 100000,
  "metodoPago": "CONTRA_ENTREGA",
  "peso": 10.5,
  "largo": 50.0,
  "ancho": 30.0,
  "alto": 20.0
}
```

### Ahora (FormData)
```http
POST /api/paquetes/admision
Content-Type: multipart/form-data

sedeId: "550e8400-e29b-41d4-a716-446655440000"
remitente.tipoDocumento: "CEDULA_CIUDADANIA"
remitente.numeroDocumento: "12345678"
remitente.nombreCompleto: "Remitente"
remitente.telefono: "3001234567"
destinatario.tipoDocumento: "CEDULA_CIUDADANIA"
destinatario.numeroDocumento: "87654321"
destinatario.nombreCompleto: "Destinatario"
destinatario.telefono: "3109876543"
direccionDestino.calle: "Calle Falsa 123"
direccionDestino.complemento: "Apto 101"
direccionDestino.barrio: "Centro"
direccionDestino.ciudad: "Bogotá"
valorDeclarado: 100000
metodoPago: "CONTRA_ENTREGA"
peso: 10.5
largo: 50.0
ancho: 30.0
alto: 20.0
coordenadasManuales.latitud: 4.7110
coordenadasManuales.longitud: -74.0721
evidencia: [ARCHIVO]  ← opcional
```

### Ejemplo JavaScript (Fetch)
```javascript
const formData = new FormData();
formData.append('sedeId', '550e8400-e29b-41d4-a716-446655440000');
formData.append('remitente.tipoDocumento', 'CEDULA_CIUDADANIA');
formData.append('remitente.numeroDocumento', '12345678');
formData.append('remitente.nombreCompleto', 'Juan Pérez');
formData.append('remitente.telefono', '3001234567');
formData.append('destinatario.tipoDocumento', 'CEDULA_CIUDADANIA');
formData.append('destinatario.numeroDocumento', '87654321');
formData.append('destinatario.nombreCompleto', 'María Gómez');
formData.append('destinatario.telefono', '3109876543');
formData.append('direccionDestino.calle', 'Carrera 15 #23-45');
formData.append('direccionDestino.complemento', 'Oficina 301');
formData.append('direccionDestino.barrio', 'Centro');
formData.append('direccionDestino.ciudad', 'Bogotá');
formData.append('valorDeclarado', '100000');
formData.append('metodoPago', 'CONTRA_ENTREGA');
formData.append('peso', '10.5');
formData.append('largo', '50.0');
formData.append('ancho', '30.0');
formData.append('alto', '20.0');

// Opcional: agregar evidencia
if (fileInput.files[0]) {
  formData.append('evidencia', fileInput.files[0]);
}

const response = await fetch('/api/paquetes/admision', {
  method: 'POST',
  body: formData
});
```

### Ejemplo Angular
```typescript
const formData = new FormData();
formData.append('sedeId', this.sedeId);
formData.append('remitente.tipoDocumento', this.remitente.tipoDocumento);
// ... demás campos ...
if (this.selectedFile) {
  formData.append('evidencia', this.selectedFile, this.selectedFile.name);
}

this.http.post('/api/paquetes/admision', formData).subscribe(response => {
  console.log('Paquete creado:', response);
});
```

---

## 2. Procesar Pesaje

### Antes (JSON)
```http
POST /api/paquetes/pesaje
Content-Type: application/json

{
  "paqueteId": "uuid",
  "peso": 10.5,
  "dimensiones": { "largo": 50, "ancho": 30, "alto": 20 },
  "tipoMercancia": "ESTANDAR",
  "formaIrregular": false,
  "valorDeclarado": 100000,
  "tarifaBase": 5000,
  "tarifaPorKg": 200,
  "tarifaPorKm": 100,
  "recargoTipoMercancia": 0,
  "recargoCategoriaCarga": 0
}
```

### Ahora (FormData)
```http
POST /api/paquetes/pesaje
Content-Type: multipart/form-data

paqueteId: "uuid"
peso: 10.5
largo: 50.0
ancho: 30.0
alto: 20.0
tipoMercancia: "ESTANDAR"
formaIrregular: false
valorDeclarado: 100000
tarifaBase: 5000
tarifaPorKg: 200
tarifaPorKm: 100
recargoTipoMercancia: 0
recargoCategoriaCarga: 0
evidencia: [ARCHIVO]  ← opcional
```

### Ejemplo JavaScript
```javascript
const formData = new FormData();
formData.append('paqueteId', paqueteId);
formData.append('peso', '10.5');
formData.append('largo', '50.0');
formData.append('ancho', '30.0');
formData.append('alto', '20.0');
formData.append('tipoMercancia', 'ESTANDAR');
formData.append('formaIrregular', 'false');
formData.append('valorDeclarado', '100000');
formData.append('tarifaBase', '5000');
formData.append('tarifaPorKg', '200');
formData.append('tarifaPorKm', '100');
formData.append('recargoTipoMercancia', '0');
formData.append('recargoCategoriaCarga', '0');

if (fileInput.files[0]) {
  formData.append('evidencia', fileInput.files[0]);
}
```

---

## 3. Confirmar Clasificación

### Antes (JSON)
```http
POST /api/paquetes/clasificacion/confirmar
Content-Type: application/json

{
  "paqueteId": "uuid",
  "zonaDestinoId": "uuid"
}
```

### Ahora (FormData)
```http
POST /api/paquetes/clasificacion/confirmar
Content-Type: multipart/form-data

paqueteId: "uuid"
zonaDestinoId: "uuid"
evidencia: [ARCHIVO]  ← opcional
```

### Ejemplo JavaScript
```javascript
const formData = new FormData();
formData.append('paqueteId', paqueteId);
formData.append('zonaDestinoId', zonaDestinoId);

if (fileInput.files[0]) {
  formData.append('evidencia', fileInput.files[0]);
}
```

---

## Notas Importantes

1. **No enviar `Content-Type` manualmente**: El navegador/`FormData` establece automáticamente `Content-Type: multipart/form-data` con el `boundary` correcto. No lo fuerces a `application/json`.

2. **Campos anidados**: Los objetos como `remitente`, `destinatario`, `direccionDestino` y `coordenadasManuales` se aplanan con notación de punto (`remitente.tipoDocumento`, `direccionDestino.calle`, etc.).

3. **Tamaño máximo de archivo**: Verifica la configuración de `spring.servlet.multipart.max-file-size` en el servidor (default 1MB). Si esperas fotos grandes, ajusta el límite en `application.yml`:
   ```yaml
   spring:
     servlet:
       multipart:
         max-file-size: 10MB
         max-request-size: 10MB
   ```

4. **Formatos de imagen aceptados**: Cualquier formato que Spring pueda leer como `MultipartFile` (JPEG, PNG, WEBP, etc.). El adapter extrae la extensión del nombre original del archivo.
