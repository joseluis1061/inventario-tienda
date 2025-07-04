package com.jlzDev.inventario.dto.response;

import com.jlzDev.inventario.entity.Usuario;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * DTO para respuestas de autenticación
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {

    private boolean success;
    private String message;
    private String accessToken;
    private String refreshToken;
    private Long expiresIn; // segundos hasta expiración
    private LocalDateTime expiresAt; // fecha exacta de expiración
    private LocalDateTime refreshExpiresAt;
    private UserInfo user;
    private String[] authorities;
    private String sessionId;
    private Boolean isExtendedSession;
    private ClientConfig clientConfig;
    private String timestamp;

    /**
     * DTO interno para información del usuario
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserInfo {
        private Long id;
        private String username;
        private String nombreCompleto;
        private String email;
        private String role;
        private boolean activo;
        private LocalDateTime fechaCreacion;

        /**
         * Crear UserInfo desde entidad Usuario
         */
        public static UserInfo fromUsuario(Usuario usuario) {
            return UserInfo.builder()
                    .id(usuario.getId())
                    .username(usuario.getUsername())
                    .nombreCompleto(usuario.getNombreCompleto())
                    .email(usuario.getEmail())
                    .role(usuario.getRol().getNombre())
                    .activo(usuario.getActivo())
                    .fechaCreacion(usuario.getFechaCreacion())
                    .build();
        }
    }

    /**
     * DTO interno para configuración del cliente
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ClientConfig {
        private Integer refreshThreshold; // segundos antes de expirar para refrescar
        private Boolean autoRefresh;
        private String[] allowedOperations;
        private Integer maxConcurrentSessions;
        private Map<String, Object> additionalConfig;
    }

    /**
     * Constructor para login exitoso
     */
    public static AuthResponse loginExitoso(String accessToken, String refreshToken,
                                            Long expiresIn, LocalDateTime expiresAt,
                                            LocalDateTime refreshExpiresAt, UserInfo user,
                                            String[] authorities, String sessionId) {
        return AuthResponse.builder()
                .success(true)
                .message("Login exitoso")
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .expiresIn(expiresIn)
                .expiresAt(expiresAt)
                .refreshExpiresAt(refreshExpiresAt)
                .user(user)
                .authorities(authorities)
                .sessionId(sessionId)
                .isExtendedSession(false)
                .timestamp(LocalDateTime.now().toString())
                .build();
    }

    /**
     * Constructor para login exitoso con sesión extendida
     */
    public static AuthResponse loginExtendido(String accessToken, String refreshToken,
                                              Long expiresIn, LocalDateTime expiresAt,
                                              LocalDateTime refreshExpiresAt, UserInfo user,
                                              String[] authorities, String sessionId) {
        return AuthResponse.builder()
                .success(true)
                .message("Login exitoso con sesión extendida")
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .expiresIn(expiresIn)
                .expiresAt(expiresAt)
                .refreshExpiresAt(refreshExpiresAt)
                .user(user)
                .authorities(authorities)
                .sessionId(sessionId)
                .isExtendedSession(true)
                .timestamp(LocalDateTime.now().toString())
                .build();
    }

    /**
     * Constructor para refresh exitoso
     */
    public static AuthResponse refreshExitoso(String accessToken, Long expiresIn,
                                              LocalDateTime expiresAt, UserInfo user,
                                              String[] authorities, String sessionId) {
        return AuthResponse.builder()
                .success(true)
                .message("Token renovado exitosamente")
                .accessToken(accessToken)
                .expiresIn(expiresIn)
                .expiresAt(expiresAt)
                .user(user)
                .authorities(authorities)
                .sessionId(sessionId)
                .timestamp(LocalDateTime.now().toString())
                .build();
    }

    /**
     * Constructor para errores de autenticación
     */
    public static AuthResponse error(String mensaje) {
        return AuthResponse.builder()
                .success(false)
                .message(mensaje)
                .timestamp(LocalDateTime.now().toString())
                .build();
    }

    /**
     * Verificar si la respuesta es exitosa
     */
    public boolean esExitoso() {
        return success && accessToken != null && !accessToken.trim().isEmpty();
    }

    /**
     * Verificar si tiene refresh token
     */
    public boolean tieneRefreshToken() {
        return refreshToken != null && !refreshToken.trim().isEmpty();
    }

    /**
     * Obtener tiempo restante en minutos
     */
    public Long getTiempoRestanteMinutos() {
        if (expiresIn == null) return null;
        return expiresIn / 60;
    }
}