package com.jlzDev.inventario.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para requests de refresh token
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RefreshTokenRequest {

    @NotBlank(message = "Refresh token es requerido")
    private String refreshToken;

    private String username; // Opcional para validación adicional
    private String deviceInfo;
    private boolean extendSession; // Si quiere extender la sesión

    /**
     * Normalizar datos del request
     */
    public void normalizar() {
        if (refreshToken != null) {
            refreshToken = refreshToken.trim();
        }
        if (username != null) {
            username = username.trim().toLowerCase();
        }
        if (deviceInfo != null) {
            deviceInfo = deviceInfo.trim();
        }
    }

    /**
     * Verificar si el request es válido
     */
    public boolean esRequestValido() {
        return refreshToken != null && !refreshToken.trim().isEmpty();
    }

    /**
     * Verificar si tiene username para validación
     */
    public boolean tieneUsername() {
        return username != null && !username.trim().isEmpty();
    }

    /**
     * Verificar si solicita extensión de sesión
     */
    public boolean solicitaExtensionSesion() {
        return extendSession;
    }

    /**
     * Crear resumen para logs
     */
    public String getResumenParaLog() {
        return String.format("username=%s, extendSession=%s", username, extendSession);
    }
}
