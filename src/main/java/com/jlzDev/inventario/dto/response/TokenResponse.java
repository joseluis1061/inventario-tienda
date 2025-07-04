package com.jlzDev.inventario.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Response DTO para operaciones relacionadas con tokens JWT
 * Maneja respuestas de validación, creación y revocación de tokens
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TokenResponse {

    private boolean success;
    private String message;
    private String error;
    private TokenStatus status;
    private String username;
    private String role;
    private String sessionId;
    private String accessToken;
    private String refreshToken;
    private Long expiresIn; // segundos hasta expiración
    private LocalDateTime expiresAt; // fecha exacta de expiración
    private LocalDateTime refreshExpiresAt;
    private String timestamp;

    /**
     * Enum para estados de token
     */
    public enum TokenStatus {
        VALID("VALID"),
        INVALID("INVALID"),
        EXPIRED("EXPIRED"),
        REVOKED("REVOKED"),
        REFRESHED("REFRESHED"),
        GENERATED("GENERATED");

        private final String value;

        TokenStatus(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }
    }

    /**
     * Verificar si la respuesta es exitosa
     */
    public boolean esExitoso() {
        return success && (status == TokenStatus.VALID ||
                status == TokenStatus.REFRESHED ||
                status == TokenStatus.GENERATED ||
                status == TokenStatus.REVOKED);
    }

    /**
     * Constructor para token exitoso (login/refresh)
     */
    public static TokenResponse tokenExitoso(String accessToken, String refreshToken,
                                             Long expiresIn, LocalDateTime expiresAt,
                                             String username, String role, String sessionId) {
        return TokenResponse.builder()
                .success(true)
                .status(TokenStatus.GENERATED)
                .message("Token generado exitosamente")
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .expiresIn(expiresIn)
                .expiresAt(expiresAt)
                .username(username)
                .role(role)
                .sessionId(sessionId)
                .timestamp(LocalDateTime.now().toString())
                .build();
    }

    /**
     * Constructor para token renovado (refresh)
     */
    public static TokenResponse tokenRenovado(String accessToken, Long expiresIn,
                                              LocalDateTime expiresAt, String username,
                                              String role, String sessionId) {
        return TokenResponse.builder()
                .success(true)
                .status(TokenStatus.REFRESHED)
                .message("Token renovado exitosamente")
                .accessToken(accessToken)
                .expiresIn(expiresIn)
                .expiresAt(expiresAt)
                .username(username)
                .role(role)
                .sessionId(sessionId)
                .timestamp(LocalDateTime.now().toString())
                .build();
    }

    /**
     * Constructor para token válido (validación exitosa)
     */
    public static TokenResponse tokenValido(String username, String role,
                                            LocalDateTime expiresAt, String sessionId) {
        return TokenResponse.builder()
                .success(true)
                .status(TokenStatus.VALID)
                .message("Token válido")
                .username(username)
                .role(role)
                .expiresAt(expiresAt)
                .sessionId(sessionId)
                .timestamp(LocalDateTime.now().toString())
                .build();
    }

    /**
     * Constructor para token inválido
     */
    public static TokenResponse tokenInvalido(String motivo) {
        return TokenResponse.builder()
                .success(false)
                .status(TokenStatus.INVALID)
                .message("Token inválido: " + motivo)
                .error("INVALID_TOKEN")
                .timestamp(LocalDateTime.now().toString())
                .build();
    }

    /**
     * Constructor para token expirado
     */
    public static TokenResponse tokenExpirado(String username, LocalDateTime expiredAt) {
        return TokenResponse.builder()
                .success(false)
                .status(TokenStatus.EXPIRED)
                .message("Token expirado")
                .error("TOKEN_EXPIRED")
                .username(username)
                .expiresAt(expiredAt)
                .timestamp(LocalDateTime.now().toString())
                .build();
    }

    /**
     * Constructor para token revocado (logout)
     */
    public static TokenResponse tokenRevocado(String username, String sessionId) {
        return TokenResponse.builder()
                .success(true)
                .status(TokenStatus.REVOKED)
                .message("Token revocado exitosamente")
                .username(username)
                .sessionId(sessionId)
                .timestamp(LocalDateTime.now().toString())
                .build();
    }

    /**
     * Constructor para errores de validación
     */
    public static TokenResponse error(String mensaje, String codigo) {
        return TokenResponse.builder()
                .success(false)
                .status(TokenStatus.INVALID)
                .message(mensaje)
                .error(codigo)
                .timestamp(LocalDateTime.now().toString())
                .build();
    }

    /**
     * Constructor simple para respuestas básicas
     */
    public static TokenResponse simple(boolean success, String message) {
        return TokenResponse.builder()
                .success(success)
                .message(message)
                .status(success ? TokenStatus.VALID : TokenStatus.INVALID)
                .timestamp(LocalDateTime.now().toString())
                .build();
    }

    /**
     * Agregar información adicional al response
     */
    public TokenResponse withAdditionalInfo(String sessionId, String role) {
        this.sessionId = sessionId;
        this.role = role;
        return this;
    }

    /**
     * Verificar si el token tiene refresh token
     */
    public boolean tieneRefreshToken() {
        return refreshToken != null && !refreshToken.trim().isEmpty();
    }

    /**
     * Verificar si el token está por expirar (menos de 5 minutos)
     */
    public boolean estaPorExpirar() {
        if (expiresAt == null) return false;
        return expiresAt.isBefore(LocalDateTime.now().plusMinutes(5));
    }

    /**
     * Obtener tiempo restante en segundos
     */
    public Long getTiempoRestanteSegundos() {
        if (expiresAt == null) return null;

        LocalDateTime ahora = LocalDateTime.now();
        if (expiresAt.isBefore(ahora)) return 0L;

        return java.time.Duration.between(ahora, expiresAt).getSeconds();
    }

    /**
     * Crear copia con nuevo timestamp
     */
    public TokenResponse conNuevoTimestamp() {
        this.timestamp = LocalDateTime.now().toString();
        return this;
    }
}