package com.jlzDev.inventario.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.web.client.RestTemplate;

import java.text.SimpleDateFormat;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.Executor;
import java.util.TimeZone;

/**
 * Configuración general de la aplicación
 * Beans y configuraciones transversales
 */
@Configuration
@EnableAsync
@EnableScheduling
public class AppConfig {

    /**
     * Configuración del ObjectMapper para JSON
     * Manejo de fechas, formato, etc.
     */
    @Bean
    @Primary
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();

        // Registrar módulo para Java Time API
        mapper.registerModule(new JavaTimeModule());

        // Configurar formato de fechas
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        dateFormat.setTimeZone(TimeZone.getDefault());
        mapper.setDateFormat(dateFormat);

        // Deshabilitar escritura de fechas como timestamps
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        // No fallar en propiedades desconocidas
        mapper.configure(com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        // Configurar indentación (false para producción)
        mapper.configure(SerializationFeature.INDENT_OUTPUT, false);

        // Configurar timezone
        mapper.setTimeZone(TimeZone.getDefault());

        return mapper;
    }

    /**
     * Configuración de RestTemplate para llamadas HTTP externas
     */
    @Bean
    public RestTemplate restTemplate() {
        RestTemplate restTemplate = new RestTemplate();

        // Configurar timeouts
        restTemplate.getRequestFactory();

        return restTemplate;
    }

    /**
     * Configuración del executor para tareas asíncronas
     */
    @Bean(name = "taskExecutor")
    public Executor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();

        // Configuración del pool de threads
        executor.setCorePoolSize(AppConstants.ASYNC_CORE_POOL_SIZE);
        executor.setMaxPoolSize(AppConstants.ASYNC_MAX_POOL_SIZE);
        executor.setQueueCapacity(AppConstants.ASYNC_QUEUE_CAPACITY);

        // Prefijo para nombres de threads
        executor.setThreadNamePrefix("InventarioAsync-");

        // Política de rechazo
        executor.setRejectedExecutionHandler(new java.util.concurrent.ThreadPoolExecutor.CallerRunsPolicy());

        // Esperar a que terminen las tareas al shutdown
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(AppConstants.ASYNC_AWAIT_TERMINATION_SECONDS);

