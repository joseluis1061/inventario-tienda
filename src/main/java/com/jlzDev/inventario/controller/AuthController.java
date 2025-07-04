package com.jlzDev.inventario.controller;

import com.jlzDev.inventario.dto.request.LoginRequest;
import com.jlzDev.inventario.dto.request.RefreshTokenRequest;
import com.jlzDev.inventario.dto.response.AuthResponse;
import com.jlzDev.inventario.dto.response.TokenResponse;
import com.jlzDev.inventario.entity.Usuario;
import com.jlzDev.inventario.security.JwtAuthenticationFilter;
import com.jlzDev.inventario.security.JwtUtil;
import com.jlzDev.inventario.security.UserDetailsServiceImpl;
import com.jlzDev.inventario.service.UsuarioService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

/**
 * Controller REST para manejo de autenticación
 * Endpoints para login, refresh token, logout y validación
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*") // Configurar según necesidades de CORS
public class AuthController {

    private final UsuarioService usuarioService;
    private final UserDetailsServiceImpl userDetailsService;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;

    /**
     * Login de usuario
     * POST /api/auth/login
     */
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest loginRequest,
                                              HttpServletRequest request) {
        log.info("POST /api/auth/login - Intento de login: {}", loginRequest.getResumenParaLog());

        try {
            // Normalizar datos del request
            loginRequest.normalizar();

            // Validaciones adicionales
            if (!loginRequest.esCredencialValida()) {
                log.warn("Credenciales inválidas en login: {}", loginRequest.getUsername());
                return ResponseEntity.badRequest().build();
            }

            // Autenticar usuario
            Usuario usuario = autenticarUsuario(loginRequest);

            // Generar tokens
            String sessionId = jwtUtil.generateSessionId(usuario.getUsername());
            String deviceInfo = obtenerDeviceInfo(loginRequest, request);

            String accessToken = jwtUtil.generateAccessToken(usuario, sessionId, deviceInfo);
            String refreshToken = jwtUtil.generateRefreshToken(usuario, sessionId, deviceInfo);

            // Calcular fechas de expiración
            LocalDateTime accessTokenExpiry = jwtUtil.extractExpirationAsLocalDateTime(accessToken);
            LocalDateTime refreshTokenExpiry = jwtUtil.extractExpirationAsLocalDateTime(refreshToken);
            Long expiresInSeconds = jwtUtil.getTokenRemainingTime(accessToken);

            // Crear información del usuario
            AuthResponse.UserInfo userInfo = AuthResponse.UserInfo.fromUsuario(usuario);

            // Obtener authorities como array de strings
            String[] authorities = userDetailsService.getAuthoritiesAsStrings(usuario.getUsername());

            // Crear respuesta según tipo de sesión
            AuthResponse response;
            if (loginRequest.esSesionExtendida()) {
                response = AuthResponse.loginExtendido(
                        accessToken, refreshToken, expiresInSeconds,
                        accessTokenExpiry, refreshTokenExpiry,
                        userInfo, authorities, sessionId
                );
            } else {
                response = AuthResponse.loginExitoso(
                        accessToken, refreshToken, expiresInSeconds,
                        accessTokenExpiry, refreshTokenExpiry,
                        userInfo, authorities, sessionId
                );
            }

            // Agregar configuración del cliente
            response.setClientConfig(crearConfiguracionCliente(usuario));

            log.info("Login exitoso para usuario: {} con sessionId: {}", usuario.getUsername(), sessionId);
            return ResponseEntity.ok(response);

        } catch (BadCredentialsException e) {
            log.warn("Credenciales incorrectas para usuario: {}", loginRequest.getUsername());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        } catch (UsernameNotFoundException e) {
            log.warn("Usuario no encontrado: {}", loginRequest.getUsername());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        } catch (IllegalArgumentException e) {
            log.warn("Datos inválidos en login: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            log.error("Error interno en login para usuario {}: {}", loginRequest.getUsername(), e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Refresh de access token usando refresh token
     * POST /api/auth/refresh
     */
    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refreshToken(@Valid @RequestBody RefreshTokenRequest refreshRequest) {
        log.info("POST /api/auth/refresh - Solicitando refresh: {}", refreshRequest.getResumenParaLog());

        try {
            // Normalizar datos del request
            refreshRequest.normalizar();

            // Validaciones adicionales
            if (!refreshRequest.esRequestValido()) {
                log.warn("Request de refresh inválido");
                return ResponseEntity.badRequest().build();
            }

            // Validar refresh token
            String refreshToken = refreshRequest.getRefreshToken();
            if (!jwtUtil.isValidRefreshToken(refreshToken)) {
                log.warn("Refresh token inválido o expirado");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }

            // Extraer información del refresh token
            String username = jwtUtil.extractUsername(refreshToken);
            String sessionId = jwtUtil.extractSessionId(refreshToken);

            // Obtener usuario actual
            Optional<Usuario> usuarioOpt = usuarioService.obtenerPorUsername(username);
            if (usuarioOpt.isEmpty()) {
                log.warn("Usuario no encontrado durante refresh: {}", username);
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }

            Usuario usuario = usuarioOpt.get();

            // Verificar que el usuario siga activo
            if (!usuario.getActivo()) {
                log.warn("Usuario inactivo intentando refresh: {}", username);
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }

            // Validación adicional de username si viene en el request
            if (refreshRequest.tieneUsername() && !refreshRequest.getUsername().equals(username)) {
                log.warn("Username en request no coincide con token: {} vs {}",
                        refreshRequest.getUsername(), username);
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }

            // Generar nuevo access token
            String newAccessToken = jwtUtil.refreshAccessToken(refreshToken, usuario, sessionId);

            // Calcular nueva fecha de expiración
            LocalDateTime newAccessTokenExpiry = jwtUtil.extractExpirationAsLocalDateTime(newAccessToken);
            Long expiresInSeconds = jwtUtil.getTokenRemainingTime(newAccessToken);

            // Crear información del usuario
            AuthResponse.UserInfo userInfo = AuthResponse.UserInfo.fromUsuario(usuario);

            // Obtener authorities
            String[] authorities = userDetailsService.getAuthoritiesAsStrings(username);

            // Crear respuesta de refresh
            AuthResponse response = AuthResponse.refreshExitoso(
                    newAccessToken, expiresInSeconds, newAccessTokenExpiry,
                    userInfo, authorities, sessionId
            );

            // Si solicita extensión de sesión, generar nuevo refresh token también
            if (refreshRequest.solicitaExtensionSesion()) {
                String deviceInfo = refreshRequest.getDeviceInfo();
                String newRefreshToken = jwtUtil.generateRefreshToken(usuario, sessionId, deviceInfo);
                LocalDateTime newRefreshExpiry = jwtUtil.extractExpirationAsLocalDateTime(newRefreshToken);

                response.setRefreshToken(newRefreshToken);
                response.setRefreshExpiresAt(newRefreshExpiry);
                response.setIsExtendedSession(true);
                response.setMessage("Token y sesión renovados con extensión");
            }

            log.info("Refresh exitoso para usuario: {} con sessionId: {}", username, sessionId);
            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            log.warn("Error de validación en refresh: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (RuntimeException e) {
            log.warn("Error de negocio en refresh: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        } catch (Exception e) {
            log.error("Error interno en refresh: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Logout de usuario (invalidar token)
     * POST /api/auth/logout
     */
    @PostMapping("/logout")
    public ResponseEntity<TokenResponse> logout(HttpServletRequest request) {
        log.info("POST /api/auth/logout - Solicitando logout");

        try {
            // Obtener información del token desde la request (agregada por el filtro)
            String username = JwtAuthenticationFilter.getUsernameFromRequest(request);
            String sessionId = JwtAuthenticationFilter.getSessionIdFromRequest(request);

            if (username == null) {
                log.warn("No se encontró información de usuario en logout");
                return ResponseEntity.badRequest().build();
            }

            // En una implementación completa, aquí se podría:
            // 1. Agregar token a blacklist
            // 2. Invalidar sesión en caché
            // 3. Registrar logout en auditoría
            // Por ahora, simplemente logueamos el evento

            TokenResponse response = TokenResponse.tokenRevocado(username, sessionId);

            log.info("Logout exitoso para usuario: {} con sessionId: {}", username, sessionId);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error interno en logout: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Validar token actual
     * GET /api/auth/validate
     */
    @GetMapping("/validate")
    public ResponseEntity<TokenResponse> validateToken(HttpServletRequest request) {
        log.debug("GET /api/auth/validate - Validando token");

        try {
            // Obtener información del token desde la request
            String token = JwtAuthenticationFilter.getTokenFromRequest(request);
            String username = JwtAuthenticationFilter.getUsernameFromRequest(request);
            String role = JwtAuthenticationFilter.getRoleFromRequest(request);
            String sessionId = JwtAuthenticationFilter.getSessionIdFromRequest(request);

            if (token == null || username == null) {
                log.warn("Información de token incompleta en validación");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }

            // Validar token directamente
            TokenResponse validation = jwtUtil.validateToken(token);

            if (!validation.esExitoso()) {
                log.warn("Token inválido en validación para usuario: {}", username);
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(validation);
            }

            // Enriquecer respuesta con información de la request
            validation.setUsername(username);
            validation.setRole(role);
            validation.setSessionId(sessionId);

            log.debug("Token válido para usuario: {}", username);
            return ResponseEntity.ok(validation);

        } catch (Exception e) {
            log.error("Error interno en validación de token: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Obtener información del usuario actual
     * GET /api/auth/me
     */
    @GetMapping("/me")
    public ResponseEntity<AuthResponse.UserInfo> getCurrentUser(HttpServletRequest request) {
        log.debug("GET /api/auth/me - Obteniendo información del usuario actual");

        try {
            String username = JwtAuthenticationFilter.getUsernameFromRequest(request);

            if (username == null) {
                log.warn("No se encontró username en request para /me");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }

            // Obtener usuario actual
            Optional<Usuario> usuarioOpt = usuarioService.obtenerPorUsername(username);
            if (usuarioOpt.isEmpty()) {
                log.warn("Usuario no encontrado en /me: {}", username);
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }

            Usuario usuario = usuarioOpt.get();
            AuthResponse.UserInfo userInfo = AuthResponse.UserInfo.fromUsuario(usuario);

            log.debug("Información de usuario obtenida para: {}", username);
            return ResponseEntity.ok(userInfo);

        } catch (Exception e) {
            log.error("Error interno en /me: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Cambiar contraseña del usuario actual
     * POST /api/auth/change-password
     */
    @PostMapping("/change-password")
    public ResponseEntity<Map<String, String>> changePassword(
            @RequestParam String currentPassword,
            @RequestParam String newPassword,
            HttpServletRequest request) {

        log.info("POST /api/auth/change-password - Cambiando contraseña");

        try {
            String username = JwtAuthenticationFilter.getUsernameFromRequest(request);
            Long userId = JwtAuthenticationFilter.getUserIdFromRequest(request);

            if (username == null || userId == null) {
                log.warn("Información de usuario incompleta en cambio de contraseña");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }

            // Usar el service existente para cambiar contraseña
            usuarioService.cambiarPassword(userId, currentPassword, newPassword);

            Map<String, String> response = Map.of(
                    "message", "Contraseña cambiada exitosamente",
                    "timestamp", LocalDateTime.now().toString()
            );

            log.info("Contraseña cambiada exitosamente para usuario: {}", username);
            return ResponseEntity.ok(response);

        } catch (RuntimeException e) {
            log.warn("Error al cambiar contraseña: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            log.error("Error interno al cambiar contraseña: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Endpoint de salud del sistema de autenticación
     * GET /api/auth/health
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {
        Map<String, Object> health = Map.of(
                "status", "UP",
                "service", "AuthController",
                "timestamp", LocalDateTime.now().toString(),
                "jwtConfig", jwtUtil.getExpirationConfig()
        );

        return ResponseEntity.ok(health);
    }

    // ===== MÉTODOS PRIVADOS =====

    /**
     * Autenticar usuario con username/email y password
     */
    private Usuario autenticarUsuario(LoginRequest loginRequest) {
        try {
            // Cargar UserDetails para validar existencia y estado
            UserDetails userDetails = userDetailsService.loadUserByUsernameOrEmail(loginRequest.getUsername());

            // Obtener Usuario entity
            Optional<Usuario> usuarioOpt = usuarioService.obtenerPorUsername(userDetails.getUsername());
            if (usuarioOpt.isEmpty()) {
                throw new UsernameNotFoundException("Usuario no encontrado");
            }

            Usuario usuario = usuarioOpt.get();

            // Verificar contraseña
            if (!passwordEncoder.matches(loginRequest.getPassword(), usuario.getPassword())) {
                throw new BadCredentialsException("Contraseña incorrecta");
            }

            // Verificar que el usuario esté activo
            if (!usuario.getActivo()) {
                throw new BadCredentialsException("Usuario inactivo");
            }

            return usuario;

        } catch (UsernameNotFoundException | BadCredentialsException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error inesperado en autenticación: {}", e.getMessage());
            throw new RuntimeException("Error en autenticación", e);
        }
    }

    /**
     * Obtener información del dispositivo
     */
    private String obtenerDeviceInfo(LoginRequest loginRequest, HttpServletRequest request) {
        // Priorizar device info del request
        if (loginRequest.tieneInfoDispositivo()) {
            return loginRequest.getDeviceInfo();
        }

        // Generar desde User-Agent si está disponible
        String userAgent = request.getHeader("User-Agent");
        if (userAgent != null && !userAgent.trim().isEmpty()) {
            return "Web: " + userAgent.substring(0, Math.min(userAgent.length(), 50));
        }

        // Fallback
        return "Unknown Device";
    }

    /**
     * Crear configuración del cliente basada en el rol del usuario
     */
    private AuthResponse.ClientConfig crearConfiguracionCliente(Usuario usuario) {
        String roleName = usuario.getRol().getNombre().toUpperCase();

        return AuthResponse.ClientConfig.builder()
                .refreshThreshold(300) // 5 minutos antes de expirar
                .autoRefresh(true)
                .allowedOperations(determinarOperacionesPermitidas(roleName))
                .maxConcurrentSessions(determinarMaxSesiones(roleName))
                .build();
    }

    /**
     * Determinar operaciones permitidas basadas en el rol
     */
    private String[] determinarOperacionesPermitidas(String roleName) {
        switch (roleName) {
            case "ADMIN":
                return new String[]{"READ", "WRITE", "DELETE", "ADMIN", "REPORTS", "USERS"};
            case "GERENTE":
                return new String[]{"READ", "WRITE", "REPORTS", "INVENTORY"};
            case "EMPLEADO":
                return new String[]{"READ", "INVENTORY"};
            default:
                return new String[]{"READ"};
        }
    }

    /**
     * Determinar máximo de sesiones concurrentes por rol
     */
    private Integer determinarMaxSesiones(String roleName) {
        switch (roleName) {
            case "ADMIN":
                return 5;
            case "GERENTE":
                return 3;
            case "EMPLEADO":
                return 2;
            default:
                return 1;
        }
    }
}