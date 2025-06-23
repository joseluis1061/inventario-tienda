package com.jlzDev.inventario.service;

import com.jlzDev.inventario.entity.Movimiento;
import com.jlzDev.inventario.entity.Producto;
import com.jlzDev.inventario.entity.Usuario;
import com.jlzDev.inventario.repository.MovimientoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class MovimientoService {

    private final MovimientoRepository movimientoRepository;
    private final ProductoService productoService;
    private final UsuarioService usuarioService;

    /**
     * Obtener todos los movimientos
     */
    @Transactional(readOnly = true)
    public List<Movimiento> obtenerTodos() {
        log.debug("Obteniendo todos los movimientos");
        return movimientoRepository.findAll();
    }

    /**
     * Obtener historial paginado (más recientes primero)
     */
    @Transactional(readOnly = true)
    public Page<Movimiento> obtenerHistorialPaginado(int pagina, int tamaño) {
        log.debug("Obteniendo historial paginado - Página: {}, Tamaño: {}", pagina, tamaño);

        validarParametrosPaginacion(pagina, tamaño);

        Pageable pageable = PageRequest.of(pagina, tamaño, Sort.by("fecha").descending());
        return movimientoRepository.findAllByOrderByFechaDesc(pageable);
    }

    /**
     * Obtener movimiento por ID
     */
    @Transactional(readOnly = true)
    public Optional<Movimiento> obtenerPorId(Long id) {
        log.debug("Obteniendo movimiento por ID: {}", id);
        validarIdNoNulo(id);
        return movimientoRepository.findById(id);
    }

    /**
     * Obtener movimientos por producto
     */
    @Transactional(readOnly = true)
    public List<Movimiento> obtenerPorProducto(Long productoId) {
        log.debug("Obteniendo movimientos por producto ID: {}", productoId);
        validarIdNoNulo(productoId);

        // Verificar que el producto existe
        productoService.obtenerRequerido(productoId);

        return movimientoRepository.findByProductoId(productoId);
    }

    /**
     * Obtener movimientos por usuario
     */
    @Transactional(readOnly = true)
    public List<Movimiento> obtenerPorUsuario(Long usuarioId) {
        log.debug("Obteniendo movimientos por usuario ID: {}", usuarioId);
        validarIdNoNulo(usuarioId);

        // Verificar que el usuario existe
        usuarioService.obtenerRequerido(usuarioId);

        return movimientoRepository.findByUsuarioId(usuarioId);
    }

    /**
     * Obtener movimientos por tipo
     */
    @Transactional(readOnly = true)
    public List<Movimiento> obtenerPorTipo(Movimiento.TipoMovimiento tipo) {
        log.debug("Obteniendo movimientos por tipo: {}", tipo);

        if (tipo == null) {
            throw new IllegalArgumentException("El tipo de movimiento no puede ser nulo");
        }

        return movimientoRepository.findByTipoMovimiento(tipo);
    }

    /**
     * Obtener movimientos por rango de fechas
     */
    @Transactional(readOnly = true)
    public List<Movimiento> obtenerPorRangoFecha(LocalDateTime fechaInicio, LocalDateTime fechaFin) {
        log.debug("Obteniendo movimientos por rango de fecha: {} - {}", fechaInicio, fechaFin);

        validarRangoFecha(fechaInicio, fechaFin);

        return movimientoRepository.findByFechaBetween(fechaInicio, fechaFin);
    }

    /**
     * Obtener movimientos recientes (últimos N días)
     */
    @Transactional(readOnly = true)
    public List<Movimiento> obtenerRecientes(int dias) {
        log.debug("Obteniendo movimientos de los últimos {} días", dias);

        if (dias <= 0) {
            throw new IllegalArgumentException("El número de días debe ser mayor a 0");
        }

        LocalDateTime fechaDesde = LocalDateTime.now().minusDays(dias);
        return movimientoRepository.findMovimientosRecientes(fechaDesde);
    }

    /**
     * Obtener movimientos por categoría de producto
     */
    @Transactional(readOnly = true)
    public List<Movimiento> obtenerPorCategoria(Long categoriaId) {
        log.debug("Obteniendo movimientos por categoría ID: {}", categoriaId);
        validarIdNoNulo(categoriaId);

        return movimientoRepository.findByProductoCategoriaId(categoriaId);
    }

    /**
     * CREAR ENTRADA DE INVENTARIO
     */
    public Movimiento crearEntrada(Long productoId, Long usuarioId, Integer cantidad, String motivo) {
        log.debug("Creando entrada de inventario - Producto: {}, Usuario: {}, Cantidad: {}",
                productoId, usuarioId, cantidad);

        // Validaciones
        validarDatosMovimiento(productoId, usuarioId, cantidad);
        validarMotivo(motivo);

        // Obtener entidades
        Producto producto = productoService.obtenerRequerido(productoId);
        Usuario usuario = usuarioService.obtenerRequerido(usuarioId);

        // Crear movimiento
        Movimiento movimiento = Movimiento.builder()
                .producto(producto)
                .usuario(usuario)
                .tipoMovimiento(Movimiento.TipoMovimiento.ENTRADA)
                .cantidad(cantidad)
                .motivo(motivo != null ? motivo.trim() : "Entrada de inventario")
                .build();

        // Guardar movimiento
        Movimiento movimientoGuardado = movimientoRepository.save(movimiento);

        // Actualizar stock del producto (SUMAR)
        Integer stockActual = producto.getStockActual();
        Integer nuevoStock = stockActual + cantidad;
        productoService.actualizarStock(productoId, nuevoStock);

        log.info("Entrada creada exitosamente - Producto: '{}', Stock: {} -> {}, Cantidad entrada: {}",
                producto.getNombre(), stockActual, nuevoStock, cantidad);

        return movimientoGuardado;
    }

    /**
     * CREAR SALIDA DE INVENTARIO
     */
    public Movimiento crearSalida(Long productoId, Long usuarioId, Integer cantidad, String motivo) {
        log.debug("Creando salida de inventario - Producto: {}, Usuario: {}, Cantidad: {}",
                productoId, usuarioId, cantidad);

        // Validaciones
        validarDatosMovimiento(productoId, usuarioId, cantidad);
        validarMotivo(motivo);

        // Obtener entidades
        Producto producto = productoService.obtenerRequerido(productoId);
        Usuario usuario = usuarioService.obtenerRequerido(usuarioId);

        // Validar disponibilidad de stock
        if (!productoService.hayStockDisponible(productoId, cantidad)) {
            throw new RuntimeException("Stock insuficiente para el producto '" + producto.getNombre() +
                    "'. Stock disponible: " + producto.getStockActual() + ", cantidad solicitada: " + cantidad);
        }

        // Crear movimiento
        Movimiento movimiento = Movimiento.builder()
                .producto(producto)
                .usuario(usuario)
                .tipoMovimiento(Movimiento.TipoMovimiento.SALIDA)
                .cantidad(cantidad)
                .motivo(motivo != null ? motivo.trim() : "Salida de inventario")
                .build();

        // Guardar movimiento
        Movimiento movimientoGuardado = movimientoRepository.save(movimiento);

        // Actualizar stock del producto (RESTAR)
        Integer stockActual = producto.getStockActual();
        Integer nuevoStock = stockActual - cantidad;
        productoService.actualizarStock(productoId, nuevoStock);

        log.info("Salida creada exitosamente - Producto: '{}', Stock: {} -> {}, Cantidad salida: {}",
                producto.getNombre(), stockActual, nuevoStock, cantidad);

        // Verificar si el stock quedó bajo y generar alerta
        if (nuevoStock <= producto.getStockMinimo()) {
            log.warn("ALERTA: El producto '{}' quedó con stock {} (mínimo: {})",
                    producto.getNombre(), nuevoStock, producto.getStockMinimo());
        }

        return movimientoGuardado;
    }

    /**
     * CREAR MOVIMIENTO GENÉRICO (entrada o salida)
     */
    public Movimiento crearMovimiento(Long productoId, Long usuarioId,
                                      Movimiento.TipoMovimiento tipo, Integer cantidad, String motivo) {
        log.debug("Creando movimiento genérico - Tipo: {}, Producto: {}, Usuario: {}, Cantidad: {}",
                tipo, productoId, usuarioId, cantidad);

        if (tipo == null) {
            throw new IllegalArgumentException("El tipo de movimiento es obligatorio");
        }

        switch (tipo) {
            case ENTRADA:
                return crearEntrada(productoId, usuarioId, cantidad, motivo);
            case SALIDA:
                return crearSalida(productoId, usuarioId, cantidad, motivo);
            default:
                throw new IllegalArgumentException("Tipo de movimiento no válido: " + tipo);
        }
    }

    /**
     * Obtener resumen de movimientos por producto
     */
    @Transactional(readOnly = true)
    public ResumenMovimientoProducto obtenerResumenPorProducto(Long productoId) {
        log.debug("Obteniendo resumen de movimientos para producto ID: {}", productoId);

        validarIdNoNulo(productoId);
        Producto producto = productoService.obtenerRequerido(productoId);

        Integer totalEntradas = movimientoRepository.sumEntradasByProductoId(productoId);
        Integer totalSalidas = movimientoRepository.sumSalidasByProductoId(productoId);

        if (totalEntradas == null) totalEntradas = 0;
        if (totalSalidas == null) totalSalidas = 0;

        return ResumenMovimientoProducto.builder()
                .productoId(productoId)
                .nombreProducto(producto.getNombre())
                .stockActual(producto.getStockActual())
                .stockMinimo(producto.getStockMinimo())
                .totalEntradas(totalEntradas)
                .totalSalidas(totalSalidas)
                .stockCalculado(totalEntradas - totalSalidas)
                .estadoStock(producto.getEstadoStock())
                .build();
    }

    /**
     * Obtener estadísticas de movimientos por periodo
     */
    @Transactional(readOnly = true)
    public EstadisticasMovimientos obtenerEstadisticasPorPeriodo(LocalDateTime fechaInicio, LocalDateTime fechaFin) {
        log.debug("Obteniendo estadísticas de movimientos por periodo: {} - {}", fechaInicio, fechaFin);

        validarRangoFecha(fechaInicio, fechaFin);

        List<Object[]> resultados = movimientoRepository.getEstadisticasPorPeriodo(fechaInicio, fechaFin);

        Long totalEntradas = 0L;
        Long cantidadEntradas = 0L;
        Long totalSalidas = 0L;
        Long cantidadSalidas = 0L;

        for (Object[] resultado : resultados) {
            Movimiento.TipoMovimiento tipo = (Movimiento.TipoMovimiento) resultado[0];
            Long cantidad = ((Number) resultado[1]).longValue();
            Long totalUnidades = ((Number) resultado[2]).longValue();

            if (tipo == Movimiento.TipoMovimiento.ENTRADA) {
                cantidadEntradas = cantidad;
                totalEntradas = totalUnidades;
            } else if (tipo == Movimiento.TipoMovimiento.SALIDA) {
                cantidadSalidas = cantidad;
                totalSalidas = totalUnidades;
            }
        }

        return EstadisticasMovimientos.builder()
                .fechaInicio(fechaInicio)
                .fechaFin(fechaFin)
                .totalMovimientos(cantidadEntradas + cantidadSalidas)
                .cantidadEntradas(cantidadEntradas)
                .cantidadSalidas(cantidadSalidas)
                .unidadesEntradas(totalEntradas)
                .unidadesSalidas(totalSalidas)
                .diferenciaNeta(totalEntradas - totalSalidas)
                .build();
    }

    /**
     * Obtener productos más movidos en un periodo
     */
    @Transactional(readOnly = true)
    public List<ProductoMovimientos> obtenerProductosMasMovidos(LocalDateTime fechaInicio, LocalDateTime fechaFin, int limite) {
        log.debug("Obteniendo productos más movidos por periodo: {} - {}, límite: {}", fechaInicio, fechaFin, limite);

        validarRangoFecha(fechaInicio, fechaFin);

        if (limite <= 0 || limite > 100) {
            throw new IllegalArgumentException("El límite debe estar entre 1 y 100");
        }

        List<Object[]> resultados = movimientoRepository.getProductosMasMovidos(fechaInicio, fechaFin);

        return resultados.stream()
                .limit(limite)
                .map(resultado -> ProductoMovimientos.builder()
                        .nombreProducto((String) resultado[0])
                        .totalMovimientos(((Number) resultado[1]).longValue())
                        .totalEntradas(((Number) resultado[2]).longValue())
                        .totalSalidas(((Number) resultado[3]).longValue())
                        .build())
                .toList();
    }

    /**
     * Buscar movimientos por motivo
     */
    @Transactional(readOnly = true)
    public List<Movimiento> buscarPorMotivo(String motivo) {
        log.debug("Buscando movimientos por motivo: {}", motivo);

        if (motivo == null || motivo.trim().isEmpty()) {
            throw new IllegalArgumentException("El motivo de búsqueda no puede estar vacío");
        }

        return movimientoRepository.findByMotivoContainingIgnoreCase(motivo.trim());
    }

    /**
     * Contar movimientos por tipo
     */
    @Transactional(readOnly = true)
    public Long contarPorTipo(Movimiento.TipoMovimiento tipo) {
        if (tipo == null) {
            throw new IllegalArgumentException("El tipo de movimiento no puede ser nulo");
        }

        return movimientoRepository.countByTipoMovimiento(tipo);
    }

    /**
     * Obtener últimos movimientos por usuario
     */
    @Transactional(readOnly = true)
    public Page<Movimiento> obtenerUltimosPorUsuario(Long usuarioId, int limite) {
        log.debug("Obteniendo últimos {} movimientos del usuario ID: {}", limite, usuarioId);

        validarIdNoNulo(usuarioId);
        usuarioService.obtenerRequerido(usuarioId);

        if (limite <= 0 || limite > 100) {
            throw new IllegalArgumentException("El límite debe estar entre 1 y 100");
        }

        Pageable pageable = PageRequest.of(0, limite);
        return movimientoRepository.findUltimosMovimientosByUsuario(usuarioId, pageable);
    }

    /**
     * Obtener movimiento requerido (lanza excepción si no existe)
     */
    @Transactional(readOnly = true)
    public Movimiento obtenerRequerido(Long id) {
        return obtenerPorId(id)
                .orElseThrow(() -> new RuntimeException("Movimiento no encontrado con ID: " + id));
    }

    // ===== MÉTODOS DE VALIDACIÓN =====

    private void validarDatosMovimiento(Long productoId, Long usuarioId, Integer cantidad) {
        validarIdNoNulo(productoId);
        validarIdNoNulo(usuarioId);
        validarCantidad(cantidad);
    }

    private void validarIdNoNulo(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("El ID no puede ser nulo");
        }
        if (id <= 0) {
            throw new IllegalArgumentException("El ID debe ser mayor a 0");
        }
    }

    private void validarCantidad(Integer cantidad) {
        if (cantidad == null) {
            throw new IllegalArgumentException("La cantidad es obligatoria");
        }
        if (cantidad <= 0) {
            throw new IllegalArgumentException("La cantidad debe ser mayor a 0");
        }
        if (cantidad > 100000) {
            throw new IllegalArgumentException("La cantidad no puede exceder 100,000 unidades por movimiento");
        }
    }

    private void validarMotivo(String motivo) {
        if (motivo != null && motivo.trim().length() > 255) {
            throw new IllegalArgumentException("El motivo no puede exceder 255 caracteres");
        }
    }

    private void validarRangoFecha(LocalDateTime fechaInicio, LocalDateTime fechaFin) {
        if (fechaInicio == null || fechaFin == null) {
            throw new IllegalArgumentException("Las fechas de inicio y fin son obligatorias");
        }
        if (fechaInicio.isAfter(fechaFin)) {
            throw new IllegalArgumentException("La fecha de inicio no puede ser posterior a la fecha de fin");
        }
        if (ChronoUnit.DAYS.between(fechaInicio, fechaFin) > 365) {
            throw new IllegalArgumentException("El rango de fechas no puede exceder 365 días");
        }
    }

    private void validarParametrosPaginacion(int pagina, int tamaño) {
        if (pagina < 0) {
            throw new IllegalArgumentException("El número de página no puede ser negativo");
        }
        if (tamaño <= 0 || tamaño > 100) {
            throw new IllegalArgumentException("El tamaño de página debe estar entre 1 y 100");
        }
    }

    // ===== CLASES INTERNAS PARA ESTADÍSTICAS =====

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class ResumenMovimientoProducto {
        private Long productoId;
        private String nombreProducto;
        private Integer stockActual;
        private Integer stockMinimo;
        private Integer totalEntradas;
        private Integer totalSalidas;
        private Integer stockCalculado;
        private String estadoStock;

        public Boolean isStockConsistente() {
            return stockActual.equals(stockCalculado);
        }
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class EstadisticasMovimientos {
        private LocalDateTime fechaInicio;
        private LocalDateTime fechaFin;
        private Long totalMovimientos;
        private Long cantidadEntradas;
        private Long cantidadSalidas;
        private Long unidadesEntradas;
        private Long unidadesSalidas;
        private Long diferenciaNeta;

        public Double getPorcentajeEntradas() {
            if (totalMovimientos == 0) return 0.0;
            return (cantidadEntradas.doubleValue() / totalMovimientos.doubleValue()) * 100;
        }

        public Double getPorcentajeSalidas() {
            if (totalMovimientos == 0) return 0.0;
            return (cantidadSalidas.doubleValue() / totalMovimientos.doubleValue()) * 100;
        }
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class ProductoMovimientos {
        private String nombreProducto;
        private Long totalMovimientos;
        private Long totalEntradas;
        private Long totalSalidas;

        public Long getDiferenciaNeta() {
            return totalEntradas - totalSalidas;
        }
    }
}