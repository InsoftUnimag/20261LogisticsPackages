# 04 — Data Flows

## Flujo 1: Admisión Completa con Pesaje (UC-1 + UC-2 + UC-5)

```mermaid
sequenceDiagram
    participant C as Cliente HTTP
    participant CTRL as AdmisionController
    participant UC1 as RegistrarAdmisionUseCase
    participant GEO as GoogleMapsAdapter
    participant COV as CoverageAreaAdapter
    participant DIST as DistanceCalculatorAdapter
    participant PRICE as PriceCalculationServiceImpl
    participant REPO as PaqueteJpaAdapter
    participant EVT as RutaEventAdapter
    participant DB as PostgreSQL
    participant MQ as RabbitMQ

    C->>CTRL: POST /api/paquetes/admision\n{RegistroAdmisionRequest}
    CTRL->>UC1: registrarAdmision(command)
    
    alt coordenadasManuales presentes
        UC1->>UC1: usar coordenadasManuales
    else
        UC1->>GEO: localizar(direccionDestino)
        GEO-->>UC1: Optional<Coordenadas>
    end
    
    UC1->>COV: isWithinCoverage(coordenadas)
    COV-->>UC1: true/false
    alt false
        UC1-->>CTRL: InvalidCoverageException
        CTRL-->>C: 400 COBERTURA_INVALIDA
    end
    
    UC1->>UC1: Paquete.builder() + prePersist()
    UC1->>UC1: paquete.asignarCoordenadas()
    
    alt incluye peso y dimensiones
        UC1->>UC1: new Peso() + new Dimensiones() ← validación
        UC1->>UC1: paquete.procesarPesaje()
        UC1->>DIST: calcularDistanciaDesdeSede(coordenadas)
        DIST-->>UC1: distanciaKm
        UC1->>UC1: paquete.setDistanciaEstimadaKm()
        UC1->>PRICE: calculatePrice(paquete)
        PRICE-->>UC1: BigDecimal precio
        UC1->>UC1: paquete.asignarPrecio(precio)
    end
    
    UC1->>REPO: save(paquete)
    REPO->>DB: INSERT INTO paquetes (...)
    DB-->>REPO: PaqueteDbo
    REPO-->>UC1: Paquete
    
    alt incluye pesaje
        UC1->>EVT: publicarSolicitudRuta(paqueteId)
        EVT-->>UC1: void
    end
    
    UC1-->>CTRL: UUID paqueteId
    CTRL-->>C: 200 { paqueteId: "UUID" }
```

## Flujo 2: Pesaje Separado con Solicitud de Ruta Asíncrona (UC-2 + UC-5 + UC-6)

```mermaid
sequenceDiagram
    participant C as Cliente HTTP
    participant CTRL as PesajeController
    participant UC2 as ProcesarPesajeUseCase
    participant REPO as PaqueteJpaAdapter
    participant UC5 as SolicitarRutaUseCase
    participant MQ as RabbitMQ
    participant LSN as RutaMessageListener
    participant UC6 as AsignarRutaUseCase
    participant DB as PostgreSQL

    C->>CTRL: POST /api/paquetes/pesaje\n{PesajeRequest}
    CTRL->>UC2: procesarPesaje(command)
    
    UC2->>REPO: findById(paqueteId)
    REPO-->>UC2: Paquete
    
    UC2->>UC2: paquete.procesarPesaje(\n  peso, dimensiones,\n  tipoMercancia, irregular)
    Note over UC2: Valida Peso>0, <=70kg\nValida cada dimensión>0\nCalcula volumen, peso volumetrico\nCalcula peso facturable\nDetermina categoría carga\nVerifica densidad atípica
    
    UC2->>UC2: paquete.calcularPrecioEnvio(\n  tarifas)
    UC2->>REPO: save(paquete)
    REPO->>DB: UPDATE paquetes SET ...
    
    UC2->>UC5: handle(SolicitudRutaEvent)
    UC5->>REPO: findById(paqueteId)
    REPO-->>UC5: Paquete
    UC5->>UC5: SolicitudRutaPayload.from(paquete)
    UC5->>MQ: enviarSolicitud(payload)
    Note over MQ: Exchange: solicitudes_ruta_exchange\nRouting Key: solicitud.nueva
    
    UC2-->>CTRL: PesajeResponse
    
    alt UI websocket/polling
        CTRL-->>C: 200 { PesajeResponseDto }
    end
    
    Note over MQ,UC6: --- Flujo Asíncrono ---
    MQ-->>LSN: Respuesta del Módulo Rutas
    LSN->>UC6: asignarRuta(payload)
    UC6->>REPO: findById(paqueteId)
    alt estado == "asignada"
        UC6->>UC6: paquete.asignarRuta(rutaId)
        Note over UC6: Estado → LISTO_PARA_DESPACHO
        UC6->>REPO: save(paquete)
        REPO->>DB: UPDATE paquetes SET ruta_id, estado
    else pendiente/timeout
        UC6->>UC6: log.warn (no guarda cambios)
    end
```