        executor.initialize();
        return executor;
    }

    /**
     * Configuración específica del executor para reportes
     */
    @Bean(name = "reportExecutor")
    public Executor reportExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();

        // Pool más pequeño para reportes (tareas pesadas)
        executor.setCorePoolSize(2);
        executor.setMaxPoolSize(5);
        executor.setQueueCapacity(10);

        executor.setThreadNamePrefix("InventarioReport-");
        executor.setRejectedExecutionHandler(new java.util.concurrent.ThreadPoolExecutor.CallerRunsPolicy());

        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(60);

        executor.initialize();
        return executor;
    }

    /**
     * Constantes de configuración de la aplicación
     */
    public static class AppConstants {

        // Información de la aplicación
        public static final String APP_NAME = "Sistema de Inventario";
        public static final String APP_VERSION = "1.0.0";
        public static final String APP_DESCRIPTION = "Sistema de gestión de inventario para tienda";

        // Configuraciones de paginación
        public static final int DEFAULT_PAGE_SIZE = 10;
        public static final int MAX_PAGE_SIZE = 100;
        public static final String DEFAULT_SORT_DIRECTION = "ASC";
        public static final String DEFAULT_SORT_FIELD = "id";

        // Configuraciones de archivos
        public static final long MAX_FILE_SIZE = 10 * 1024 * 1024; // 10MB
        public static final String[] ALLOWED_FILE_TYPES = {"jpg", "jpeg", "png", "pdf", "xlsx", "csv"};
        public static final String UPLOAD_DIR = "./uploads/";
        public static final String TEMP_DIR = "./temp/";

        // Configuraciones de cache
        public static final int CACHE_DEFAULT_TTL_MINUTES = 60;
        public static final int CACHE_MAX_SIZE = 1000;

        // Configuraciones de async/threading
        public static final int ASYNC_CORE_POOL_SIZE = 5;
        public static final int ASYNC_MAX_POOL_SIZE = 20;
        public static final int ASYNC_QUEUE_CAPACITY = 100;
        public static final int ASYNC_AWAIT_TERMINATION_SECONDS = 30;

        // Configuraciones de validación
        public static final int MIN_PASSWORD_LENGTH = 6;
        public static final int MAX_PASSWORD_LENGTH = 100;
        public static final int MIN_USERNAME_LENGTH = 3;
        public static final int MAX_USERNAME_LENGTH = 20;

        // Configuraciones de negocio
        public static final int STOCK_CRITICO_THRESHOLD = 0;
        public static final double STOCK_BAJO_MULTIPLIER = 1.5;
        public static final int MAX_MOVIMIENTO_CANTIDAD = 100000;

        // Configuraciones de reportes
        public static final int REPORTE_MAX_REGISTROS = 10000;
        public static final int REPORTE_TIMEOUT_MINUTES = 10;

        // Configuraciones de API
        public static final String API_VERSION = "v1";
        public static final String API_BASE_PATH = "/api";
        public static final int API_RATE_LIMIT_PER_MINUTE = 100;

        // Configuraciones de formatos
        public static final String DATE_FORMAT = "yyyy-MM-dd";
        public static final String DATETIME_FORMAT = "yyyy-MM-dd HH:mm:ss";
        public static final String TIME_FORMAT = "HH:mm:ss";

        // Configuraciones de timezone
        public static final String DEFAULT_TIMEZONE = "America/Bogota";

        // Configuraciones de moneda
        public static final String DEFAULT_CURRENCY = "COP";
        public static final String CURRENCY_SYMBOL = "$";

        // Configuraciones de email (si se implementa)
        public static final String DEFAULT_FROM_EMAIL = "noreply@inventario.com";
        public static final String DEFAULT_FROM_NAME = "Sistema de Inventario";

        // Configuraciones de logging
        public static final boolean LOG_REQUEST_DETAILS = false; // true en desarrollo
        public static final boolean LOG_RESPONSE_DETAILS = false; // true en desarrollo
        public static final int LOG_MAX_REQUEST_SIZE = 1024; // bytes

        // Configuraciones de seguridad
        public static final int JWT_EXPIRATION_HOURS = 24;
        public static final int REFRESH_TOKEN_EXPIRATION_DAYS = 30;
        public static final int MAX_LOGIN_ATTEMPTS = 5;
        public static final int LOCKOUT_DURATION_MINUTES = 15;

        // Configuraciones de backup (si se implementa)
        public static final String BACKUP_DIR = "./backups/";
        public static final int BACKUP_RETENTION_DAYS = 30;
        public static final boolean AUTO_BACKUP_ENABLED = false;

        // Configuraciones de notificaciones
        public static final boolean NOTIFICACIONES_ENABLED = true;
        public static final boolean EMAIL_NOTIFICATIONS_ENABLED = false;
        public static final boolean SMS_NOTIFICATIONS_ENABLED = false;
    }

    /**
     * Configuración de formatos de fecha y hora
     */
    public static class DateTimeConfig {

        public static final DateTimeFormatter DATE_FORMATTER =
                DateTimeFormatter.ofPattern(AppConstants.DATE_FORMAT);

        public static final DateTimeFormatter DATETIME_FORMATTER =
                DateTimeFormatter.ofPattern(AppConstants.DATETIME_FORMAT);

        public static final DateTimeFormatter TIME_FORMATTER =
                DateTimeFormatter.ofPattern(AppConstants.TIME_FORMAT);

        // Formatos alternativos para parsing
        public static final DateTimeFormatter[] ALTERNATIVE_DATE_FORMATTERS = {
                DateTimeFormatter.ofPattern("dd/MM/yyyy"),
                DateTimeFormatter.ofPattern("dd-MM-yyyy"),
                DateTimeFormatter.ofPattern("yyyy/MM/dd"),
                DateTimeFormatter.ofPattern("MM/dd/yyyy")
        };

        public static final DateTimeFormatter[] ALTERNATIVE_DATETIME_FORMATTERS = {
                DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"),
                DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss"),
                DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss"),
                DateTimeFormatter.ofPattern("MM/dd/yyyy HH:mm:ss"),
                DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss"),
                DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS")
        };
    }

    /**
     * Configuración de mensajes del sistema
     */
    public static class MessageConfig {

        // Mensajes de éxito
        public static final String SUCCESS_CREATE = "Registro creado exitosamente";
        public static final String SUCCESS_UPDATE = "Registro actualizado exitosamente";
        public static final String SUCCESS_DELETE = "Registro eliminado exitosamente";

        // Mensajes de error
        public static final String ERROR_NOT_FOUND = "Registro no encontrado";
        public static final String ERROR_DUPLICATE = "Ya existe un registro con estos datos";
        public static final String ERROR_INVALID_DATA = "Los datos proporcionados no son válidos";
        public static final String ERROR_INSUFFICIENT_STOCK = "Stock insuficiente para la operación";
        public static final String ERROR_PERMISSION_DENIED = "No tiene permisos para realizar esta operación";
        public static final String ERROR_INVALID_CREDENTIALS = "Credenciales inválidas";

        // Mensajes de validación
        public static final String VALIDATION_REQUIRED = "Este campo es obligatorio";
        public static final String VALIDATION_EMAIL = "El formato del email no es válido";
        public static final String VALIDATION_LENGTH = "La longitud del campo no es válida";
        public static final String VALIDATION_NUMERIC = "El valor debe ser numérico";
        public static final String VALIDATION_POSITIVE = "El valor debe ser positivo";

        // Mensajes de información
        public static final String INFO_NO_RECORDS = "No se encontraron registros";
        public static final String INFO_PROCESSING = "Procesando solicitud...";
        public static final String INFO_COMPLETED = "Operación completada";
    }

    /**
     * Configuración de códigos de respuesta personalizados
     */
    public static class ResponseCodes {

        // Códigos de éxito (2xx)
        public static final String SUCCESS = "200";
        public static final String CREATED = "201";
        public static final String ACCEPTED = "202";
        public static final String NO_CONTENT = "204";

        // Códigos de error del cliente (4xx)
        public static final String BAD_REQUEST = "400";
        public static final String UNAUTHORIZED = "401";
        public static final String FORBIDDEN = "403";
        public static final String NOT_FOUND = "404";
        public static final String CONFLICT = "409";
        public static final String UNPROCESSABLE_ENTITY = "422";

        // Códigos de error del servidor (5xx)
        public static final String INTERNAL_SERVER_ERROR = "500";
        public static final String SERVICE_UNAVAILABLE = "503";

        // Códigos personalizados de negocio
        public static final String INSUFFICIENT_STOCK = "1001";
        public static final String INVALID_MOVEMENT = "1002";
        public static final String CATEGORY_HAS_PRODUCTS = "1003";
        public static final String USER_HAS_MOVEMENTS = "1004";
        public static final String ROLE_HAS_USERS = "1005";
    }

    /**
     * Configuración de patrones de validación
     */
    public static class ValidationPatterns {

        // Patrones de texto
        public static final String EMAIL_PATTERN =
                "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";
        public static final String USERNAME_PATTERN =
                "^[a-zA-Z0-9._-]{3,20}$";
        public static final String PHONE_PATTERN =
                "^\\+?[1-9]\\d{1,14}$";
        public static final String NAME_PATTERN =
                "^[a-zA-ZáéíóúÁÉÍÓÚñÑ\\s]{2,100}$";

        // Patrones numéricos
        public static final String PRICE_PATTERN =
                "^\\d+(\\.\\d{1,2})?$";
        public static final String INTEGER_PATTERN =
                "^\\d+$";
        public static final String DECIMAL_PATTERN =
                "^\\d+(\\.\\d+)?$";

        // Patrones de fecha
        public static final String DATE_PATTERN =
                "^\\d{4}-\\d{2}-\\d{2}$";
        public static final String DATETIME_PATTERN =
                "^\\d{4}-\\d{2}-\\d{2}\\s\\d{2}:\\d{2}:\\d{2}$";
    }

    /**
     * Configuración de límites del sistema
     */
    public static class SystemLimits {

        // Límites de entidades
        public static final int MAX_CATEGORIES = 1000;
        public static final int MAX_PRODUCTS_PER_CATEGORY = 10000;
        public static final int MAX_USERS = 1000;
        public static final int MAX_ROLES = 50;

        // Límites de operaciones
        public static final int MAX_BULK_OPERATIONS = 100;
        public static final int MAX_SEARCH_RESULTS = 1000;
        public static final int MAX_EXPORT_RECORDS = 50000;

        // Límites de campos
        public static final int MAX_NAME_LENGTH = 150;
        public static final int MAX_DESCRIPTION_LENGTH = 1000;
        public static final int MAX_COMMENT_LENGTH = 500;
        public static final int MAX_REASON_LENGTH = 255;

        // Límites de valores
        public static final double MAX_PRICE = 99999999.99;
        public static final int MAX_STOCK = 1000000;
        public static final int MAX_MOVEMENT_QUANTITY = 100000;

        // Límites de tiempo
        public static final int MAX_SESSION_DURATION_HOURS = 24;
        public static final int MAX_REPORT_GENERATION_MINUTES = 30;
        public static final int MAX_IMPORT_PROCESSING_MINUTES = 60;
    }

    /**
     * Configuración de características del sistema
     */
    public static class FeatureFlags {

        // Características principales
        public static final boolean ENABLE_USER_REGISTRATION = true;
        public static final boolean ENABLE_EMAIL_VERIFICATION = false;
        public static final boolean ENABLE_TWO_FACTOR_AUTH = false;

        // Características de inventario
        public static final boolean ENABLE_NEGATIVE_STOCK = false;
        public static final boolean ENABLE_STOCK_RESERVATIONS = false;
        public static final boolean ENABLE_AUTOMATIC_REORDER = false;

        // Características de reportes
        public static final boolean ENABLE_ADVANCED_REPORTS = true;
        public static final boolean ENABLE_REAL_TIME_DASHBOARD = true;
        public static final boolean ENABLE_EXPORT_TO_EXCEL = true;
        public static final boolean ENABLE_EXPORT_TO_PDF = true;

        // Características de integración
        public static final boolean ENABLE_API_RATE_LIMITING = false;
        public static final boolean ENABLE_AUDIT_LOGGING = true;
        public static final boolean ENABLE_PERFORMANCE_MONITORING = false;

        // Características experimentales
        public static final boolean ENABLE_DARK_MODE = false;
        public static final boolean ENABLE_NOTIFICATIONS = true;
        public static final boolean ENABLE_BULK_OPERATIONS = true;
    }

    /**
     * Configuración de cache
     */
    public static class CacheConfig {

        // Nombres de cache
        public static final String CACHE_CATEGORIES = "categories";
        public static final String CACHE_PRODUCTS = "products";
        public static final String CACHE_USERS = "users";
        public static final String CACHE_ROLES = "roles";
        public static final String CACHE_STATISTICS = "statistics";

        // TTL por tipo de cache (en minutos)
        public static final int CACHE_TTL_CATEGORIES = 60;
        public static final int CACHE_TTL_PRODUCTS = 30;
        public static final int CACHE_TTL_USERS = 15;
        public static final int CACHE_TTL_ROLES = 120;
        public static final int CACHE_TTL_STATISTICS = 5;

        // Tamaños máximos de cache
        public static final int CACHE_MAX_SIZE_CATEGORIES = 1000;
        public static final int CACHE_MAX_SIZE_PRODUCTS = 5000;
        public static final int CACHE_MAX_SIZE_USERS = 1000;
        public static final int CACHE_MAX_SIZE_ROLES = 100;
        public static final int CACHE_MAX_SIZE_STATISTICS = 500;
    }

    /**
     * Configuración de logging específico de la aplicación
     */
    public static class LoggingConfig {

        // Niveles de log por componente
        public static final String CONTROLLER_LOG_LEVEL = "INFO";
        public static final String SERVICE_LOG_LEVEL = "DEBUG";
        public static final String REPOSITORY_LOG_LEVEL = "WARN";
        public static final String SECURITY_LOG_LEVEL = "INFO";

        // Configuración de formato
        public static final String LOG_PATTERN =
                "%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level %logger{36} - %msg%n";

        // Archivos de log
        public static final String LOG_FILE_PATH = "./logs/inventario.log";
        public static final String ERROR_LOG_FILE_PATH = "./logs/inventario-error.log";
        public static final String AUDIT_LOG_FILE_PATH = "./logs/inventario-audit.log";

        // Configuración de rotación
        public static final String LOG_ROTATION_POLICY = "daily";
        public static final int LOG_MAX_FILE_SIZE_MB = 100;
        public static final int LOG_MAX_HISTORY_DAYS = 30;
    }
}