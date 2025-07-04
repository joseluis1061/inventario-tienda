package com.jlzDev.inventario.config;

import com.jlzDev.inventario.security.JwtAuthenticationFilter;
import com.jlzDev.inventario.security.UserDetailsServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfigurationSource;

/**
 * Configuración de seguridad principal con JWT
 * Maneja autenticación, autorización y configuraciones de seguridad
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true, securedEnabled = true, jsr250Enabled = true)
@RequiredArgsConstructor // Lombok para constructor con final fields
public class SecurityConfig {

    private final CorsConfigurationSource corsConfigurationSource;
    private final UserDetailsServiceImpl userDetailsService;

    /**
     * Bean para encriptar contraseñas con BCrypt
     * Fuerza de encriptación: 12 (más seguro que el default 10)
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }

    /**
     * Bean del AuthenticationManager
     * Necesario para el proceso de autenticación
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    /**
     * Bean del Authentication Provider que usa nuestro UserDetailsService
     */
    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        provider.setHideUserNotFoundExceptions(false); // Para debugging, cambiar a true en producción
        return provider;
    }

    /**
     * Bean del JwtAuthenticationFilter
     * Se crea aquí para evitar dependencias circulares
     */
    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter() {
        return new JwtAuthenticationFilter();
    }

    /**
     * Configuración principal de seguridad HTTP con JWT
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // Deshabilitar CSRF para APIs REST (JWT es stateless)
                .csrf(csrf -> csrf.disable())

                // Configurar CORS usando el bean de CorsConfig
                .cors(cors -> cors.configurationSource(corsConfigurationSource))

                // Configuración de autorización por endpoints
                .authorizeHttpRequests(authz -> authz
                        // ===== ENDPOINTS PÚBLICOS (sin autenticación) =====
                        .requestMatchers("/api/auth/**").permitAll()
                        .requestMatchers("/api/public/**").permitAll()
                        .requestMatchers("/h2-console/**").permitAll()
                        .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()
                        .requestMatchers("/actuator/health", "/actuator/info").permitAll()
                        .requestMatchers("/favicon.ico", "/error").permitAll()

                        // ===== ENDPOINTS DE ADMINISTRADOR =====
                        .requestMatchers("/api/usuarios/**").hasRole("ADMIN")
                        .requestMatchers("/api/roles/**").hasRole("ADMIN")
                        .requestMatchers("/actuator/**").hasRole("ADMIN")

                        // ===== ENDPOINTS DE GERENTE O SUPERIOR =====
                        .requestMatchers("/api/reportes/**").hasAnyRole("ADMIN", "GERENTE")
                        .requestMatchers("/api/estadisticas/**").hasAnyRole("ADMIN", "GERENTE")

                        // ===== ENDPOINTS DE PRODUCTOS (por método HTTP) =====
                        // Solo lectura para empleados
                        .requestMatchers("GET", "/api/productos/**").hasAnyRole("ADMIN", "GERENTE", "EMPLEADO")
                        // Escritura solo para gerente o superior
                        .requestMatchers("POST", "/api/productos/**").hasAnyRole("ADMIN", "GERENTE")
                        .requestMatchers("PUT", "/api/productos/**").hasAnyRole("ADMIN", "GERENTE")
                        .requestMatchers("DELETE", "/api/productos/**").hasRole("ADMIN")

                        // ===== ENDPOINTS DE CATEGORÍAS (por método HTTP) =====
                        .requestMatchers("GET", "/api/categorias/**").hasAnyRole("ADMIN", "GERENTE", "EMPLEADO")
                        .requestMatchers("POST", "/api/categorias/**").hasAnyRole("ADMIN", "GERENTE")
                        .requestMatchers("PUT", "/api/categorias/**").hasAnyRole("ADMIN", "GERENTE")
                        .requestMatchers("DELETE", "/api/categorias/**").hasRole("ADMIN")

                        // ===== ENDPOINTS DE MOVIMIENTOS =====
                        // Lectura: todos los roles autenticados
                        .requestMatchers("GET", "/api/movimientos/**").hasAnyRole("ADMIN", "GERENTE", "EMPLEADO")
                        // Crear movimientos: gerente o superior
                        .requestMatchers("POST", "/api/movimientos/**").hasAnyRole("ADMIN", "GERENTE")

                        // ===== CUALQUIER OTRA REQUEST REQUIERE AUTENTICACIÓN =====
                        .anyRequest().authenticated()
                )

                // Configurar sesiones como stateless (para JWT)
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )

                // Configurar el authentication provider
                .authenticationProvider(authenticationProvider())

                // Agregar el filtro JWT antes del filtro de autenticación estándar
                .addFilterBefore(jwtAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class)

                // Deshabilitar headers de frame para H2 Console (solo para desarrollo)
                .headers(headers -> headers.frameOptions(frameOptions -> frameOptions.disable()))

                // Configurar manejo de excepciones de autenticación
                .exceptionHandling(exceptions -> exceptions
                        .authenticationEntryPoint((request, response, authException) -> {
                            // Manejo personalizado para requests no autenticadas
                            response.setStatus(401);
                            response.setContentType("application/json");
                            response.setCharacterEncoding("UTF-8");

                            String errorResponse = String.format(
                                    "{\"timestamp\":\"%s\",\"status\":401,\"error\":\"Unauthorized\",\"message\":\"%s\",\"path\":\"%s\"}",
                                    java.time.LocalDateTime.now().toString(),
                                    "Token JWT requerido",
                                    request.getRequestURI()
                            );

                            response.getWriter().write(errorResponse);
                        })
                        .accessDeniedHandler((request, response, accessDeniedException) -> {
                            // Manejo personalizado para requests sin permisos suficientes
                            response.setStatus(403);
                            response.setContentType("application/json");
                            response.setCharacterEncoding("UTF-8");

                            String errorResponse = String.format(
                                    "{\"timestamp\":\"%s\",\"status\":403,\"error\":\"Forbidden\",\"message\":\"%s\",\"path\":\"%s\"}",
                                    java.time.LocalDateTime.now().toString(),
                                    "Permisos insuficientes",
                                    request.getRequestURI()
                            );

                            response.getWriter().write(errorResponse);
                        })
                );

        return http.build();
    }

    /**
     * Configuración de endpoints para diferentes niveles de acceso
     * Documentación de la estructura de permisos
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
                "/actuator/info",
                "/favicon.ico",
                "/error"
        };

        // Endpoints que requieren rol de ADMIN
        public static final String[] ADMIN_ONLY_ENDPOINTS = {
                "/api/usuarios/**",
                "/api/roles/**",
                "/actuator/**",
                "DELETE:/api/productos/**",
                "DELETE:/api/categorias/**"
        };

        // Endpoints que requieren rol de GERENTE o superior
        public static final String[] MANAGER_OR_ABOVE_ENDPOINTS = {
                "/api/reportes/**",
                "/api/estadisticas/**",
                "POST:/api/productos/**",
                "PUT:/api/productos/**",
                "POST:/api/categorias/**",
                "PUT:/api/categorias/**",
                "POST:/api/movimientos/**"
        };

        // Endpoints de solo lectura para EMPLEADO
        public static final String[] EMPLOYEE_READ_ENDPOINTS = {
                "GET:/api/productos/**",
                "GET:/api/categorias/**",
                "GET:/api/movimientos/**"
        };
    }

    /**
     * Configuración de roles y permisos
     * Documentación del sistema de autorización
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
     * Configuración de JWT
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