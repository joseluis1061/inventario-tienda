package com.jlzDev.inventario.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para solicitudes de creación y actualización de roles
 * Contiene solo los campos que el cliente puede enviar
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RolRequest {

    /**
     * Nombre del rol (obligatorio)
     * Se convertirá automáticamente a mayúsculas en el service
     */
    @NotBlank(message = "El nombre del rol es obligatorio")
    @Size(min = 2, max = 50, message = "El nombre del rol debe tener entre 2 y 50 caracteres")
    private String nombre;

    /**
     * Descripción del rol (opcional)
     * Puede ser null o vacío
     */
    @Size(max = 255, message = "La descripción no puede exceder 255 caracteres")
    private String descripcion;

    /**
     * Método para normalizar los datos antes de enviar al service
     * Limpia espacios en blanco y convierte nombre a uppercase
     */
    public RolRequest normalizar() {
        if (this.nombre != null) {
            this.nombre = this.nombre.trim();
        }

        if (this.descripcion != null && !this.descripcion.trim().isEmpty()) {
            this.descripcion = this.descripcion.trim();
        } else {
            this.descripcion = null; // Convertir string vacío a null
        }

        return this;
    }

    /**
     * Validar que el nombre no contenga caracteres especiales peligrosos
     * Solo letras, números, espacios y guiones
     */
    public boolean isNombreValido() {
        if (nombre == null || nombre.trim().isEmpty()) {
            return false;
        }

        // Patrón para validar: solo letras, números, espacios y guiones
        return nombre.trim().matches("^[a-zA-ZáéíóúÁÉÍÓÚñÑ0-9\\s\\-_]+$");
    }

    /**
     * Verificar si la descripción está presente y no vacía
     */
    public boolean tieneDescripcion() {
        return descripcion != null && !descripcion.trim().isEmpty();
    }

    /**
     * Obtener resumen para logs (método que faltaba)
     */
    public String getResumenParaLog() {
        return String.format("RolRequest{nombre='%s', tieneDescripcion=%s}",
                nombre, tieneDescripcion());
    }

    @Override
    public String toString() {
        return "RolRequest{" +
                "nombre='" + nombre + '\'' +
                ", descripcion='" + (descripcion != null ?
                descripcion.substring(0, Math.min(descripcion.length(), 50)) + "..." : "null") + '\'' +
                '}';
    }
}