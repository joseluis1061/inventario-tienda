package com.jlzDev.inventario.service;

import com.jlzDev.inventario.entity.Categoria;
import com.jlzDev.inventario.entity.Producto;
import com.jlzDev.inventario.repository.ProductoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class ProductoService {

    private final ProductoRepository productoRepository;
    private final CategoriaService categoriaService;

    /**
     * Obtener todos los productos
     */
    @Transactional(readOnly = true)
    public List<Producto> obtenerTodos() {
        log.debug("Obteniendo todos los productos");
        return productoRepository.findAll();
    }

    /**
     * Obtener producto por ID
     */
    @Transactional(readOnly = true)
    public Optional<Producto> obtenerPorId(Long id) {
        log.debug("Obteniendo producto por ID: {}", id);
        validarIdNoNulo(id);
        return productoRepository.findById(id);
    }

    /**
     * Obtener producto por nombre
     */
    @Transactional(readOnly = true)
    public Optional<Producto> obtenerPorNombre(String nombre) {
        log.debug("Obteniendo producto por nombre: {}", nombre);
        validarNombreNoVacio(nombre);
        return productoRepository.findByNombre(nombre.trim());
    }

    /**
     * Buscar productos por nombre que contenga el texto
     */
    @Transactional(readOnly = true)
    public List<Producto> buscarPorNombre(String nombre) {
        log.debug("Buscando productos que contengan: {}", nombre);
        validarNombreNoVacio(nombre);
        return productoRepository.findByNombreContainingIgnoreCase(nombre.trim());
    }

    /**
     * Obtener productos por categoría
     */
    @Transactional(readOnly = true)
    public List<Producto> obtenerPorCategoria(Long categoriaId) {
        log.debug("Obteniendo productos por categoría ID: {}", categoriaId);
        validarIdNoNulo(categoriaId);

        // Verificar que la categoría existe
        categoriaService.obtenerRequerida(categoriaId);

        return productoRepository.findByCategoriaId(categoriaId);
    }

    /**
     * Obtener productos por categoría con stock disponible
     */
    @Transactional(readOnly = true)
    public List<Producto> obtenerPorCategoriaConStock(Long categoriaId) {
        log.debug("Obteniendo productos con stock por categoría ID: {}", categoriaId);
        validarIdNoNulo(categoriaId);

        // Verificar que la categoría existe
        categoriaService.obtenerRequerida(categoriaId);

        return productoRepository.findByCategoriaIdAndStockDisponible(categoriaId);
    }

    /**
     * Obtener productos con stock bajo (stock actual <= stock mínimo)
     */
    @Transactional(readOnly = true)
    public List<Producto> obtenerProductosConStockBajo() {
        log.debug("Obteniendo productos con stock bajo");
        return productoRepository.findProductosConStockBajo();
    }

    /**
     * Obtener productos con stock crítico (stock = 0)
     */
    @Transactional(readOnly = true)
    public List<Producto> obtenerProductosConStockCritico() {
        log.debug("Obteniendo productos con stock crítico");
        return productoRepository.findProductosConStockCritico();
    }

    /**
     * Obtener productos por rango de precio
     */
    @Transactional(readOnly = true)
    public List<Producto> obtenerPorRangoPrecio(BigDecimal precioMin, BigDecimal precioMax) {
        log.debug("Obteniendo productos por rango de precio: {} - {}", precioMin, precioMax);

        validarRangoPrecio(precioMin, precioMax);

        return productoRepository.findByPrecioBetween(precioMin, precioMax);
    }

    /**
     * Obtener productos con stock mayor a una cantidad
     */
    @Transactional(readOnly = true)
    public List<Producto> obtenerConStockMayorA(Integer cantidad) {
        log.debug("Obteniendo productos con stock mayor a: {}", cantidad);

        if (cantidad == null || cantidad < 0) {
            throw new IllegalArgumentException("La cantidad debe ser mayor o igual a 0");
        }

        return productoRepository.findByStockActualGreaterThan(cantidad);
    }

    /**
     * Obtener productos más vendidos
     */
    @Transactional(readOnly = true)
    public List<Producto> obtenerMasVendidos() {
        log.debug("Obteniendo productos más vendidos");
        return productoRepository.findProductosMasVendidos();
    }

    /**
     * Obtener productos creados después de una fecha
     */
    @Transactional(readOnly = true)
    public List<Producto> obtenerCreadosDespuesDe(LocalDateTime fecha) {
        log.debug("Obteniendo productos creados después de: {}", fecha);

        if (fecha == null) {
            throw new IllegalArgumentException("La fecha no puede ser nula");
        }

        return productoRepository.findByFechaCreacionAfter(fecha);
    }

    /**
     * Crear nuevo producto
     */
    public Producto crear(Producto producto) {
        log.debug("Creando nuevo producto: {}", producto.getNombre());

        // Validaciones de negocio
        validarProductoParaCreacion(producto);

        // Normalizar nombre
        producto.setNombre(producto.getNombre().trim());

        if (producto.getDescripcion() != null) {
            producto.setDescripcion(producto.getDescripcion().trim());
        }

        // Normalizar imagen
        if (producto.getImagen() != null) {
            producto.setImagen(producto.getImagen().trim());
            if (producto.getImagen().isEmpty()) {
                producto.setImagen(null);
            }
        }

        // Verificar que la categoría existe
        Categoria categoria = categoriaService.obtenerRequerida(producto.getCategoria().getId());
        producto.setCategoria(categoria);

        // Establecer valores por defecto si no están definidos
        if (producto.getStockActual() == null) {
            producto.setStockActual(0);
        }

        if (producto.getStockMinimo() == null) {
            producto.setStockMinimo(0);
        }

        Producto productoGuardado = productoRepository.save(producto);
        log.info("Producto creado exitosamente con ID: {} - {}",
                productoGuardado.getId(), productoGuardado.getNombre());

        return productoGuardado;
    }

    /**
     * Actualizar producto existente
     */
    public Producto actualizar(Long id, Producto productoActualizado) {
        log.debug("Actualizando producto con ID: {}", id);

        validarIdNoNulo(id);
        validarProductoParaActualizacion(productoActualizado);

        Producto productoExistente = productoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Producto no encontrado con ID: " + id));

        // Verificar unicidad de nombre (excluyendo el producto actual)
        if (!productoExistente.getNombre().equals(productoActualizado.getNombre().trim())) {
            validarNombreUnico(productoActualizado.getNombre());
        }

        // Verificar que la categoría existe si cambió
        if (!productoExistente.getCategoria().getId().equals(productoActualizado.getCategoria().getId())) {
            Categoria categoriaNueva = categoriaService.obtenerRequerida(productoActualizado.getCategoria().getId());
            productoExistente.setCategoria(categoriaNueva);
        }

        // Actualizar campos básicos
        productoExistente.setNombre(productoActualizado.getNombre().trim());

        if (productoActualizado.getDescripcion() != null) {
            productoExistente.setDescripcion(productoActualizado.getDescripcion().trim());
        } else {
            productoExistente.setDescripcion(null);
        }

        // Actualizar imagen
        if (productoActualizado.getImagen() != null) {
            productoExistente.setImagen(productoActualizado.getImagen().trim());
            if (productoExistente.getImagen().isEmpty()) {
                productoExistente.setImagen(null);
            }
        } else {
            productoExistente.setImagen(null);
        }

        productoExistente.setPrecio(productoActualizado.getPrecio());

        // Actualizar stock mínimo (el stock actual se maneja por movimientos)
        if (productoActualizado.getStockMinimo() != null) {
            productoExistente.setStockMinimo(productoActualizado.getStockMinimo());
        }

        Producto productoGuardado = productoRepository.save(productoExistente);
        log.info("Producto actualizado exitosamente: {}", productoGuardado.getNombre());

        return productoGuardado;
    }

    /**
     * Actualizar stock de producto (usado por MovimientoService)
     */
    public Producto actualizarStock(Long productoId, Integer nuevoStock) {
        log.debug("Actualizando stock del producto ID: {} a {}", productoId, nuevoStock);

        validarIdNoNulo(productoId);
        validarStock(nuevoStock);

        Producto producto = productoRepository.findById(productoId)
                .orElseThrow(() -> new RuntimeException("Producto no encontrado con ID: " + productoId));

        Integer stockAnterior = producto.getStockActual();
        producto.setStockActual(nuevoStock);

        Producto productoActualizado = productoRepository.save(producto);

        log.info("Stock actualizado para producto '{}': {} -> {}",
                producto.getNombre(), stockAnterior, nuevoStock);

        return productoActualizado;
    }

    /**
     * Ajustar stock (sumar o restar cantidad)
     */
    public Producto ajustarStock(Long productoId, Integer cantidad, String tipoOperacion) {
        log.debug("Ajustando stock del producto ID: {} en {} unidades ({})",
                productoId, cantidad, tipoOperacion);

        validarIdNoNulo(productoId);

        if (cantidad == null || cantidad <= 0) {
            throw new IllegalArgumentException("La cantidad debe ser mayor a 0");
        }

        if (!"SUMA".equals(tipoOperacion) && !"RESTA".equals(tipoOperacion)) {
            throw new IllegalArgumentException("Tipo de operación debe ser 'SUMA' o 'RESTA'");
        }

        Producto producto = productoRepository.findById(productoId)
                .orElseThrow(() -> new RuntimeException("Producto no encontrado con ID: " + productoId));

        Integer stockActual = producto.getStockActual();
        Integer nuevoStock;

        if ("SUMA".equals(tipoOperacion)) {
            nuevoStock = stockActual + cantidad;
        } else {
            nuevoStock = stockActual - cantidad;
            if (nuevoStock < 0) {
                throw new RuntimeException("No hay suficiente stock disponible. Stock actual: " + stockActual +
                        ", cantidad solicitada: " + cantidad);
            }
        }

        return actualizarStock(productoId, nuevoStock);
    }

    /**
     * Eliminar producto
     */
    public void eliminar(Long id) {
        log.debug("Eliminando producto con ID: {}", id);

        validarIdNoNulo(id);

        Producto producto = productoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Producto no encontrado con ID: " + id));

        // Verificar si tiene movimientos asociados
        if (productoRepository.hasMovimientos(id)) {
            throw new RuntimeException("No se puede eliminar el producto '" + producto.getNombre() +
                    "' porque tiene movimientos de inventario asociados");
        }

        productoRepository.deleteById(id);
        log.info("Producto eliminado exitosamente: {}", producto.getNombre());
    }

    /**
     * Verificar si existe producto por nombre
     */
    @Transactional(readOnly = true)
    public boolean existe(String nombre) {
        validarNombreNoVacio(nombre);
        return productoRepository.existsByNombre(nombre.trim());
    }

    /**
     * Verificar disponibilidad de stock
     */
    @Transactional(readOnly = true)
    public boolean hayStockDisponible(Long productoId, Integer cantidadRequerida) {
        validarIdNoNulo(productoId);

        if (cantidadRequerida == null || cantidadRequerida <= 0) {
            throw new IllegalArgumentException("La cantidad requerida debe ser mayor a 0");
        }

        Producto producto = obtenerRequerido(productoId);
        return producto.getStockActual() >= cantidadRequerida;
    }

    /**
     * Obtener estadísticas de stock
     */
    @Transactional(readOnly = true)
    public StockStats obtenerEstadisticasStock() {
        log.debug("Obteniendo estadísticas de stock");

        try {
            Object[] result = productoRepository.getEstadisticasStock();

            // Debugging - quitar después de validar
            log.debug("Resultado de query - Tipo: {}, Longitud: {}",
                    result.getClass().getSimpleName(), result.length);
            for (int i = 0; i < result.length; i++) {
                log.debug("result[{}] = {} (tipo: {})", i, result[i],
                        result[i] != null ? result[i].getClass().getSimpleName() : "null");
            }

            // El resultado viene como: [Object[]] donde Object[] = [3, 3, 0]
            // Necesitamos extraer el array interno
            Object[] stats;
            if (result.length == 1 && result[0] instanceof Object[]) {
                // Caso: SQL nativo retorna un array dentro de otro array
                stats = (Object[]) result[0];
                log.debug("Extrayendo array interno: {}", java.util.Arrays.toString(stats));
            } else {
                // Caso: JPA query retorna directamente los valores
                stats = result;
            }

            // Validar que tenemos exactamente 3 valores
            if (stats.length != 3) {
                throw new RuntimeException("Se esperaban 3 valores en las estadísticas, pero se recibieron: " + stats.length);
            }

            // Conversión segura de tipos
            Long totalProductos = convertToLong(stats[0]);
            Long productosStockBajo = convertToLong(stats[1]);
            Long productosSinStock = convertToLong(stats[2]);

            log.debug("Estadísticas procesadas - Total: {}, Stock Bajo: {}, Sin Stock: {}",
                    totalProductos, productosStockBajo, productosSinStock);

            return StockStats.builder()
                    .totalProductos(totalProductos)
                    .productosStockBajo(productosStockBajo)
                    .productosSinStock(productosSinStock)
                    .build();

        } catch (Exception e) {
            log.error("Error al obtener estadísticas de stock: {}", e.getMessage(), e);
            throw new RuntimeException("Error al calcular estadísticas de stock", e);
        }
    }

    /**
     * Método auxiliar para conversión segura a Long
     */
    private Long convertToLong(Object value) {
        if (value == null) {
            return 0L;
        }

        if (value instanceof Number) {
            return ((Number) value).longValue();
        }

        if (value instanceof String) {
            try {
                return Long.parseLong((String) value);
            } catch (NumberFormatException e) {
                log.warn("No se pudo convertir string a Long: {}", value);
                return 0L;
            }
        }

        log.warn("Tipo inesperado en estadísticas: {} - {}", value.getClass(), value);
        return 0L;
    }

    /**
     * Contar productos por categoría
     */
    @Transactional(readOnly = true)
    public Long contarPorCategoria(Long categoriaId) {
        validarIdNoNulo(categoriaId);
        return productoRepository.countByCategoriaId(categoriaId);
    }

    /**
     * Obtener producto requerido (lanza excepción si no existe)
     */
    @Transactional(readOnly = true)
    public Producto obtenerRequerido(Long id) {
        return obtenerPorId(id)
                .orElseThrow(() -> new RuntimeException("Producto no encontrado con ID: " + id));
    }

    // ===== MÉTODOS DE VALIDACIÓN =====

    private void validarProductoParaCreacion(Producto producto) {
        if (producto == null) {
            throw new IllegalArgumentException("El producto no puede ser nulo");
        }

        validarNombreNoVacio(producto.getNombre());
        validarNombreUnico(producto.getNombre());
        validarLongitudNombre(producto.getNombre());

        if (producto.getDescripcion() != null && !producto.getDescripcion().trim().isEmpty()) {
            validarLongitudDescripcion(producto.getDescripcion());
        }

        // Validar imagen si está presente
        if (producto.getImagen() != null && !producto.getImagen().trim().isEmpty()) {
            validarUrlImagen(producto.getImagen());
        }

        validarPrecio(producto.getPrecio());

        if (producto.getStockActual() != null) {
            validarStock(producto.getStockActual());
        }

        if (producto.getStockMinimo() != null) {
            validarStock(producto.getStockMinimo());
        }

        if (producto.getCategoria() == null || producto.getCategoria().getId() == null) {
            throw new IllegalArgumentException("La categoría es obligatoria");
        }
    }

    private void validarProductoParaActualizacion(Producto producto) {
        if (producto == null) {
            throw new IllegalArgumentException("El producto no puede ser nulo");
        }

        validarNombreNoVacio(producto.getNombre());
        validarLongitudNombre(producto.getNombre());

        if (producto.getDescripcion() != null && !producto.getDescripcion().trim().isEmpty()) {
            validarLongitudDescripcion(producto.getDescripcion());
        }

        // Validar imagen si está presente
        if (producto.getImagen() != null && !producto.getImagen().trim().isEmpty()) {
            validarUrlImagen(producto.getImagen());
        }

        validarPrecio(producto.getPrecio());

        if (producto.getStockMinimo() != null) {
            validarStock(producto.getStockMinimo());
        }

        if (producto.getCategoria() == null || producto.getCategoria().getId() == null) {
            throw new IllegalArgumentException("La categoría es obligatoria");
        }
    }

    private void validarUrlImagen(String imagen) {
        if (imagen == null || imagen.trim().isEmpty()) {
            return; // null o vacío es válido
        }

        String imagenTrimmed = imagen.trim();

        if (imagenTrimmed.length() > 500) {
            throw new IllegalArgumentException("La URL de la imagen no puede exceder 500 caracteres");
        }

        // Validar formato básico de URL
        if (!imagenTrimmed.matches("^https?://.*")) {
            throw new IllegalArgumentException("La imagen debe ser una URL válida que comience con http:// o https://");
        }

        // Validar extensiones permitidas (opcional pero recomendado)
        String imagenLower = imagenTrimmed.toLowerCase();
        if (!imagenLower.matches(".*\\.(jpg|jpeg|png|gif|webp|svg).*")) {
            log.warn("La imagen no parece tener una extensión de imagen válida: {}", imagenTrimmed);
            // No lanzar excepción, solo advertencia
        }

        // Validar que no contenga caracteres peligrosos
        if (imagenTrimmed.matches(".*[<>\"'].*")) {
            throw new IllegalArgumentException("La URL de la imagen contiene caracteres no permitidos");
        }
    }

    private void validarIdNoNulo(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("El ID no puede ser nulo");
        }
        if (id <= 0) {
            throw new IllegalArgumentException("El ID debe ser mayor a 0");
        }
    }

    private void validarNombreNoVacio(String nombre) {
        if (nombre == null || nombre.trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre del producto no puede estar vacío");
        }
    }

    private void validarNombreUnico(String nombre) {
        if (productoRepository.existsByNombre(nombre.trim())) {
            throw new RuntimeException("Ya existe un producto con el nombre: " + nombre);
        }
    }

    private void validarLongitudNombre(String nombre) {
        if (nombre.trim().length() > 150) {
            throw new IllegalArgumentException("El nombre del producto no puede exceder 150 caracteres");
        }
        if (nombre.trim().length() < 2) {
            throw new IllegalArgumentException("El nombre del producto debe tener al menos 2 caracteres");
        }
    }

    private void validarLongitudDescripcion(String descripcion) {
        if (descripcion.trim().length() > 1000) {
            throw new IllegalArgumentException("La descripción no puede exceder 1000 caracteres");
        }
    }

    private void validarPrecio(BigDecimal precio) {
        if (precio == null) {
            throw new IllegalArgumentException("El precio es obligatorio");
        }
        if (precio.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("El precio no puede ser negativo");
        }
        if (precio.compareTo(new BigDecimal("99999999.99")) > 0) {
            throw new IllegalArgumentException("El precio no puede exceder 99,999,999.99");
        }
    }

    private void validarStock(Integer stock) {
        if (stock == null) {
            throw new IllegalArgumentException("El stock no puede ser nulo");
        }
        if (stock < 0) {
            throw new IllegalArgumentException("El stock no puede ser negativo");
        }
        if (stock > 1000000) {
            throw new IllegalArgumentException("El stock no puede exceder 1,000,000 unidades");
        }
    }

    private void validarRangoPrecio(BigDecimal precioMin, BigDecimal precioMax) {
        if (precioMin == null || precioMax == null) {
            throw new IllegalArgumentException("Los precios mínimo y máximo son obligatorios");
        }
        if (precioMin.compareTo(BigDecimal.ZERO) < 0 || precioMax.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Los precios no pueden ser negativos");
        }
        if (precioMin.compareTo(precioMax) > 0) {
            throw new IllegalArgumentException("El precio mínimo no puede ser mayor al precio máximo");
        }
    }

    // ===== CLASE INTERNA PARA ESTADÍSTICAS =====

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class StockStats {
        private Long totalProductos;
        private Long productosStockBajo;
        private Long productosSinStock;

        public Double getPorcentajeStockBajo() {
            if (totalProductos == 0) return 0.0;
            return (productosStockBajo.doubleValue() / totalProductos.doubleValue()) * 100;
        }

        public Double getPorcentajeSinStock() {
            if (totalProductos == 0) return 0.0;
            return (productosSinStock.doubleValue() / totalProductos.doubleValue()) * 100;
        }
    }
}