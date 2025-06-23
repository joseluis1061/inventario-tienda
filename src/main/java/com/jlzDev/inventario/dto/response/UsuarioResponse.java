package com.jlzDev.inventario.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.jlzDev.inventario.entity.Usuario;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO para respuestas de consultas de usuarios
 * Contiene toda la información que se puede exponer al cliente (sin password)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL) // No incluir campos null en JSON
public class UsuarioResponse {

    /**
     * ID único del usuario
     */
    private Long id;

    /**
     * Username único del usuario
     */
    private String username;

    /**
     * Nombre completo del usuario
     */
    private String nombreCompleto;

    /**
     * Email del usuario (puede ser null)
     */
    private String email;

    /**
     * Estado activo/inactivo del usuario
     */
    private Boolean activo;

    /**
     * Fecha y hora de creación del usuario
     * Formato: yyyy-MM-dd HH:mm:ss
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime fechaCreacion;

    /**
     * Información básica del rol asignado
     * Solo incluye id, nombre y descripción
     */
    private RolInfo rol;

    /**
     * Cantidad de movimientos realizados por este usuario
     * Se incluye solo en consultas detalladas
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Long cantidadMovimientos;

    /**
     * Fecha del último movimiento realizado
     * Se incluye solo en consultas detalladas
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime fechaUltimoMovimiento;

    /**
     * Indica si el usuario se puede eliminar
     * Depende de si tiene movimientos asociados y si no es el admin principal
     */
    private Boolean eliminable;

    /**
     * Indica si es el usuario administrador principal del sistema
     */
    private Boolean esAdminPrincipal;

    /**
     * Clase interna para información básica del rol
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RolInfo {
        private Long id;
        private String nombre;
        private String descripcion;

        public static RolInfo fromRol(com.jlzDev.inventario.entity.Rol rol) {
            if (rol == null) {
                return null;
            }

            return RolInfo.builder()
                    .id(rol.getId())
                    .nombre(rol.getNombre())
                    .descripcion(rol.getDescripcion())
                    .build();
        }
    }

    /**
     * Constructor de conveniencia para crear desde Entity
     */
    public static UsuarioResponse fromEntity(Usuario usuario) {
        if (usuario == null) {
            return null;
        }

        return UsuarioResponse.builder()
                .id(usuario.getId())
                .username(usuario.getUsername())
                .nombreCompleto(usuario.getNombreCompleto())
                .email(usuario.getEmail())
                .activo(usuario.getActivo())
                .fechaCreacion(usuario.getFechaCreacion())
                .rol(RolInfo.fromRol(usuario.getRol()))
                .esAdminPrincipal(esAdministradorPrincipal(usuario.getUsername()))
                .eliminable(determinarSiEsEliminable(usuario.getUsername(), null))
                .build();
    }

    /**
     * Constructor de conveniencia para crear desde Entity con información adicional
     */
    public static UsuarioResponse fromEntityConDetalles(Usuario usuario, Long cantidadMovimientos, LocalDateTime fechaUltimoMovimiento) {
        UsuarioResponse response = fromEntity(usuario);

        if (response != null) {
            response.setCantidadMovimientos(cantidadMovimientos);
            response.setFechaUltimoMovimiento(fechaUltimoMovimiento);
            response.setEliminable(determinarSiEsEliminable(usuario.getUsername(), cantidadMovimientos));
        }

        return response;
    }

    /**
     * Constructor simplificado para listas (sin información adicional)
     */
    public static UsuarioResponse simple(Usuario usuario) {
        if (usuario == null) {
            return null;
        }

        return UsuarioResponse.builder()
                .id(usuario.getId())
                .username(usuario.getUsername())
                .nombreCompleto(usuario.getNombreCompleto())
                .activo(usuario.getActivo())
                .rol(RolInfo.fromRol(usuario.getRol()))
                .build();
    }

    /**
     * Constructor para información pública (sin email ni detalles sensibles)
     */
    public static UsuarioResponse publico(Usuario usuario) {
        if (usuario == null) {
            return null;
        }

        return UsuarioResponse.builder()
                .id(usuario.getId())
                .username(usuario.getUsername())
                .nombreCompleto(usuario.getNombreCompleto())
                .rol(RolInfo.builder()
                        .id(usuario.getRol().getId())
                        .nombre(usuario.getRol().getNombre())
                        .build())
                .build();
    }

    /**
     * Determinar si es el administrador principal
     */
    private static Boolean esAdministradorPrincipal(String username) {
        return "admin".equalsIgnoreCase(username);
    }

    /**
     * Determinar si el usuario se puede eliminar
     */
    private static Boolean determinarSiEsEliminable(String username, Long cantidadMovimientos) {
        // No se puede eliminar el admin principal
        if (esAdministradorPrincipal(username)) {
            return false;
        }

        // No se puede eliminar si tiene movimientos asociados
        if (cantidadMovimientos != null && cantidadMovimientos > 0) {
            return false;
        }

        return true;
    }

    /**
     * Obtener estado del usuario como texto
     */
    public String getEstadoTexto() {
        if (Boolean.TRUE.equals(esAdminPrincipal)) {
            return "Administrador Principal";
        }

        if (Boolean.FALSE.equals(activo)) {
            return "Inactivo";
        }

        if (cantidadMovimientos != null && cantidadMovimientos > 0) {
            return "Activo (" + cantidadMovimientos + " movimientos)";
        }

        return "Activo";
    }

    /**
     * Obtener nombre del rol
     */
    public String getNombreRol() {
        return rol != null ? rol.getNombre() : "Sin rol";
    }

    /**
     * Verificar si tiene email configurado
     */
    public boolean tieneEmail() {
        return email != null && !email.trim().isEmpty();
    }

    /**
     * Verificar si está activo
     */
    public boolean estaActivo() {
        return Boolean.TRUE.equals(activo);
    }

    /**
     * Verificar si tiene movimientos
     */
    public boolean tieneMovimientos() {
        return cantidadMovimientos != null && cantidadMovimientos > 0;
    }

    /**
     * Verificar si es administrador (cualquier tipo)
     */
    public boolean esAdministrador() {
        return rol != null && "ADMIN".equalsIgnoreCase(rol.getNombre());
    }

    /**
     * Verificar si es gerente
     */
    public boolean esGerente() {
        return rol != null && "GERENTE".equalsIgnoreCase(rol.getNombre());
    }

    /**
     * Verificar si es empleado
     */
    public boolean esEmpleado() {
        return rol != null && "EMPLEADO".equalsIgnoreCase(rol.getNombre());
    }

    /**
     * Obtener iniciales del nombre completo
     */
    public String getIniciales() {
        if (nombreCompleto == null || nombreCompleto.trim().isEmpty()) {
            return username != null ? username.substring(0, Math.min(2, username.length())).toUpperCase() : "??";
        }

        String[] palabras = nombreCompleto.trim().split("\\s+");
        StringBuilder iniciales = new StringBuilder();

        for (int i = 0; i < Math.min(2, palabras.length); i++) {
            if (palabras[i].length() > 0) {
                iniciales.append(Character.toUpperCase(palabras[i].charAt(0)));
            }
        }

        return iniciales.length() > 0 ? iniciales.toString() : "??";
    }

    @Override
    public String toString() {
        return "UsuarioResponse{" +
                "id=" + id +
                ", username='" + username + '\'' +
                ", nombreCompleto='" + nombreCompleto + '\'' +
                ", activo=" + activo +
                ", rol=" + (rol != null ? rol.getNombre() : "null") +
                ", cantidadMovimientos=" + cantidadMovimientos +
                '}';
    }
}