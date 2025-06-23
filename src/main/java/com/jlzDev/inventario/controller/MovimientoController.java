package com.jlzDev.inventario.controller;

import com.jlzDev.inventario.dto.request.MovimientoRequest;
import com.jlzDev.inventario.dto.response.MovimientoResponse;
import com.jlzDev.inventario.entity.Movimiento;
import com.jlzDev.inventario.service.MovimientoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Controller REST para gestión de movimientos de inventario
 * Endpoints para operaciones de entrada y salida de productos
 */
@RestController
@RequestMapping("/api/movimientos")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*") // Configurar según necesidades de CORS
public class MovimientoController {

    private final MovimientoService movimientoService;

    /**
     * Obtener todos los movimientos
     * GET /api/movimientos
     */
    @GetMapping
    public ResponseEntity<List<MovimientoResponse>> obtenerTodos() {
        log.info("GET /api/movimientos - Solicitando todos los movimientos");

        try {
            List<Movimiento> movimientos = movimientoService.obtenerTodos();

            List<MovimientoResponse> movimientosResponse = movimientos.stream()
                    .map(MovimientoResponse::fromEntity)
                    .collect(Collectors.toList());

            log.info("GET /api/movimientos - Encontrados {} movimientos", movimientosResponse.size());
            return ResponseEntity.ok(movimientosResponse);

        } catch (Exception e) {
            log.error("GET /api/movimientos - Error al obtener movimientos: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Obtener historial paginado de movimientos
     * GET /api/movimientos/historial?pagina={pagina}&tamaño={tamaño}
     */
    @GetMapping("/historial")
    public ResponseEntity<Page<MovimientoResponse>> obtenerHistorialPaginado(
            @RequestParam(defaultValue = "0") int pagina,
            @RequestParam(defaultValue = "10") int tamaño) {

        log.info("GET /api/movimientos/historial?pagina={}&tamaño={} - Solicitando historial paginado", pagina, tamaño);

        try {
            Page<Movimiento> movimientosPage = movimientoService.obtenerHistorialPaginado(pagina, tamaño);

            Page<MovimientoResponse> movimientosResponsePage = movimientosPage
                    .map(MovimientoResponse::fromEntity);

            log.info("GET /api/movimientos/historial - Página {}/{}, {} movimientos",
                    pagina + 1, movimientosPage.getTotalPages(), movimientosPage.getNumberOfElements());
            return ResponseEntity.ok(movimientosResponsePage);

        } catch (IllegalArgumentException e) {
            log.warn("GET /api/movimientos/historial - Parámetros inválidos: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            log.error("GET /api/movimientos/historial - Error interno: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Obtener movimiento por ID
     * GET /api/movimientos/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<MovimientoResponse> obtenerPorId(@PathVariable Long id) {
        log.info("GET /api/movimientos/{} - Solicitando movimiento por ID", id);

        try {
            Optional<Movimiento> movimientoOpt = movimientoService.obtenerPorId(id);

            if (movimientoOpt.isPresent()) {
                MovimientoResponse movimientoResponse = MovimientoResponse.fromEntity(movimientoOpt.get());
                log.info("GET /api/movimientos/{} - Movimiento encontrado: {} {}",
                        id, movimientoResponse.getTipoMovimiento(), movimientoResponse.getCantidad());
                return ResponseEntity.ok(movimientoResponse);
            } else {
                log.warn("GET /api/movimientos/{} - Movimiento no encontrado", id);
                return ResponseEntity.notFound().build();
            }

        } catch (IllegalArgumentException e) {
            log.warn("GET /api/movimientos/{} - ID inválido: {}", id, e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            log.error("GET /api/movimientos/{} - Error interno: {}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Obtener movimientos por producto
     * GET /api/movimientos/por-producto/{productoId}
     */
    @GetMapping("/por-producto/{productoId}")
    public ResponseEntity<List<MovimientoResponse>> obtenerPorProducto(@PathVariable Long productoId) {
        log.info("GET /api/movimientos/por-producto/{} - Solicitando movimientos por producto", productoId);

        try {
            List<Movimiento> movimientos = movimientoService.obtenerPorProducto(productoId);

            List<MovimientoResponse> movimientosResponse = movimientos.stream()
                    .map(MovimientoResponse::fromEntity)
                    .collect(Collectors.toList());

            log.info("GET /api/movimientos/por-producto/{} - Encontrados {} movimientos",
                    productoId, movimientosResponse.size());
            return ResponseEntity.ok(movimientosResponse);

        } catch (IllegalArgumentException e) {
            log.warn("GET /api/movimientos/por-producto/{} - ID de producto inválido: {}", productoId, e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (RuntimeException e) {
            if (e.getMessage().contains("no encontrado")) {
                log.warn("GET /api/movimientos/por-producto/{} - Producto no encontrado", productoId);
                return ResponseEntity.notFound().build();
            } else {
                throw e;
            }
        } catch (Exception e) {
            log.error("GET /api/movimientos/por-producto/{} - Error interno: {}", productoId, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Obtener movimientos por usuario
     * GET /api/movimientos/por-usuario/{usuarioId}
     */
    @GetMapping("/por-usuario/{usuarioId}")
    public ResponseEntity<List<MovimientoResponse>> obtenerPorUsuario(@PathVariable Long usuarioId) {
        log.info("GET /api/movimientos/por-usuario/{} - Solicitando movimientos por usuario", usuarioId);

        try {
            List<Movimiento> movimientos = movimientoService.obtenerPorUsuario(usuarioId);

            List<MovimientoResponse> movimientosResponse = movimientos.stream()
                    .map(MovimientoResponse::fromEntity)
                    .collect(Collectors.toList());

            log.info("GET /api/movimientos/por-usuario/{} - Encontrados {} movimientos",
                    usuarioId, movimientosResponse.size());
            return ResponseEntity.ok(movimientosResponse);

        } catch (IllegalArgumentException e) {
            log.warn("GET /api/movimientos/por-usuario/{} - ID de usuario inválido: {}", usuarioId, e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (RuntimeException e) {
            if (e.getMessage().contains("no encontrado")) {
                log.warn("GET /api/movimientos/por-usuario/{} - Usuario no encontrado", usuarioId);
                return ResponseEntity.notFound().build();
            } else {
                throw e;
            }
        } catch (Exception e) {
            log.error("GET /api/movimientos/por-usuario/{} - Error interno: {}", usuarioId, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Obtener movimientos por tipo
     * GET /api/movimientos/por-tipo/{tipo}
     */
    @GetMapping("/por-tipo/{tipo}")
    public ResponseEntity<List<MovimientoResponse>> obtenerPorTipo(@PathVariable Movimiento.TipoMovimiento tipo) {
        log.info("GET /api/movimientos/por-tipo/{} - Solicitando movimientos por tipo", tipo);

        try {
            List<Movimiento> movimientos = movimientoService.obtenerPorTipo(tipo);

            List<MovimientoResponse> movimientosResponse = movimientos.stream()
                    .map(MovimientoResponse::fromEntity)
                    .collect(Collectors.toList());

            log.info("GET /api/movimientos/por-tipo/{} - Encontrados {} movimientos",
                    tipo, movimientosResponse.size());
            return ResponseEntity.ok(movimientosResponse);

        } catch (Exception e) {
            log.error("GET /api/movimientos/por-tipo/{} - Error interno: {}", tipo, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Obtener movimientos por rango de fechas
     * GET /api/movimientos/por-fecha?inicio={yyyy-MM-dd HH:mm:ss}&fin={yyyy-MM-dd HH:mm:ss}
     */
    @GetMapping("/por-fecha")
    public ResponseEntity<List<MovimientoResponse>> obtenerPorRangoFecha(
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime inicio,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime fin) {

        log.info("GET /api/movimientos/por-fecha?inicio={}&fin={} - Buscando por rango de fechas", inicio, fin);

        try {
            List<Movimiento> movimientos = movimientoService.obtenerPorRangoFecha(inicio, fin);

            List<MovimientoResponse> movimientosResponse = movimientos.stream()
                    .map(MovimientoResponse::fromEntity)
                    .collect(Collectors.toList());

            log.info("GET /api/movimientos/por-fecha - Encontrados {} movimientos", movimientosResponse.size());
            return ResponseEntity.ok(movimientosResponse);

        } catch (IllegalArgumentException e) {
            log.warn("GET /api/movimientos/por-fecha - Parámetros de fecha inválidos: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            log.error("GET /api/movimientos/por-fecha - Error interno: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Obtener movimientos recientes (últimos N días)
     * GET /api/movimientos/recientes?dias={dias}
     */
    @GetMapping("/recientes")
    public ResponseEntity<List<MovimientoResponse>> obtenerRecientes(@RequestParam(defaultValue = "7") int dias) {
        log.info("GET /api/movimientos/recientes?dias={} - Solicitando movimientos recientes", dias);

        try {
            List<Movimiento> movimientos = movimientoService.obtenerRecientes(dias);

            List<MovimientoResponse> movimientosResponse = movimientos.stream()
                    .map(MovimientoResponse::fromEntity)
                    .collect(Collectors.toList());

            log.info("GET /api/movimientos/recientes - Encontrados {} movimientos de los últimos {} días",
                    movimientosResponse.size(), dias);
            return ResponseEntity.ok(movimientosResponse);

        } catch (IllegalArgumentException e) {
            log.warn("GET /api/movimientos/recientes - Parámetro días inválido: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            log.error("GET /api/movimientos/recientes - Error interno: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Obtener movimientos por categoría de producto
     * GET /api/movimientos/por-categoria/{categoriaId}
     */
    @GetMapping("/por-categoria/{categoriaId}")
    public ResponseEntity<List<MovimientoResponse>> obtenerPorCategoria(@PathVariable Long categoriaId) {
        log.info("GET /api/movimientos/por-categoria/{} - Solicitando movimientos por categoría", categoriaId);

        try {
            List<Movimiento> movimientos = movimientoService.obtenerPorCategoria(categoriaId);

            List<MovimientoResponse> movimientosResponse = movimientos.stream()
                    .map(MovimientoResponse::fromEntity)
                    .collect(Collectors.toList());

            log.info("GET /api/movimientos/por-categoria/{} - Encontrados {} movimientos",
                    categoriaId, movimientosResponse.size());
            return ResponseEntity.ok(movimientosResponse);

        } catch (IllegalArgumentException e) {
            log.warn("GET /api/movimientos/por-categoria/{} - ID de categoría inválido: {}", categoriaId, e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            log.error("GET /api/movimientos/por-categoria/{} - Error interno: {}", categoriaId, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * CREAR ENTRADA DE INVENTARIO
     * POST /api/movimientos/entrada
     */
    @PostMapping("/entrada")
    public ResponseEntity<MovimientoResponse> crearEntrada(@Valid @RequestBody MovimientoRequest movimientoRequest) {
        log.info("POST /api/movimientos/entrada - Creando entrada: {}", movimientoRequest.getResumenParaLog());

        try {
            // Validar que es una entrada
            if (!movimientoRequest.esEntrada()) {
                log.warn("POST /api/movimientos/entrada - El tipo debe ser ENTRADA");
                return ResponseEntity.badRequest().build();
            }

            // Normalizar datos del request
            movimientoRequest.normalizar();

            // Validaciones adicionales
            if (!movimientoRequest.isCantidadRazonable()) {
                log.warn("POST /api/movimientos/entrada - Cantidad no razonable: {}", movimientoRequest.getCantidad());
                return ResponseEntity.badRequest().build();
            }

            if (!movimientoRequest.esMotivoCoherente()) {
                log.warn("POST /api/movimientos/entrada - Motivo no coherente con entrada: {}", movimientoRequest.getMotivo());
                return ResponseEntity.badRequest().build();
            }

            // Crear entrada a través del service
            Movimiento movimientoCreado = movimientoService.crearEntrada(
                    movimientoRequest.getProductoId(),
                    movimientoRequest.getUsuarioId(),
                    movimientoRequest.getCantidad(),
                    movimientoRequest.getMotivo()
            );

            // Convertir Entity a Response DTO
            MovimientoResponse movimientoResponse = MovimientoResponse.fromEntity(movimientoCreado);

            log.info("POST /api/movimientos/entrada - Entrada creada exitosamente con ID: {}", movimientoCreado.getId());
            return ResponseEntity.status(HttpStatus.CREATED).body(movimientoResponse);

        } catch (IllegalArgumentException e) {
            log.warn("POST /api/movimientos/entrada - Datos inválidos: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (RuntimeException e) {
            log.warn("POST /api/movimientos/entrada - Error de negocio: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        } catch (Exception e) {
            log.error("POST /api/movimientos/entrada - Error interno: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * CREAR SALIDA DE INVENTARIO
     * POST /api/movimientos/salida
     */
    @PostMapping("/salida")
    public ResponseEntity<MovimientoResponse> crearSalida(@Valid @RequestBody MovimientoRequest movimientoRequest) {
        log.info("POST /api/movimientos/salida - Creando salida: {}", movimientoRequest.getResumenParaLog());

        try {
            // Validar que es una salida
            if (!movimientoRequest.esSalida()) {
                log.warn("POST /api/movimientos/salida - El tipo debe ser SALIDA");
                return ResponseEntity.badRequest().build();
            }

            // Normalizar datos del request
            movimientoRequest.normalizar();

            // Validaciones adicionales
            if (!movimientoRequest.isCantidadRazonable()) {
                log.warn("POST /api/movimientos/salida - Cantidad no razonable: {}", movimientoRequest.getCantidad());
                return ResponseEntity.badRequest().build();
            }

            if (!movimientoRequest.esMotivoCoherente()) {
                log.warn("POST /api/movimientos/salida - Motivo no coherente con salida: {}", movimientoRequest.getMotivo());
                return ResponseEntity.badRequest().build();
            }

            // Crear salida a través del service
            Movimiento movimientoCreado = movimientoService.crearSalida(
                    movimientoRequest.getProductoId(),
                    movimientoRequest.getUsuarioId(),
                    movimientoRequest.getCantidad(),
                    movimientoRequest.getMotivo()
            );

            // Convertir Entity a Response DTO
            MovimientoResponse movimientoResponse = MovimientoResponse.fromEntity(movimientoCreado);

            log.info("POST /api/movimientos/salida - Salida creada exitosamente con ID: {}", movimientoCreado.getId());
            return ResponseEntity.status(HttpStatus.CREATED).body(movimientoResponse);

        } catch (IllegalArgumentException e) {
            log.warn("POST /api/movimientos/salida - Datos inválidos: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (RuntimeException e) {
            log.warn("POST /api/movimientos/salida - Error de negocio: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        } catch (Exception e) {
            log.error("POST /api/movimientos/salida - Error interno: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * CREAR MOVIMIENTO GENÉRICO (entrada o salida)
     * POST /api/movimientos
     */
    @PostMapping
    public ResponseEntity<MovimientoResponse> crear(@Valid @RequestBody MovimientoRequest movimientoRequest) {
        log.info("POST /api/movimientos - Creando movimiento: {}", movimientoRequest.getResumenParaLog());

        try {
            // Normalizar datos del request
            movimientoRequest.normalizar();

            // Validaciones adicionales
            if (!movimientoRequest.esConsistente()) {
                log.warn("POST /api/movimientos - Datos inconsistentes");
                return ResponseEntity.badRequest().build();
            }

            // Crear movimiento a través del service
            Movimiento movimientoCreado = movimientoService.crearMovimiento(
                    movimientoRequest.getProductoId(),
                    movimientoRequest.getUsuarioId(),
                    movimientoRequest.getTipoMovimiento(),
                    movimientoRequest.getCantidad(),
                    movimientoRequest.getMotivo()
            );

            // Convertir Entity a Response DTO
            MovimientoResponse movimientoResponse = MovimientoResponse.fromEntity(movimientoCreado);

            log.info("POST /api/movimientos - Movimiento {} creado exitosamente con ID: {}",
                    movimientoRequest.getTipoMovimiento(), movimientoCreado.getId());
            return ResponseEntity.status(HttpStatus.CREATED).body(movimientoResponse);

        } catch (IllegalArgumentException e) {
            log.warn("POST /api/movimientos - Datos inválidos: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (RuntimeException e) {
            log.warn("POST /api/movimientos - Error de negocio: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        } catch (Exception e) {
            log.error("POST /api/movimientos - Error interno: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Obtener resumen de movimientos por producto
     * GET /api/movimientos/resumen-producto/{productoId}
     */
    @GetMapping("/resumen-producto/{productoId}")
    public ResponseEntity<MovimientoService.ResumenMovimientoProducto> obtenerResumenPorProducto(@PathVariable Long productoId) {
        log.info("GET /api/movimientos/resumen-producto/{} - Solicitando resumen por producto", productoId);

        try {
            MovimientoService.ResumenMovimientoProducto resumen = movimientoService.obtenerResumenPorProducto(productoId);
            log.info("GET /api/movimientos/resumen-producto/{} - Resumen obtenido", productoId);
            return ResponseEntity.ok(resumen);

        } catch (IllegalArgumentException e) {
            log.warn("GET /api/movimientos/resumen-producto/{} - ID de producto inválido: {}", productoId, e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (RuntimeException e) {
            if (e.getMessage().contains("no encontrado")) {
                log.warn("GET /api/movimientos/resumen-producto/{} - Producto no encontrado", productoId);
                return ResponseEntity.notFound().build();
            } else {
                throw e;
            }
        } catch (Exception e) {
            log.error("GET /api/movimientos/resumen-producto/{} - Error interno: {}", productoId, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Obtener estadísticas de movimientos por periodo
     * GET /api/movimientos/estadisticas?inicio={yyyy-MM-dd HH:mm:ss}&fin={yyyy-MM-dd HH:mm:ss}
     */
    @GetMapping("/estadisticas")
    public ResponseEntity<MovimientoService.EstadisticasMovimientos> obtenerEstadisticasPorPeriodo(
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime inicio,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime fin) {

        log.info("GET /api/movimientos/estadisticas?inicio={}&fin={} - Solicitando estadísticas", inicio, fin);

        try {
            MovimientoService.EstadisticasMovimientos estadisticas =
                    movimientoService.obtenerEstadisticasPorPeriodo(inicio, fin);

            log.info("GET /api/movimientos/estadisticas - Estadísticas obtenidas");
            return ResponseEntity.ok(estadisticas);

        } catch (IllegalArgumentException e) {
            log.warn("GET /api/movimientos/estadisticas - Parámetros de fecha inválidos: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            log.error("GET /api/movimientos/estadisticas - Error interno: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Obtener productos más movidos en un periodo
     * GET /api/movimientos/productos-mas-movidos?inicio={yyyy-MM-dd HH:mm:ss}&fin={yyyy-MM-dd HH:mm:ss}&limite={limite}
     */
    @GetMapping("/productos-mas-movidos")
    public ResponseEntity<List<MovimientoService.ProductoMovimientos>> obtenerProductosMasMovidos(
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime inicio,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime fin,
            @RequestParam(defaultValue = "10") int limite) {

        log.info("GET /api/movimientos/productos-mas-movidos?inicio={}&fin={}&limite={} - Solicitando ranking",
                inicio, fin, limite);

        try {
            List<MovimientoService.ProductoMovimientos> ranking =
                    movimientoService.obtenerProductosMasMovidos(inicio, fin, limite);

            log.info("GET /api/movimientos/productos-mas-movidos - Ranking de {} productos obtenido", ranking.size());
            return ResponseEntity.ok(ranking);

        } catch (IllegalArgumentException e) {
            log.warn("GET /api/movimientos/productos-mas-movidos - Parámetros inválidos: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            log.error("GET /api/movimientos/productos-mas-movidos - Error interno: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Buscar movimientos por motivo
     * GET /api/movimientos/buscar-motivo?motivo={motivo}
     */
    @GetMapping("/buscar-motivo")
    public ResponseEntity<List<MovimientoResponse>> buscarPorMotivo(@RequestParam String motivo) {
        log.info("GET /api/movimientos/buscar-motivo?motivo={} - Buscando por motivo", motivo);

        try {
            List<Movimiento> movimientos = movimientoService.buscarPorMotivo(motivo);

            List<MovimientoResponse> movimientosResponse = movimientos.stream()
                    .map(MovimientoResponse::fromEntity)
                    .collect(Collectors.toList());

            log.info("GET /api/movimientos/buscar-motivo - Encontrados {} movimientos", movimientosResponse.size());
            return ResponseEntity.ok(movimientosResponse);

        } catch (IllegalArgumentException e) {
            log.warn("GET /api/movimientos/buscar-motivo - Parámetro motivo inválido: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            log.error("GET /api/movimientos/buscar-motivo - Error interno: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Contar movimientos por tipo
     * GET /api/movimientos/contar-por-tipo/{tipo}
     */
    @GetMapping("/contar-por-tipo/{tipo}")
    public ResponseEntity<Long> contarPorTipo(@PathVariable Movimiento.TipoMovimiento tipo) {
        log.info("GET /api/movimientos/contar-por-tipo/{} - Contando movimientos", tipo);

        try {
            Long cantidad = movimientoService.contarPorTipo(tipo);
            log.info("GET /api/movimientos/contar-por-tipo/{} - Cantidad: {}", tipo, cantidad);
            return ResponseEntity.ok(cantidad);

        } catch (Exception e) {
            log.error("GET /api/movimientos/contar-por-tipo/{} - Error interno: {}", tipo, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Obtener últimos movimientos por usuario
     * GET /api/movimientos/ultimos-por-usuario/{usuarioId}?limite={limite}
     */
    @GetMapping("/ultimos-por-usuario/{usuarioId}")
    public ResponseEntity<Page<MovimientoResponse>> obtenerUltimosPorUsuario(@PathVariable Long usuarioId,
                                                                             @RequestParam(defaultValue = "5") int limite) {
        log.info("GET /api/movimientos/ultimos-por-usuario/{}?limite={} - Solicitando últimos movimientos", usuarioId, limite);

        try {
            Page<Movimiento> movimientosPage = movimientoService.obtenerUltimosPorUsuario(usuarioId, limite);

            Page<MovimientoResponse> movimientosResponsePage = movimientosPage
                    .map(MovimientoResponse::fromEntity);

            log.info("GET /api/movimientos/ultimos-por-usuario/{} - Encontrados {} movimientos",
                    usuarioId, movimientosResponsePage.getNumberOfElements());
            return ResponseEntity.ok(movimientosResponsePage);

        } catch (IllegalArgumentException e) {
            log.warn("GET /api/movimientos/ultimos-por-usuario/{} - Parámetros inválidos: {}", usuarioId, e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (RuntimeException e) {
            if (e.getMessage().contains("no encontrado")) {
                log.warn("GET /api/movimientos/ultimos-por-usuario/{} - Usuario no encontrado", usuarioId);
                return ResponseEntity.notFound().build();
            } else {
                throw e;
            }
        } catch (Exception e) {
            log.error("GET /api/movimientos/ultimos-por-usuario/{} - Error interno: {}", usuarioId, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Endpoint de salud para verificar que el controller está funcionando
     * GET /api/movimientos/health
     */
    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("MovimientoController está funcionando correctamente");
    }
}