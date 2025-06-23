package com.jlzDev.inventario.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.jlzDev.inventario.entity.Categoria;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO para respuestas de consultas de categorías
 * Contiene toda la información que se puede exponer al cliente
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL) // No incluir campos null en JSON
public class CategoriaResponse {

    /**
     * ID único de la categoría
     */
    private Long id;

    /**
     * Nombre de la categoría
     */
    private String nombre;

    /**
     * Descripción de la categoría
     */
    private String descripcion;

    /**
     * Fecha y hora de creación de la categoría
     * Formato: yyyy-MM-dd HH:mm:ss
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime fechaCreacion;

    /**
     * Cantidad total de productos en esta categoría
     * Se incluye solo en consultas detalladas
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Long cantidadProductos;

    /**
     * Cantidad de productos con stock disponible (stock > 0)
     * Se incluye solo en consultas detalladas
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Long productosConStock;

    /**
     * Cantidad de productos con stock bajo (stock <= stock mínimo)
     * Se incluye solo en consultas detalladas
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Long productosStockBajo;

    /**
     * Valor total del inventario en esta categoría
     * Se incluye solo en consultas administrativas
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Double valorInventario;

    /**
     * Indica si la categoría se puede eliminar
     * Depende de si tiene productos asociados
     */
    private Boolean eliminable;

    /**
     * Indica si es una categoría del sistema o predefinida
     * Las categorías del sistema son más difíciles de eliminar
     */
    private Boolean esCategoriaComun;

    /**
     * Color sugerido para la UI (basado en el tipo de categoría)
     * Útil para iconos y visualización en frontend
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String colorSugerido;

    /**
     * Icono sugerido para la UI (basado en el tipo de categoría)
     * Útil para mostrar iconos representativos
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String iconoSugerido;

    /**
     * Constructor de conveniencia para crear desde Entity
     */
    public static CategoriaResponse fromEntity(Categoria categoria) {
        if (categoria == null) {
            return null;
        }

        return CategoriaResponse.builder()
                .id(categoria.getId())
                .nombre(categoria.getNombre())
                .descripcion(categoria.getDescripcion())
                .fechaCreacion(categoria.getFechaCreacion())
                .esCategoriaComun(determinarSiEsComun(categoria.getNombre()))
                .colorSugerido(obtenerColorSugerido(categoria.getNombre()))
                .iconoSugerido(obtenerIconoSugerido(categoria.getNombre()))
                .eliminable(true) // Se actualizará con información real
                .build();
    }

    /**
     * Constructor de conveniencia para crear desde Entity con información adicional
     */
    public static CategoriaResponse fromEntityConDetalles(Categoria categoria, Long cantidadProductos,
                                                          Long productosConStock, Long productosStockBajo,
                                                          Double valorInventario) {
        CategoriaResponse response = fromEntity(categoria);

        if (response != null) {
            response.setCantidadProductos(cantidadProductos);
            response.setProductosConStock(productosConStock);
            response.setProductosStockBajo(productosStockBajo);
            response.setValorInventario(valorInventario);
            response.setEliminable(determinarSiEsEliminable(cantidadProductos));
        }

        return response;
    }

    /**
     * Constructor simplificado para listas
     */
    public static CategoriaResponse simple(Categoria categoria) {
        if (categoria == null) {
            return null;
        }

        return CategoriaResponse.builder()
                .id(categoria.getId())
                .nombre(categoria.getNombre())
                .descripcion(categoria.getDescripcion())
                .esCategoriaComun(determinarSiEsComun(categoria.getNombre()))
                .build();
    }

    /**
     * Constructor para selectores/dropdown (solo id y nombre)
     */
    public static CategoriaResponse selector(Categoria categoria) {
        if (categoria == null) {
            return null;
        }

        return CategoriaResponse.builder()
                .id(categoria.getId())
                .nombre(categoria.getNombre())
                .build();
    }

