package com.jlzDev.inventario.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.jlzDev.inventario.entity.Producto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO para respuestas de consultas de productos
 * Contiene toda la información que se puede exponer al cliente
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL) // No incluir campos null en JSON
public class ProductoResponse {

    /**
     * ID único del producto
     */
    private Long id;

    /**
     * Nombre del producto
     */
    private String nombre;

    /**
     * Descripción del producto
     */
    private String descripcion;

    /**
     * URL de la imagen del producto
     */
    private String imagen;

    /**
     * URL de imagen placeholder si no tiene imagen
     */
    private String imagenPlaceholder;

    /**
     * Precio del producto
     */
    private BigDecimal precio;

    /**
     * Stock actual disponible
     */
    private Integer stockActual;

    /**
     * Stock mínimo para alertas
     */
    private Integer stockMinimo;

    /**
     * Fecha y hora de creación del producto
     * Formato: yyyy-MM-dd HH:mm:ss
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime fechaCreacion;

    /**
     * Fecha y hora de última actualización
     * Formato: yyyy-MM-dd HH:mm:ss
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime fechaActualizacion;

    /**
     * Información básica de la categoría
     */
    private CategoriaInfo categoria;

    /**
     * Estado actual del stock (CRÍTICO, BAJO, NORMAL)
     * Calculado automáticamente
     */
    private String estadoStock;

    /**
     * Valor total del inventario (precio * stock actual)
     * Se incluye solo en consultas administrativas
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private BigDecimal valorInventario;

    /**
     * Cantidad total de movimientos (entradas + salidas)
     * Se incluye solo en consultas detalladas
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Long totalMovimientos;

    /**
     * Cantidad total de entradas
     * Se incluye solo en consultas detalladas
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Integer totalEntradas;

    /**
     * Cantidad total de salidas
     * Se incluye solo en consultas detalladas
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Integer totalSalidas;

    /**
     * Fecha del último movimiento
     * Se incluye solo en consultas detalladas
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime fechaUltimoMovimiento;

    /**
     * Indica si el producto se puede eliminar
     * Depende de si tiene movimientos asociados
     */
    private Boolean eliminable;

    /**
     * Categoría de precio (Económico, Intermedio, Alto, Premium)
     */
    private String categoriaPrecio;