## Flujo 3: Almacenaje + Clasificación por Zona de Destino (UC-4 + UC-3)

```mermaid
sequenceDiagram
    participant C as Cliente HTTP
    participant ALM_CTRL as AlmacenajeController
    participant UC4 as PrepararAlmacenajeUseCase
    participant REPO as PaqueteJpaAdapter
    participant ZREPO as ZonaAlmacenajeJpaAdapter
    participant DB as PostgreSQL
    participant MQ as RabbitMQ
    participant CLAS_LSN as PaqueteListoClasificacionListener
    participant UC3 as ClasificarPaqueteUseCase
    participant ZDREPO as ZonaDestinoJpaAdapter

    C->>ALM_CTRL: GET /api/paquetes/{id}/almacenaje/sugerencia
    ALM_CTRL->>REPO: findById(paqueteId)
    ALM_CTRL->>ZREPO: findCompatibleZonesWithCapacity(tipoMercancia, sedeId)
    ZREPO-->>ALM_CTRL: List<ZonaAlmacenaje>
    ALM_CTRL-->>C: 200 { zonaId, nombreZona }

    C->>ALM_CTRL: POST /api/paquetes/{id}/almacenaje\n{AsignarZonaRequest}
    ALM_CTRL->>UC4: prepararAlmacenaje(command)
    UC4->>REPO: findById(paqueteId)
    UC4->>ZREPO: findById(zonaId)
    
    alt puedeAlbergar && tieneCapacidadPara
        UC4->>UC4: paquete.asignarZonaAlmacenamiento(zonaId)
        Note over UC4: Estado → EN_CLASIFICACION
        UC4->>UC4: zona.agregarPaquete(paquete)
        UC4->>REPO: save(paquete)
        UC4->>ZREPO: save(zona)
        ALM_CTRL-->>C: 201 { mensaje, zonaId }
    else no apta o saturada
        ALM_CTRL-->>C: 400 ZONA_INCOMPATIBLE / 409 ZONA_SATURADA
    end

    Note over MQ,UC3: --- Flujo Asíncrono de Clasificación ---
    MQ-->>CLAS_LSN: paquete_listo_para_clasificar_queue
    CLAS_LSN->>UC3: sugerirZonaParaPaquete(paqueteId)
    UC3->>REPO: findById(paqueteId)
    UC3->>UC3: calculoZonaService.calcularZona(paquete)
    UC3->>ZDREPO: findAllActivas()
    ZDREPO-->>UC3: List<ZonaDestino>
    UC3->>UC3: filtrar por contieneCoordenas()
    UC3-->>CLAS_LSN: ClasificacionSugeridaResponse
    
    C->>ALM_CTRL: GET /api/paquetes/clasificacion/sugerencia/{id}
    ALM_CTRL->>UC3: sugerirZonaParaPaquete(paqueteId)
    UC3-->>ALM_CTRL: zonaSugerida
    ALM_CTRL-->>C: 200 { zonaDestinoId, nombreZona }

    C->>ALM_CTRL: POST /api/paquetes/clasificacion/confirmar\n{ConfirmarZonaRequest}
    ALM_CTRL->>UC3: confirmarClasificacion(paqueteId, zonaDestinoId)
    UC3->>REPO: findById(paqueteId)
    UC3->>ZDREPO: findById(zonaDestinoId)
    Note over UC3: Valida esAptaPara(tipoMercancia)\nValida tieneCapacidadDisponible()
    UC3->>UC3: paquete.asignarZonaDestino(zonaId)
    Note over UC3: Estado → LISTO_PARA_DESPACHO
    UC3->>UC3: zona.incrementarContador()
    UC3->>REPO: save(paquete)
    UC3->>ZDREPO: save(zona)
    ALM_CTRL-->>C: 200 { estado: "LISTO_PARA_DESPACHO" }
```

## Flujo 4: Registro de Novedad en Bodega (UC-10)

