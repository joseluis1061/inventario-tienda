package com.jlzDev.inventario.controller;

import com.jlzDev.inventario.dto.request.UsuarioRequest;
import com.jlzDev.inventario.dto.response.UsuarioResponse;
import com.jlzDev.inventario.entity.Rol;
import com.jlzDev.inventario.entity.Usuario;
import com.jlzDev.inventario.service.UsuarioService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.time.LocalDateTime;

/**
 * Controller REST para gestión de usuarios
 * Endpoints para operaciones CRUD de usuarios
 */
@RestController
@RequestMapping("/api/usuarios")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*") // Configurar según necesidades de CORS
public class UsuarioController {

    private final UsuarioService usuarioService;

    /**
     * Obtener todos los usuarios
     * GET /api/usuarios
     */
    @GetMapping
    public ResponseEntity<List<UsuarioResponse>> obtenerTodos() {
        log.info("GET /api/usuarios - Solicitando todos los usuarios");

        try {
            List<Usuario> usuarios = usuarioService.obtenerTodos();

            List<UsuarioResponse> usuariosResponse = usuarios.stream()
                    .map(UsuarioResponse::fromEntity)
                    .collect(Collectors.toList());

            log.info("GET /api/usuarios - Encontrados {} usuarios", usuariosResponse.size());
            return ResponseEntity.ok(usuariosResponse);

        } catch (Exception e) {
            log.error("GET /api/usuarios - Error al obtener usuarios: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Obtener usuarios activos
     * GET /api/usuarios/activos
     */
    @GetMapping("/activos")
    public ResponseEntity<List<UsuarioResponse>> obtenerActivos() {
        log.info("GET /api/usuarios/activos - Solicitando usuarios activos");

        try {
            List<Usuario> usuarios = usuarioService.obtenerActivos();

            List<UsuarioResponse> usuariosResponse = usuarios.stream()
                    .map(UsuarioResponse::fromEntity)
                    .collect(Collectors.toList());

            log.info("GET /api/usuarios/activos - Encontrados {} usuarios activos", usuariosResponse.size());
            return ResponseEntity.ok(usuariosResponse);

        } catch (Exception e) {
            log.error("GET /api/usuarios/activos - Error al obtener usuarios activos: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Obtener usuarios inactivos
     * GET /api/usuarios/inactivos
     */
    @GetMapping("/inactivos")
    public ResponseEntity<List<UsuarioResponse>> obtenerInactivos() {
        log.info("GET /api/usuarios/inactivos - Solicitando usuarios inactivos");

        try {
            List<Usuario> usuarios = usuarioService.obtenerInactivos();

            List<UsuarioResponse> usuariosResponse = usuarios.stream()
                    .map(UsuarioResponse::fromEntity)
                    .collect(Collectors.toList());

            log.info("GET /api/usuarios/inactivos - Encontrados {} usuarios inactivos", usuariosResponse.size());
            return ResponseEntity.ok(usuariosResponse);

        } catch (Exception e) {
            log.error("GET /api/usuarios/inactivos - Error al obtener usuarios inactivos: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Obtener usuario por ID
     * GET /api/usuarios/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<UsuarioResponse> obtenerPorId(@PathVariable Long id) {
        log.info("GET /api/usuarios/{} - Solicitando usuario por ID", id);

        try {
            Optional<Usuario> usuarioOpt = usuarioService.obtenerPorId(id);

            if (usuarioOpt.isPresent()) {
                UsuarioResponse usuarioResponse = UsuarioResponse.fromEntity(usuarioOpt.get());
                log.info("GET /api/usuarios/{} - Usuario encontrado: {}", id, usuarioResponse.getUsername());
                return ResponseEntity.ok(usuarioResponse);
            } else {
                log.warn("GET /api/usuarios/{} - Usuario no encontrado", id);
                return ResponseEntity.notFound().build();
            }

        } catch (IllegalArgumentException e) {
            log.warn("GET /api/usuarios/{} - ID inválido: {}", id, e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            log.error("GET /api/usuarios/{} - Error interno: {}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Obtener usuario por username
     * GET /api/usuarios/buscar?username={username}
     */
    @GetMapping("/buscar")
    public ResponseEntity<UsuarioResponse> obtenerPorUsername(@RequestParam String username) {
        log.info("GET /api/usuarios/buscar?username={} - Buscando usuario por username", username);

        try {
            Optional<Usuario> usuarioOpt = usuarioService.obtenerPorUsername(username);

            if (usuarioOpt.isPresent()) {
                UsuarioResponse usuarioResponse = UsuarioResponse.fromEntity(usuarioOpt.get());
                log.info("GET /api/usuarios/buscar - Usuario encontrado: {}", usuarioResponse.getUsername());
                return ResponseEntity.ok(usuarioResponse);
            } else {
                log.warn("GET /api/usuarios/buscar - Usuario no encontrado con username: {}", username);
                return ResponseEntity.notFound().build();
            }

        } catch (IllegalArgumentException e) {
            log.warn("GET /api/usuarios/buscar - Parámetro inválido: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            log.error("GET /api/usuarios/buscar - Error interno: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Obtener usuarios por rol
     * GET /api/usuarios/por-rol/{rolId}
     */
    @GetMapping("/por-rol/{rolId}")
    public ResponseEntity<List<UsuarioResponse>> obtenerPorRol(@PathVariable Long rolId) {
        log.info("GET /api/usuarios/por-rol/{} - Solicitando usuarios por rol", rolId);

        try {
            List<Usuario> usuarios = usuarioService.obtenerPorRol(rolId);

            List<UsuarioResponse> usuariosResponse = usuarios.stream()
                    .map(UsuarioResponse::fromEntity)
                    .collect(Collectors.toList());

            log.info("GET /api/usuarios/por-rol/{} - Encontrados {} usuarios", rolId, usuariosResponse.size());
            return ResponseEntity.ok(usuariosResponse);

        } catch (IllegalArgumentException e) {
            log.warn("GET /api/usuarios/por-rol/{} - ID de rol inválido: {}", rolId, e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            log.error("GET /api/usuarios/por-rol/{} - Error interno: {}", rolId, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Obtener usuarios activos por rol
     * GET /api/usuarios/por-rol/{rolId}/activos
     */
    @GetMapping("/por-rol/{rolId}/activos")
    public ResponseEntity<List<UsuarioResponse>> obtenerActivosPorRol(@PathVariable Long rolId) {
        log.info("GET /api/usuarios/por-rol/{}/activos - Solicitando usuarios activos por rol", rolId);

        try {
            List<Usuario> usuarios = usuarioService.obtenerActivosPorRol(rolId);

            List<UsuarioResponse> usuariosResponse = usuarios.stream()
                    .map(UsuarioResponse::fromEntity)
                    .collect(Collectors.toList());

            log.info("GET /api/usuarios/por-rol/{}/activos - Encontrados {} usuarios activos", rolId, usuariosResponse.size());
            return ResponseEntity.ok(usuariosResponse);

        } catch (IllegalArgumentException e) {
            log.warn("GET /api/usuarios/por-rol/{}/activos - ID de rol inválido: {}", rolId, e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            log.error("GET /api/usuarios/por-rol/{}/activos - Error interno: {}", rolId, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Buscar usuarios por nombre completo
     * GET /api/usuarios/buscar-nombre?nombre={nombre}
     */
    @GetMapping("/buscar-nombre")
    public ResponseEntity<List<UsuarioResponse>> buscarPorNombreCompleto(@RequestParam String nombre) {
        log.info("GET /api/usuarios/buscar-nombre?nombre={} - Buscando usuarios por nombre", nombre);

        try {
            List<Usuario> usuarios = usuarioService.buscarPorNombreCompleto(nombre);

            List<UsuarioResponse> usuariosResponse = usuarios.stream()
                    .map(UsuarioResponse::fromEntity)
                    .collect(Collectors.toList());

            log.info("GET /api/usuarios/buscar-nombre - Encontrados {} usuarios", usuariosResponse.size());
            return ResponseEntity.ok(usuariosResponse);

        } catch (IllegalArgumentException e) {
            log.warn("GET /api/usuarios/buscar-nombre - Parámetro inválido: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            log.error("GET /api/usuarios/buscar-nombre - Error interno: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Crear nuevo usuario
     * POST /api/usuarios
     */
    @PostMapping
    public ResponseEntity<UsuarioResponse> crear(@Valid @RequestBody UsuarioRequest usuarioRequest,
                                                 @RequestParam String password) {
        log.info("POST /api/usuarios - Creando nuevo usuario: {}", usuarioRequest.getResumenParaLog());

        try {
            // Normalizar datos del request
            usuarioRequest.normalizar();

            // Validaciones adicionales
            if (!usuarioRequest.isUsernameValido()) {
                log.warn("POST /api/usuarios - Username no válido o reservado: {}", usuarioRequest.getUsername());
                return ResponseEntity.badRequest().build();
            }

            if (!usuarioRequest.tieneNombreCompleto()) {
                log.warn("POST /api/usuarios - Nombre completo debe tener al menos nombre y apellido");
                return ResponseEntity.badRequest().build();
            }

            // Convertir DTO a Entity
            Usuario usuario = Usuario.builder()
                    .username(usuarioRequest.getUsername())
                    .nombreCompleto(usuarioRequest.getNombreCompleto())
                    .email(usuarioRequest.getEmail())
                    .activo(usuarioRequest.getActivo())
                    .rol(Rol.builder().id(usuarioRequest.getRolId()).build()) // Solo el ID para la referencia
                    .build();

            // Crear el usuario a través del service
            Usuario usuarioCreado = usuarioService.crear(usuario, password);

            // Convertir Entity a Response DTO
            UsuarioResponse usuarioResponse = UsuarioResponse.fromEntity(usuarioCreado);

            log.info("POST /api/usuarios - Usuario creado exitosamente con ID: {}", usuarioCreado.getId());
            return ResponseEntity.status(HttpStatus.CREATED).body(usuarioResponse);

        } catch (IllegalArgumentException e) {
            log.warn("POST /api/usuarios - Datos inválidos: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (RuntimeException e) {
            log.warn("POST /api/usuarios - Error de negocio: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        } catch (Exception e) {
            log.error("POST /api/usuarios - Error interno: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Actualizar usuario existente
     * PUT /api/usuarios/{id}
     */
    @PutMapping("/{id}")
    public ResponseEntity<UsuarioResponse> actualizar(@PathVariable Long id,
                                                      @Valid @RequestBody UsuarioRequest usuarioRequest) {
        log.info("PUT /api/usuarios/{} - Actualizando usuario: {}", id, usuarioRequest.getResumenParaLog());

        try {
            // Normalizar datos del request
            usuarioRequest.normalizar();

            // Validaciones adicionales
            if (!usuarioRequest.isUsernameValido()) {
                log.warn("PUT /api/usuarios/{} - Username no válido o reservado: {}", id, usuarioRequest.getUsername());
                return ResponseEntity.badRequest().build();
            }

            if (!usuarioRequest.tieneNombreCompleto()) {
                log.warn("PUT /api/usuarios/{} - Nombre completo debe tener al menos nombre y apellido", id);
                return ResponseEntity.badRequest().build();
            }

            // Convertir DTO a Entity
            Usuario usuarioActualizado = Usuario.builder()
                    .username(usuarioRequest.getUsername())
                    .nombreCompleto(usuarioRequest.getNombreCompleto())
                    .email(usuarioRequest.getEmail())
                    .activo(usuarioRequest.getActivo())
                    .rol(Rol.builder().id(usuarioRequest.getRolId()).build()) // Solo el ID para la referencia
                    .build();

            // Actualizar a través del service
            Usuario usuario = usuarioService.actualizar(id, usuarioActualizado);

            // Convertir Entity a Response DTO
            UsuarioResponse usuarioResponse = UsuarioResponse.fromEntity(usuario);

            log.info("PUT /api/usuarios/{} - Usuario actualizado exitosamente", id);
            return ResponseEntity.ok(usuarioResponse);

        } catch (IllegalArgumentException e) {
            log.warn("PUT /api/usuarios/{} - Datos inválidos: {}", id, e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (RuntimeException e) {
            String mensaje = e.getMessage();
            if (mensaje.contains("no encontrado")) {
                log.warn("PUT /api/usuarios/{} - Usuario no encontrado", id);
                return ResponseEntity.notFound().build();
            } else {
                log.warn("PUT /api/usuarios/{} - Error de negocio: {}", id, mensaje);
                return ResponseEntity.status(HttpStatus.CONFLICT).build();
            }
        } catch (Exception e) {
            log.error("PUT /api/usuarios/{} - Error interno: {}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Cambiar contraseña de usuario
     * PUT /api/usuarios/{id}/cambiar-password
     */
    @PutMapping("/{id}/cambiar-password")
    public ResponseEntity<Void> cambiarPassword(@PathVariable Long id,
                                                @RequestParam String passwordActual,
                                                @RequestParam String passwordNuevo) {
        log.info("PUT /api/usuarios/{}/cambiar-password - Cambiando contraseña", id);

        try {
            usuarioService.cambiarPassword(id, passwordActual, passwordNuevo);
            log.info("PUT /api/usuarios/{}/cambiar-password - Contraseña cambiada exitosamente", id);
            return ResponseEntity.ok().build();

        } catch (IllegalArgumentException e) {
            log.warn("PUT /api/usuarios/{}/cambiar-password - Datos inválidos: {}", id, e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (RuntimeException e) {
            String mensaje = e.getMessage();
            if (mensaje.contains("no encontrado")) {
                log.warn("PUT /api/usuarios/{}/cambiar-password - Usuario no encontrado", id);
                return ResponseEntity.notFound().build();
            } else {
                log.warn("PUT /api/usuarios/{}/cambiar-password - Error: {}", id, mensaje);
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
            }
        } catch (Exception e) {
            log.error("PUT /api/usuarios/{}/cambiar-password - Error interno: {}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Resetear contraseña (solo para administradores)
     * PUT /api/usuarios/{id}/resetear-password
     */
    @PutMapping("/{id}/resetear-password")
    public ResponseEntity<String> resetearPassword(@PathVariable Long id) {
        log.info("PUT /api/usuarios/{}/resetear-password - Reseteando contraseña", id);

        try {
            String passwordTemporal = usuarioService.resetearPassword(id);
            log.info("PUT /api/usuarios/{}/resetear-password - Contraseña reseteada exitosamente", id);
            return ResponseEntity.ok(passwordTemporal);

        } catch (IllegalArgumentException e) {
            log.warn("PUT /api/usuarios/{}/resetear-password - ID inválido: {}", id, e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (RuntimeException e) {
            String mensaje = e.getMessage();
            if (mensaje.contains("no encontrado")) {
                log.warn("PUT /api/usuarios/{}/resetear-password - Usuario no encontrado", id);
                return ResponseEntity.notFound().build();
            } else {
                log.warn("PUT /api/usuarios/{}/resetear-password - Error: {}", id, mensaje);
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
            }
        } catch (Exception e) {
            log.error("PUT /api/usuarios/{}/resetear-password - Error interno: {}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Activar/Desactivar usuario
     * PUT /api/usuarios/{id}/cambiar-estado?activo={true/false}
     */
    @PutMapping("/{id}/cambiar-estado")
    public ResponseEntity<UsuarioResponse> cambiarEstado(@PathVariable Long id,
                                                         @RequestParam boolean activo) {
        log.info("PUT /api/usuarios/{}/cambiar-estado?activo={} - Cambiando estado", id, activo);

        try {
            Usuario usuario = usuarioService.cambiarEstado(id, activo);
            UsuarioResponse usuarioResponse = UsuarioResponse.fromEntity(usuario);

            log.info("PUT /api/usuarios/{}/cambiar-estado - Estado cambiado exitosamente a: {}", id, activo);
            return ResponseEntity.ok(usuarioResponse);

        } catch (IllegalArgumentException e) {
            log.warn("PUT /api/usuarios/{}/cambiar-estado - ID inválido: {}", id, e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (RuntimeException e) {
            String mensaje = e.getMessage();
            if (mensaje.contains("no encontrado")) {
                log.warn("PUT /api/usuarios/{}/cambiar-estado - Usuario no encontrado", id);
                return ResponseEntity.notFound().build();
            } else {
                log.warn("PUT /api/usuarios/{}/cambiar-estado - Error: {}", id, mensaje);
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
            }
        } catch (Exception e) {
            log.error("PUT /api/usuarios/{}/cambiar-estado - Error interno: {}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Eliminar usuario por ID
     * DELETE /api/usuarios/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        log.info("DELETE /api/usuarios/{} - Eliminando usuario", id);

        try {
            usuarioService.eliminar(id);
            log.info("DELETE /api/usuarios/{} - Usuario eliminado exitosamente", id);
            return ResponseEntity.noContent().build();

        } catch (IllegalArgumentException e) {
            log.warn("DELETE /api/usuarios/{} - ID inválido: {}", id, e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (RuntimeException e) {
            String mensaje = e.getMessage();
            if (mensaje.contains("no encontrado")) {
                log.warn("DELETE /api/usuarios/{} - Usuario no encontrado", id);
                return ResponseEntity.notFound().build();
            } else {
                log.warn("DELETE /api/usuarios/{} - Error de negocio: {}", id, mensaje);
                return ResponseEntity.status(HttpStatus.CONFLICT).build();
            }
        } catch (Exception e) {
            log.error("DELETE /api/usuarios/{} - Error interno: {}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Verificar si existe username
     * GET /api/usuarios/existe-username?username={username}
     */
    @GetMapping("/existe-username")
    public ResponseEntity<Boolean> existeUsername(@RequestParam String username) {
        log.info("GET /api/usuarios/existe-username?username={} - Verificando existencia", username);

        try {
            boolean existe = usuarioService.existeUsername(username);
            log.info("GET /api/usuarios/existe-username - Resultado: {}", existe);
            return ResponseEntity.ok(existe);

        } catch (IllegalArgumentException e) {
            log.warn("GET /api/usuarios/existe-username - Parámetro inválido: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            log.error("GET /api/usuarios/existe-username - Error interno: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Verificar si existe email
     * GET /api/usuarios/existe-email?email={email}
     */
    @GetMapping("/existe-email")
    public ResponseEntity<Boolean> existeEmail(@RequestParam String email) {
        log.info("GET /api/usuarios/existe-email?email={} - Verificando existencia", email);

        try {
            boolean existe = usuarioService.existeEmail(email);
            log.info("GET /api/usuarios/existe-email - Resultado: {}", existe);
            return ResponseEntity.ok(existe);

        } catch (IllegalArgumentException e) {
            log.warn("GET /api/usuarios/existe-email - Parámetro inválido: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            log.error("GET /api/usuarios/existe-email - Error interno: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Contar usuarios activos
     * GET /api/usuarios/contar-activos
     */
    @GetMapping("/contar-activos")
    public ResponseEntity<Long> contarUsuariosActivos() {
        log.info("GET /api/usuarios/contar-activos - Contando usuarios activos");

        try {
            Long cantidad = usuarioService.contarUsuariosActivos();
            log.info("GET /api/usuarios/contar-activos - Cantidad: {}", cantidad);
            return ResponseEntity.ok(cantidad);

        } catch (Exception e) {
            log.error("GET /api/usuarios/contar-activos - Error interno: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Endpoint de salud para verificar que el controller está funcionando
     * GET /api/usuarios/health
     */
    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("UsuarioController está funcionando correctamente");
    }
}