    /**
     * Determinar si es una categoría común/estándar
     */
    private static Boolean determinarSiEsComun(String nombre) {
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
     * Determinar si la categoría se puede eliminar
     */
    private static Boolean determinarSiEsEliminable(Long cantidadProductos) {
        return cantidadProductos == null || cantidadProductos == 0;
    }

    /**
     * Obtener color sugerido basado en el nombre de la categoría
     */
    private static String obtenerColorSugerido(String nombre) {
        if (nombre == null) {
            return "#6B7280"; // Gris por defecto
        }

        String nombreLower = nombre.toLowerCase();

        if (nombreLower.contains("electrón") || nombreLower.contains("tecnolog")) {
            return "#3B82F6"; // Azul
        } else if (nombreLower.contains("ropa") || nombreLower.contains("vestim")) {
            return "#EC4899"; // Rosa
        } else if (nombreLower.contains("hogar") || nombreLower.contains("casa")) {
            return "#F59E0B"; // Amarillo/Naranja
        } else if (nombreLower.contains("deporte")) {
            return "#10B981"; // Verde
        } else if (nombreLower.contains("libro")) {
            return "#8B5CF6"; // Púrpura
        } else if (nombreLower.contains("aliment") || nombreLower.contains("comida")) {
            return "#EF4444"; // Rojo
        } else if (nombreLower.contains("belleza") || nombreLower.contains("cosmét")) {
            return "#F97316"; // Naranja
        } else if (nombreLower.contains("juguete")) {
            return "#06B6D4"; // Cian
        } else if (nombreLower.contains("oficina") || nombreLower.contains("papeler")) {
            return "#6366F1"; // Índigo
        } else if (nombreLower.contains("salud") || nombreLower.contains("medicin")) {
            return "#84CC16"; // Lima
        }

        return "#6B7280"; // Gris por defecto
    }

    /**
     * Obtener icono sugerido basado en el nombre de la categoría
     */
    private static String obtenerIconoSugerido(String nombre) {
        if (nombre == null) {
            return "tag";
        }

        String nombreLower = nombre.toLowerCase();

        if (nombreLower.contains("electrón") || nombreLower.contains("tecnolog")) {
            return "smartphone";
        } else if (nombreLower.contains("ropa") || nombreLower.contains("vestim")) {
            return "shirt";
        } else if (nombreLower.contains("hogar") || nombreLower.contains("casa")) {
            return "home";
        } else if (nombreLower.contains("deporte")) {
            return "dumbbell";
        } else if (nombreLower.contains("libro")) {
            return "book";
        } else if (nombreLower.contains("aliment") || nombreLower.contains("comida")) {
            return "utensils";
        } else if (nombreLower.contains("belleza") || nombreLower.contains("cosmét")) {
            return "sparkles";
        } else if (nombreLower.contains("juguete")) {
            return "gamepad-2";
        } else if (nombreLower.contains("oficina") || nombreLower.contains("papeler")) {
            return "briefcase";
        } else if (nombreLower.contains("salud") || nombreLower.contains("medicin")) {
            return "heart-pulse";
        }

        return "tag";
    }

    /**
     * Obtener estado de la categoría como texto
     */
    public String getEstadoTexto() {
        if (cantidadProductos == null) {
            return "Sin información";
        }

        if (cantidadProductos == 0) {
            return "Sin productos";
        }

        if (productosStockBajo != null && productosStockBajo > 0) {
            return cantidadProductos + " productos (" + productosStockBajo + " con stock bajo)";
        }

        return cantidadProductos + " producto" + (cantidadProductos == 1 ? "" : "s");
    }

    /**
     * Obtener porcentaje de productos con stock
     */
    public Double getPorcentajeConStock() {
        if (cantidadProductos == null || cantidadProductos == 0) {
            return 0.0;
        }

        if (productosConStock == null) {
            return null;
        }

        return (productosConStock.doubleValue() / cantidadProductos.doubleValue()) * 100;
    }

    /**
     * Obtener porcentaje de productos con stock bajo
     */
    public Double getPorcentajeStockBajo() {
        if (cantidadProductos == null || cantidadProductos == 0) {
            return 0.0;
        }

        if (productosStockBajo == null) {
            return null;
        }

        return (productosStockBajo.doubleValue() / cantidadProductos.doubleValue()) * 100;
    }

    /**
     * Verificar si tiene productos
     */
    public boolean tieneProductos() {
        return cantidadProductos != null && cantidadProductos > 0;
    }

    /**
     * Verificar si tiene descripción
     */
    public boolean tieneDescripcion() {
        return descripcion != null && !descripcion.trim().isEmpty();
    }

    /**
     * Verificar si necesita atención (productos con stock bajo)
     */
    public boolean necesitaAtencion() {
        return productosStockBajo != null && productosStockBajo > 0;
    }

    /**
     * Obtener valor promedio por producto
     */
    public Double getValorPromedioPorProducto() {
        if (cantidadProductos == null || cantidadProductos == 0 || valorInventario == null) {
            return null;
        }

        return valorInventario / cantidadProductos;
    }

    @Override
    public String toString() {
        return "CategoriaResponse{" +
                "id=" + id +
                ", nombre='" + nombre + '\'' +
                ", descripcion='" + (descripcion != null ?
                descripcion.substring(0, Math.min(descripcion.length(), 30)) + "..." : "null") + '\'' +
                ", cantidadProductos=" + cantidadProductos +
                ", eliminable=" + eliminable +
                '}';
    }
}