```mermaid
sequenceDiagram
    participant C as Cliente HTTP
    participant CTRL as NovedadController
    participant UC10 as RegistrarNovedadUseCase
    participant REPO as PaqueteJpaAdapter
    participant HIST as HistorialEstadoJpaAdapter
    participant S3 as S3ArchivoStorageAdapter
    participant SQS as NovedadEventAdapter

    C->>CTRL: POST /api/paquetes/{id}/novedades\n(multipart: tipoNovedad, observaciones,\n usuarioId, evidencia)

    CTRL->>UC10: registrarNovedad(command)
    UC10->>REPO: findById(paqueteId)
    REPO-->>UC10: Paquete

    alt evidencia presente
        UC10->>S3: guardar("novedades", paqueteId, file)
        S3-->>UC10: urlEvidencia (S3 URL)
    end

    UC10->>UC10: paquete.registrarNovedad(\n  tipoNovedad, observaciones,\n  usuarioId, urlEvidencia)
    Note over UC10: Valida estado (RECIBIDO_EN_SEDE | EN_CLASIFICACION)\nValida evidencia para DAÑADO\nEstado → NOVEDAD_EN_BODEGA\nRetorna HistorialEstado

    UC10->>REPO: save(paquete)
    REPO->>DB: UPDATE paquetes SET estado = NOVEDAD_EN_BODEGA
    UC10->>HIST: guardar(historial)
    HIST->>DB: INSERT INTO historial_estados (...)

    UC10->>SQS: publicarNovedadRegistrada(paqueteId, historialId)
    Note over SQS: Evento: NOVEDAD_REGISTRADA

    UC10-->>CTRL: RegistroNovedadResponse
    CTRL-->>C: 201 { paqueteId, estadoActual, historialId }
```

## Flujo 5: Procesamiento de Eventos de Ruta con Idempotencia (UC-9)

```mermaid
sequenceDiagram
    participant MQ as RabbitMQ\n(eventos_ruta_queue)
    participant LSN as RutaEventListener
    participant UC9 as ProcesarEventoRutaUseCase
    participant EPR as EventoProcesadoJpaAdapter
    participant REPO as PaqueteJpaAdapter
    participant HIST as HistorialEstadoJpaAdapter
    participant NOTIF as MockNotificacionAdapter
    participant DB as PostgreSQL

    MQ->>LSN: EventoRutaDto (JSON)
    LSN->>UC9: procesar(eventoDto)

    UC9->>EPR: yaFueProcesado(eventoId)
    alt evento duplicado
        EPR-->>UC9: true
        UC9-->>LSN: EventoDuplicadoException
        LSN->>LSN: log.warn (descarta)
    else evento nuevo
        EPR-->>UC9: false
        
        UC9->>REPO: findById(paqueteId)
        
        alt tipoEvento == EN_TRANSITO
            UC9->>UC9: paquete.transitarAEnRuta()
            Note over UC9: Estado: LISTO_PARA_DESPACHO → EN_TRANSITO
        else tipoEvento == EN_PARADA_DE_ENTREGA
            UC9->>UC9: paquete.transitarAParadaDeEntrega()
            Note over UC9: Estado: EN_TRANSITO → EN_PARADA_DE_ENTREGA
        else tipoEvento == ENTREGADO
            UC9->>UC9: paquete.entregarPaquete()
            Note over UC9: Estado: EN_PARADA_DE_ENTREGA → ENTREGADO
            Note over UC9: Requiere urlEvidencia y nombreFirmante
        else tipoEvento == DEVOLUCION
            UC9->>UC9: paquete.registrarDevolucionEnRuta()
            Note over UC9: Estado: EN_TRANSITO|PARADA → DEVOLUCION_EN_RUTA
        else tipoEvento == EXTRAVIADO
            UC9->>UC9: paquete.registrarExtraviadoEnRuta()
            Note over UC9: Estado: EN_TRANSITO|PARADA → EXTRAVIADO_EN_RUTA
        else tipoEvento == DAÑADO
            UC9->>UC9: paquete.registrarDañadoEnRuta()
            Note over UC9: Estado: EN_TRANSITO|PARADA → DAÑADO_EN_RUTA
            Note over UC9: Requiere urlEvidencia
        end
        
        UC9->>REPO: save(paquete)
        DB-->>REPO: OK
        
        alt devolvió HistorialEstado
            UC9->>HIST: guardar(historial)
            DB-->>HIST: OK
        end
        
        UC9->>EPR: guardar(EventoProcesado)
        DB-->>EPR: OK
        
        UC9->>NOTIF: enviarSms(remitente.teléfono, mensaje)
        UC9->>NOTIF: enviarSms(destinatario.teléfono, mensaje)
        UC9->>NOTIF: enviarEmail(destinatario.email, asunto, mensaje)
        
        UC9-->>LSN: OK
        LSN->>LSN: log.info
    end
```

