package com.jlzDev.inventario.controller;

import com.jlzDev.inventario.dto.request.CategoriaRequest;
import com.jlzDev.inventario.dto.response.CategoriaResponse;
import com.jlzDev.inventario.entity.Categoria;
import com.jlzDev.inventario.service.CategoriaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Controller REST para gestión de categorías
 * Endpoints para operaciones CRUD de categorías
 */
@RestController
@RequestMapping("/api/categorias")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*") // Configurar según necesidades de CORS
public class CategoriaController {

    private final CategoriaService categoriaService;

    /**
     * Obtener todas las categorías
     * GET /api/categorias
     */
    @GetMapping
    public ResponseEntity<List<CategoriaResponse>> obtenerTodas() {
        log.info("GET /api/categorias - Solicitando todas las categorías");

        try {
            List<Categoria> categorias = categoriaService.obtenerTodas();

            List<CategoriaResponse> categoriasResponse = categorias.stream()
                    .map(CategoriaResponse::fromEntity)
                    .collect(Collectors.toList());

            log.info("GET /api/categorias - Encontradas {} categorías", categoriasResponse.size());
            return ResponseEntity.ok(categoriasResponse);

        } catch (Exception e) {
            log.error("GET /api/categorias - Error al obtener categorías: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Obtener categoría por ID
     * GET /api/categorias/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<CategoriaResponse> obtenerPorId(@PathVariable Long id) {
        log.info("GET /api/categorias/{} - Solicitando categoría por ID", id);

        try {
            Optional<Categoria> categoriaOpt = categoriaService.obtenerPorId(id);

            if (categoriaOpt.isPresent()) {
                CategoriaResponse categoriaResponse = CategoriaResponse.fromEntity(categoriaOpt.get());
                log.info("GET /api/categorias/{} - Categoría encontrada: {}", id, categoriaResponse.getNombre());
                return ResponseEntity.ok(categoriaResponse);
            } else {
                log.warn("GET /api/categorias/{} - Categoría no encontrada", id);
                return ResponseEntity.notFound().build();
            }

        } catch (IllegalArgumentException e) {
            log.warn("GET /api/categorias/{} - ID inválido: {}", id, e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            log.error("GET /api/categorias/{} - Error interno: {}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Obtener categoría por nombre
     * GET /api/categorias/buscar?nombre={nombre}
     */
    @GetMapping("/buscar")
    public ResponseEntity<CategoriaResponse> obtenerPorNombre(@RequestParam String nombre) {
        log.info("GET /api/categorias/buscar?nombre={} - Buscando categoría por nombre", nombre);

        try {
            Optional<Categoria> categoriaOpt = categoriaService.obtenerPorNombre(nombre);

            if (categoriaOpt.isPresent()) {
                CategoriaResponse categoriaResponse = CategoriaResponse.fromEntity(categoriaOpt.get());
                log.info("GET /api/categorias/buscar - Categoría encontrada: {}", categoriaResponse.getNombre());
                return ResponseEntity.ok(categoriaResponse);
            } else {
                log.warn("GET /api/categorias/buscar - Categoría no encontrada con nombre: {}", nombre);
                return ResponseEntity.notFound().build();
            }

        } catch (IllegalArgumentException e) {
            log.warn("GET /api/categorias/buscar - Parámetro inválido: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            log.error("GET /api/categorias/buscar - Error interno: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Buscar categorías que contengan el texto en el nombre
     * GET /api/categorias/buscar-similares?nombre={nombre}
     */
    @GetMapping("/buscar-similares")
    public ResponseEntity<List<CategoriaResponse>> buscarSimilares(@RequestParam String nombre) {
        log.info("GET /api/categorias/buscar-similares?nombre={} - Buscando categorías similares", nombre);

        try {
            List<Categoria> categorias = categoriaService.buscarPorNombre(nombre);

            List<CategoriaResponse> categoriasResponse = categorias.stream()
                    .map(CategoriaResponse::fromEntity)
                    .collect(Collectors.toList());

            log.info("GET /api/categorias/buscar-similares - Encontradas {} categorías similares", categoriasResponse.size());
            return ResponseEntity.ok(categoriasResponse);

        } catch (IllegalArgumentException e) {
            log.warn("GET /api/categorias/buscar-similares - Parámetro inválido: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            log.error("GET /api/categorias/buscar-similares - Error interno: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Obtener categorías que tienen productos asociados
     * GET /api/categorias/con-productos
     */
    @GetMapping("/con-productos")
    public ResponseEntity<List<CategoriaResponse>> obtenerConProductos() {
        log.info("GET /api/categorias/con-productos - Solicitando categorías con productos");

        try {
            List<Categoria> categorias = categoriaService.obtenerCategoriasConProductos();

            List<CategoriaResponse> categoriasResponse = categorias.stream()
                    .map(CategoriaResponse::fromEntity)
                    .collect(Collectors.toList());

            log.info("GET /api/categorias/con-productos - Encontradas {} categorías con productos", categoriasResponse.size());
            return ResponseEntity.ok(categoriasResponse);

        } catch (Exception e) {
            log.error("GET /api/categorias/con-productos - Error interno: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Obtener categorías que NO tienen productos asociados
     * GET /api/categorias/sin-productos
     */
    @GetMapping("/sin-productos")
    public ResponseEntity<List<CategoriaResponse>> obtenerSinProductos() {
        log.info("GET /api/categorias/sin-productos - Solicitando categorías sin productos");

        try {
            List<Categoria> categorias = categoriaService.obtenerCategoriasSinProductos();

            List<CategoriaResponse> categoriasResponse = categorias.stream()
                    .map(CategoriaResponse::fromEntity)
                    .collect(Collectors.toList());

            log.info("GET /api/categorias/sin-productos - Encontradas {} categorías sin productos", categoriasResponse.size());
            return ResponseEntity.ok(categoriasResponse);

        } catch (Exception e) {
            log.error("GET /api/categorias/sin-productos - Error interno: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Crear nueva categoría
     * POST /api/categorias
     */
    @PostMapping
    public ResponseEntity<CategoriaResponse> crear(@Valid @RequestBody CategoriaRequest categoriaRequest) {
        log.info("POST /api/categorias - Creando nueva categoría: {}", categoriaRequest.getResumenParaLog());

        try {
            // Normalizar datos del request
            categoriaRequest.normalizar();

            // Validaciones adicionales
            if (!categoriaRequest.isNombreValido()) {
                log.warn("POST /api/categorias - Nombre de categoría contiene caracteres inválidos: {}", categoriaRequest.getNombre());
                return ResponseEntity.badRequest().build();
            }

            // Convertir DTO a Entity
            Categoria categoria = Categoria.builder()
                    .nombre(categoriaRequest.getNombre())
                    .descripcion(categoriaRequest.getDescripcion())
                    .build();

            // Crear la categoría a través del service
            Categoria categoriaCreada = categoriaService.crear(categoria);

            // Convertir Entity a Response DTO
            CategoriaResponse categoriaResponse = CategoriaResponse.fromEntity(categoriaCreada);

            log.info("POST /api/categorias - Categoría creada exitosamente con ID: {}", categoriaCreada.getId());
            return ResponseEntity.status(HttpStatus.CREATED).body(categoriaResponse);

        } catch (IllegalArgumentException e) {
            log.warn("POST /api/categorias - Datos inválidos: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (RuntimeException e) {
            log.warn("POST /api/categorias - Error de negocio: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        } catch (Exception e) {
            log.error("POST /api/categorias - Error interno: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Actualizar categoría existente
     * PUT /api/categorias/{id}
     */
    @PutMapping("/{id}")
    public ResponseEntity<CategoriaResponse> actualizar(@PathVariable Long id,
                                                        @Valid @RequestBody CategoriaRequest categoriaRequest) {
        log.info("PUT /api/categorias/{} - Actualizando categoría: {}", id, categoriaRequest.getResumenParaLog());

        try {
            // Normalizar datos del request
            categoriaRequest.normalizar();

            // Validaciones adicionales
            if (!categoriaRequest.isNombreValido()) {
                log.warn("PUT /api/categorias/{} - Nombre de categoría contiene caracteres inválidos: {}", id, categoriaRequest.getNombre());
                return ResponseEntity.badRequest().build();
            }

            // Convertir DTO a Entity
            Categoria categoriaActualizada = Categoria.builder()
                    .nombre(categoriaRequest.getNombre())
                    .descripcion(categoriaRequest.getDescripcion())
                    .build();

            // Actualizar a través del service
            Categoria categoria = categoriaService.actualizar(id, categoriaActualizada);

            // Convertir Entity a Response DTO
            CategoriaResponse categoriaResponse = CategoriaResponse.fromEntity(categoria);

            log.info("PUT /api/categorias/{} - Categoría actualizada exitosamente", id);
            return ResponseEntity.ok(categoriaResponse);

        } catch (IllegalArgumentException e) {
            log.warn("PUT /api/categorias/{} - Datos inválidos: {}", id, e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (RuntimeException e) {
            String mensaje = e.getMessage();
            if (mensaje.contains("no encontrada")) {
                log.warn("PUT /api/categorias/{} - Categoría no encontrada", id);
                return ResponseEntity.notFound().build();
            } else {
                log.warn("PUT /api/categorias/{} - Error de negocio: {}", id, mensaje);
                return ResponseEntity.status(HttpStatus.CONFLICT).build();
            }
        } catch (Exception e) {
            log.error("PUT /api/categorias/{} - Error interno: {}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Eliminar categoría por ID
     * DELETE /api/categorias/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        log.info("DELETE /api/categorias/{} - Eliminando categoría", id);

        try {
            categoriaService.eliminar(id);
            log.info("DELETE /api/categorias/{} - Categoría eliminada exitosamente", id);
            return ResponseEntity.noContent().build();

        } catch (IllegalArgumentException e) {
            log.warn("DELETE /api/categorias/{} - ID inválido: {}", id, e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (RuntimeException e) {
            String mensaje = e.getMessage();
            if (mensaje.contains("no encontrada")) {
                log.warn("DELETE /api/categorias/{} - Categoría no encontrada", id);
                return ResponseEntity.notFound().build();
            } else {
                log.warn("DELETE /api/categorias/{} - Error de negocio: {}", id, mensaje);
                return ResponseEntity.status(HttpStatus.CONFLICT).build();
            }
        } catch (Exception e) {
            log.error("DELETE /api/categorias/{} - Error interno: {}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Verificar si existe una categoría por nombre
     * GET /api/categorias/existe?nombre={nombre}
     */
    @GetMapping("/existe")
    public ResponseEntity<Boolean> existe(@RequestParam String nombre) {
        log.info("GET /api/categorias/existe?nombre={} - Verificando existencia de la categoría", nombre);

        try {
            boolean existe = categoriaService.existe(nombre);
            log.info("GET /api/categorias/existe - Resultado: {}", existe);
            return ResponseEntity.ok(existe);

        } catch (IllegalArgumentException e) {
            log.warn("GET /api/categorias/existe - Parámetro inválido: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            log.error("GET /api/categorias/existe - Error interno: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Contar productos por categoría
     * GET /api/categorias/{id}/productos/contar
     */
    @GetMapping("/{id}/productos/contar")
    public ResponseEntity<Long> contarProductos(@PathVariable Long id) {
        log.info("GET /api/categorias/{}/productos/contar - Contando productos de la categoría", id);

        try {
            Long cantidad = categoriaService.contarProductos(id);
            log.info("GET /api/categorias/{}/productos/contar - Cantidad: {}", id, cantidad);
            return ResponseEntity.ok(cantidad);

        } catch (IllegalArgumentException e) {
            log.warn("GET /api/categorias/{}/productos/contar - ID inválido: {}", id, e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            log.error("GET /api/categorias/{}/productos/contar - Error interno: {}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Verificar si la categoría tiene productos asociados
     * GET /api/categorias/{id}/tiene-productos
     */
    @GetMapping("/{id}/tiene-productos")
    public ResponseEntity<Boolean> tieneProductosAsociados(@PathVariable Long id) {
        log.info("GET /api/categorias/{}/tiene-productos - Verificando si tiene productos asociados", id);

        try {
            boolean tieneProductos = categoriaService.tieneProductosAsociados(id);
            log.info("GET /api/categorias/{}/tiene-productos - Resultado: {}", id, tieneProductos);
            return ResponseEntity.ok(tieneProductos);

        } catch (IllegalArgumentException e) {
            log.warn("GET /api/categorias/{}/tiene-productos - ID inválido: {}", id, e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            log.error("GET /api/categorias/{}/tiene-productos - Error interno: {}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Endpoint de salud para verificar que el controller está funcionando
     * GET /api/categorias/health
     */
    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("CategoriaController está funcionando correctamente");
    }
}