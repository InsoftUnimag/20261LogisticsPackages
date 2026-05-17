# Consideraciones para Agentes de Generación y Revisión de Código

Este documento establece un conjunto de directrices y reglas críticas que todos los agentes (Analista, Ejecutor, Revisor) deben seguir al interactuar con el código fuente del proyecto. Su propósito es corregir las desviaciones detectadas entre las especificaciones (`/Externo`) y la implementación actual (`/src/main/java/com`), y prevenir futuras violaciones arquitectónicas.

## Principio Rector: Inmutabilidad y Encapsulamiento del Dominio

La desviación más crítica observada es la **mutabilidad de las entidades del dominio**. Las entidades como `Paquete.java` contienen setters públicos (`@Setter`), lo que permite que cualquier parte de la aplicación modifique su estado interno sin control, violando la **Regla de Dependencia** y el **Encapsulamiento**.

**Directiva Principal:** Las entidades de dominio deben ser tan inmutables como sea posible.

1.  **Prohibido `@Setter`**: No se debe usar la anotación `@Setter` de Lombok a nivel de clase o en campos de entidades de dominio.
2.  **Campos `final`**: Los atributos que no cambian después de la creación del objeto deben ser declarados como `final`.
3.  **Métodos de Negocio para Transiciones**: Cualquier cambio en el estado de una entidad debe realizarse a través de un método con un nombre explícito que represente una acción de negocio (ej. `paquete.asignarCoordenadas(coords)` en lugar de `paquete.setCoordenadas(coords)`). Este método es el único lugar donde se debe modificar el estado interno, garantizando que todas las reglas de negocio asociadas se cumplan.

---

## Consideraciones para el Agente Ejecutor de Código (Coder Agent)

Al generar o modificar código, el agente debe adherirse estrictamente a las siguientes reglas:

### Regla #1: El Dominio es Puro y Aislado

El código bajo el paquete `com.logistics.packages.domain` **NO DEBE** tener dependencias de frameworks externos.

-   **Acción Correctiva**: Eliminar cualquier `import` que apunte a `jakarta.validation.*`, `org.springframework.*`, o cualquier otra librería externa de las clases en `domain/model`, `domain/valueobject`, etc.
-   **Ejemplo de Violación Actual**: La clase `Persona.java` utiliza anotaciones de `jakarta.validation`. Estas deben moverse a los DTOs en la capa de infraestructura (ej. `RegistroAdmisionRequest.java`).

### Regla #2: Implementar Entidades Inmutables

Las entidades deben proteger sus invariantes.

-   **Acción Correctiva**: Refactorizar la clase `Paquete.java` para eliminar todos los `@Setter`. Los atributos que se establecen durante el pesaje o cálculos posteriores deben ser modificados a través de métodos específicos.
-   **Ejemplo de Refactorización (`Paquete.java`)**:

    ```java
    // Antes (Incorrecto)
    @Setter
    private Double peso;
    
    // Después (Correcto)
    private Double peso;
    
    public void procesarPesaje(Double peso, Double largo, Double ancho, Double alto, double factorConversion) {
        if (peso <= 0 || largo <= 0 || ancho <= 0 || alto <= 0) {
            throw new IllegalArgumentException("Las dimensiones y el peso deben ser mayores a cero.");
        }
        this.peso = peso;
        this.largo = largo;
        this.ancho = ancho;
        this.alto = alto;
        this.calcularVolumen();
        this.calcularPesoVolumetrico(factorConversion);
        this.calcularPesoFacturable();
        // Aquí se podrían añadir más lógicas, como la asignación de CategoriaCarga
    }
    ```

### Regla #3: Consistencia entre Puertos y Adaptadores

La firma de un método en una interfaz de puerto (capa de aplicación) debe coincidir exactamente con su implementación en el adaptador (capa de infraestructura).

-   **Acción Correctiva**: Corregir la inconsistencia en `GeocodingService`. La interfaz declara `localizar(String direccion)` mientras que el `RegistroAdmisionUseCase` le pasa un objeto `Direccion` y el adaptador `GoogleMapsAdapter` implementa `localizar(Direccion direccion)`. La firma correcta, según el uso, debe ser `localizar(Direccion direccion)`.

### Regla #4: Mapeo sin Pérdida de Datos

Los mappers entre el dominio y la persistencia no deben simplificar o perder la estructura de los Value Objects.

-   **Acción Correctiva**: Modificar `PaqueteDbo.java` y `PaqueteMapper.java`. El campo `direccionDestino` en `PaqueteDbo` es un `String`, pero el Value Object `Direccion` contiene `direccion`, `ciudad`, `departamento` y `pais`. El DBO debe tener columnas separadas para cada uno de estos atributos (`direccion_calle`, `direccion_ciudad`, etc.) para permitir una reconstrucción fiel del Value Object. El uso de `@Embedded` y `@Embeddable` es una alternativa recomendada.

