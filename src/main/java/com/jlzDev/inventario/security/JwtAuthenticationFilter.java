package com.jlzDev.inventario.security;

import com.jlzDev.inventario.config.SecurityConfig;
import com.jlzDev.inventario.dto.response.TokenResponse;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 * Filtro de autenticación JWT que intercepta todas las requests HTTP
 * Valida tokens JWT y establece el contexto de seguridad de Spring Security
 */
@Component
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UserDetailsServiceImpl userDetailsService;

    // Headers donde puede venir el token
    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String TOKEN_PREFIX = SecurityConfig.JwtConfig.JWT_TOKEN_PREFIX;

    // Rutas que no requieren autenticación (endpoints públicos)
    private static final List<String> PUBLIC_ENDPOINTS = Arrays.asList(
            "/api/auth/login",
            "/api/auth/refresh",
            "/api/auth/health",
            "/api/public",
            "/h2-console",
            "/swagger-ui",
            "/v3/api-docs",
            "/actuator/health",
            "/actuator/info",
            "/favicon.ico",
            "/error"
    );

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain) throws ServletException, IOException {

        String requestURI = request.getRequestURI();
        String method = request.getMethod();

        log.debug("Procesando request: {} {}", method, requestURI);

        try {
            // Verificar si es un endpoint público que no requiere autenticación
            if (isPublicEndpoint(requestURI)) {
                log.debug("Endpoint público detectado: {}. Saltando validación JWT", requestURI);
                filterChain.doFilter(request, response);
                return;
            }

            // Permitir OPTIONS requests (CORS preflight)
            if ("OPTIONS".equalsIgnoreCase(method)) {
                log.debug("OPTIONS request detectado. Saltando validación JWT");
                filterChain.doFilter(request, response);
                return;
            }

            // Obtener token del header Authorization
            String authHeader = request.getHeader(AUTHORIZATION_HEADER);
            String token = extractTokenFromHeader(authHeader);

            if (token == null) {
                log.debug("No se encontró token JWT en header Authorization para: {}", requestURI);
                handleMissingToken(response);
                return;
            }

            // Extraer username del token
            String username = jwtUtil.extractUsername(token);

            if (username == null) {
                log.warn("No se pudo extraer username del token para request: {}", requestURI);
                handleInvalidToken(response, TokenResponse.tokenInvalido("Token malformado"));
                return;
            }

            // Verificar si ya existe autenticación en el contexto
            if (SecurityContextHolder.getContext().getAuthentication() != null) {
                log.debug("Usuario ya autenticado en contexto: {} para request: {}", username, requestURI);
                addTokenInfoToRequest(request, token, username);
                filterChain.doFilter(request, response);
                return;
            }

            // Cargar detalles del usuario
            UserDetails userDetails = loadUserDetails(username);

            if (userDetails == null) {
                log.warn("No se pudieron cargar detalles del usuario: {}", username);
                handleInvalidToken(response, TokenResponse.tokenInvalido("Usuario no encontrado"));
                return;
            }

            // Validar token contra el usuario específico
            if (!jwtUtil.validateToken(token, username)) {
                log.warn("Token no válido para el usuario: {}", username);
                handleInvalidToken(response, TokenResponse.tokenInvalido("Token no válido para el usuario"));
                return;
            }

            // Crear autenticación y establecer contexto de seguridad
            UsernamePasswordAuthenticationToken authentication = createAuthentication(userDetails, request);
            SecurityContextHolder.getContext().setAuthentication(authentication);

            log.debug("Usuario autenticado exitosamente: {} para request: {}", username, requestURI);

            // Agregar información adicional a la request para uso posterior
            addTokenInfoToRequest(request, token, username);

        } catch (Exception e) {
            log.error("Error inesperado en filtro JWT para request {}: {}", requestURI, e.getMessage(), e);

            // Limpiar contexto de seguridad en caso de error
            SecurityContextHolder.clearContext();

            handleFilterError(response, e);
            return;
        }

        // Continuar con la cadena de filtros
        filterChain.doFilter(request, response);
    }

    /**
     * Verificar si es un endpoint público que no requiere autenticación
     */
    private boolean isPublicEndpoint(String requestURI) {
        if (requestURI == null) {
            return false;
        }

        return PUBLIC_ENDPOINTS.stream()
                .anyMatch(endpoint -> requestURI.startsWith(endpoint));
    }

    /**
     * Extraer token del header Authorization
     */
    private String extractTokenFromHeader(String authHeader) {
        if (authHeader == null || authHeader.trim().isEmpty()) {
            return null;
        }

        if (!authHeader.startsWith(TOKEN_PREFIX)) {
            log.debug("Header Authorization no tiene el prefijo Bearer correcto");
            return null;
        }

        String token = authHeader.substring(TOKEN_PREFIX.length()).trim();

        if (token.isEmpty()) {
            log.debug("Token vacío después de remover prefijo Bearer");
            return null;
        }

        return token;
    }

    /**
     * Cargar detalles del usuario de forma segura
     */
    private UserDetails loadUserDetails(String username) {
        try {
            return userDetailsService.loadUserByUsername(username);
        } catch (Exception e) {
            log.warn("Error al cargar detalles del usuario {}: {}", username, e.getMessage());
            return null;
        }
    }

    /**
     * Crear token de autenticación para Spring Security
     */
    private UsernamePasswordAuthenticationToken createAuthentication(UserDetails userDetails,
                                                                     HttpServletRequest request) {
        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                userDetails,
                null, // credentials no necesarias después de autenticación JWT
                userDetails.getAuthorities()
        );

        // Agregar detalles de la web request (IP, session, etc.)
        authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

        return authentication;
    }

    /**
     * Agregar información del token a la request para uso posterior
     */
    private void addTokenInfoToRequest(HttpServletRequest request, String token, String username) {
        try {
            request.setAttribute("jwt.token", token);
            request.setAttribute("jwt.username", username);
            request.setAttribute("jwt.extractedAt", System.currentTimeMillis());

            // Extraer información adicional del token
            String sessionId = jwtUtil.extractSessionId(token);
            String role = jwtUtil.extractRole(token);
            Long userId = jwtUtil.extractUserId(token);

            if (sessionId != null) request.setAttribute("jwt.sessionId", sessionId);
            if (role != null) request.setAttribute("jwt.role", role);
            if (userId != null) request.setAttribute("jwt.userId", userId);

        } catch (Exception e) {
            log.warn("Error agregando información del token a la request: {}", e.getMessage());
        }
    }

    /**
     * Manejar caso cuando no hay token en la request
     */
    private void handleMissingToken(HttpServletResponse response) throws IOException {
        TokenResponse tokenResponse = TokenResponse.builder()
                .success(false)
                .message("Token de autenticación requerido")
                .error("MISSING_TOKEN")
                .timestamp(java.time.LocalDateTime.now().toString())
                .build();

        sendJsonError(response, 401, tokenResponse);
    }

    /**
     * Manejar caso cuando el token es inválido
     */
    private void handleInvalidToken(HttpServletResponse response, TokenResponse tokenResponse) throws IOException {
        sendJsonError(response, 401, tokenResponse);
    }

    /**
     * Manejar errores generales del filtro
     */
    private void handleFilterError(HttpServletResponse response, Exception e) throws IOException {
        TokenResponse tokenResponse = TokenResponse.builder()
                .success(false)
                .message("Error en autenticación: " + e.getMessage())
                .error("AUTHENTICATION_ERROR")
                .timestamp(java.time.LocalDateTime.now().toString())
                .build();

        sendJsonError(response, 500, tokenResponse);
    }

    /**
     * Enviar respuesta de error en formato JSON
     */
    private void sendJsonError(HttpServletResponse response, int status, TokenResponse tokenResponse) throws IOException {
        response.setStatus(status);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        // Crear JSON manualmente para evitar dependencias adicionales
        String jsonResponse = String.format(
                "{\"success\":%s,\"message\":\"%s\",\"error\":\"%s\",\"timestamp\":\"%s\"}",
                tokenResponse.isSuccess(),
                tokenResponse.getMessage(),
                tokenResponse.getError() != null ? tokenResponse.getError() : "",
                tokenResponse.getTimestamp()
        );

        response.getWriter().write(jsonResponse);
    }

    /**
     * Especificar que este filtro debe aplicarse a todas las requests
     */
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        // Retorna false para indicar que el filtro debe aplicarse siempre
        // La lógica de exclusión se maneja en doFilterInternal
        return false;
    }

    // ===== MÉTODOS ESTÁTICOS PARA ACCEDER A INFORMACIÓN DEL TOKEN DESDE CONTROLLERS =====

    /**
     * Obtener token desde la request (agregado por el filtro)
     */
    public static String getTokenFromRequest(HttpServletRequest request) {
        Object token = request.getAttribute("jwt.token");
        return token != null ? token.toString() : null;
    }

    /**
     * Obtener username desde la request (agregado por el filtro)
     */
    public static String getUsernameFromRequest(HttpServletRequest request) {
        Object username = request.getAttribute("jwt.username");
        return username != null ? username.toString() : null;
    }

    /**
     * Obtener session ID desde la request
     */
    public static String getSessionIdFromRequest(HttpServletRequest request) {
        Object sessionId = request.getAttribute("jwt.sessionId");
        return sessionId != null ? sessionId.toString() : null;
    }

    /**
     * Obtener rol desde la request
     */
    public static String getRoleFromRequest(HttpServletRequest request) {
        Object role = request.getAttribute("jwt.role");
        return role != null ? role.toString() : null;
    }

    /**
     * Obtener user ID desde la request
     */
    public static Long getUserIdFromRequest(HttpServletRequest request) {
        Object userId = request.getAttribute("jwt.userId");
        if (userId instanceof Long) {
            return (Long) userId;
        } else if (userId instanceof String) {
            try {
                return Long.parseLong(userId.toString());
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return null;
    }

    /**
     * Obtener timestamp de cuando se extrajo el token
     */
    public static Long getTokenExtractionTime(HttpServletRequest request) {
        Object timestamp = request.getAttribute("jwt.extractedAt");
        if (timestamp instanceof Long) {
            return (Long) timestamp;
        } else if (timestamp instanceof String) {
            try {
                return Long.parseLong(timestamp.toString());
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return null;
    }

    /**
     * Verificar si la request tiene información de token
     */
    public static boolean hasTokenInfo(HttpServletRequest request) {
        return request.getAttribute("jwt.token") != null &&
                request.getAttribute("jwt.username") != null;
    }

    /**
     * Obtener toda la información del token como un mapa
     */
    public static java.util.Map<String, Object> getTokenInfoMap(HttpServletRequest request) {
        java.util.Map<String, Object> tokenInfo = new java.util.HashMap<>();

        String token = getTokenFromRequest(request);
        String username = getUsernameFromRequest(request);
        String sessionId = getSessionIdFromRequest(request);
        String role = getRoleFromRequest(request);
        Long userId = getUserIdFromRequest(request);
        Long extractedAt = getTokenExtractionTime(request);

        if (token != null) tokenInfo.put("token", token);
        if (username != null) tokenInfo.put("username", username);
        if (sessionId != null) tokenInfo.put("sessionId", sessionId);
        if (role != null) tokenInfo.put("role", role);
        if (userId != null) tokenInfo.put("userId", userId);
        if (extractedAt != null) tokenInfo.put("extractedAt", extractedAt);

        return tokenInfo;
    }
}