# Análisis de la Estructura de Carpetas del Backend

El proyecto `Backend` está construido bajo **Java** y **Spring Boot**, utilizando **Gradle** como herramienta de construcción. Además, la estructura del código está fuertemente orientada hacia la **Arquitectura Hexagonal (Clean Architecture)**, dividiendo claramente las responsabilidades en capas concéntricas.

A continuación se detalla la estructura principal de carpetas y su propósito:

## Estructura Raíz (`./Backend/`)

- `.gradle/`, `.idea/`, `build/`: Directorios generados automáticamente por el IDE (IntelliJ IDEA) y el sistema de construcción de Gradle. Contienen binarios compilados y caché. No deben versionarse ni modificarse manualmente.
- `Externo/`: Contiene documentación técnica, guías de desarrollo y archivos para agentes (`Agent`).
  - `Agent/`: Directorio específico de contexto para agentes de IA, que incluye documentación clave como `ApiDocs.md`, `HTTP-Methods.md`, `ConsideracionesAgentes.md`, y la subcarpeta `Project/` donde se ubica este documento.
- `gradle/`, `build.gradle`, `settings.gradle`, `gradlew`, `gradlew.bat`: Archivos de configuración de dependencias y wrappers de Gradle para compilar el proyecto de forma estandarizada.
- `docker-compose.yml`, `Dockerfile`: Configuración para la contenerización del proyecto, permitiendo levantar la base de datos y/u otros servicios mediante Docker.
- `flyway.conf`: Archivo de configuración para Flyway, utilizado para la gestión de migraciones de la base de datos.
- `src/`: Carpeta fundamental que contiene todo el código fuente y recursos estáticos del proyecto.

---

## Código Fuente (`./Backend/src/`)

El directorio `src` se divide clásicamente en el código de producción (`main`) y el código de pruebas (`test`).

### 1. Capa Principal de Producción (`src/main/`)

#### 1.1 `java/com/logistics/packages/`
Este es el paquete raíz de la aplicación. Aquí es donde se materializa la Arquitectura Hexagonal a través de tres subdirectorios principales:

- **`domain/` (Capa de Dominio)**
  Representa el núcleo de la aplicación. Contiene la lógica de negocio pura y las reglas fundamentales, sin depender de ningún framework externo.
  - `model/`: Entidades centrales del negocio (ej. `Paquete.java`, `Usuario.java`, `ZonaAlmacenaje.java`).
  - `valueobject/`: Objetos de valor inmutables que representan conceptos descriptivos sin identidad.
  - `exception/`: Excepciones personalizadas y específicas del dominio logístico.
  - `service/`: Servicios de dominio para lógica que no pertenece a una sola entidad.
  - `event/`: Modelos para los eventos de dominio.

- **`application/` (Capa de Aplicación)**
  Contiene los casos de uso del sistema. Orquesta el flujo de datos hacia y desde el dominio, definiendo interfaces (puertos) para la comunicación exterior.
  - `usecase/`: Implementaciones de los flujos de negocio (ej. `RegistrarAdmisionUseCase.java`, `ClasificarPaqueteUseCase.java`).
  - `ports/`: Interfaces (Puertos de entrada/salida) que dictan cómo interactuar con el dominio o cómo el dominio espera interactuar con servicios externos.
  - `repository/`: Interfaces abstractas para la persistencia de datos.

- **`infrastructure/` (Capa de Infraestructura)**
  Es la capa más externa y técnica. Implementa los puertos definidos en la capa de aplicación y gestiona detalles como bases de datos, APIs web, frameworks, seguridad, etc.
  - `controller/`: Controladores REST (Endpoints que exponen la API hacia el exterior).
  - `dto/`: Objetos de Transferencia de Datos (Request y Response) para aislar la capa web.
  - `adapter/`: Implementaciones concretas de los puertos (ej. repositorios JPA, llamadas a APIs externas).
  - `security/`: Configuración y filtros de seguridad (Spring Security, manejo de tokens JWT).
  - `config/`: Clases globales de configuración de Spring Boot (CORS, OpenAPI, Beans).
  - `services/`: Implementación de servicios técnicos (ej. integración con S3, mensajería).

#### 1.2 `resources/`
Contiene los archivos de configuración no compilables y propiedades de entorno.
- `application.yml`, `application.properties`: Configuraciones generales de Spring Boot.
- `application-local.yml`: Perfil de configuración específico para el entorno de desarrollo local.
- `db/migration/`: Directorio crítico para Flyway. Contiene todos los scripts SQL (`V1__...sql`, `V2__...sql`) que versionan la estructura de la base de datos, asegurando un esquema consistente.

---

### 2. Capa de Pruebas (`src/test/`)

Sigue una estructura en espejo a `main` para facilitar la localización de los tests correspondientes.
- `java/com/logistics/packages/`: Contiene pruebas unitarias, de integración y de arquitectura. Se puede observar el uso de `Testcontainers` (ej. `TestcontainersConfiguration.java`) para ejecutar pruebas de integración robustas con contenedores Docker reales (bases de datos efímeras).
- `resources/`: Propiedades específicas de los tests, como `application-test.yml` y datos semilla, asegurando que las pruebas corran en un entorno predecible y aislado.
