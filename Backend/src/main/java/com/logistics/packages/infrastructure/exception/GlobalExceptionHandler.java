package com.logistics.packages.infrastructure.exception;

import com.logistics.packages.domain.exception.EvidenciaRequeridaException;
import com.logistics.packages.domain.exception.PaqueteEnTransitoException;
import com.logistics.packages.domain.exception.PaqueteNoEncontradoException;
import com.logistics.packages.domain.exception.TransicionInvalidaException;
import com.logistics.packages.infrastructure.dto.response.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

/**
 * Manejador global de excepciones para la aplicación.
 * Convierte excepciones de dominio en respuestas HTTP apropiadas.
 * MOD1-IP-006: Actualizar Estado de Paquete por Novedad
 */
@RestControllerAdvice
public class GlobalExceptionHandler {
    
    /**
     * Maneja excepción cuando no se encuentra un paquete.
     * HTTP 404 - Not Found
     */
    @ExceptionHandler(PaqueteNoEncontradoException.class)
    public ResponseEntity<ErrorResponse> handlePaqueteNoEncontrado(
            PaqueteNoEncontradoException ex,
            HttpServletRequest request) {
        
        ErrorResponse error = new ErrorResponse(
            HttpStatus.NOT_FOUND.value(),
            "Paquete No Encontrado",
            ex.getMessage(),
            request.getRequestURI()
        );
        
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }
    
    /**
     * Maneja excepción cuando falta evidencia obligatoria.
     * HTTP 400 - Bad Request
     */
    @ExceptionHandler(EvidenciaRequeridaException.class)
    public ResponseEntity<ErrorResponse> handleEvidenciaRequerida(
            EvidenciaRequeridaException ex,
            HttpServletRequest request) {
        
        ErrorResponse error = new ErrorResponse(
            HttpStatus.BAD_REQUEST.value(),
            "Evidencia Requerida",
            ex.getMessage(),
            request.getRequestURI()
        );
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }
    
    /**
     * Maneja excepción cuando se intenta modificar un paquete en tránsito.
     * HTTP 409 - Conflict
     */
    @ExceptionHandler(PaqueteEnTransitoException.class)
    public ResponseEntity<ErrorResponse> handlePaqueteEnTransito(
            PaqueteEnTransitoException ex,
            HttpServletRequest request) {
        
        ErrorResponse error = new ErrorResponse(
            HttpStatus.CONFLICT.value(),
            "Paquete En Tránsito",
            ex.getMessage(),
            request.getRequestURI()
        );
        
        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }
    
    /**
     * Maneja excepción cuando se intenta una transición de estado inválida.
     * HTTP 422 - Unprocessable Entity
     */
    @ExceptionHandler(TransicionInvalidaException.class)
    public ResponseEntity<ErrorResponse> handleTransicionInvalida(
            TransicionInvalidaException ex,
            HttpServletRequest request) {
        
        ErrorResponse error = new ErrorResponse(
            HttpStatus.UNPROCESSABLE_ENTITY.value(),
            "Transición Inválida",
            ex.getMessage(),
            request.getRequestURI()
        );
        
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(error);
    }
    
    /**
     * Maneja errores de validación de Bean Validation.
     * HTTP 400 - Bad Request
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationErrors(
            MethodArgumentNotValidException ex,
            HttpServletRequest request) {
        
        String mensajesError = ex.getBindingResult()
            .getFieldErrors()
            .stream()
            .map(FieldError::getDefaultMessage)
            .collect(Collectors.joining(", "));
        
        ErrorResponse error = new ErrorResponse(
            HttpStatus.BAD_REQUEST.value(),
            "Error de Validación",
            mensajesError,
            request.getRequestURI()
        );
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }
    
    /**
     * Maneja excepciones de argumento ilegal.
     * HTTP 400 - Bad Request
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgument(
            IllegalArgumentException ex,
            HttpServletRequest request) {
        
        ErrorResponse error = new ErrorResponse(
            HttpStatus.BAD_REQUEST.value(),
            "Argumento Inválido",
            ex.getMessage(),
            request.getRequestURI()
        );
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }
    
    /**
     * Maneja cualquier otra excepción no prevista.
     * HTTP 500 - Internal Server Error
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(
            Exception ex,
            HttpServletRequest request) {
        
        ErrorResponse error = new ErrorResponse(
            HttpStatus.INTERNAL_SERVER_ERROR.value(),
            "Error Interno del Servidor",
            "Ha ocurrido un error inesperado: " + ex.getMessage(),
            request.getRequestURI()
        );
        
        // Log de la excepción para debugging
        ex.printStackTrace();
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }
}
