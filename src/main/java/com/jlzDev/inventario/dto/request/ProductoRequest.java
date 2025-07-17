package com.jlzDev.inventario.dto.request;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * DTO para solicitudes de creación y actualización de productos
 * Contiene solo los campos que el cliente puede enviar
 * Nota: El stock actual se maneja por movimientos, no por este DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductoRequest {

    /**
     * Nombre del producto (obligatorio)
     * Se capitalizará apropiadamente en el service
     */
    @NotBlank(message = "El nombre del producto es obligatorio")
    @Size(min = 2, max = 150, message = "El nombre del producto debe tener entre 2 y 150 caracteres")
    private String nombre;

    /**
     * Descripción detallada del producto (opcional)
     * Puede ser null o vacío
     */
    @Size(max = 1000, message = "La descripción no puede exceder 1000 caracteres")
    private String descripcion;

    /**
     * URL de la imagen del producto (opcional)
     * Debe ser una URL válida si se proporciona
     */
    @Size(max = 500, message = "La URL de la imagen no puede exceder 500 caracteres")
    @Pattern(regexp = "^(https?://)?(www\\.)?[a-zA-Z0-9][a-zA-Z0-9-]{1,61}[a-zA-Z0-9]\\.[a-zA-Z]{2,}(/.*)?$|^$",
            message = "La imagen debe ser una URL válida")
    private String imagen;

    /**
     * Precio del producto (obligatorio)
     * Debe ser positivo y tener máximo 2 decimales
     */
    @NotNull(message = "El precio es obligatorio")
    @DecimalMin(value = "0.01", message = "El precio debe ser mayor a 0")
    @DecimalMax(value = "99999999.99", message = "El precio no puede exceder 99,999,999.99")
    @Digits(integer = 8, fraction = 2, message = "El precio debe tener máximo 8 dígitos enteros y 2 decimales")
    private BigDecimal precio;

    /**
     * Stock mínimo para alertas (opcional)
     * Por defecto será 0 si no se especifica
     */
    @Min(value = 0, message = "El stock mínimo no puede ser negativo")
    @Max(value = 1000000, message = "El stock mínimo no puede exceder 1,000,000")
    private Integer stockMinimo;

    /**
     * ID de la categoría a la que pertenece el producto (obligatorio)
     */
    @NotNull(message = "La categoría es obligatoria")
    @Positive(message = "El ID de la categoría debe ser un número positivo")
    private Long categoriaId;

    /**
     * Método para normalizar los datos antes de enviar al service
     */
    public ProductoRequest normalizar() {
        if (this.nombre != null) {
            // Capitalizar nombre del producto apropiadamente
            this.nombre = capitalizarNombreProducto(this.nombre.trim());
        }

        if (this.descripcion != null && !this.descripcion.trim().isEmpty()) {
            this.descripcion = this.descripcion.trim();
            // Capitalizar primera letra de la descripción
            this.descripcion = capitalizarDescripcion(this.descripcion);
        } else {
            this.descripcion = null; // Convertir string vacío a null
        }

        // Normalizar imagen
        if (this.imagen != null && !this.imagen.trim().isEmpty()) {
            this.imagen = this.imagen.trim();
            // Añadir protocolo https si no está presente
            if (!this.imagen.startsWith("http://") && !this.imagen.startsWith("https://")) {
                this.imagen = "https://" + this.imagen;
            }
        } else {
            this.imagen = null; // Convertir string vacío a null
        }

        // Establecer stock mínimo como 0 por defecto si no se especifica
        if (this.stockMinimo == null) {
            this.stockMinimo = 0;
        }

        // Redondear precio a 2 decimales si es necesario
        if (this.precio != null) {
            this.precio = this.precio.setScale(2, BigDecimal.ROUND_HALF_UP);
        }

        return this;
    }

    /**
     * Validar que el nombre no contenga caracteres especiales peligrosos
     * Permite caracteres comunes en nombres de productos
     */
    public boolean isNombreValido() {
        if (nombre == null || nombre.trim().isEmpty()) {
            return false;
        }

        // Patrón para validar: letras, números, espacios, guiones, puntos, paréntesis, comillas
        return nombre.trim().matches("^[a-zA-ZáéíóúÁÉÍÓÚñÑ0-9\\s\\-_.,()\"'/&%°]+$");
    }

    /**
     * Verificar si el precio está en un rango razonable
     * Útil para detectar posibles errores de entrada
     */
    public boolean isPrecioRazonable() {
        if (precio == null) {
            return false;
        }

        // Considerar razonable entre $0.01 y $1,000,000
        return precio.compareTo(new BigDecimal("0.01")) >= 0 &&
                precio.compareTo(new BigDecimal("1000000.00")) <= 0;
    }

    /**
     * Verificar si el producto es considerado "premium" basado en el precio
     */
    public boolean esProductoPremium() {
        if (precio == null) {
            return false;
        }

        // Considerar premium si el precio es mayor a $1000
        return precio.compareTo(new BigDecimal("1000.00")) > 0;
    }

    /**
     * Verificar si la descripción está presente y no vacía
     */
    public boolean tieneDescripcion() {
        return descripcion != null && !descripcion.trim().isEmpty();
    }

    /**
     * Verificar si tiene imagen configurada
     */
    public boolean tieneImagen() {
        return imagen != null && !imagen.trim().isEmpty();
    }

    /**
     * Verificar si tiene stock mínimo configurado (mayor a 0)
     */
    public boolean tieneStockMinimoConfigurado() {
        return stockMinimo != null && stockMinimo > 0;
    }

    /**
     * Verificar si la URL de imagen parece válida
     */
    public boolean esImagenValida() {
        if (!tieneImagen()) {
            return true; // null es válido
        }

        try {
            return imagen.matches("^https?://.*\\.(jpg|jpeg|png|gif|webp|svg).*$");
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Obtener extensión de la imagen
     */
    public String getExtensionImagen() {
        if (!tieneImagen()) {
            return null;
        }

        int lastDot = imagen.lastIndexOf('.');
        if (lastDot > 0 && lastDot < imagen.length() - 1) {
            return imagen.substring(lastDot + 1).toLowerCase();
        }

        return null;
    }

    /**
     * Obtener categoría de precio basada en el valor
     */
    public String getCategoriaPrecio() {
        if (precio == null) {
            return "Sin precio";
        }

        if (precio.compareTo(new BigDecimal("50.00")) <= 0) {
            return "Económico";
        } else if (precio.compareTo(new BigDecimal("200.00")) <= 0) {
            return "Intermedio";
        } else if (precio.compareTo(new BigDecimal("1000.00")) <= 0) {
            return "Alto";
        } else {
            return "Premium";
        }
    }

    /**
     * Validar que el nombre del producto sea descriptivo
     * Debe tener al menos 2 palabras para ser considerado descriptivo
     */
    public boolean esNombreDescriptivo() {
        if (nombre == null || nombre.trim().isEmpty()) {
            return false;
        }

        String[] palabras = nombre.trim().split("\\s+");
        return palabras.length >= 2;
    }

    /**
     * Detectar si podría ser un código de barras en lugar de un nombre
     */
    public boolean pareceCodigoBarras() {
        if (nombre == null) {
            return false;
        }

        // Si es solo números y tiene más de 8 dígitos, podría ser código de barras
        return nombre.trim().matches("^\\d{8,}$");
    }

    /**
     * Capitalizar nombre del producto manteniendo formato comercial
     * Ejemplo: "iphone 15 pro max" -> "iPhone 15 Pro Max"
     */
    private String capitalizarNombreProducto(String nombre) {
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

            // Casos especiales para marcas y modelos comunes
            if (palabra.equals("iphone") || palabra.equals("ipad") || palabra.equals("imac")) {
                resultado.append("i").append(Character.toUpperCase(palabra.charAt(1)))
                        .append(palabra.substring(2));
            } else if (palabra.equals("pro") || palabra.equals("max") || palabra.equals("mini") ||
                    palabra.equals("plus") || palabra.equals("air") || palabra.equals("ultra")) {
                resultado.append(Character.toUpperCase(palabra.charAt(0)))
                        .append(palabra.substring(1));
            } else if (palabra.matches("^\\d+.*")) {
                // Si empieza con número, mantener como está
                resultado.append(palabra);
            } else if (palabra.length() > 0) {
                // Capitalizar primera letra
                resultado.append(Character.toUpperCase(palabra.charAt(0)));
                if (palabra.length() > 1) {
                    resultado.append(palabra.substring(1));
                }
            }
        }

        return resultado.toString();
    }

    /**
     * Capitalizar primera letra de la descripción
     */
    private String capitalizarDescripcion(String descripcion) {
        if (descripcion == null || descripcion.trim().isEmpty()) {
            return descripcion;
        }

        String desc = descripcion.trim();
        if (desc.length() > 0) {
            return Character.toUpperCase(desc.charAt(0)) +
                    (desc.length() > 1 ? desc.substring(1) : "");
        }

        return desc;
    }

    /**
     * Obtener resumen para logs (sin información sensible)
     */
    public String getResumenParaLog() {
        return String.format("ProductoRequest{nombre='%s', precio=%s, categoriaId=%d, tieneImagen=%s}",
                nombre, precio, categoriaId, tieneImagen());
    }

    /**
     * Validar consistency de datos
     */
    public boolean esConsistente() {
        // Verificaciones básicas de consistencia
        if (nombre != null && pareceCodigoBarras()) {
            return false; // Nombre parece código de barras
        }

        if (!isPrecioRazonable()) {
            return false; // Precio fuera de rango razonable
        }

        if (stockMinimo != null && stockMinimo > 10000) {
            return false; // Stock mínimo excesivamente alto
        }

        // Validar imagen si está presente
        if (tieneImagen() && !esImagenValida()) {
            return false; // URL de imagen inválida
        }

        return true;
    }

    @Override
    public String toString() {
        return "ProductoRequest{" +
                "nombre='" + nombre + '\'' +
                ", descripcion='" + (descripcion != null ?
                descripcion.substring(0, Math.min(descripcion.length(), 50)) + "..." : "null") + '\'' +
                ", imagen='" + (imagen != null ?
                imagen.substring(0, Math.min(imagen.length(), 50)) + "..." : "null") + '\'' +
                ", precio=" + precio +
                ", stockMinimo=" + stockMinimo +
                ", categoriaId=" + categoriaId +
                '}';
    }
}