package com.jlzDev.inventario.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para solicitudes de creación y actualización de categorías
 * Contiene solo los campos que el cliente puede enviar
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CategoriaRequest {

    /**
     * Nombre de la categoría (obligatorio)
     * Se mantendrá el formato original pero limpio
     */
    @NotBlank(message = "El nombre de la categoría es obligatorio")
    @Size(min = 2, max = 100, message = "El nombre de la categoría debe tener entre 2 y 100 caracteres")
    private String nombre;

    /**
     * Descripción de la categoría (opcional)
     * Puede ser null o vacío
     */
    @Size(max = 255, message = "La descripción no puede exceder 255 caracteres")
    private String descripcion;

    /**
     * Método para normalizar los datos antes de enviar al service
     * Limpia espacios en blanco y formatea el nombre apropiadamente
     */
    public CategoriaRequest normalizar() {
        if (this.nombre != null) {
            // Capitalizar primera letra de cada palabra manteniendo formato natural
            this.nombre = capitalizarNombre(this.nombre.trim());
        }

        if (this.descripcion != null && !this.descripcion.trim().isEmpty()) {
            this.descripcion = this.descripcion.trim();
            // Capitalizar primera letra de la descripción
            this.descripcion = capitalizarDescripcion(this.descripcion);
        } else {
            this.descripcion = null; // Convertir string vacío a null
        }

        return this;
    }

    /**
     * Validar que el nombre no contenga caracteres especiales peligrosos
     * Permite letras, números, espacios, guiones y algunos caracteres especiales comunes
     */
    public boolean isNombreValido() {
        if (nombre == null || nombre.trim().isEmpty()) {
            return false;
        }

        // Patrón para validar: letras, números, espacios, guiones, ampersand, paréntesis
        return nombre.trim().matches("^[a-zA-ZáéíóúÁÉÍÓÚñÑ0-9\\s\\-_&().]+$");
    }

    /**
     * Verificar si el nombre contiene solo letras (sin números ni símbolos)
     * Útil para validaciones específicas de categorías
     */
    public boolean esSoloLetras() {
        if (nombre == null || nombre.trim().isEmpty()) {
            return false;
        }

        return nombre.trim().matches("^[a-zA-ZáéíóúÁÉÍÓÚñÑ\\s]+$");
    }

    /**
     * Verificar si la descripción está presente y no vacía
     */
    public boolean tieneDescripcion() {
        return descripcion != null && !descripcion.trim().isEmpty();
    }

    /**
     * Verificar si el nombre es una categoría común/estándar
     * Útil para sugerir descripciones automáticas
     */
    public boolean esCategoriaComun() {
        if (nombre == null) {
            return false;
        }

        String nombreLower = nombre.toLowerCase();
        String[] categoriasComunes = {
                "electrónicos", "electronics", "tecnología", "technology",
                "ropa", "clothing", "vestimenta", "apparel",
                "hogar", "home", "casa", "house",
                "deportes", "sports", "deporte",
                "libros", "books", "literatura",
                "alimentación", "food", "comida", "alimentos",
                "belleza", "beauty", "cosmética", "cosmeticos",
                "juguetes", "toys", "juguetería",
                "oficina", "office", "papelería", "stationery",
                "salud", "health", "medicina", "medical"
        };

        for (String categoria : categoriasComunes) {
            if (nombreLower.contains(categoria)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Obtener sugerencia de descripción basada en el nombre
     * Solo si es una categoría común
     */
    public String getSugerenciaDescripcion() {
        if (!esCategoriaComun() || nombre == null) {
            return null;
        }

        String nombreLower = nombre.toLowerCase();

        if (nombreLower.contains("electrón") || nombreLower.contains("tecnolog")) {
            return "Dispositivos electrónicos, tecnología y gadgets";
        } else if (nombreLower.contains("ropa") || nombreLower.contains("vestim")) {
            return "Prendas de vestir y accesorios de moda";
        } else if (nombreLower.contains("hogar") || nombreLower.contains("casa")) {
            return "Artículos y decoración para el hogar";
        } else if (nombreLower.contains("deporte")) {
            return "Equipos y artículos deportivos";
        } else if (nombreLower.contains("libro")) {
            return "Libros y material de lectura";
        } else if (nombreLower.contains("aliment") || nombreLower.contains("comida")) {
            return "Productos alimenticios y bebidas";
        } else if (nombreLower.contains("belleza") || nombreLower.contains("cosmét")) {
            return "Productos de belleza y cuidado personal";
        } else if (nombreLower.contains("juguete")) {
            return "Juguetes y entretenimiento infantil";
        } else if (nombreLower.contains("oficina") || nombreLower.contains("papeler")) {
            return "Artículos de oficina y papelería";
        } else if (nombreLower.contains("salud") || nombreLower.contains("medicin")) {
            return "Productos de salud y medicina";
        }

        return "Categoría de productos " + nombre.toLowerCase();
    }

    /**
     * Capitalizar nombre manteniendo formato natural
     * Ejemplo: "electrónicos y gadgets" -> "Electrónicos y Gadgets"
     */
    private String capitalizarNombre(String nombre) {
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

            // Palabras que deben permanecer en minúsculas (artículos, preposiciones)
            String[] minusculas = {"y", "e", "o", "u", "de", "del", "la", "el", "en", "con", "para", "por"};
            boolean esMinuscula = false;

            for (String min : minusculas) {
                if (palabra.equals(min) && i > 0) { // No aplicar a la primera palabra
                    esMinuscula = true;
                    break;
                }
            }

            if (esMinuscula) {
                resultado.append(palabra);
            } else if (palabra.length() > 0) {
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
     * Obtener resumen para logs
     */
    public String getResumenParaLog() {
        return String.format("CategoriaRequest{nombre='%s', tieneDescripcion=%s}",
                nombre, tieneDescripcion());
    }

    @Override
    public String toString() {
        return "CategoriaRequest{" +
                "nombre='" + nombre + '\'' +
                ", descripcion='" + (descripcion != null ?
                descripcion.substring(0, Math.min(descripcion.length(), 50)) + "..." : "null") + '\'' +
                '}';
    }
}