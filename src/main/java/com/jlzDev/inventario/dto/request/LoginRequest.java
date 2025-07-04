package com.jlzDev.inventario.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para requests de login
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginRequest {

    @NotBlank(message = "Username/email es requerido")
    @Size(min = 3, max = 100, message = "Username/email debe tener entre 3 y 100 caracteres")
    private String username;

    @NotBlank(message = "Password es requerido")
    @Size(min = 6, max = 100, message = "Password debe tener entre 6 y 100 caracteres")
    private String password;

    private String deviceInfo;
    private boolean rememberMe;
    private boolean extendedSession;

    /**
     * Normalizar datos del request
     */
    public void normalizar() {
        if (username != null) {
            username = username.trim().toLowerCase();
        }
        if (deviceInfo != null) {
            deviceInfo = deviceInfo.trim();
        }
    }

    /**
     * Verificar si las credenciales son válidas
     */
    public boolean esCredencialValida() {
        return username != null && !username.trim().isEmpty() &&
                password != null && !password.trim().isEmpty();
    }

    /**
     * Verificar si es sesión extendida
     */
    public boolean esSesionExtendida() {
        return extendedSession || rememberMe;
    }

    /**
     * Verificar si tiene información de dispositivo
     */
    public boolean tieneInfoDispositivo() {
        return deviceInfo != null && !deviceInfo.trim().isEmpty();
    }

    /**
     * Crear resumen para logs (sin password)
     */
    public String getResumenParaLog() {
        return String.format("username=%s, deviceInfo=%s, rememberMe=%s",
                username, deviceInfo, rememberMe);
    }
}