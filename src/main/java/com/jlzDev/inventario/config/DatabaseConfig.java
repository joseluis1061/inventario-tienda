package com.jlzDev.inventario.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * Configuración de base de datos y JPA
 * Configuraciones específicas para el manejo de datos
 */
@Configuration
@EnableJpaRepositories(basePackages = "com.jlzDev.inventario.repository")
@EnableJpaAuditing
@EnableTransactionManagement
public class DatabaseConfig {

    // El DataSource se configura automáticamente con application.yml
    // No necesitamos crear un bean personalizado para casos básicos

    /**
     * Configuración adicional para optimización de queries
     * Configuraciones específicas para MySQL
     */
    public static class DatabaseConstants {

        // Tamaños de página para consultas paginadas
        public static final int DEFAULT_PAGE_SIZE = 10;
        public static final int MAX_PAGE_SIZE = 100;

        // Timeouts para queries complejas (en segundos)
        public static final int QUERY_TIMEOUT = 30;

        // Configuraciones para batch operations
        public static final int BATCH_SIZE = 50;

        // Configuraciones de cache
        public static final int CACHE_SIZE = 1000;
        public static final long CACHE_TTL_MINUTES = 60;

        // Configuraciones de conexión
        public static final int MAX_POOL_SIZE = 20;
        public static final int MIN_POOL_SIZE = 5;
        public static final long CONNECTION_TIMEOUT_MS = 30000; // 30 segundos
        public static final long IDLE_TIMEOUT_MS = 600000; // 10 minutos
        public static final long MAX_LIFETIME_MS = 1800000; // 30 minutos

        // Configuraciones específicas de MySQL
        public static final String MYSQL_CHARSET = "utf8mb4";
        public static final String MYSQL_COLLATION = "utf8mb4_unicode_ci";

        // Patrones de naming
        public static final String TABLE_PREFIX = "";
        public static final String SEQUENCE_SUFFIX = "_seq";

        // Configuraciones de validación
        public static final boolean VALIDATE_ON_MIGRATE = true;
        public static final boolean CLEAN_ON_VALIDATION_ERROR = false;
    }

    /**
     * Configuración de propiedades JPA adicionales
     * Para optimización y debugging
     */
    public static class JpaProperties {

        // Configuraciones de Hibernate
        public static final String HIBERNATE_DIALECT = "org.hibernate.dialect.MySQL8Dialect";
        public static final String HIBERNATE_DDL_AUTO = "validate"; // validate, update, create, create-drop
        public static final boolean HIBERNATE_SHOW_SQL = false; // Cambiar a true para desarrollo
        public static final boolean HIBERNATE_FORMAT_SQL = true;
        public static final boolean HIBERNATE_USE_SQL_COMMENTS = true;

        // Configuraciones de performance
        public static final int HIBERNATE_JDBC_BATCH_SIZE = 50;
        public static final int HIBERNATE_JDBC_FETCH_SIZE = 50;
        public static final boolean HIBERNATE_ORDER_INSERTS = true;
        public static final boolean HIBERNATE_ORDER_UPDATES = true;
        public static final boolean HIBERNATE_BATCH_VERSIONED_DATA = true;

        // Configuraciones de cache de segundo nivel (si se usa)
        public static final boolean HIBERNATE_CACHE_USE_SECOND_LEVEL_CACHE = false;
        public static final boolean HIBERNATE_CACHE_USE_QUERY_CACHE = false;
        public static final String HIBERNATE_CACHE_REGION_FACTORY = "org.hibernate.cache.jcache.JCacheRegionFactory";

        // Configuraciones de estadísticas
        public static final boolean HIBERNATE_GENERATE_STATISTICS = false; // true para monitoring

        // Configuraciones de conexión
        public static final boolean HIBERNATE_CONNECTION_AUTOCOMMIT = false;
        public static final String HIBERNATE_CONNECTION_ISOLATION = "READ_COMMITTED";
    }

    /**
     * Configuración para auditoría automática
     * Configura createdDate, lastModifiedDate automáticamente
     */
    /* Descomentada si necesitas auditoría automática
    @Bean
    public AuditorAware<String> auditorProvider() {
        return () -> {
            // Aquí deberías obtener el usuario actual del contexto de seguridad
            // Por ahora retornamos un valor por defecto
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.isAuthenticated()
                && !"anonymousUser".equals(authentication.getPrincipal())) {
                return Optional.of(authentication.getName());
            }
            return Optional.of("system");
        };
    }
    */

    /**
     * Configuración de transacciones
     * Define el comportamiento por defecto de las transacciones
     */
    public static class TransactionConfig {

        // Timeout por defecto para transacciones (en segundos)
        public static final int DEFAULT_TRANSACTION_TIMEOUT = 30;

        // Configuración de rollback
        public static final boolean ROLLBACK_ON_COMMIT_FAILURE = true;

        // Configuración de isolation levels
        public enum IsolationLevel {
            READ_UNCOMMITTED(1),
            READ_COMMITTED(2),
            REPEATABLE_READ(4),
            SERIALIZABLE(8);

            private final int value;

            IsolationLevel(int value) {
                this.value = value;
            }

            public int getValue() {
                return value;
            }
        }

        // Configuración de propagation
        public enum PropagationType {
            REQUIRED,
            REQUIRES_NEW,
            SUPPORTS,
            NOT_SUPPORTED,
            MANDATORY,
            NEVER,
            NESTED
        }
    }

    /**
     * Configuración para validación de entidades
     */
    public static class ValidationConfig {

        // Mensajes de validación personalizados
        public static final String REQUIRED_FIELD_MESSAGE = "Este campo es obligatorio";
        public static final String INVALID_EMAIL_MESSAGE = "El formato del email no es válido";
        public static final String INVALID_LENGTH_MESSAGE = "La longitud del campo no es válida";
        public static final String INVALID_NUMBER_MESSAGE = "El valor numérico no es válido";
        public static final String INVALID_DATE_MESSAGE = "La fecha no es válida";

        // Patrones de validación
        public static final String EMAIL_PATTERN = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";
        public static final String USERNAME_PATTERN = "^[a-zA-Z0-9._-]{3,20}$";
        public static final String PHONE_PATTERN = "^\\+?[1-9]\\d{1,14}$";

        // Límites de longitud
        public static final int MIN_PASSWORD_LENGTH = 6;
        public static final int MAX_PASSWORD_LENGTH = 100;
        public static final int MIN_USERNAME_LENGTH = 3;
        public static final int MAX_USERNAME_LENGTH = 20;
        public static final int MAX_NAME_LENGTH = 100;
        public static final int MAX_DESCRIPTION_LENGTH = 255;
        public static final int MAX_TEXT_LENGTH = 1000;
    }

    /**
     * Configuración de logging para SQL
     */
    public static class SqlLoggingConfig {

        // Configuración de niveles de log
        public static final boolean LOG_SQL_STATEMENTS = false; // Cambiar a true en desarrollo
        public static final boolean LOG_SQL_PARAMETERS = false; // Cambiar a true para debugging
        public static final boolean LOG_SQL_RESULTS = false; // Solo para debugging profundo
        public static final boolean LOG_SLOW_QUERIES = true; // Siempre activado

        // Threshold para slow queries (en milisegundos)
        public static final long SLOW_QUERY_THRESHOLD_MS = 1000; // 1 segundo

        // Configuración de formato
        public static final boolean FORMAT_SQL = true;
        public static final boolean SHOW_SQL_COMMENTS = false;
    }
}