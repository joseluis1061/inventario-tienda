package com.jlzDev.inventario.dto.request;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para solicitudes de creación y actualización de usuarios
 * Excluye campos como ID, fechaCreacion y password (se maneja por separado)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UsuarioRequest {

    /**
     * Username único del usuario (obligatorio)
     * Se convertirá automáticamente a minúsculas en el service
     */
    @NotBlank(message = "El username es obligatorio")
    @Size(min = 3, max = 20, message = "El username debe tener entre 3 y 20 caracteres")
    @Pattern(regexp = "^[a-zA-Z0-9._-]+$",
            message = "El username solo puede contener letras, números, puntos, guiones y guiones bajos")
    private String username;

    /**
     * Nombre completo del usuario (obligatorio)
     */
    @NotBlank(message = "El nombre completo es obligatorio")
    @Size(min = 2, max = 100, message = "El nombre completo debe tener entre 2 y 100 caracteres")
    @Pattern(regexp = "^[a-zA-ZáéíóúÁÉÍÓÚñÑ\\s]+$",
            message = "El nombre completo solo puede contener letras y espacios")
    private String nombreCompleto;

    /**
     * Email del usuario (opcional pero debe ser válido si se proporciona)
     */
    @Email(message = "El formato del email no es válido")
    @Size(max = 100, message = "El email no puede exceder 100 caracteres")
    private String email;

    /**
     * Estado activo/inactivo del usuario
     * Por defecto será true si no se especifica
     */
    private Boolean activo;

    /**
     * ID del rol asignado al usuario (obligatorio)
     */
    @NotNull(message = "El rol es obligatorio")
    @Positive(message = "El ID del rol debe ser un número positivo")
    private Long rolId;

    /**
     * Método para normalizar los datos antes de enviar al service
     */
    public UsuarioRequest normalizar() {
        if (this.username != null) {
            this.username = this.username.trim().toLowerCase();
        }

        if (this.nombreCompleto != null) {
            // Capitalizar nombre completo (Primera letra de cada palabra en mayúscula)
            this.nombreCompleto = capitalizarNombreCompleto(this.nombreCompleto.trim());
        }

        if (this.email != null && !this.email.trim().isEmpty()) {
            this.email = this.email.trim().toLowerCase();
        } else {
            this.email = null; // Convertir string vacío a null
        }

        // Establecer activo como true por defecto si no se especifica
        if (this.activo == null) {
            this.activo = true;
        }

        return this;
    }

    /**
     * Validar que el username no contenga palabras reservadas
     */
    public boolean isUsernameValido() {
        if (username == null || username.trim().isEmpty()) {
            return false;
        }

        String usernameLower = username.toLowerCase();

        // Lista de usernames reservados
        String[] reservados = {"admin", "root", "system", "user", "test", "null", "undefined"};

        for (String reservado : reservados) {
            if (usernameLower.equals(reservado)) {
                return false;
            }
        }

        return true;
    }

    /**
     * Validar que el nombre completo tenga al menos nombre y apellido
     */
    public boolean tieneNombreCompleto() {
        if (nombreCompleto == null || nombreCompleto.trim().isEmpty()) {
            return false;
        }

        // Verificar que tenga al menos 2 palabras (nombre y apellido)
        String[] palabras = nombreCompleto.trim().split("\\s+");
        return palabras.length >= 2;
    }

    /**
     * Verificar si el email está presente y no vacío
     */
    public boolean tieneEmail() {
        return email != null && !email.trim().isEmpty();
    }

    /**
     * Verificar si el usuario será creado como activo
     */
    public boolean esActivo() {
        return Boolean.TRUE.equals(activo);
    }

    /**
     * Capitalizar nombre completo (Primera letra de cada palabra en mayúscula)
     */
    private String capitalizarNombreCompleto(String nombre) {
        if (nombre == null || nombre.trim().isEmpty()) {
            return nombre;
        }

        StringBuilder resultado = new StringBuilder();
        String[] palabras = nombre.trim().split("\\s+");

        for (int i = 0; i < palabras.length; i++) {
            if (i > 0) {
                resultado.append(" ");
            }

            String palabra = palabras[i].toLowerCase();
            if (palabra.length() > 0) {
                resultado.append(Character.toUpperCase(palabra.charAt(0)));
                if (palabra.length() > 1) {
                    resultado.append(palabra.substring(1));
                }
            }
        }

        return resultado.toString();
    }

    /**
     * Obtener resumen para logs (sin información sensible)
     */
    public String getResumenParaLog() {
        return String.format("UsuarioRequest{username='%s', nombreCompleto='%s', rolId=%d, activo=%s}",
                username, nombreCompleto, rolId, activo);
    }

    @Override
    public String toString() {
        return "UsuarioRequest{" +
                "username='" + username + '\'' +
                ", nombreCompleto='" + nombreCompleto + '\'' +
                ", email='" + (email != null ? email.replaceAll("(.{2}).*(@.*)", "$1***$2") : "null") + '\'' +
                ", activo=" + activo +
                ", rolId=" + rolId +
                '}';
    }
}