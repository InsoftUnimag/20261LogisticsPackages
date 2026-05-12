package com.logistics.packages.infrastructure.controller;

import com.logistics.packages.domain.exception.EstadoTransicionInvalidaException;
import com.logistics.packages.domain.exception.EvidenciaRequeridaException;
import com.logistics.packages.domain.exception.PaqueteNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Manejador global de excepciones para los controladores REST.
 * MOD1-UC-006: Manejo centralizado de errores con mensajes descriptivos.
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    /**
     * Maneja excepciones cuando no se encuentra un paquete.
     */
    @ExceptionHandler(PaqueteNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handlePaqueteNotFound(PaqueteNotFoundException ex) {
        log.warn("Paquete no encontrado: {}", ex.getMessage());
        return buildErrorResponse(HttpStatus.NOT_FOUND, "PAQUETE_NO_ENCONTRADO", ex.getMessage());
    }

    /**
     * Maneja excepciones cuando se intenta una transición de estado inválida.
     */
    @ExceptionHandler(EstadoTransicionInvalidaException.class)
    public ResponseEntity<Map<String, Object>> handleEstadoTransicionInvalida(EstadoTransicionInvalidaException ex) {
        log.warn("Transición de estado inválida: {}", ex.getMessage());
        return buildErrorResponse(HttpStatus.BAD_REQUEST, "TRANSICION_INVALIDA", ex.getMessage());
    }

    /**
     * Maneja excepciones cuando falta evidencia requerida.
     */
    @ExceptionHandler(EvidenciaRequeridaException.class)
    public ResponseEntity<Map<String, Object>> handleEvidenciaRequerida(EvidenciaRequeridaException ex) {
        log.warn("Evidencia requerida faltante: {}", ex.getMessage());
        return buildErrorResponse(HttpStatus.BAD_REQUEST, "EVIDENCIA_REQUERIDA", ex.getMessage());
    }

    /**
     * Maneja conflictos de concurrencia optimista.
     */
    @ExceptionHandler(ObjectOptimisticLockingFailureException.class)
    public ResponseEntity<Map<String, Object>> handleOptimisticLocking(ObjectOptimisticLockingFailureException ex) {
        log.warn("Conflicto de concurrencia: {}", ex.getMessage());
        return buildErrorResponse(
            HttpStatus.CONFLICT,
            "CONFLICTO_CONCURRENCIA",
            "El paquete fue actualizado por otro usuario. Por favor, intente de nuevo."
        );
    }

    /**
     * Maneja recursos estáticos no encontrados (favicon.ico, .well-known, etc.)
     */
    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<Void> handleNoResourceFound(NoResourceFoundException ex) {
        log.warn("Recurso estático no encontrado: {} {}", ex.getHttpMethod(), ex.getResourcePath());
        return ResponseEntity.notFound().build();
    }

    /**
     * Maneja excepciones genéricas no capturadas.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGenericException(Exception ex) {
        log.error("Error inesperado: ", ex);
        return buildErrorResponse(
            HttpStatus.INTERNAL_SERVER_ERROR,
            "ERROR_INTERNO",
            "Ocurrió un error inesperado. Por favor, contacte al administrador."
        );
    }

    /**
     * Construye una respuesta de error estándar.
     */
    private ResponseEntity<Map<String, Object>> buildErrorResponse(HttpStatus status, String codigo, String mensaje) {
        Map<String, Object> error = new HashMap<>();
        error.put("timestamp", LocalDateTime.now());
        error.put("status", status.value());
        error.put("codigo", codigo);
        error.put("mensaje", mensaje);
        return ResponseEntity.status(status).body(error);
    }
}