    /**
     * Rotación del producto (Alta, Media, Baja, Sin movimientos)
     * Basada en la frecuencia de movimientos
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String rotacion;

    /**
     * Días desde el último movimiento
     * Útil para identificar productos sin rotación
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Long diasSinMovimiento;

    /**
     * Clase interna para información básica de la categoría
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CategoriaInfo {
        private Long id;
        private String nombre;
        private String descripcion;
        private String colorSugerido;
        private String iconoSugerido;

        public static CategoriaInfo fromCategoria(com.jlzDev.inventario.entity.Categoria categoria) {
            if (categoria == null) {
                return null;
            }

            return CategoriaInfo.builder()
                    .id(categoria.getId())
                    .nombre(categoria.getNombre())
                    .descripcion(categoria.getDescripcion())
                    .colorSugerido(obtenerColorSugerido(categoria.getNombre()))
                    .iconoSugerido(obtenerIconoSugerido(categoria.getNombre()))
                    .build();
        }

        private static String obtenerColorSugerido(String nombre) {
            if (nombre == null) return "#6B7280";
            String nombreLower = nombre.toLowerCase();

            if (nombreLower.contains("electrón") || nombreLower.contains("tecnolog")) return "#3B82F6";
            if (nombreLower.contains("ropa") || nombreLower.contains("vestim")) return "#EC4899";
            if (nombreLower.contains("hogar") || nombreLower.contains("casa")) return "#F59E0B";
            if (nombreLower.contains("deporte")) return "#10B981";
            if (nombreLower.contains("libro")) return "#8B5CF6";
            if (nombreLower.contains("aliment") || nombreLower.contains("comida")) return "#EF4444";

            return "#6B7280";
        }

        private static String obtenerIconoSugerido(String nombre) {
            if (nombre == null) return "tag";
            String nombreLower = nombre.toLowerCase();

            if (nombreLower.contains("electrón") || nombreLower.contains("tecnolog")) return "smartphone";
            if (nombreLower.contains("ropa") || nombreLower.contains("vestim")) return "shirt";
            if (nombreLower.contains("hogar") || nombreLower.contains("casa")) return "home";
            if (nombreLower.contains("deporte")) return "dumbbell";
            if (nombreLower.contains("libro")) return "book";
            if (nombreLower.contains("aliment") || nombreLower.contains("comida")) return "utensils";

            return "tag";
        }
    }

    /**
     * Constructor de conveniencia para crear desde Entity
     */
    public static ProductoResponse fromEntity(Producto producto) {
        if (producto == null) {
            return null;
        }

        return ProductoResponse.builder()
                .id(producto.getId())
                .nombre(producto.getNombre())
                .descripcion(producto.getDescripcion())
                .imagen(producto.getImagen())
                .imagenPlaceholder(obtenerImagenPlaceholder(producto.getImagen(), producto.getCategoria()))
                .precio(producto.getPrecio())
                .stockActual(producto.getStockActual())
                .stockMinimo(producto.getStockMinimo())
                .fechaCreacion(producto.getFechaCreacion())
                .fechaActualizacion(producto.getFechaActualizacion())
                .categoria(CategoriaInfo.fromCategoria(producto.getCategoria()))
                .estadoStock(producto.getEstadoStock())
                .valorInventario(calcularValorInventario(producto.getPrecio(), producto.getStockActual()))
                .eliminable(true) // Se actualizará con información real
                .categoriaPrecio(determinarCategoriaPrecio(producto.getPrecio()))
                .build();
    }

    /**
     * Constructor de conveniencia para crear desde Entity con información adicional
     */
    public static ProductoResponse fromEntityConDetalles(Producto producto, Long totalMovimientos,
                                                         Integer totalEntradas, Integer totalSalidas,
                                                         LocalDateTime fechaUltimoMovimiento) {
        ProductoResponse response = fromEntity(producto);

        if (response != null) {
            response.setTotalMovimientos(totalMovimientos);
            response.setTotalEntradas(totalEntradas);
            response.setTotalSalidas(totalSalidas);
            response.setFechaUltimoMovimiento(fechaUltimoMovimiento);
            response.setEliminable(determinarSiEsEliminable(totalMovimientos));
            response.setRotacion(determinarRotacion(totalMovimientos, fechaUltimoMovimiento));
            response.setDiasSinMovimiento(calcularDiasSinMovimiento(fechaUltimoMovimiento));
        }

        return response;
    }

    /**
     * Constructor simplificado para listas
     */
    public static ProductoResponse simple(Producto producto) {
        if (producto == null) {
            return null;
        }

        return ProductoResponse.builder()
                .id(producto.getId())
                .nombre(producto.getNombre())
                .imagen(producto.getImagen())
                .precio(producto.getPrecio())
                .stockActual(producto.getStockActual())
                .stockMinimo(producto.getStockMinimo())
                .categoria(CategoriaInfo.builder()
                        .id(producto.getCategoria().getId())
                        .nombre(producto.getCategoria().getNombre())
                        .build())
                .estadoStock(producto.getEstadoStock())
                .categoriaPrecio(determinarCategoriaPrecio(producto.getPrecio()))
                .build();
    }

    /**
     * Constructor para selectores/dropdown
     */
    public static ProductoResponse selector(Producto producto) {
        if (producto == null) {
            return null;
        }

        return ProductoResponse.builder()
                .id(producto.getId())
                .nombre(producto.getNombre())
                .imagen(producto.getImagen())
                .precio(producto.getPrecio())
                .stockActual(producto.getStockActual())
                .estadoStock(producto.getEstadoStock())
                .build();
    }

