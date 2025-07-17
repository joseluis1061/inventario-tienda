package com.jlzDev.inventario.controller;

import com.jlzDev.inventario.dto.request.ProductoRequest;
import com.jlzDev.inventario.dto.response.ProductoResponse;
import com.jlzDev.inventario.entity.Categoria;
import com.jlzDev.inventario.entity.Producto;
import com.jlzDev.inventario.service.ProductoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Controller REST para gestión de productos
 * Endpoints para operaciones CRUD de productos
 */
@RestController
@RequestMapping("/api/productos")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*") // Configurar según necesidades de CORS
public class ProductoController {

    private final ProductoService productoService;

    /**
     * Obtener todos los productos
     * GET /api/productos
     */
    @GetMapping
    public ResponseEntity<List<ProductoResponse>> obtenerTodos() {
        log.info("GET /api/productos - Solicitando todos los productos");

        try {
            List<Producto> productos = productoService.obtenerTodos();

            List<ProductoResponse> productosResponse = productos.stream()
                    .map(ProductoResponse::fromEntity)
                    .collect(Collectors.toList());

            log.info("GET /api/productos - Encontrados {} productos", productosResponse.size());
            return ResponseEntity.ok(productosResponse);

        } catch (Exception e) {
            log.error("GET /api/productos - Error al obtener productos: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Obtener producto por ID
     * GET /api/productos/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<ProductoResponse> obtenerPorId(@PathVariable Long id) {
        log.info("GET /api/productos/{} - Solicitando producto por ID", id);

        try {
            Optional<Producto> productoOpt = productoService.obtenerPorId(id);

            if (productoOpt.isPresent()) {
                ProductoResponse productoResponse = ProductoResponse.fromEntity(productoOpt.get());
                log.info("GET /api/productos/{} - Producto encontrado: {}", id, productoResponse.getNombre());
                return ResponseEntity.ok(productoResponse);
            } else {
                log.warn("GET /api/productos/{} - Producto no encontrado", id);
                return ResponseEntity.notFound().build();
            }

        } catch (IllegalArgumentException e) {
            log.warn("GET /api/productos/{} - ID inválido: {}", id, e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            log.error("GET /api/productos/{} - Error interno: {}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Obtener producto por nombre
     * GET /api/productos/buscar?nombre={nombre}
     */
    @GetMapping("/buscar")
    public ResponseEntity<ProductoResponse> obtenerPorNombre(@RequestParam String nombre) {
        log.info("GET /api/productos/buscar?nombre={} - Buscando producto por nombre", nombre);

        try {
            Optional<Producto> productoOpt = productoService.obtenerPorNombre(nombre);

            if (productoOpt.isPresent()) {
                ProductoResponse productoResponse = ProductoResponse.fromEntity(productoOpt.get());
                log.info("GET /api/productos/buscar - Producto encontrado: {}", productoResponse.getNombre());
                return ResponseEntity.ok(productoResponse);
            } else {
                log.warn("GET /api/productos/buscar - Producto no encontrado con nombre: {}", nombre);
                return ResponseEntity.notFound().build();
            }

        } catch (IllegalArgumentException e) {
            log.warn("GET /api/productos/buscar - Parámetro inválido: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            log.error("GET /api/productos/buscar - Error interno: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Buscar productos que contengan el texto en el nombre
     * GET /api/productos/buscar-similares?nombre={nombre}
     */
    @GetMapping("/buscar-similares")
    public ResponseEntity<List<ProductoResponse>> buscarSimilares(@RequestParam String nombre) {
        log.info("GET /api/productos/buscar-similares?nombre={} - Buscando productos similares", nombre);

        try {
            List<Producto> productos = productoService.buscarPorNombre(nombre);

            List<ProductoResponse> productosResponse = productos.stream()
                    .map(ProductoResponse::fromEntity)
                    .collect(Collectors.toList());

            log.info("GET /api/productos/buscar-similares - Encontrados {} productos similares", productosResponse.size());
            return ResponseEntity.ok(productosResponse);

        } catch (IllegalArgumentException e) {
            log.warn("GET /api/productos/buscar-similares - Parámetro inválido: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            log.error("GET /api/productos/buscar-similares - Error interno: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Obtener productos por categoría
     * GET /api/productos/por-categoria/{categoriaId}
     */
    @GetMapping("/por-categoria/{categoriaId}")
    public ResponseEntity<List<ProductoResponse>> obtenerPorCategoria(@PathVariable Long categoriaId) {
        log.info("GET /api/productos/por-categoria/{} - Solicitando productos por categoría", categoriaId);

        try {
            List<Producto> productos = productoService.obtenerPorCategoria(categoriaId);

            List<ProductoResponse> productosResponse = productos.stream()
                    .map(ProductoResponse::fromEntity)
                    .collect(Collectors.toList());

            log.info("GET /api/productos/por-categoria/{} - Encontrados {} productos", categoriaId, productosResponse.size());
            return ResponseEntity.ok(productosResponse);

        } catch (IllegalArgumentException e) {
            log.warn("GET /api/productos/por-categoria/{} - ID de categoría inválido: {}", categoriaId, e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (RuntimeException e) {
            if (e.getMessage().contains("no encontrada")) {
                log.warn("GET /api/productos/por-categoria/{} - Categoría no encontrada", categoriaId);
                return ResponseEntity.notFound().build();
            } else {
                throw e;
            }
        } catch (Exception e) {
            log.error("GET /api/productos/por-categoria/{} - Error interno: {}", categoriaId, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Obtener productos por categoría con stock disponible
     * GET /api/productos/por-categoria/{categoriaId}/con-stock
     */
    @GetMapping("/por-categoria/{categoriaId}/con-stock")
    public ResponseEntity<List<ProductoResponse>> obtenerPorCategoriaConStock(@PathVariable Long categoriaId) {
        log.info("GET /api/productos/por-categoria/{}/con-stock - Solicitando productos con stock", categoriaId);

        try {
            List<Producto> productos = productoService.obtenerPorCategoriaConStock(categoriaId);

            List<ProductoResponse> productosResponse = productos.stream()
                    .map(ProductoResponse::fromEntity)
                    .collect(Collectors.toList());

            log.info("GET /api/productos/por-categoria/{}/con-stock - Encontrados {} productos con stock",
                    categoriaId, productosResponse.size());
            return ResponseEntity.ok(productosResponse);

        } catch (IllegalArgumentException e) {
            log.warn("GET /api/productos/por-categoria/{}/con-stock - ID inválido: {}", categoriaId, e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (RuntimeException e) {
            if (e.getMessage().contains("no encontrada")) {
                log.warn("GET /api/productos/por-categoria/{}/con-stock - Categoría no encontrada", categoriaId);
                return ResponseEntity.notFound().build();
            } else {
                throw e;
            }
        } catch (Exception e) {
            log.error("GET /api/productos/por-categoria/{}/con-stock - Error interno: {}", categoriaId, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Obtener productos con stock bajo
     * GET /api/productos/stock-bajo
     */
    @GetMapping("/stock-bajo")
    public ResponseEntity<List<ProductoResponse>> obtenerConStockBajo() {
        log.info("GET /api/productos/stock-bajo - Solicitando productos con stock bajo");

        try {
            List<Producto> productos = productoService.obtenerProductosConStockBajo();

            List<ProductoResponse> productosResponse = productos.stream()
                    .map(ProductoResponse::fromEntity)
                    .collect(Collectors.toList());

            log.info("GET /api/productos/stock-bajo - Encontrados {} productos con stock bajo", productosResponse.size());
            return ResponseEntity.ok(productosResponse);

        } catch (Exception e) {
            log.error("GET /api/productos/stock-bajo - Error interno: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Obtener productos con stock crítico (stock = 0)
     * GET /api/productos/stock-critico
     */
    @GetMapping("/stock-critico")
    public ResponseEntity<List<ProductoResponse>> obtenerConStockCritico() {
        log.info("GET /api/productos/stock-critico - Solicitando productos con stock crítico");

        try {
            List<Producto> productos = productoService.obtenerProductosConStockCritico();

            List<ProductoResponse> productosResponse = productos.stream()
                    .map(ProductoResponse::fromEntity)
                    .collect(Collectors.toList());

            log.info("GET /api/productos/stock-critico - Encontrados {} productos con stock crítico", productosResponse.size());
            return ResponseEntity.ok(productosResponse);

        } catch (Exception e) {
            log.error("GET /api/productos/stock-critico - Error interno: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Obtener productos por rango de precio
     * GET /api/productos/por-precio?min={precioMin}&max={precioMax}
     */
    @GetMapping("/por-precio")
    public ResponseEntity<List<ProductoResponse>> obtenerPorRangoPrecio(@RequestParam BigDecimal min,
                                                                        @RequestParam BigDecimal max) {
        log.info("GET /api/productos/por-precio?min={}&max={} - Buscando productos por rango de precio", min, max);

        try {
            List<Producto> productos = productoService.obtenerPorRangoPrecio(min, max);

            List<ProductoResponse> productosResponse = productos.stream()
                    .map(ProductoResponse::fromEntity)
                    .collect(Collectors.toList());

            log.info("GET /api/productos/por-precio - Encontrados {} productos en el rango", productosResponse.size());
            return ResponseEntity.ok(productosResponse);

        } catch (IllegalArgumentException e) {
            log.warn("GET /api/productos/por-precio - Parámetros inválidos: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            log.error("GET /api/productos/por-precio - Error interno: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Obtener productos con stock mayor a una cantidad
     * GET /api/productos/con-stock?cantidad={cantidad}
     */
    @GetMapping("/con-stock")
    public ResponseEntity<List<ProductoResponse>> obtenerConStockMayorA(@RequestParam Integer cantidad) {
        log.info("GET /api/productos/con-stock?cantidad={} - Buscando productos con stock mayor a {}", cantidad, cantidad);

        try {
            List<Producto> productos = productoService.obtenerConStockMayorA(cantidad);

            List<ProductoResponse> productosResponse = productos.stream()
                    .map(ProductoResponse::fromEntity)
                    .collect(Collectors.toList());

            log.info("GET /api/productos/con-stock - Encontrados {} productos", productosResponse.size());
            return ResponseEntity.ok(productosResponse);

        } catch (IllegalArgumentException e) {
            log.warn("GET /api/productos/con-stock - Parámetro inválido: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            log.error("GET /api/productos/con-stock - Error interno: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Obtener productos más vendidos
     * GET /api/productos/mas-vendidos
     */
    @GetMapping("/mas-vendidos")
    public ResponseEntity<List<ProductoResponse>> obtenerMasVendidos() {
        log.info("GET /api/productos/mas-vendidos - Solicitando productos más vendidos");

        try {
            List<Producto> productos = productoService.obtenerMasVendidos();

            List<ProductoResponse> productosResponse = productos.stream()
                    .map(ProductoResponse::fromEntity)
                    .collect(Collectors.toList());

            log.info("GET /api/productos/mas-vendidos - Encontrados {} productos", productosResponse.size());
            return ResponseEntity.ok(productosResponse);

        } catch (Exception e) {
            log.error("GET /api/productos/mas-vendidos - Error interno: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Crear nuevo producto
     * POST /api/productos
     */
    @PostMapping
    public ResponseEntity<ProductoResponse> crear(@Valid @RequestBody ProductoRequest productoRequest) {
        log.info("POST /api/productos - Creando nuevo producto: {}", productoRequest.getResumenParaLog());

        try {
            // Normalizar datos del request
            productoRequest.normalizar();

            // Validaciones adicionales
            if (!productoRequest.isNombreValido()) {
                log.warn("POST /api/productos - Nombre de producto contiene caracteres inválidos: {}", productoRequest.getNombre());
                return ResponseEntity.badRequest().build();
            }

            if (!productoRequest.isPrecioRazonable()) {
                log.warn("POST /api/productos - Precio fuera de rango razonable: {}", productoRequest.getPrecio());
                return ResponseEntity.badRequest().build();
            }

            if (!productoRequest.esConsistente()) {
                log.warn("POST /api/productos - Datos inconsistentes en el producto");
                return ResponseEntity.badRequest().build();
            }

            // Convertir DTO a Entity
            Producto producto = Producto.builder()
                    .nombre(productoRequest.getNombre())
                    .descripcion(productoRequest.getDescripcion())
                    .imagen(productoRequest.getImagen())  // ⭐ CAMPO IMAGEN AÑADIDO
                    .precio(productoRequest.getPrecio())
                    .stockMinimo(productoRequest.getStockMinimo())
                    .categoria(Categoria.builder().id(productoRequest.getCategoriaId()).build()) // Solo el ID para la referencia
                    .build();

            // Crear el producto a través del service
            Producto productoCreado = productoService.crear(producto);

            // Convertir Entity a Response DTO
            ProductoResponse productoResponse = ProductoResponse.fromEntity(productoCreado);

            log.info("POST /api/productos - Producto creado exitosamente con ID: {}", productoCreado.getId());
            return ResponseEntity.status(HttpStatus.CREATED).body(productoResponse);

        } catch (IllegalArgumentException e) {
            log.warn("POST /api/productos - Datos inválidos: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (RuntimeException e) {
            log.warn("POST /api/productos - Error de negocio: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        } catch (Exception e) {
            log.error("POST /api/productos - Error interno: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Actualizar producto existente
     * PUT /api/productos/{id}
     */
    @PutMapping("/{id}")
    public ResponseEntity<ProductoResponse> actualizar(@PathVariable Long id,
                                                       @Valid @RequestBody ProductoRequest productoRequest) {
        log.info("PUT /api/productos/{} - Actualizando producto: {}", id, productoRequest.getResumenParaLog());

        try {
            // Normalizar datos del request
            productoRequest.normalizar();

            // Validaciones adicionales
            if (!productoRequest.isNombreValido()) {
                log.warn("PUT /api/productos/{} - Nombre contiene caracteres inválidos: {}", id, productoRequest.getNombre());
                return ResponseEntity.badRequest().build();
            }

            if (!productoRequest.isPrecioRazonable()) {
                log.warn("PUT /api/productos/{} - Precio fuera de rango razonable: {}", id, productoRequest.getPrecio());
                return ResponseEntity.badRequest().build();
            }

            if (!productoRequest.esConsistente()) {
                log.warn("PUT /api/productos/{} - Datos inconsistentes", id);
                return ResponseEntity.badRequest().build();
            }

            // Convertir DTO a Entity
            Producto productoActualizado = Producto.builder()
                    .nombre(productoRequest.getNombre())
                    .descripcion(productoRequest.getDescripcion())
                    .imagen(productoRequest.getImagen())  // ⭐ CAMPO IMAGEN AÑADIDO
                    .precio(productoRequest.getPrecio())
                    .stockMinimo(productoRequest.getStockMinimo())
                    .categoria(Categoria.builder().id(productoRequest.getCategoriaId()).build()) // Solo el ID para la referencia
                    .build();

            // Actualizar a través del service
            Producto producto = productoService.actualizar(id, productoActualizado);

            // Convertir Entity a Response DTO
            ProductoResponse productoResponse = ProductoResponse.fromEntity(producto);

            log.info("PUT /api/productos/{} - Producto actualizado exitosamente", id);
            return ResponseEntity.ok(productoResponse);

        } catch (IllegalArgumentException e) {
            log.warn("PUT /api/productos/{} - Datos inválidos: {}", id, e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (RuntimeException e) {
            String mensaje = e.getMessage();
            if (mensaje.contains("no encontrado")) {
                log.warn("PUT /api/productos/{} - Producto no encontrado", id);
                return ResponseEntity.notFound().build();
            } else {
                log.warn("PUT /api/productos/{} - Error de negocio: {}", id, mensaje);
                return ResponseEntity.status(HttpStatus.CONFLICT).build();
            }
        } catch (Exception e) {
            log.error("PUT /api/productos/{} - Error interno: {}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Actualizar solo la imagen del producto
     * PATCH /api/productos/{id}/imagen
     */
    @PatchMapping("/{id}/imagen")
    public ResponseEntity<ProductoResponse> actualizarImagen(@PathVariable Long id,
                                                             @RequestBody Map<String, String> request) {
        log.info("PATCH /api/productos/{}/imagen - Actualizando imagen del producto", id);

        try {
            String nuevaImagen = request.get("imagen");

            // Validación básica
            if (nuevaImagen != null && !nuevaImagen.trim().isEmpty()) {
                nuevaImagen = nuevaImagen.trim();
                if (nuevaImagen.length() > 500) {
                    log.warn("PATCH /api/productos/{}/imagen - URL demasiado larga", id);
                    return ResponseEntity.badRequest().build();
                }
            } else {
                nuevaImagen = null;
            }

            // Obtener producto existente
            Producto producto = productoService.obtenerRequerido(id);

            // Actualizar solo la imagen
            producto.setImagen(nuevaImagen);
            Producto productoActualizado = productoService.actualizar(id, producto);

            // Convertir a response
            ProductoResponse productoResponse = ProductoResponse.fromEntity(productoActualizado);

            log.info("PATCH /api/productos/{}/imagen - Imagen actualizada exitosamente", id);
            return ResponseEntity.ok(productoResponse);

        } catch (IllegalArgumentException e) {
            log.warn("PATCH /api/productos/{}/imagen - Datos inválidos: {}", id, e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (RuntimeException e) {
            if (e.getMessage().contains("no encontrado")) {
                log.warn("PATCH /api/productos/{}/imagen - Producto no encontrado", id);
                return ResponseEntity.notFound().build();
            } else {
                log.warn("PATCH /api/productos/{}/imagen - Error de negocio: {}", id, e.getMessage());
                return ResponseEntity.status(HttpStatus.CONFLICT).build();
            }
        } catch (Exception e) {
            log.error("PATCH /api/productos/{}/imagen - Error interno: {}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Eliminar producto por ID
     * DELETE /api/productos/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        log.info("DELETE /api/productos/{} - Eliminando producto", id);

        try {
            productoService.eliminar(id);
            log.info("DELETE /api/productos/{} - Producto eliminado exitosamente", id);
            return ResponseEntity.noContent().build();

        } catch (IllegalArgumentException e) {
            log.warn("DELETE /api/productos/{} - ID inválido: {}", id, e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (RuntimeException e) {
            String mensaje = e.getMessage();
            if (mensaje.contains("no encontrado")) {
                log.warn("DELETE /api/productos/{} - Producto no encontrado", id);
                return ResponseEntity.notFound().build();
            } else {
                log.warn("DELETE /api/productos/{} - Error de negocio: {}", id, mensaje);
                return ResponseEntity.status(HttpStatus.CONFLICT).build();
            }
        } catch (Exception e) {
            log.error("DELETE /api/productos/{} - Error interno: {}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Verificar si existe producto por nombre
     * GET /api/productos/existe?nombre={nombre}
     */
    @GetMapping("/existe")
    public ResponseEntity<Boolean> existe(@RequestParam String nombre) {
        log.info("GET /api/productos/existe?nombre={} - Verificando existencia", nombre);

        try {
            boolean existe = productoService.existe(nombre);
            log.info("GET /api/productos/existe - Resultado: {}", existe);
            return ResponseEntity.ok(existe);

        } catch (IllegalArgumentException e) {
            log.warn("GET /api/productos/existe - Parámetro inválido: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            log.error("GET /api/productos/existe - Error interno: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Verificar disponibilidad de stock
     * GET /api/productos/{id}/stock-disponible?cantidad={cantidad}
     */
    @GetMapping("/{id}/stock-disponible")
    public ResponseEntity<Boolean> hayStockDisponible(@PathVariable Long id, @RequestParam Integer cantidad) {
        log.info("GET /api/productos/{}/stock-disponible?cantidad={} - Verificando stock", id, cantidad);

        try {
            boolean disponible = productoService.hayStockDisponible(id, cantidad);
            log.info("GET /api/productos/{}/stock-disponible - Resultado: {}", id, disponible);
            return ResponseEntity.ok(disponible);

        } catch (IllegalArgumentException e) {
            log.warn("GET /api/productos/{}/stock-disponible - Parámetros inválidos: {}", id, e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (RuntimeException e) {
            if (e.getMessage().contains("no encontrado")) {
                log.warn("GET /api/productos/{}/stock-disponible - Producto no encontrado", id);
                return ResponseEntity.notFound().build();
            } else {
                throw e;
            }
        } catch (Exception e) {
            log.error("GET /api/productos/{}/stock-disponible - Error interno: {}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Obtener estadísticas de stock
     * GET /api/productos/estadisticas-stock
     */
    @GetMapping("/estadisticas-stock")
    public ResponseEntity<ProductoService.StockStats> obtenerEstadisticasStock() {
        log.info("GET /api/productos/estadisticas-stock - Solicitando estadísticas de stock");

        try {
            ProductoService.StockStats stats = productoService.obtenerEstadisticasStock();
            log.info("GET /api/productos/estadisticas-stock - Estadísticas obtenidas");
            return ResponseEntity.ok(stats);

        } catch (Exception e) {
            log.error("GET /api/productos/estadisticas-stock - Error interno: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Contar productos por categoría
     * GET /api/productos/contar-por-categoria/{categoriaId}
     */
    @GetMapping("/contar-por-categoria/{categoriaId}")
    public ResponseEntity<Long> contarPorCategoria(@PathVariable Long categoriaId) {
        log.info("GET /api/productos/contar-por-categoria/{} - Contando productos", categoriaId);

        try {
            Long cantidad = productoService.contarPorCategoria(categoriaId);
            log.info("GET /api/productos/contar-por-categoria/{} - Cantidad: {}", categoriaId, cantidad);
            return ResponseEntity.ok(cantidad);

        } catch (IllegalArgumentException e) {
            log.warn("GET /api/productos/contar-por-categoria/{} - ID inválido: {}", categoriaId, e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            log.error("GET /api/productos/contar-por-categoria/{} - Error interno: {}", categoriaId, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Endpoint de salud para verificar que el controller está funcionando
     * GET /api/productos/health
     */
    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("ProductoController está funcionando correctamente");
    }
}