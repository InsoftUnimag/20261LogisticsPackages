package com.logistics.packages.infrastructure.controller;

import com.logistics.packages.domain.exception.*;
import com.logistics.packages.infrastructure.dto.ApiError;
import com.logistics.packages.infrastructure.dto.ApiError.ValidationError;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.List;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(PaqueteNotFoundException.class)
    public ResponseEntity<ApiError> handlePaqueteNotFound(PaqueteNotFoundException ex) {
        log.warn("Paquete no encontrado: {}", ex.getMessage());
        return buildErrorResponse(HttpStatus.NOT_FOUND, "PAQUETE_NO_ENCONTRADO", ex.getMessage());
    }

    @ExceptionHandler(EstadoTransicionInvalidaException.class)
    public ResponseEntity<ApiError> handleEstadoTransicionInvalida(EstadoTransicionInvalidaException ex) {
        log.warn("Transición de estado inválida: {}", ex.getMessage());
        return buildErrorResponse(HttpStatus.BAD_REQUEST, "TRANSICION_INVALIDA", ex.getMessage());
    }

    @ExceptionHandler(EvidenciaRequeridaException.class)
    public ResponseEntity<ApiError> handleEvidenciaRequerida(EvidenciaRequeridaException ex) {
        log.warn("Evidencia requerida faltante: {}", ex.getMessage());
        return buildErrorResponse(HttpStatus.BAD_REQUEST, "EVIDENCIA_REQUERIDA", ex.getMessage());
    }

    @ExceptionHandler(ObjectOptimisticLockingFailureException.class)
    public ResponseEntity<ApiError> handleOptimisticLocking(ObjectOptimisticLockingFailureException ex) {
        log.warn("Conflicto de concurrencia: {}", ex.getMessage());
        return buildErrorResponse(
            HttpStatus.CONFLICT,
            "CONFLICTO_CONCURRENCIA",
            "El paquete fue actualizado por otro usuario. Por favor, intente de nuevo."
        );
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ApiError> handleBadCredentials(BadCredentialsException ex) {
        log.warn("Intento de login con credenciales inválidas");
        return buildErrorResponse(HttpStatus.UNAUTHORIZED, "CREDENCIALES_INVALIDAS", "Usuario o contraseña incorrectos");
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ApiError> handleNoResourceFound(NoResourceFoundException ex) {
        log.warn("Recurso no encontrado: {} {}", ex.getHttpMethod(), ex.getResourcePath());
        return buildErrorResponse(HttpStatus.NOT_FOUND, "RECURSO_NO_ENCONTRADO", "El recurso solicitado no existe.");
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleValidationErrors(MethodArgumentNotValidException ex) {
        log.warn("Error de validación: {}", ex.getMessage());
        List<ValidationError> errores = ex.getBindingResult().getFieldErrors().stream()
                .map(fe -> new ValidationError(fe.getField(), fe.getDefaultMessage()))
                .toList();
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ApiError(HttpStatus.BAD_REQUEST.value(), "ERROR_VALIDACION",
                        "Errores de validación en los datos de entrada", errores));
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiError> handleMethodArgumentTypeMismatch(MethodArgumentTypeMismatchException ex) {
        log.warn("Error de tipo de argumento: {} no es un tipo válido para el parámetro '{}'", 
                 ex.getValue(), ex.getName());
        String mensaje = String.format("El parámetro '%s' debe ser del tipo %s (valor recibido: %s)", 
                ex.getName(), 
                ex.getRequiredType() != null ? ex.getRequiredType().getSimpleName() : "desconocido", 
                ex.getValue());
        return buildErrorResponse(HttpStatus.BAD_REQUEST, "TIPO_PARAMETRO_INVALIDO", mensaje);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiError> handleIllegalArgument(IllegalArgumentException ex) {
        log.warn("Argumento inválido: {}", ex.getMessage());
        return buildErrorResponse(HttpStatus.BAD_REQUEST, "ARGUMENTO_INVALIDO", ex.getMessage());
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ApiError> handleIllegalState(IllegalStateException ex) {
        log.warn("Estado inválido: {}", ex.getMessage());
        return buildErrorResponse(HttpStatus.CONFLICT, "ESTADO_INVALIDO", ex.getMessage());
    }

    @ExceptionHandler(ZonaNoAptaException.class)
    public ResponseEntity<ApiError> handleZonaNoApta(ZonaNoAptaException ex) {
        log.warn("Zona no apta: {}", ex.getMessage());
        return buildErrorResponse(HttpStatus.BAD_REQUEST, "ZONA_NO_APTA", ex.getMessage());
    }

    @ExceptionHandler(ZonaIncompatibleException.class)
    public ResponseEntity<ApiError> handleZonaIncompatible(ZonaIncompatibleException ex) {
        log.warn("Zona incompatible: {}", ex.getMessage());
        return buildErrorResponse(HttpStatus.BAD_REQUEST, "ZONA_NO_APTA", ex.getMessage());
    }

    @ExceptionHandler(ZonaSaturadaException.class)
    public ResponseEntity<ApiError> handleZonaSaturada(ZonaSaturadaException ex) {
        log.warn("Zona saturada: {}", ex.getMessage());
        return buildErrorResponse(HttpStatus.CONFLICT, "ZONA_SATURADA", ex.getMessage());
    }

    @ExceptionHandler(ZonaAlmacenajeNotFoundException.class)
    public ResponseEntity<ApiError> handleZonaAlmacenajeNotFound(ZonaAlmacenajeNotFoundException ex) {
        log.warn("Zona de almacenaje no encontrada: {}", ex.getMessage());
        return buildErrorResponse(HttpStatus.NOT_FOUND, "ZONA_ALMACENAJE_NO_ENCONTRADA", ex.getMessage());
    }

    @ExceptionHandler(ZonaDestinoNotFoundException.class)
    public ResponseEntity<ApiError> handleZonaDestinoNotFound(ZonaDestinoNotFoundException ex) {
        log.warn("Zona de destino no encontrada: {}", ex.getMessage());
        return buildErrorResponse(HttpStatus.NOT_FOUND, "ZONA_DESTINO_NO_ENCONTRADA", ex.getMessage());
    }

    @ExceptionHandler(ZonaDestinoNoEncontradaException.class)
    public ResponseEntity<ApiError> handleZonaDestinoNoEncontrada(ZonaDestinoNoEncontradaException ex) {
        log.warn("Zona de destino no encontrada para las coordenadas: {}", ex.getMessage());
        return buildErrorResponse(HttpStatus.UNPROCESSABLE_ENTITY, "ZONA_DESTINO_NO_ENCONTRADA", ex.getMessage());
    }

    @ExceptionHandler(ZonaDestinoSaturadaException.class)
    public ResponseEntity<ApiError> handleZonaDestinoSaturada(ZonaDestinoSaturadaException ex) {
        log.warn("Zona de destino saturada: {}", ex.getMessage());
        return buildErrorResponse(HttpStatus.CONFLICT, "ZONA_DESTINO_SATURADA", ex.getMessage());
    }

    @ExceptionHandler(CoordenadasInvalidasException.class)
    public ResponseEntity<ApiError> handleCoordenadasInvalidas(CoordenadasInvalidasException ex) {
        log.warn("Coordenadas inválidas: {}", ex.getMessage());
        return buildErrorResponse(HttpStatus.BAD_REQUEST, "COORDENADAS_INVALIDAS", ex.getMessage());
    }

    @ExceptionHandler(EventoDuplicadoException.class)
    public ResponseEntity<ApiError> handleEventoDuplicado(EventoDuplicadoException ex) {
        log.warn("Evento duplicado: {}", ex.getMessage());
        return buildErrorResponse(HttpStatus.CONFLICT, "EVENTO_DUPLICADO", ex.getMessage());
    }

    @ExceptionHandler(InvalidCoverageException.class)
    public ResponseEntity<ApiError> handleInvalidCoverage(InvalidCoverageException ex) {
        log.warn("Cobertura inválida: {}", ex.getMessage());
        return buildErrorResponse(HttpStatus.BAD_REQUEST, "COBERTURA_INVALIDA", ex.getMessage());
    }

    @ExceptionHandler(TimeoutGeocodingException.class)
    public ResponseEntity<ApiError> handleTimeoutGeocoding(TimeoutGeocodingException ex) {
        log.warn("Timeout en geocoding: {}", ex.getMessage());
        return buildErrorResponse(HttpStatus.ACCEPTED, "GPS_PENDIENTE", 
            "Geolocalización no disponible. El paquete fue registrado con GPS pendiente para ingreso manual de coordenadas.");
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleGenericException(Exception ex) {
        log.error("Error inesperado: ", ex);
        return buildErrorResponse(
            HttpStatus.INTERNAL_SERVER_ERROR,
            "ERROR_INTERNO",
            "Ocurrió un error inesperado. Por favor, contacte al administrador."
        );
    }

    private ResponseEntity<ApiError> buildErrorResponse(HttpStatus status, String codigo, String mensaje) {
        return ResponseEntity.status(status)
                .body(new ApiError(status.value(), codigo, mensaje));
    }
}