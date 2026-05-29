# Stage 1: Build y compilación
FROM gradle:8.5-jdk21-alpine AS build
COPY --chown=gradle:gradle . /home/gradle/src
WORKDIR /home/gradle/src

# PASO DE SEGURIDAD: Forzar el copiado manual saltándose cualquier bloqueo de .dockerignore
COPY --chown=gradle:gradle gradlew /home/gradle/src/gradlew
COPY --chown=gradle:gradle gradle /home/gradle/src/gradle

# Elimina los caracteres ocultos de Windows (\r) y repara permisos
RUN sed -i 's/\r$//' gradlew && chmod +x gradlew

RUN gradle clean bootJar -x test --no-daemon

# Stage 2: Runtime ligero de producción
FROM eclipse-temurin:21-jre-alpine
RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring
COPY --from=build /home/gradle/src/build/libs/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app.jar"]