    /**
     * Constructor para información pública (sin precios ni stocks)
     */
    public static ProductoResponse publico(Producto producto) {
        if (producto == null) {
            return null;
        }

        return ProductoResponse.builder()
                .id(producto.getId())
                .nombre(producto.getNombre())
                .descripcion(producto.getDescripcion())
                .imagen(producto.getImagen())
                .categoria(CategoriaInfo.builder()
                        .id(producto.getCategoria().getId())
                        .nombre(producto.getCategoria().getNombre())
                        .build())
                .build();
    }

    /**
     * Obtener imagen placeholder basada en la categoría
     */
    private static String obtenerImagenPlaceholder(String imagen, com.jlzDev.inventario.entity.Categoria categoria) {
        if (imagen != null && !imagen.trim().isEmpty()) {
            return null; // No necesita placeholder
        }

        if (categoria == null) {
            return "https://via.placeholder.com/300x300/6B7280/FFFFFF?text=Producto";
        }

        String nombreCategoria = categoria.getNombre().toLowerCase();

        if (nombreCategoria.contains("electrón") || nombreCategoria.contains("tecnolog")) {
            return "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcSFPAU8rBkD5OxnL5Zmi-mbhJrvyvb09n4Wfw&s";
//            return "https://via.placeholder.com/300x300/3B82F6/FFFFFF?text=Electrónico";
        }
        if (nombreCategoria.contains("ropa") || nombreCategoria.contains("vestim")) {
            return "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcSFPAU8rBkD5OxnL5Zmi-mbhJrvyvb09n4Wfw&s";
//            return "https://via.placeholder.com/300x300/EC4899/FFFFFF?text=Ropa";
        }
        if (nombreCategoria.contains("hogar") || nombreCategoria.contains("casa")) {
            return "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcSFPAU8rBkD5OxnL5Zmi-mbhJrvyvb09n4Wfw&s";
//            return "https://via.placeholder.com/300x300/F59E0B/FFFFFF?text=Hogar";
        }
        if (nombreCategoria.contains("deporte")) {
            return "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcSFPAU8rBkD5OxnL5Zmi-mbhJrvyvb09n4Wfw&s";
//            return "https://via.placeholder.com/300x300/10B981/FFFFFF?text=Deportes";
        }
        if (nombreCategoria.contains("libro")) {
            return "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcSFPAU8rBkD5OxnL5Zmi-mbhJrvyvb09n4Wfw&s";
//            return "https://via.placeholder.com/300x300/8B5CF6/FFFFFF?text=Libro";
        }
        return "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcSFPAU8rBkD5OxnL5Zmi-mbhJrvyvb09n4Wfw&s";
//        return "https://via.placeholder.com/300x300/6B7280/FFFFFF?text=" + categoria.getNombre();
    }

    /**
     * Calcular valor del inventario
     */
    private static BigDecimal calcularValorInventario(BigDecimal precio, Integer stock) {
        if (precio == null || stock == null) {
            return null;
        }

        return precio.multiply(new BigDecimal(stock));
    }

