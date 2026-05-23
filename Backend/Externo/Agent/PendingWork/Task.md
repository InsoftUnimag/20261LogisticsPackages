## 📋 Backlog Redefinido y Secuencial

Aquí está tu nueva hoja de ruta. La procesaremos estrictamente en este orden:

* **Tarea 1:** Migración de RabbitMQ a Amazon SQS. `[COMPLETADA]`
* **Tarea 2: Definición y Diseño del flujo con M2 (Contratos e Infraestructura).** `[COMPLETADA]`
* *Objetivo:* Definir la estrategia de ramas de GitFlow y mapear los DTOs técnicos basados en la SPEC de integración de M2.


* **Tarea 3: Implementación secuencial de las 3 colas de M2.** `[COMPLETADA]`
* *Cola 1 (M1 escribe):* Solicitud de Ruta (`SOLICITAR_RUTA`).
* *Cola 2 (M1 lee):* Asignación de Ruta (`respuestas-ruta-queue`). *Pendiente: producer de M2 no implementado.*
* *Cola 3 (M1 lee):* Eventos de Estado de Parada (`PAQUETE_EN_TRANSITO`, `NOVEDAD_GRAVE`, etc.). ✅ Resuelto en PR7.


* **Tarea 4: Actualización de UC e IP 007 (Gestión de Novedades — Síncrono a Asíncrono).**
* *Objetivo:* Ahora sí, con los eventos de `NOVEDAD_GRAVE` de M2 asimilados, estructuramos el flujo asíncrono final hacia M3.


* **Tarea 5: Verificación y Pruebas de Humo en AWS Real.**
* *Objetivo:* Validar la conectividad *end-to-end* enviando y consumiendo mensajes reales.


* **Tarea 6: Contextualización sobre Módulo Profesor.**
* **Tarea 7: JSON para Módulo Profesor.**

---

## ☁️ Consideraciones para trabajar con AWS SQS Real

Ya que vas a trabajar con el entorno real de AWS y no con LocalStack, debemos seguir estas buenas prácticas de infraestructura para evitar pisarse los dedos con los otros módulos:

1. **Nombres de Colas Fijos:** Las colas ya no usan prefijo dinámico. Los nombres actuales son: `solicitudes-ruta-queue`, `logistics-eventos-paquete`, `respuestas-ruta-queue`, `paquete-listo-clasificar-queue`. Estos deben coincidir exactamente con los nombres creados en AWS.
2. **Manejo Seguro de Credenciales:** Queda terminantemente prohibido escribir las `aws.secret-key` o tokens en los archivos de configuración del proyecto. Spring Cloud AWS detecta automáticamente las credenciales si tienes configurado tu entorno local mediante el AWS CLI (`~/.aws/credentials`). Usaremos el `DefaultCredentialsProvider`.