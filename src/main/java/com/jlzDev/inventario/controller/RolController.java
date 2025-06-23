package com.jlzDev.inventario.controller;

import com.jlzDev.inventario.dto.request.RolRequest;
import com.jlzDev.inventario.dto.response.RolResponse;
import com.jlzDev.inventario.entity.Rol;
import com.jlzDev.inventario.service.RolService;
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
 * Controller REST para gestión de roles
 * Endpoints para operaciones CRUD de roles
 */
@RestController
@RequestMapping("/api/roles")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*") // Configurar según necesidades de CORS
public class RolController {

    private final RolService rolService;

    /**
     * Obtener todos los roles
     * GET /api/roles
     */
    @GetMapping
    public ResponseEntity<List<RolResponse>> obtenerTodos() {
        log.info("GET /api/roles - Solicitando todos los roles");

        try {
            List<Rol> roles = rolService.obtenerTodos();

            List<RolResponse> rolesResponse = roles.stream()
                    .map(RolResponse::fromEntity)
                    .collect(Collectors.toList());

            log.info("GET /api/roles - Encontrados {} roles", rolesResponse.size());
            return ResponseEntity.ok(rolesResponse);

        } catch (Exception e) {
            log.error("GET /api/roles - Error al obtener roles: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Obtener rol por ID
     * GET /api/roles/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<RolResponse> obtenerPorId(@PathVariable Long id) {
        log.info("GET /api/roles/{} - Solicitando rol por ID", id);

        try {
            Optional<Rol> rolOpt = rolService.obtenerPorId(id);

            if (rolOpt.isPresent()) {
                RolResponse rolResponse = RolResponse.fromEntity(rolOpt.get());
                log.info("GET /api/roles/{} - Rol encontrado: {}", id, rolResponse.getNombre());
                return ResponseEntity.ok(rolResponse);
            } else {
                log.warn("GET /api/roles/{} - Rol no encontrado", id);
                return ResponseEntity.notFound().build();
            }

        } catch (IllegalArgumentException e) {
            log.warn("GET /api/roles/{} - ID inválido: {}", id, e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            log.error("GET /api/roles/{} - Error interno: {}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Obtener rol por nombre
     * GET /api/roles/buscar?nombre={nombre}
     */
    @GetMapping("/buscar")
    public ResponseEntity<RolResponse> obtenerPorNombre(@RequestParam String nombre) {
        log.info("GET /api/roles/buscar?nombre={} - Buscando rol por nombre", nombre);

        try {
            Optional<Rol> rolOpt = rolService.obtenerPorNombre(nombre);

            if (rolOpt.isPresent()) {
                RolResponse rolResponse = RolResponse.fromEntity(rolOpt.get());
                log.info("GET /api/roles/buscar - Rol encontrado: {}", rolResponse.getNombre());
                return ResponseEntity.ok(rolResponse);
            } else {
                log.warn("GET /api/roles/buscar - Rol no encontrado con nombre: {}", nombre);
                return ResponseEntity.notFound().build();
            }

        } catch (IllegalArgumentException e) {
            log.warn("GET /api/roles/buscar - Parámetro inválido: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            log.error("GET /api/roles/buscar - Error interno: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Crear nuevo rol
     * POST /api/roles
     */
    @PostMapping
    public ResponseEntity<RolResponse> crear(@Valid @RequestBody RolRequest rolRequest) {
        log.info("POST /api/roles - Creando nuevo rol: {}", rolRequest.getResumenParaLog());

        try {
            // Normalizar datos del request
            rolRequest.normalizar();

            // Validaciones adicionales
            if (!rolRequest.isNombreValido()) {
                log.warn("POST /api/roles - Nombre de rol contiene caracteres inválidos: {}", rolRequest.getNombre());
                return ResponseEntity.badRequest().build();
            }

            // Convertir DTO a Entity
            Rol rol = Rol.builder()
                    .nombre(rolRequest.getNombre())
                    .descripcion(rolRequest.getDescripcion())
                    .build();

            // Crear el rol a través del service
            Rol rolCreado = rolService.crear(rol);

            // Convertir Entity a Response DTO
            RolResponse rolResponse = RolResponse.fromEntity(rolCreado);

            log.info("POST /api/roles - Rol creado exitosamente con ID: {}", rolCreado.getId());
            return ResponseEntity.status(HttpStatus.CREATED).body(rolResponse);

        } catch (IllegalArgumentException e) {
            log.warn("POST /api/roles - Datos inválidos: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (RuntimeException e) {
            log.warn("POST /api/roles - Error de negocio: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        } catch (Exception e) {
            log.error("POST /api/roles - Error interno: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Actualizar rol existente
     * PUT /api/roles/{id}
     */
    @PutMapping("/{id}")
    public ResponseEntity<RolResponse> actualizar(@PathVariable Long id,
                                                  @Valid @RequestBody RolRequest rolRequest) {
        log.info("PUT /api/roles/{} - Actualizando rol: {}", id, rolRequest.getResumenParaLog());

        try {
            // Normalizar datos del request
            rolRequest.normalizar();

            // Validaciones adicionales
            if (!rolRequest.isNombreValido()) {
                log.warn("PUT /api/roles/{} - Nombre de rol contiene caracteres inválidos: {}", id, rolRequest.getNombre());
                return ResponseEntity.badRequest().build();
            }

            // Convertir DTO a Entity
            Rol rolActualizado = Rol.builder()
                    .nombre(rolRequest.getNombre())
                    .descripcion(rolRequest.getDescripcion())
                    .build();

            // Actualizar a través del service
            Rol rol = rolService.actualizar(id, rolActualizado);

            // Convertir Entity a Response DTO
            RolResponse rolResponse = RolResponse.fromEntity(rol);

            log.info("PUT /api/roles/{} - Rol actualizado exitosamente", id);
            return ResponseEntity.ok(rolResponse);

        } catch (IllegalArgumentException e) {
            log.warn("PUT /api/roles/{} - Datos inválidos: {}", id, e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (RuntimeException e) {
            String mensaje = e.getMessage();
            if (mensaje.contains("no encontrado")) {
                log.warn("PUT /api/roles/{} - Rol no encontrado", id);
                return ResponseEntity.notFound().build();
            } else {
                log.warn("PUT /api/roles/{} - Error de negocio: {}", id, mensaje);
                return ResponseEntity.status(HttpStatus.CONFLICT).build();
            }
        } catch (Exception e) {
            log.error("PUT /api/roles/{} - Error interno: {}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Eliminar rol por ID
     * DELETE /api/roles/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        log.info("DELETE /api/roles/{} - Eliminando rol", id);

        try {
            rolService.eliminar(id);
            log.info("DELETE /api/roles/{} - Rol eliminado exitosamente", id);
            return ResponseEntity.noContent().build();

        } catch (IllegalArgumentException e) {
            log.warn("DELETE /api/roles/{} - ID inválido: {}", id, e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (RuntimeException e) {
            String mensaje = e.getMessage();
            if (mensaje.contains("no encontrado")) {
                log.warn("DELETE /api/roles/{} - Rol no encontrado", id);
                return ResponseEntity.notFound().build();
            } else {
                log.warn("DELETE /api/roles/{} - Error de negocio: {}", id, mensaje);
                return ResponseEntity.status(HttpStatus.CONFLICT).build();
            }
        } catch (Exception e) {
            log.error("DELETE /api/roles/{} - Error interno: {}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Verificar si existe un rol por nombre
     * GET /api/roles/existe?nombre={nombre}
     */
    @GetMapping("/existe")
    public ResponseEntity<Boolean> existe(@RequestParam String nombre) {
        log.info("GET /api/roles/existe?nombre={} - Verificando existencia del rol", nombre);

        try {
            boolean existe = rolService.existe(nombre);
            log.info("GET /api/roles/existe - Resultado: {}", existe);
            return ResponseEntity.ok(existe);

        } catch (IllegalArgumentException e) {
            log.warn("GET /api/roles/existe - Parámetro inválido: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            log.error("GET /api/roles/existe - Error interno: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Contar usuarios activos por rol
     * GET /api/roles/{id}/usuarios/contar
     */
    @GetMapping("/{id}/usuarios/contar")
    public ResponseEntity<Long> contarUsuariosActivos(@PathVariable Long id) {
        log.info("GET /api/roles/{}/usuarios/contar - Contando usuarios activos del rol", id);

        try {
            Long cantidad = rolService.contarUsuariosActivos(id);
            log.info("GET /api/roles/{}/usuarios/contar - Cantidad: {}", id, cantidad);
            return ResponseEntity.ok(cantidad);

        } catch (IllegalArgumentException e) {
            log.warn("GET /api/roles/{}/usuarios/contar - ID inválido: {}", id, e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            log.error("GET /api/roles/{}/usuarios/contar - Error interno: {}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Endpoint de salud para verificar que el controller está funcionando
     * GET /api/roles/health
     */
    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("RolController está funcionando correctamente");
    }
}