### Regla #5: Uso Correcto de Constructores y Builders

Al instanciar objetos, se debe respetar el método de construcción definido en la clase.

-   **Acción Correctiva**: El `PaqueteMapper` intenta instanciar `Persona` con un constructor `new Persona(...)` que no existe, ya que la clase `Persona` usa `@Builder`. El mapper debe ser corregido para usar `Persona.builder()...build()`.

---

## Consideraciones para el Agente Revisor y QC (Reviewer Agent)

El agente revisor debe actuar como un guardián de la arquitectura, validando los cambios del Coder Agent contra las siguientes listas de verificación.

### Checklist de Revisión de Dominio (`/domain`)

-   [ ] **`@Setter` Inexistente**: ¿La clase de dominio contiene la anotación `@Setter`? -> **Bloquear PR si es afirmativo.**
-   [ ] **Importaciones Puras**: ¿La clase de dominio importa `org.springframework.*`, `jakarta.persistence.*`, `jakarta.validation.*` o similares? -> **Bloquear PR si es afirmativo.**
-   [ ] **Invariantes Protegidas**: ¿Los cambios de estado se realizan a través de métodos de negocio explícitos en lugar de setters directos?
-   [ ] **Value Objects Completos**: ¿Los Value Objects (como `Direccion` en `Persona`) se usan correctamente o se han aplanado a tipos primitivos (como `String`)?

### Checklist de Revisión de Infraestructura (`/infrastructure`)

-   [ ] **Validación en la Frontera**: ¿Los DTOs de entrada (ej. `RegistroAdmisionRequest`) contienen las anotaciones de validación (`@NotNull`, `@Valid`, `@Size`, etc.)?
-   [ ] **Consistencia Puerto-Adaptador**: ¿Las implementaciones de adaptadores (`/adapter/out`) coinciden con las interfaces de puerto (`/application/ports/out`)?
-   [ ] **Mapeo Fiel**: ¿El `PaqueteMapper` (y otros mappers) reconstruye los objetos de dominio completamente sin pérdida de datos (ej. campos de `Direccion`)?
-   [ ] **Manejo de Excepciones**: ¿El `AdmisionController` (y otros controladores) maneja las excepciones de dominio personalizadas (como `InvalidCoverageException`) y las traduce a códigos de estado HTTP apropiados (ej. 4xx)?

---

## Modelo de Generación de Código (SDD — Spec Driven Design)

El proceso de generación de código sigue estos 5 pasos:

1. **Selección del Plan de Implementación:** El usuario especifica qué `MOD-IP-*.md` ejecutar.
2. **Análisis de Documentos:** Analizar el `MOD-IP` y los `MOD-UC` referenciados: requerimientos, entidades, relaciones, edge cases.
3. **Aplicación de Arquitectura Hexagonal:** El código generado debe cumplir: dominio aislado (sin Spring/jakarta), comunicación vía puertos (interfaces) y adaptadores (implementaciones), casos de uso orquestando flujo.
4. **Generación de Código:** Crear en orden: entidades dominio (`domain/model`), Value Objects (`domain/valueobject`), puertos (`application/ports/out` e `in`), casos de uso (`application/usecase`), adaptadores entrada/salida (`infrastructure/adapter/in/web` y `out`), DTOs (`infrastructure/dto`).
5. **Pruebas Unitarias:** Por cada nueva funcionalidad, generar tests JUnit 5 que validen escenarios de éxito, edge cases y manejo de errores.

## Consideraciones para el Agente Analista (Analysis Agent)

Para evitar que estos problemas se originen desde la planificación:

1.  **Especificar Inmutabilidad**: Al crear un Plan de Implementación (`MOD-IP-*.md`), incluir explícitamente la restricción de no usar setters y favorecer campos `final` en las entidades de dominio.
2.  **Detallar Mapeo de Persistencia**: El plan debe sugerir cómo persistir los Value Objects complejos. Por ejemplo, para el objeto `Direccion`, el plan debe especificar que la tabla `paquetes` tendrá columnas como `direccion_calle`, `direccion_ciudad`, `direccion_departamento`, `direccion_pais`.
3.  **Definir Contratos de Puertos Claramente**: Las firmas de los métodos en los puertos de salida (`ports/out`) deben ser precisas en el plan de implementación para que el Coder Agent las implemente sin ambigüedad.

Al seguir estas directrices, los agentes asegurarán que el código generado y modificado se mantenga alineado con los principios de **Arquitectura Limpia y Hexagonal**, resultando en un sistema más robusto, mantenible y fácil de probar.