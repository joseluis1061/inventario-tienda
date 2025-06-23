//package com.jlzDev.inventario.config;
//
//public class ExceptionConfig {
//}
package com.jlzDev.inventario.config;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Configuración global para manejo de excepciones
 * Centraliza el manejo de errores en toda la aplicación
 */
@RestControllerAdvice
@Slf4j
public class ExceptionConfig {

    /**
     * Manejo de errores de validación de campos (@Valid)
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationErrors(MethodArgumentNotValidException ex) {
        log.warn("Error de validación: {}", ex.getMessage());

        List<ValidationError> validationErrors = new ArrayList<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            if (error instanceof FieldError) {
                FieldError fieldError = (FieldError) error;
                validationErrors.add(ValidationError.builder()
                        .field(fieldError.getField())
                        .value(fieldError.getRejectedValue())
                        .message(fieldError.getDefaultMessage())
                        .build());
            }
        });

        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.BAD_REQUEST.value())
                .error("Validation Error")
                .message("Los datos proporcionados no son válidos")
                .code(AppConfig.ResponseCodes.UNPROCESSABLE_ENTITY)
                .validationErrors(validationErrors)
                .build();

        return ResponseEntity.badRequest().body(errorResponse);
    }

    /**
     * Manejo de errores de validación de constraints (@NotNull, @Size, etc.)
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraintViolation(ConstraintViolationException ex) {
        log.warn("Error de constraint violation: {}", ex.getMessage());

        List<ValidationError> validationErrors = new ArrayList<>();
        Set<ConstraintViolation<?>> violations = ex.getConstraintViolations();

        violations.forEach(violation -> {
            String fieldName = violation.getPropertyPath().toString();
            validationErrors.add(ValidationError.builder()
                    .field(fieldName)
                    .value(violation.getInvalidValue())
                    .message(violation.getMessage())
                    .build());
        });

        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.BAD_REQUEST.value())
                .error("Constraint Violation")
                .message("Violación de restricciones de validación")
                .code(AppConfig.ResponseCodes.UNPROCESSABLE_ENTITY)
                .validationErrors(validationErrors)
                .build();

        return ResponseEntity.badRequest().body(errorResponse);
    }

    /**
     * Manejo de entidades no encontradas
     */
    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleEntityNotFound(EntityNotFoundException ex) {
        log.warn("Entidad no encontrada: {}", ex.getMessage());

        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.NOT_FOUND.value())
                .error("Entity Not Found")
                .message(ex.getMessage())
                .code(AppConfig.ResponseCodes.NOT_FOUND)
                .build();

