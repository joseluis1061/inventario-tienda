package com.jlzDev.inventario.dto.request;

import com.jlzDev.inventario.entity.Movimiento;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para solicitudes de creación de movimientos de inventario
 * Contiene solo los campos que el cliente puede enviar
 * Nota: La fecha se genera automáticamente en el servidor
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MovimientoRequest {

    /**
     * ID del producto al que se aplicará el movimiento (obligatorio)
     */
    @NotNull(message = "El producto es obligatorio")
    @Positive(message = "El ID del producto debe ser un número positivo")
    private Long productoId;

    /**
     * ID del usuario que realiza el movimiento (obligatorio)
     */
    @NotNull(message = "El usuario es obligatorio")
    @Positive(message = "El ID del usuario debe ser un número positivo")
    private Long usuarioId;

    /**
     * Tipo de movimiento: ENTRADA o SALIDA (obligatorio)
     */
    @NotNull(message = "El tipo de movimiento es obligatorio")
    private Movimiento.TipoMovimiento tipoMovimiento;

    /**
     * Cantidad de unidades del movimiento (obligatorio)
     * Debe ser un número positivo
     */
    @NotNull(message = "La cantidad es obligatoria")
    @Positive(message = "La cantidad debe ser mayor a 0")
    @Max(value = 100000, message = "La cantidad no puede exceder 100,000 unidades por movimiento")
    private Integer cantidad;

    /**
     * Motivo o descripción del movimiento (opcional)
     * Si no se proporciona, se usará un motivo por defecto
     */
    @Size(max = 255, message = "El motivo no puede exceder 255 caracteres")
    private String motivo;

    /**
     * Método para normalizar los datos antes de enviar al service
     */
    public MovimientoRequest normalizar() {
        if (this.motivo != null && !this.motivo.trim().isEmpty()) {
            this.motivo = this.motivo.trim();
            // Capitalizar primera letra del motivo
            this.motivo = capitalizarMotivo(this.motivo);
        } else {
            // Generar motivo por defecto basado en el tipo
            this.motivo = generarMotivoDefecto();
        }

        return this;
    }

    /**
     * Validar que la cantidad sea razonable según el tipo de movimiento
     */
    public boolean isCantidadRazonable() {
        if (cantidad == null || tipoMovimiento == null) {
            return false;
        }

        // Para entradas, cantidades muy grandes podrían ser sospechosas
        if (tipoMovimiento == Movimiento.TipoMovimiento.ENTRADA && cantidad > 10000) {
            return false;
        }

        // Para salidas, cantidades muy grandes podrían exceder el stock
        if (tipoMovimiento == Movimiento.TipoMovimiento.SALIDA && cantidad > 1000) {
            return false; // Se validará contra stock real en el service
        }

        return true;
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
     * Verificar si tiene motivo personalizado
     */
    public boolean tieneMotivo() {
        return motivo != null && !motivo.trim().isEmpty() && !esMotivoPorDefecto();
    }

    /**
     * Verificar si el motivo es uno de los por defecto
     */
    public boolean esMotivoPorDefecto() {
        if (motivo == null) {
            return false;
        }

        String motivoLower = motivo.toLowerCase();
        return motivoLower.equals("entrada de inventario") ||
                motivoLower.equals("salida de inventario") ||
                motivoLower.equals("entrada de mercancía") ||
                motivoLower.equals("salida de mercancía");
    }

    /**
     * Validar que el motivo sea apropiado para el tipo de movimiento
     */
    public boolean esMotivoCoherente() {
        if (motivo == null || tipoMovimiento == null) {
            return true; // Se usará motivo por defecto
        }

        String motivoLower = motivo.toLowerCase();

        if (esEntrada()) {
            // Palabras que indican entrada
            return motivoLower.contains("entrada") ||
                    motivoLower.contains("compra") ||
                    motivoLower.contains("recepción") ||
                    motivoLower.contains("reposición") ||
                    motivoLower.contains("devolución") ||
                    motivoLower.contains("ajuste positivo") ||
                    !contieneIndicadoresSalida(motivoLower);
        } else {
            // Para salidas
            return motivoLower.contains("salida") ||
                    motivoLower.contains("venta") ||
                    motivoLower.contains("entrega") ||
                    motivoLower.contains("consumo") ||
                    motivoLower.contains("merma") ||
                    motivoLower.contains("ajuste negativo") ||
                    !contieneIndicadoresEntrada(motivoLower);
        }
    }

    /**
     * Obtener sugerencias de motivo basadas en el tipo de movimiento
     */
    public String[] getSugerenciasMotivo() {
        if (tipoMovimiento == null) {
            return new String[0];
        }

        if (esEntrada()) {
            return new String[]{
                    "Compra de mercancía",
                    "Reposición de stock",
                    "Devolución de cliente",
                    "Ajuste de inventario positivo",
                    "Recepción de proveedor",
                    "Transferencia desde otra sucursal"
            };
        } else {
            return new String[]{
                    "Venta al cliente",
                    "Entrega a domicilio",
                    "Consumo interno",
                    "Merma por vencimiento",
                    "Ajuste de inventario negativo",
                    "Transferencia a otra sucursal",
                    "Producto dañado"
            };
        }
    }

    /**
     * Obtener el nivel de impacto del movimiento
     */
    public String getNivelImpacto() {
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
     * Verificar si es un movimiento masivo
     */
    public boolean esMovimientoMasivo() {
        return cantidad != null && cantidad > 100;
    }

    /**
     * Verificar si requiere autorización especial
     */
    public boolean requiereAutorizacion() {
        if (cantidad == null) {
            return false;
        }

        // Movimientos masivos o salidas grandes requieren autorización
        return cantidad > 500 ||
                (esSalida() && cantidad > 100);
    }

    /**
     * Validar consistencia de todos los datos
     */
    public boolean esConsistente() {
        // Verificar que los IDs sean válidos
        if (productoId == null || usuarioId == null || productoId <= 0 || usuarioId <= 0) {
            return false;
        }

        // Verificar que el tipo esté definido
        if (tipoMovimiento == null) {
            return false;
        }

        // Verificar que la cantidad sea razonable
        if (!isCantidadRazonable()) {
            return false;
        }

        // Verificar coherencia del motivo
        if (!esMotivoCoherente()) {
            return false;
        }

        return true;
    }

    /**
     * Generar motivo por defecto basado en el tipo de movimiento
     */
    private String generarMotivoDefecto() {
        if (tipoMovimiento == null) {
            return "Movimiento de inventario";
        }

        return tipoMovimiento == Movimiento.TipoMovimiento.ENTRADA ?
                "Entrada de inventario" : "Salida de inventario";
    }

    /**
     * Capitalizar primera letra del motivo
     */
    private String capitalizarMotivo(String motivo) {
        if (motivo == null || motivo.trim().isEmpty()) {
            return motivo;
        }

        String motivoTrim = motivo.trim();
        if (motivoTrim.length() > 0) {
            return Character.toUpperCase(motivoTrim.charAt(0)) +
                    (motivoTrim.length() > 1 ? motivoTrim.substring(1) : "");
        }

        return motivoTrim;
    }

    /**
     * Verificar si el motivo contiene indicadores de salida
     */
    private boolean contieneIndicadoresSalida(String motivoLower) {
        String[] indicadoresSalida = {"salida", "venta", "entrega", "consumo", "merma", "negativo"};

        for (String indicador : indicadoresSalida) {
            if (motivoLower.contains(indicador)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Verificar si el motivo contiene indicadores de entrada
     */
    private boolean contieneIndicadoresEntrada(String motivoLower) {
        String[] indicadoresEntrada = {"entrada", "compra", "recepción", "reposición", "devolución", "positivo"};

        for (String indicador : indicadoresEntrada) {
            if (motivoLower.contains(indicador)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Obtener resumen para logs
     */
    public String getResumenParaLog() {
        return String.format("MovimientoRequest{producto=%d, usuario=%d, tipo=%s, cantidad=%d}",
                productoId, usuarioId, tipoMovimiento, cantidad);
    }

    /**
     * Obtener descripción completa del movimiento
     */
    public String getDescripcionCompleta() {
        if (tipoMovimiento == null || cantidad == null) {
            return "Movimiento incompleto";
        }

        String accion = esEntrada() ? "Entrada de" : "Salida de";
        String unidades = cantidad == 1 ? "unidad" : "unidades";

        return String.format("%s %d %s", accion, cantidad, unidades);
    }

    @Override
    public String toString() {
        return "MovimientoRequest{" +
                "productoId=" + productoId +
                ", usuarioId=" + usuarioId +
                ", tipoMovimiento=" + tipoMovimiento +
                ", cantidad=" + cantidad +
                ", motivo='" + (motivo != null ?
                motivo.substring(0, Math.min(motivo.length(), 30)) + "..." : "null") + '\'' +
                '}';
    }
}