## Flujo 6: Consulta Financiera (UC-8)

```mermaid
sequenceDiagram
    participant MOD as Módulo Finanzas
    participant CTRL as ConsultaFinancieraController
    participant UC8 as ConsultarEstadoPaqueteUseCase
    participant REPO as PaqueteJpaAdapter
    participant HIST as HistorialEstadoJpaAdapter
    participant DB as PostgreSQL

    MOD->>CTRL: GET /route/{idRoute}/package/{idPaquete}
    CTRL->>UC8: consultar(idRoute, idPaquete)
    UC8->>REPO: findById(paqueteId)
    
    alt paquete no existe
        REPO-->>UC8: empty
        UC8-->>CTRL: PaqueteNotFoundException
        CTRL-->>MOD: 404 Not Found
    else paquete existe
        REPO-->>UC8: Paquete
        
        alt rutaId no coincide
            UC8-->>CTRL: IllegalArgumentException
            CTRL-->>MOD: 404 Not Found
        else rutaId coincide
            UC8->>HIST: obtenerHistorialPorPaqueteId(paqueteId)
            HIST->>DB: SELECT * FROM historial_estados\nWHERE paquete_id = ?\nORDER BY fecha_transicion ASC
            DB-->>HIST: List<HistorialEstadoEntity>
            HIST-->>UC8: List<HistorialEstado>
            
            UC8-->>CTRL: ConsultaPaqueteResponse
            Note over CTRL: DTO incluye:\n- idRoute, idPaquete\n- estado actual\n- valorDeclarado, precioEnvio\n- metodoPago\n- fechaIngresoUtc, fechaEntregaUtc\n- urlEvidenciaEntrega, nombreFirmante\n- historialEstados[]
            CTRL-->>MOD: 200 OK
        end
    end
```

## Mapa General de Flujo de Datos (End-to-End)

```mermaid
flowchart TD
    subgraph INPUT[Entrada]
        A1[POST /api/paquetes/admision]
        A2[POST /api/paquetes/pesaje]
        A3[POST /api/paquetes/{id}/almacenaje]
        A4[POST /api/paquetes/clasificacion/confirmar]
        A5[POST /api/paquetes/{id}/novedades]
        A6[Evento Módulo Rutas]
    end

    subgraph APP[Capa Aplicación - Use Cases]
        UC1[RegistrarAdmision]
        UC2[ProcesarPesaje]
        UC4[PrepararAlmacenaje]
        UC3[ClasificarPaquete]
        UC10[RegistrarNovedad]
        UC5[SolicitarRuta]
        UC6[AsignarRuta]
        UC9[ProcesarEventoRuta]
        UC8[ConsultarEstado]
    end

    subgraph DOMAIN[Domain Model]
        P[Paquete]
        ZA[ZonaAlmacenaje]
        ZD[ZonaDestino]
        HE[HistorialEstado]
        NOT[Notificacion]
        EP[EventoProcesado]
    end

    subgraph INFRA[Infrastructure]
        JPA[JPA Adapters\nPostgreSQL]
        MQ[RabbitMQ]
        S3[AWS S3]
        SQS[AWS SQS]
        EXT[APIs Externas\nGoogle Maps]
    end

    subgraph OUTPUT[Salida]
        R1[200 JSON Responses]
        R2[RabbitMQ Out]
        R3[S3 URLs]
    end

    A1 --> UC1
    A2 --> UC2
    A3 --> UC4
    A4 --> UC3
    A5 --> UC10
    A6 --> UC9

    UC1 --> P
    UC2 --> P
    UC4 --> P & ZA
    UC3 --> P & ZD
    UC10 --> P & HE
    UC5 --> P
    UC6 --> P
    UC9 --> P & HE & EP & NOT

    UC1 --> EXT
    UC1 --> JPA
    UC2 --> JPA
    UC4 --> JPA
    UC3 --> JPA
    UC10 --> JPA & S3
    UC9 --> JPA

    UC1 --> MQ
    UC5 --> MQ
    UC6 --> MQ
    UC9 --> MQ

    UC10 --> SQS

    JPA --> R1
    MQ --> R2
    S3 --> R3
    R1 --> UI[Frontend / Clientes API]
```