    /**
     * Determinar categoría de precio
     */
    private static String determinarCategoriaPrecio(BigDecimal precio) {
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
     * Determinar si el producto se puede eliminar
     */
    private static Boolean determinarSiEsEliminable(Long totalMovimientos) {
        return totalMovimientos == null || totalMovimientos == 0;
    }

    /**
     * Determinar rotación del producto
     */
    private static String determinarRotacion(Long totalMovimientos, LocalDateTime fechaUltimoMovimiento) {
        if (totalMovimientos == null || totalMovimientos == 0) {
            return "Sin movimientos";
        }

        if (fechaUltimoMovimiento == null) {
            return "Sin información";
        }

        long diasSinMovimiento = java.time.temporal.ChronoUnit.DAYS.between(fechaUltimoMovimiento, LocalDateTime.now());

        if (diasSinMovimiento <= 7 && totalMovimientos >= 10) {
            return "Alta";
        } else if (diasSinMovimiento <= 30 && totalMovimientos >= 5) {
            return "Media";
        } else if (totalMovimientos > 0) {
            return "Baja";
        } else {
            return "Sin movimientos";
        }
    }

    /**
     * Calcular días sin movimiento
     */
    private static Long calcularDiasSinMovimiento(LocalDateTime fechaUltimoMovimiento) {
        if (fechaUltimoMovimiento == null) {
            return null;
        }

        return java.time.temporal.ChronoUnit.DAYS.between(fechaUltimoMovimiento, LocalDateTime.now());
    }

    /**
     * Verificar si el producto está disponible (tiene stock)
     */
    public boolean estaDisponible() {
        return stockActual != null && stockActual > 0;
    }

    /**
     * Verificar si el stock está crítico
     */
    public boolean esStockCritico() {
        return "CRÍTICO".equals(estadoStock);
    }

    /**
     * Verificar si el stock está bajo
     */
    public boolean esStockBajo() {
        return "BAJO".equals(estadoStock) || esStockCritico();
    }

    /**
     * Verificar si es producto premium
     */
    public boolean esProductoPremium() {
        return "Premium".equals(categoriaPrecio);
    }

    /**
     * Verificar si tiene descripción
     */
    public boolean tieneDescripcion() {
        return descripcion != null && !descripcion.trim().isEmpty();
    }

    /**
     * Verificar si tiene imagen
     */
    public boolean tieneImagen() {
        return imagen != null && !imagen.trim().isEmpty();
    }

    /**
     * Obtener URL de imagen a mostrar (imagen real o placeholder)
     */
    public String getImagenDisplay() {
        return tieneImagen() ? imagen : imagenPlaceholder;
    }

    /**
     * Verificar si la imagen es un placeholder
     */
    public boolean esImagenPlaceholder() {
        return !tieneImagen() && imagenPlaceholder != null;
    }

    /**
     * Verificar si tiene movimientos
     */
    public boolean tieneMovimientos() {
        return totalMovimientos != null && totalMovimientos > 0;
    }

    /**
     * Verificar si necesita reposición urgente
     */
    public boolean necesitaReposicionUrgente() {
        return esStockCritico() || (stockActual != null && stockActual == 0);
    }

    /**
     * Verificar si la rotación es buena
     */
    public boolean tieneBuenaRotacion() {
        return "Alta".equals(rotacion) || "Media".equals(rotacion);
    }

    /**
     * Obtener porcentaje de stock respecto al mínimo
     */
    public Double getPorcentajeStockMinimo() {
        if (stockActual == null || stockMinimo == null || stockMinimo == 0) {
            return null;
        }

        return (stockActual.doubleValue() / stockMinimo.doubleValue()) * 100;
    }

    /**
     * Obtener unidades faltantes para alcanzar stock mínimo
     */
    public Integer getUnidadesFaltantesMinimo() {
        if (stockActual == null || stockMinimo == null) {
            return null;
        }

        int faltantes = stockMinimo - stockActual;
        return faltantes > 0 ? faltantes : 0;
    }

    /**
     * Obtener nombre de la categoría
     */
    public String getNombreCategoria() {
        return categoria != null ? categoria.getNombre() : "Sin categoría";
    }

    /**
     * Obtener resumen del estado
     */
    public String getResumenEstado() {
        if (!estaDisponible()) {
            return "Sin stock";
        }

        if (necesitaReposicionUrgente()) {
            return "Reposición urgente";
        }

        if (esStockBajo()) {
            return "Stock bajo (" + stockActual + " unidades)";
        }

        return "Stock normal (" + stockActual + " unidades)";
    }

    @Override
    public String toString() {
        return "ProductoResponse{" +
                "id=" + id +
                ", nombre='" + nombre + '\'' +
                ", precio=" + precio +
                ", stockActual=" + stockActual +
                ", stockMinimo=" + stockMinimo +
                ", estadoStock='" + estadoStock + '\'' +
                ", categoria='" + (categoria != null ? categoria.getNombre() : "null") + '\'' +
                ", tieneImagen=" + tieneImagen() +
                '}';
    }
}