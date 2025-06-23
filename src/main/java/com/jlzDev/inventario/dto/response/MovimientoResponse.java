package com.jlzDev.inventario.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.jlzDev.inventario.entity.Movimiento;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

/**
 * DTO para respuestas de consultas de movimientos de inventario
 * Contiene toda la información que se puede exponer al cliente
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL) // No incluir campos null en JSON
public class MovimientoResponse {

    /**
     * ID único del movimiento
     */
    private Long id;

    /**
     * Tipo de movimiento (ENTRADA o SALIDA)
     */
    private Movimiento.TipoMovimiento tipoMovimiento;

    /**
     * Descripción del tipo de movimiento
     */
    private String descripcionTipo;

    /**
     * Cantidad de unidades del movimiento
     */
    private Integer cantidad;

    /**
     * Motivo o descripción del movimiento
     */
    private String motivo;

    /**
     * Fecha y hora del movimiento
     * Formato: yyyy-MM-dd HH:mm:ss
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime fecha;

    /**
     * Información básica del producto
     */
    private ProductoInfo producto;

    /**
     * Información básica del usuario que realizó el movimiento
     */
    private UsuarioInfo usuario;

    /**
     * Valor monetario del movimiento (precio unitario × cantidad)
     * Se incluye solo en consultas administrativas
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private BigDecimal valorMovimiento;

    /**
     * Stock resultante después del movimiento
     * Se incluye solo en consultas detalladas
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Integer stockResultante;

    /**
     * Stock anterior antes del movimiento
     * Se incluye solo en consultas detalladas
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Integer stockAnterior;

    /**
     * Nivel de impacto del movimiento (Bajo, Medio, Alto, Muy Alto)
     */
    private String nivelImpacto;

    /**
     * Tiempo transcurrido desde el movimiento (ej: "hace 2 horas")
     */
    private String tiempoTranscurrido;

    /**
     * Indica si fue un movimiento masivo (cantidad > 100)
     */
    private Boolean esMovimientoMasivo;

    /**
     * Categoría del motivo (Operacional, Comercial, Ajuste, etc.)
     */
    private String categoriaMotivo;

    /**
     * Estado del stock después del movimiento (CRÍTICO, BAJO, NORMAL)
     * Se incluye solo en consultas detalladas
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String estadoStockResultante;

    /**
     * Clase interna para información básica del producto
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProductoInfo {
        private Long id;
        private String nombre;
        private BigDecimal precio;
        private String nombreCategoria;
        private String colorCategoria;
        private String iconoCategoria;

        public static ProductoInfo fromProducto(com.jlzDev.inventario.entity.Producto producto) {
            if (producto == null) {
                return null;
            }

            return ProductoInfo.builder()
                    .id(producto.getId())
                    .nombre(producto.getNombre())
                    .precio(producto.getPrecio())
                    .nombreCategoria(producto.getCategoria() != null ? producto.getCategoria().getNombre() : null)
                    .colorCategoria(obtenerColorCategoria(producto.getCategoria() != null ? producto.getCategoria().getNombre() : null))
                    .iconoCategoria(obtenerIconoCategoria(producto.getCategoria() != null ? producto.getCategoria().getNombre() : null))
                    .build();
        }

        private static String obtenerColorCategoria(String nombreCategoria) {
            if (nombreCategoria == null) return "#6B7280";
            String nombreLower = nombreCategoria.toLowerCase();

            if (nombreLower.contains("electrón") || nombreLower.contains("tecnolog")) return "#3B82F6";
            if (nombreLower.contains("ropa") || nombreLower.contains("vestim")) return "#EC4899";
            if (nombreLower.contains("hogar") || nombreLower.contains("casa")) return "#F59E0B";
            if (nombreLower.contains("deporte")) return "#10B981";
            if (nombreLower.contains("libro")) return "#8B5CF6";
            if (nombreLower.contains("aliment") || nombreLower.contains("comida")) return "#EF4444";

            return "#6B7280";
        }

        private static String obtenerIconoCategoria(String nombreCategoria) {
            if (nombreCategoria == null) return "tag";
            String nombreLower = nombreCategoria.toLowerCase();

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
     * Clase interna para información básica del usuario
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UsuarioInfo {
        private Long id;
        private String username;
        private String nombreCompleto;
        private String nombreRol;
        private String iniciales;

        public static UsuarioInfo fromUsuario(com.jlzDev.inventario.entity.Usuario usuario) {
            if (usuario == null) {
                return null;
            }

            return UsuarioInfo.builder()
                    .id(usuario.getId())
                    .username(usuario.getUsername())
                    .nombreCompleto(usuario.getNombreCompleto())
                    .nombreRol(usuario.getRol() != null ? usuario.getRol().getNombre() : null)
                    .iniciales(generarIniciales(usuario.getNombreCompleto(), usuario.getUsername()))
                    .build();
        }

        private static String generarIniciales(String nombreCompleto, String username) {
            if (nombreCompleto != null && !nombreCompleto.trim().isEmpty()) {
                String[] palabras = nombreCompleto.trim().split("\\s+");
                StringBuilder iniciales = new StringBuilder();

                for (int i = 0; i < Math.min(2, palabras.length); i++) {
                    if (palabras[i].length() > 0) {
                        iniciales.append(Character.toUpperCase(palabras[i].charAt(0)));
                    }
                }

                return iniciales.length() > 0 ? iniciales.toString() : "??";
            }

            return username != null ? username.substring(0, Math.min(2, username.length())).toUpperCase() : "??";
        }
    }

    /**
     * Constructor de conveniencia para crear desde Entity
     */
    public static MovimientoResponse fromEntity(Movimiento movimiento) {
        if (movimiento == null) {
            return null;
        }

        return MovimientoResponse.builder()
                .id(movimiento.getId())
                .tipoMovimiento(movimiento.getTipoMovimiento())
                .descripcionTipo(movimiento.getTipoMovimiento().getDescripcion())
                .cantidad(movimiento.getCantidad())
                .motivo(movimiento.getMotivo())
                .fecha(movimiento.getFecha())
                .producto(ProductoInfo.fromProducto(movimiento.getProducto()))
                .usuario(UsuarioInfo.fromUsuario(movimiento.getUsuario()))
                .valorMovimiento(calcularValorMovimiento(movimiento))
                .nivelImpacto(determinarNivelImpacto(movimiento.getCantidad()))
                .tiempoTranscurrido(calcularTiempoTranscurrido(movimiento.getFecha()))
                .esMovimientoMasivo(movimiento.getCantidad() > 100)
                .categoriaMotivo(determinarCategoriaMotivo(movimiento.getMotivo(), movimiento.getTipoMovimiento()))
                .build();
    }

    /**
     * Constructor de conveniencia para crear desde Entity con información adicional
     */
    public static MovimientoResponse fromEntityConDetalles(Movimiento movimiento, Integer stockAnterior,
                                                           Integer stockResultante, String estadoStockResultante) {
        MovimientoResponse response = fromEntity(movimiento);

        if (response != null) {
            response.setStockAnterior(stockAnterior);
            response.setStockResultante(stockResultante);
            response.setEstadoStockResultante(estadoStockResultante);
        }

        return response;
    }

    /**
     * Constructor simplificado para listas
     */
    public static MovimientoResponse simple(Movimiento movimiento) {
        if (movimiento == null) {
            return null;
        }

        return MovimientoResponse.builder()
                .id(movimiento.getId())
                .tipoMovimiento(movimiento.getTipoMovimiento())
                .cantidad(movimiento.getCantidad())
                .motivo(movimiento.getMotivo())
                .fecha(movimiento.getFecha())
                .producto(ProductoInfo.builder()
                        .id(movimiento.getProducto().getId())
                        .nombre(movimiento.getProducto().getNombre())
                        .build())
                .usuario(UsuarioInfo.builder()
                        .username(movimiento.getUsuario().getUsername())
                        .nombreCompleto(movimiento.getUsuario().getNombreCompleto())
                        .build())
                .tiempoTranscurrido(calcularTiempoTranscurrido(movimiento.getFecha()))
                .build();
    }

    /**
     * Constructor para historial público (sin información sensible)
     */
    public static MovimientoResponse publico(Movimiento movimiento) {
        if (movimiento == null) {
            return null;
        }

        return MovimientoResponse.builder()
                .tipoMovimiento(movimiento.getTipoMovimiento())
                .cantidad(movimiento.getCantidad())
                .fecha(movimiento.getFecha())
                .producto(ProductoInfo.builder()
                        .nombre(movimiento.getProducto().getNombre())
                        .nombreCategoria(movimiento.getProducto().getCategoria().getNombre())
                        .build())
                .tiempoTranscurrido(calcularTiempoTranscurrido(movimiento.getFecha()))
                .build();
    }

    /**
     * Calcular valor monetario del movimiento
     */
    private static BigDecimal calcularValorMovimiento(Movimiento movimiento) {
        if (movimiento.getProducto() == null || movimiento.getProducto().getPrecio() == null) {
            return null;
        }

        return movimiento.getProducto().getPrecio().multiply(new BigDecimal(movimiento.getCantidad()));
    }

    /**
     * Determinar nivel de impacto basado en la cantidad
     */
    private static String determinarNivelImpacto(Integer cantidad) {
        if (cantidad == null) {
            return "Sin información";
        }

        if (cantidad <= 5) {
            return "Bajo";
        } else if (cantidad <= 50) {
            return "Medio";
        } else if (cantidad <= 500) {
            return "Alto";
        } else {
            return "Muy Alto";
        }
    }

    /**
     * Calcular tiempo transcurrido desde el movimiento
     */
    private static String calcularTiempoTranscurrido(LocalDateTime fecha) {
        if (fecha == null) {
            return "Fecha desconocida";
        }

        LocalDateTime ahora = LocalDateTime.now();

        long segundos = ChronoUnit.SECONDS.between(fecha, ahora);
        long minutos = ChronoUnit.MINUTES.between(fecha, ahora);
        long horas = ChronoUnit.HOURS.between(fecha, ahora);
        long dias = ChronoUnit.DAYS.between(fecha, ahora);

        if (segundos < 60) {
            return "Hace " + segundos + " segundo" + (segundos != 1 ? "s" : "");
        } else if (minutos < 60) {
            return "Hace " + minutos + " minuto" + (minutos != 1 ? "s" : "");
        } else if (horas < 24) {
            return "Hace " + horas + " hora" + (horas != 1 ? "s" : "");
        } else if (dias < 30) {
            return "Hace " + dias + " día" + (dias != 1 ? "s" : "");
        } else {
            return "Hace más de un mes";
        }
    }

    /**
     * Determinar categoría del motivo
     */
    private static String determinarCategoriaMotivo(String motivo, Movimiento.TipoMovimiento tipo) {
        if (motivo == null) {
            return "Sin categoría";
        }

        String motivoLower = motivo.toLowerCase();

        // Categorías comerciales
        if (motivoLower.contains("venta") || motivoLower.contains("cliente") || motivoLower.contains("entrega")) {
            return "Comercial";
        }

        // Categorías operacionales
        if (motivoLower.contains("compra") || motivoLower.contains("proveedor") || motivoLower.contains("recepción")) {
            return "Operacional";
        }

        // Ajustes de inventario
        if (motivoLower.contains("ajuste") || motivoLower.contains("corrección") || motivoLower.contains("inventario")) {
            return "Ajuste";
        }

        // Mermas y pérdidas
        if (motivoLower.contains("merma") || motivoLower.contains("vencimiento") || motivoLower.contains("dañado") || motivoLower.contains("pérdida")) {
            return "Merma";
        }

        // Devoluciones
        if (motivoLower.contains("devolución") || motivoLower.contains("retorno")) {
            return "Devolución";
        }

        // Transferencias
        if (motivoLower.contains("transferencia") || motivoLower.contains("traslado") || motivoLower.contains("sucursal")) {
            return "Transferencia";
        }

        // Por defecto según el tipo
        return tipo == Movimiento.TipoMovimiento.ENTRADA ? "Entrada" : "Salida";
    }

    /**
     * Verificar si es un movimiento de entrada
     */
    public boolean esEntrada() {
        return Movimiento.TipoMovimiento.ENTRADA.equals(tipoMovimiento);
    }

    /**
     * Verificar si es un movimiento de salida
     */
    public boolean esSalida() {
        return Movimiento.TipoMovimiento.SALIDA.equals(tipoMovimiento);
    }

    /**
     * Verificar si fue reciente (menos de 24 horas)
     */
    public boolean esReciente() {
        if (fecha == null) {
            return false;
        }

        return ChronoUnit.HOURS.between(fecha, LocalDateTime.now()) < 24;
    }

    /**
     * Verificar si tiene impacto alto
     */
    public boolean tieneImpactoAlto() {
        return "Alto".equals(nivelImpacto) || "Muy Alto".equals(nivelImpacto);
    }

    /**
     * Verificar si causó alerta de stock
     */
    public boolean causoAlertaStock() {
        return "CRÍTICO".equals(estadoStockResultante) || "BAJO".equals(estadoStockResultante);
    }

    /**
     * Obtener icono basado en el tipo de movimiento
     */
    public String getIconoTipo() {
        return esEntrada() ? "arrow-down-circle" : "arrow-up-circle";
    }

    /**
     * Obtener color basado en el tipo de movimiento
     */
    public String getColorTipo() {
        return esEntrada() ? "#10B981" : "#EF4444"; // Verde para entrada, Rojo para salida
    }

    /**
     * Obtener variante de badge para el tipo
     */
    public String getVarianteTipo() {
        return esEntrada() ? "success" : "danger";
    }

    /**
     * Obtener resumen del movimiento
     */
    public String getResumenMovimiento() {
        String accion = esEntrada() ? "Entrada de" : "Salida de";
        String unidades = cantidad == 1 ? "unidad" : "unidades";

        return String.format("%s %d %s", accion, cantidad, unidades);
    }

    /**
     * Obtener impacto en stock como texto
     */
    public String getImpactoStockTexto() {
        if (stockAnterior == null || stockResultante == null) {
            return "Sin información";
        }

        int diferencia = stockResultante - stockAnterior;
        String signo = diferencia > 0 ? "+" : "";

        return String.format("Stock: %d → %d (%s%d)", stockAnterior, stockResultante, signo, diferencia);
    }

    /**
     * Verificar si tiene valor monetario significativo
     */
    public boolean tieneValorSignificativo() {
        return valorMovimiento != null && valorMovimiento.compareTo(new BigDecimal("100.00")) > 0;
    }

    @Override
    public String toString() {
        return "MovimientoResponse{" +
                "id=" + id +
                ", tipo=" + tipoMovimiento +
                ", cantidad=" + cantidad +
                ", producto='" + (producto != null ? producto.getNombre() : "null") + '\'' +
                ", usuario='" + (usuario != null ? usuario.getUsername() : "null") + '\'' +
                ", fecha=" + fecha +
                ", nivelImpacto='" + nivelImpacto + '\'' +
                '}';
    }
}