        return ResponseEntity.notFound().build();
    }

    /**
     * Manejo de errores de integridad de datos (FK, unique constraints, etc.)
     */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> handleDataIntegrityViolation(DataIntegrityViolationException ex) {
        log.error("Error de integridad de datos: {}", ex.getMessage());

        String message = "Error de integridad de datos";
        String code = AppConfig.ResponseCodes.CONFLICT;

        // Analizar el tipo de violación para dar mensajes más específicos
        String exceptionMessage = ex.getMessage().toLowerCase();
        if (exceptionMessage.contains("duplicate") || exceptionMessage.contains("unique")) {
            message = "Ya existe un registro con estos datos";
        } else if (exceptionMessage.contains("foreign key") || exceptionMessage.contains("constraint")) {
            message = "No se puede realizar la operación debido a dependencias existentes";
        }

        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.CONFLICT.value())
                .error("Data Integrity Violation")
                .message(message)
                .code(code)
                .build();

        return ResponseEntity.status(HttpStatus.CONFLICT).body(errorResponse);
    }

    /**
     * Manejo de argumentos inválidos
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgument(IllegalArgumentException ex) {
        log.warn("Argumento inválido: {}", ex.getMessage());

        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.BAD_REQUEST.value())
                .error("Invalid Argument")
                .message(ex.getMessage())
                .code(AppConfig.ResponseCodes.BAD_REQUEST)
                .build();

        return ResponseEntity.badRequest().body(errorResponse);
    }

    /**
     * Manejo de errores de negocio (RuntimeException personalizadas)
     */
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ErrorResponse> handleBusinessLogicError(RuntimeException ex) {
        log.warn("Error de lógica de negocio: {}", ex.getMessage());

        // Determinar el código y estado basado en el mensaje
        HttpStatus status = HttpStatus.CONFLICT;
        String code = AppConfig.ResponseCodes.CONFLICT;

        String message = ex.getMessage().toLowerCase();
        if (message.contains("no encontrado") || message.contains("not found")) {
            status = HttpStatus.NOT_FOUND;
            code = AppConfig.ResponseCodes.NOT_FOUND;
        } else if (message.contains("stock insuficiente") || message.contains("insufficient")) {
            code = AppConfig.ResponseCodes.INSUFFICIENT_STOCK;
        } else if (message.contains("productos asociados")) {
            code = AppConfig.ResponseCodes.CATEGORY_HAS_PRODUCTS;
        } else if (message.contains("usuarios asociados")) {
            code = AppConfig.ResponseCodes.ROLE_HAS_USERS;
        } else if (message.contains("movimientos asociados")) {
            code = AppConfig.ResponseCodes.USER_HAS_MOVEMENTS;
        }

        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(status.value())
                .error("Business Logic Error")
                .message(ex.getMessage())
                .code(code)
                .build();

        return ResponseEntity.status(status).body(errorResponse);
    }

    /**
     * Manejo de parámetros faltantes
     */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ErrorResponse> handleMissingParameter(MissingServletRequestParameterException ex) {
        log.warn("Parámetro faltante: {}", ex.getMessage());

        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.BAD_REQUEST.value())
                .error("Missing Parameter")
                .message("Parámetro requerido faltante: " + ex.getParameterName())
                .code(AppConfig.ResponseCodes.BAD_REQUEST)
                .build();

        return ResponseEntity.badRequest().body(errorResponse);
    }

    /**
     * Manejo de tipo de argumento incorrecto
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
        log.warn("Tipo de argumento incorrecto: {}", ex.getMessage());

        String message = String.format("Tipo de dato incorrecto para el parámetro '%s'. Se esperaba: %s",
                ex.getName(), ex.getRequiredType().getSimpleName());

        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.BAD_REQUEST.value())
                .error("Type Mismatch")
                .message(message)
                .code(AppConfig.ResponseCodes.BAD_REQUEST)
                .build();

        return ResponseEntity.badRequest().body(errorResponse);
    }

    /**
     * Manejo de JSON malformado
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleHttpMessageNotReadable(HttpMessageNotReadableException ex) {
        log.warn("JSON malformado: {}", ex.getMessage());

        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.BAD_REQUEST.value())
                .error("Malformed JSON")
                .message("El formato del JSON no es válido")
                .code(AppConfig.ResponseCodes.BAD_REQUEST)
                .build();

        return ResponseEntity.badRequest().body(errorResponse);
    }

    /**
     * Manejo de errores genéricos no contemplados
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericError(Exception ex, WebRequest request) {
        log.error("Error interno del servidor: {}", ex.getMessage(), ex);

        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .error("Internal Server Error")
                .message("Ha ocurrido un error interno del servidor")
                .code(AppConfig.ResponseCodes.INTERNAL_SERVER_ERROR)
                .path(request.getDescription(false))
                .build();

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }

    /**
     * Clase para representar la respuesta de error estándar
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class ErrorResponse {

        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime timestamp;

        private int status;
        private String error;
        private String message;
        private String code;
        private String path;

        @JsonInclude(JsonInclude.Include.NON_EMPTY)
        private List<ValidationError> validationErrors;

        @JsonInclude(JsonInclude.Include.NON_NULL)
        private Object details;
    }

    /**
     * Clase para representar errores de validación específicos
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ValidationError {
        private String field;
        private Object value;
        private String message;
    }

    /**
     * Utilidades para manejo de excepciones
     */
    public static class ExceptionUtils {

        /**
         * Crear respuesta de error simple
         */
        public static ErrorResponse createSimpleError(HttpStatus status, String message) {
            return ErrorResponse.builder()
                    .timestamp(LocalDateTime.now())
                    .status(status.value())
                    .error(status.getReasonPhrase())
                    .message(message)
                    .build();
        }

        /**
         * Crear respuesta de error con código personalizado
         */
        public static ErrorResponse createCustomError(HttpStatus status, String message, String code) {
            return ErrorResponse.builder()
                    .timestamp(LocalDateTime.now())
                    .status(status.value())
                    .error(status.getReasonPhrase())
                    .message(message)
                    .code(code)
                    .build();
        }

        /**
         * Extraer mensaje root de excepción anidada
         */
        public static String getRootCauseMessage(Throwable throwable) {
            Throwable rootCause = throwable;
            while (rootCause.getCause() != null && rootCause.getCause() != rootCause) {
                rootCause = rootCause.getCause();
            }
            return rootCause.getMessage();
        }

        /**
         * Verificar si la excepción es de tipo específico
         */
        public static boolean isExceptionOfType(Throwable throwable, Class<? extends Throwable> exceptionType) {
            Throwable current = throwable;
            while (current != null) {
                if (exceptionType.isInstance(current)) {
                    return true;
                }
                current = current.getCause();
            }
            return false;
        }
    }

    /**
     * Excepciones personalizadas del dominio
     */
    public static class DomainExceptions {

        public static class StockInsuficienteException extends RuntimeException {
            public StockInsuficienteException(String productName, int requested, int available) {
                super(String.format("Stock insuficiente para el producto '%s'. Solicitado: %d, Disponible: %d",
                        productName, requested, available));
            }
        }

        public static class ProductoNoEncontradoException extends RuntimeException {
            public ProductoNoEncontradoException(Long id) {
                super("Producto no encontrado con ID: " + id);
            }

            public ProductoNoEncontradoException(String nombre) {
                super("Producto no encontrado con nombre: " + nombre);
            }
        }

        public static class CategoriaConProductosException extends RuntimeException {
            public CategoriaConProductosException(String categoryName, long productCount) {
                super(String.format("No se puede eliminar la categoría '%s' porque tiene %d producto(s) asociado(s)",
                        categoryName, productCount));
            }
        }

        public static class UsuarioConMovimientosException extends RuntimeException {
            public UsuarioConMovimientosException(String username) {
                super(String.format("No se puede eliminar el usuario '%s' porque tiene movimientos asociados", username));
            }
        }

        public static class RolConUsuariosException extends RuntimeException {
            public RolConUsuariosException(String rolName, long userCount) {
                super(String.format("No se puede eliminar el rol '%s' porque tiene %d usuario(s) asociado(s)",
                        rolName, userCount));
            }
        }

        public static class OperacionNoPermitidaException extends RuntimeException {
            public OperacionNoPermitidaException(String message) {
                super(message);
            }
        }
    }
}