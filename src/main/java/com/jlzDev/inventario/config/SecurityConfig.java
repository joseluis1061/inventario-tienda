package com.jlzDev.inventario.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfigurationSource;

/**
 * Configuración de seguridad principal
 * Maneja autenticación, autorización y configuraciones de seguridad
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private CorsConfigurationSource corsConfigurationSource;

    /**
     * Bean para encriptar contraseñas con BCrypt
     * Fuerza de encriptación: 12 (más seguro que el default 10)
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }

    /**
     * Configuración de seguridad HTTP
     * Por ahora configuración básica para desarrollo
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // Deshabilitar CSRF para APIs REST
                .csrf(csrf -> csrf.disable())

                // Configurar CORS usando el bean de CorsConfig
                .cors(cors -> cors.configurationSource(corsConfigurationSource))

                // Configuración de autorización
                .authorizeHttpRequests(authz -> authz
                                // Permitir acceso sin autenticación para desarrollo
                                .requestMatchers("/api/auth/**").permitAll()
                                .requestMatchers("/api/public/**").permitAll()
                                .requestMatchers("/h2-console/**").permitAll() // Para H2 Database Console si se usa
                                .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll() // Para Swagger
                                .requestMatchers("/actuator/**").permitAll() // Para Spring Boot Actuator

                                // IMPORTANTE: Cambiar esta línea en producción
                                // Por ahora permitimos todo para desarrollo y testing
                                .anyRequest().permitAll() // Cambiar a .authenticated() cuando se implemente JWT

                        // Para producción usar algo como:
                        // .requestMatchers("/api/admin/**").hasRole("ADMIN")
                        // .requestMatchers("/api/manager/**").hasAnyRole("ADMIN", "MANAGER")
                        // .anyRequest().authenticated()
                )

                // Configurar sesiones como stateless (para JWT)
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )

                // Deshabilitar headers de frame para H2 Console (solo para desarrollo)
                .headers(headers -> headers.frameOptions(frame -> frame.disable()));

        return http.build();
    }

    /**
     * Configuración de endpoints para diferentes niveles de acceso
     * Estos serán útiles cuando implementemos JWT y roles
     */
    public static class SecurityEndpoints {

        // Endpoints públicos (sin autenticación)
        public static final String[] PUBLIC_ENDPOINTS = {
                "/api/auth/**",
                "/api/public/**",
                "/h2-console/**",
                "/swagger-ui/**",
                "/v3/api-docs/**",
                "/actuator/health",
                "/actuator/info"
        };

        // Endpoints que requieren autenticación básica
        public static final String[] AUTHENTICATED_ENDPOINTS = {
                "/api/productos/**",
                "/api/categorias/**",
                "/api/movimientos/**"
        };

        // Endpoints que requieren rol de ADMIN
        public static final String[] ADMIN_ENDPOINTS = {
                "/api/admin/**",
                "/api/usuarios/**",
                "/api/roles/**",
                "/actuator/**"
        };

        // Endpoints que requieren rol de MANAGER o superior
        public static final String[] MANAGER_ENDPOINTS = {
                "/api/manager/**",
                "/api/reportes/**",
                "/api/estadisticas/**"
        };

        // Endpoints de solo lectura para EMPLEADO
        public static final String[] EMPLOYEE_READ_ENDPOINTS = {
                "GET:/api/productos/**",
                "GET:/api/categorias/**",
                "GET:/api/movimientos/mis-movimientos/**"
        };
    }

    /**
     * Configuración de roles y permisos
     * Para futuro uso con JWT
     */
    public static class SecurityRoles {

        public static final String ADMIN = "ADMIN";
        public static final String MANAGER = "GERENTE";
        public static final String EMPLOYEE = "EMPLEADO";

        // Prefijos para Spring Security
        public static final String ROLE_ADMIN = "ROLE_" + ADMIN;
        public static final String ROLE_MANAGER = "ROLE_" + MANAGER;
        public static final String ROLE_EMPLOYEE = "ROLE_" + EMPLOYEE;

        /**
         * Verificar si un rol tiene permisos de administrador
         */
        public static boolean isAdmin(String role) {
            return ADMIN.equals(role) || ROLE_ADMIN.equals(role);
        }

        /**
         * Verificar si un rol tiene permisos de gerente o superior
         */
        public static boolean isManagerOrAbove(String role) {
            return isAdmin(role) || MANAGER.equals(role) || ROLE_MANAGER.equals(role);
        }

        /**
         * Verificar si un rol es válido
         */
        public static boolean isValidRole(String role) {
            return ADMIN.equals(role) || MANAGER.equals(role) || EMPLOYEE.equals(role) ||
                    ROLE_ADMIN.equals(role) || ROLE_MANAGER.equals(role) || ROLE_EMPLOYEE.equals(role);
        }
    }

    /**
     * Configuración de JWT (para implementación futura)
     */
    public static class JwtConfig {

        // Configuraciones JWT
        public static final String JWT_SECRET_KEY = "mi-clave-secreta-super-segura-para-jwt-inventario-2024";
        public static final String JWT_TOKEN_PREFIX = "Bearer ";
        public static final String JWT_HEADER_NAME = "Authorization";

        // Tiempos de expiración
        public static final long JWT_EXPIRATION_TIME = 86400000; // 24 horas en milisegundos
        public static final long JWT_REFRESH_EXPIRATION_TIME = 2592000000L; // 30 días en milisegundos

        // Claims personalizados
        public static final String JWT_CLAIM_ROLE = "role";
        public static final String JWT_CLAIM_USER_ID = "userId";
        public static final String JWT_CLAIM_USERNAME = "username";
        public static final String JWT_CLAIM_PERMISSIONS = "permissions";

        /**
         * Obtener nombre del header sin el prefijo Bearer
         */
        public static String extractTokenFromHeader(String header) {
            if (header != null && header.startsWith(JWT_TOKEN_PREFIX)) {
                return header.substring(JWT_TOKEN_PREFIX.length());
            }
            return null;
        }
    }

    /**
     * Configuración de password policy
     */
    public static class PasswordPolicy {

        public static final int MIN_LENGTH = 6;
        public static final int MAX_LENGTH = 100;
        public static final boolean REQUIRE_UPPERCASE = false;
        public static final boolean REQUIRE_LOWERCASE = false;
        public static final boolean REQUIRE_DIGITS = false;
        public static final boolean REQUIRE_SPECIAL_CHARS = false;

        // Caracteres especiales permitidos
        public static final String ALLOWED_SPECIAL_CHARS = "!@#$%^&*()_+-=[]{}|;:,.<>?";

        // Configuración de bloqueo de cuenta
        public static final int MAX_FAILED_ATTEMPTS = 5;
        public static final long LOCKOUT_DURATION_MINUTES = 15;

        /**
         * Validar si una contraseña cumple con la política
         */
        public static boolean isValidPassword(String password) {
            if (password == null || password.length() < MIN_LENGTH || password.length() > MAX_LENGTH) {
                return false;
            }

            if (REQUIRE_UPPERCASE && !password.matches(".*[A-Z].*")) {
                return false;
            }

            if (REQUIRE_LOWERCASE && !password.matches(".*[a-z].*")) {
                return false;
            }

            if (REQUIRE_DIGITS && !password.matches(".*\\d.*")) {
                return false;
            }

            if (REQUIRE_SPECIAL_CHARS && !password.matches(".*[" + ALLOWED_SPECIAL_CHARS + "].*")) {
                return false;
            }

            return true;
        }

        /**
         * Generar mensaje de política de contraseñas
         */
        public static String getPolicyMessage() {
            StringBuilder message = new StringBuilder("La contraseña debe tener entre ")
                    .append(MIN_LENGTH).append(" y ").append(MAX_LENGTH).append(" caracteres");

            if (REQUIRE_UPPERCASE) message.append(", al menos una mayúscula");
            if (REQUIRE_LOWERCASE) message.append(", al menos una minúscula");
            if (REQUIRE_DIGITS) message.append(", al menos un número");
            if (REQUIRE_SPECIAL_CHARS) message.append(", al menos un carácter especial");

            return message.toString();
        }
    }

    /**
     * Configuración de auditoría de seguridad
     */
    public static class SecurityAudit {

        public static final boolean ENABLE_LOGIN_AUDIT = true;
        public static final boolean ENABLE_ACCESS_AUDIT = false; // true en producción
        public static final boolean ENABLE_FAILED_LOGIN_AUDIT = true;

        // Eventos de auditoría
        public enum AuditEvent {
            LOGIN_SUCCESS,
            LOGIN_FAILED,
            LOGOUT,
            ACCESS_DENIED,
            PASSWORD_CHANGED,
            ACCOUNT_LOCKED,
            ACCOUNT_UNLOCKED
        }

        /**
         * Verificar si un evento debe ser auditado
         */
        public static boolean shouldAudit(AuditEvent event) {
            switch (event) {
                case LOGIN_SUCCESS:
                case LOGOUT:
                    return ENABLE_LOGIN_AUDIT;
                case LOGIN_FAILED:
                case ACCOUNT_LOCKED:
                    return ENABLE_FAILED_LOGIN_AUDIT;
                case ACCESS_DENIED:
                    return ENABLE_ACCESS_AUDIT;
                default:
                    return true;
            }
        }
    }
}