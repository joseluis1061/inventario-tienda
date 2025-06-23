package com.jlzDev.inventario.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.jlzDev.inventario.entity.Rol;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO para respuestas de consultas de roles
 * Contiene toda la información que se puede exponer al cliente
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL) // No incluir campos null en JSON
public class RolResponse {

    /**
     * ID único del rol
     */
    private Long id;

    /**
     * Nombre del rol (siempre en mayúsculas)
     */
    private String nombre;

    /**
     * Descripción del rol
     */
    private String descripcion;

    /**
     * Fecha y hora de creación del rol
     * Formato: yyyy-MM-dd HH:mm:ss
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime fechaCreacion;

    /**
     * Cantidad de usuarios activos asociados a este rol
     * Se incluye solo en consultas detalladas
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Long cantidadUsuarios;

    /**
     * Cantidad total de usuarios (activos + inactivos) asociados a este rol
     * Se incluye solo en consultas administrativas
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Long cantidadUsuariosTotal;

    /**
     * Indica si es un rol del sistema (ADMIN, GERENTE, EMPLEADO)
     * Los roles del sistema no se pueden eliminar
     */
    private Boolean esRolSistema;

    /**
     * Indica si el rol se puede eliminar
     * Depende de si es rol de sistema y si tiene usuarios asociados
     */
    private Boolean eliminable;

    /**
     * Constructor de conveniencia para crear desde Entity
     */
    public static RolResponse fromEntity(Rol rol) {
        if (rol == null) {
            return null;
        }

        return RolResponse.builder()
                .id(rol.getId())
                .nombre(rol.getNombre())
                .descripcion(rol.getDescripcion())
                .fechaCreacion(rol.getFechaCreacion())
                .esRolSistema(esRolDeSistema(rol.getNombre()))
                .build();
    }

    /**
     * Constructor de conveniencia para crear desde Entity con información adicional
     */
    public static RolResponse fromEntityConDetalles(Rol rol, Long cantidadUsuarios, Long cantidadUsuariosTotal) {
        RolResponse response = fromEntity(rol);

        if (response != null) {
            response.setCantidadUsuarios(cantidadUsuarios);
            response.setCantidadUsuariosTotal(cantidadUsuariosTotal);
            response.setEliminable(determinarSiEsEliminable(rol.getNombre(), cantidadUsuariosTotal));
        }

        return response;
    }

    /**
     * Constructor simplificado para listas
     */
    public static RolResponse simple(Rol rol) {
        if (rol == null) {
            return null;
        }

        return RolResponse.builder()
                .id(rol.getId())
                .nombre(rol.getNombre())
                .descripcion(rol.getDescripcion())
                .build();
    }

    /**
     * Determinar si es un rol del sistema
     */
    private static Boolean esRolDeSistema(String nombre) {
        if (nombre == null) {
            return false;
        }

        String nombreUpper = nombre.toUpperCase();
        return "ADMIN".equals(nombreUpper) ||
                "GERENTE".equals(nombreUpper) ||
                "EMPLEADO".equals(nombreUpper);
    }

    /**
     * Determinar si el rol se puede eliminar
     */
    private static Boolean determinarSiEsEliminable(String nombre, Long cantidadUsuarios) {
        // No se puede eliminar si es rol de sistema
        if (esRolDeSistema(nombre)) {
            return false;
        }

        // No se puede eliminar si tiene usuarios asociados
        if (cantidadUsuarios != null && cantidadUsuarios > 0) {
            return false;
        }

        return true;
    }

    /**
     * Obtener estado del rol como texto
     */
    public String getEstadoTexto() {
        if (Boolean.TRUE.equals(esRolSistema)) {
            return "Rol del Sistema";
        }

        if (cantidadUsuarios != null && cantidadUsuarios > 0) {
            return "En uso (" + cantidadUsuarios + " usuarios)";
        }

        return "Disponible";
    }

    /**
     * Verificar si tiene usuarios asociados
     */
    public boolean tieneUsuarios() {
        return cantidadUsuarios != null && cantidadUsuarios > 0;
    }

    /**
     * Verificar si tiene descripción
     */
    public boolean tieneDescripcion() {
        return descripcion != null && !descripcion.trim().isEmpty();
    }

    @Override
    public String toString() {
        return "RolResponse{" +
                "id=" + id +
                ", nombre='" + nombre + '\'' +
                ", descripcion='" + (descripcion != null ?
                descripcion.substring(0, Math.min(descripcion.length(), 30)) + "..." : "null") + '\'' +
                ", cantidadUsuarios=" + cantidadUsuarios +
                ", esRolSistema=" + esRolSistema +
                '}';
    }
}