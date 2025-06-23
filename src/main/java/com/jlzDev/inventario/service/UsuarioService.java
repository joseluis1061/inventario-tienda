package com.jlzDev.inventario.service;

import com.jlzDev.inventario.entity.Rol;
import com.jlzDev.inventario.entity.Usuario;
import com.jlzDev.inventario.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final RolService rolService;
    private final PasswordEncoder passwordEncoder;

    // Patrones de validación
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"
    );
    private static final Pattern USERNAME_PATTERN = Pattern.compile("^[a-zA-Z0-9._-]{3,20}$");

    /**
     * Obtener todos los usuarios
     */
    @Transactional(readOnly = true)
    public List<Usuario> obtenerTodos() {
        log.debug("Obteniendo todos los usuarios");
        return usuarioRepository.findAll();
    }

    /**
     * Obtener usuarios activos
     */
    @Transactional(readOnly = true)
    public List<Usuario> obtenerActivos() {
        log.debug("Obteniendo usuarios activos");
        return usuarioRepository.findByActivoTrue();
    }

    /**
     * Obtener usuarios inactivos
     */
    @Transactional(readOnly = true)
    public List<Usuario> obtenerInactivos() {
        log.debug("Obteniendo usuarios inactivos");
        return usuarioRepository.findByActivoFalse();
    }

    /**
     * Obtener usuario por ID
     */
    @Transactional(readOnly = true)
    public Optional<Usuario> obtenerPorId(Long id) {
        log.debug("Obteniendo usuario por ID: {}", id);
        validarIdNoNulo(id);
        return usuarioRepository.findById(id);
    }

    /**
     * Obtener usuario por username
     */
    @Transactional(readOnly = true)
    public Optional<Usuario> obtenerPorUsername(String username) {
        log.debug("Obteniendo usuario por username: {}", username);
        validarUsernameNoVacio(username);
        return usuarioRepository.findByUsername(username.trim().toLowerCase());
    }

    /**
     * Obtener usuario por email
     */
    @Transactional(readOnly = true)
    public Optional<Usuario> obtenerPorEmail(String email) {
        log.debug("Obteniendo usuario por email: {}", email);
        validarEmailNoVacio(email);
        return usuarioRepository.findByEmail(email.trim().toLowerCase());
    }

    /**
     * Obtener usuarios por rol
     */
    @Transactional(readOnly = true)
    public List<Usuario> obtenerPorRol(Long rolId) {
        log.debug("Obteniendo usuarios por rol ID: {}", rolId);
        validarIdNoNulo(rolId);
        return usuarioRepository.findByRolId(rolId);
    }

    /**
     * Obtener usuarios activos por rol
     */
    @Transactional(readOnly = true)
    public List<Usuario> obtenerActivosPorRol(Long rolId) {
        log.debug("Obteniendo usuarios activos por rol ID: {}", rolId);
        validarIdNoNulo(rolId);
        return usuarioRepository.findByRolIdAndActivoTrue(rolId);
    }

    /**
     * Buscar usuarios por nombre completo
     */
    @Transactional(readOnly = true)
    public List<Usuario> buscarPorNombreCompleto(String nombreCompleto) {
        log.debug("Buscando usuarios por nombre completo: {}", nombreCompleto);
        validarNombreCompletoNoVacio(nombreCompleto);
        return usuarioRepository.findByNombreCompletoContainingIgnoreCase(nombreCompleto.trim());
    }

    /**
     * Obtener usuarios creados después de una fecha
     */
    @Transactional(readOnly = true)
    public List<Usuario> obtenerCreadosDespuesDe(LocalDateTime fecha) {
        log.debug("Obteniendo usuarios creados después de: {}", fecha);
        if (fecha == null) {
            throw new IllegalArgumentException("La fecha no puede ser nula");
        }
        return usuarioRepository.findByFechaCreacionAfter(fecha);
    }

    /**
     * Crear nuevo usuario
     */
    public Usuario crear(Usuario usuario, String passwordSinEncriptar) {
        log.debug("Creando nuevo usuario: {}", usuario.getUsername());

        // Validaciones de negocio
        validarUsuarioParaCreacion(usuario);
        validarPassword(passwordSinEncriptar);

        // Normalizar datos
        usuario.setUsername(usuario.getUsername().trim().toLowerCase());
        usuario.setNombreCompleto(usuario.getNombreCompleto().trim());

        if (usuario.getEmail() != null) {
            usuario.setEmail(usuario.getEmail().trim().toLowerCase());
        }

        // Verificar que el rol existe
        Rol rol = rolService.obtenerPorId(usuario.getRol().getId())
                .orElseThrow(() -> new RuntimeException("Rol no encontrado con ID: " + usuario.getRol().getId()));
        usuario.setRol(rol);

        // Encriptar contraseña
        usuario.setPassword(passwordEncoder.encode(passwordSinEncriptar));

        // Establecer valores por defecto
        if (usuario.getActivo() == null) {
            usuario.setActivo(true);
        }

        Usuario usuarioGuardado = usuarioRepository.save(usuario);
        log.info("Usuario creado exitosamente con ID: {} y username: {}",
                usuarioGuardado.getId(), usuarioGuardado.getUsername());

        return usuarioGuardado;
    }

    /**
     * Actualizar usuario existente
     */
    public Usuario actualizar(Long id, Usuario usuarioActualizado) {
        log.debug("Actualizando usuario con ID: {}", id);

        validarIdNoNulo(id);
        validarUsuarioParaActualizacion(usuarioActualizado);

        Usuario usuarioExistente = usuarioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado con ID: " + id));

        // Verificar unicidad de username (excluyendo el usuario actual)
        if (!usuarioExistente.getUsername().equals(usuarioActualizado.getUsername().trim().toLowerCase())) {
            validarUsernameUnico(usuarioActualizado.getUsername());
        }

        // Verificar unicidad de email (excluyendo el usuario actual)
        if (usuarioActualizado.getEmail() != null) {
            String emailNuevo = usuarioActualizado.getEmail().trim().toLowerCase();
            if (usuarioExistente.getEmail() == null || !usuarioExistente.getEmail().equals(emailNuevo)) {
                validarEmailUnico(emailNuevo);
            }
        }

        // Verificar que el rol existe
        if (!usuarioExistente.getRol().getId().equals(usuarioActualizado.getRol().getId())) {
            Rol rolNuevo = rolService.obtenerPorId(usuarioActualizado.getRol().getId())
                    .orElseThrow(() -> new RuntimeException("Rol no encontrado con ID: " + usuarioActualizado.getRol().getId()));
            usuarioExistente.setRol(rolNuevo);
        }

        // Actualizar campos (sin password)
        usuarioExistente.setUsername(usuarioActualizado.getUsername().trim().toLowerCase());
        usuarioExistente.setNombreCompleto(usuarioActualizado.getNombreCompleto().trim());

        if (usuarioActualizado.getEmail() != null) {
            usuarioExistente.setEmail(usuarioActualizado.getEmail().trim().toLowerCase());
        } else {
            usuarioExistente.setEmail(null);
        }

        if (usuarioActualizado.getActivo() != null) {
            usuarioExistente.setActivo(usuarioActualizado.getActivo());
        }

        Usuario usuarioGuardado = usuarioRepository.save(usuarioExistente);
        log.info("Usuario actualizado exitosamente: {}", usuarioGuardado.getUsername());

        return usuarioGuardado;
    }

    /**
     * Cambiar contraseña de usuario
     */
    public void cambiarPassword(Long usuarioId, String passwordActual, String passwordNuevo) {
        log.debug("Cambiando contraseña para usuario ID: {}", usuarioId);

        validarIdNoNulo(usuarioId);
        validarPassword(passwordActual);
        validarPassword(passwordNuevo);

        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado con ID: " + usuarioId));

        // Verificar contraseña actual
        if (!passwordEncoder.matches(passwordActual, usuario.getPassword())) {
            throw new RuntimeException("La contraseña actual es incorrecta");
        }

        // Verificar que la nueva contraseña sea diferente
        if (passwordEncoder.matches(passwordNuevo, usuario.getPassword())) {
            throw new RuntimeException("La nueva contraseña debe ser diferente a la actual");
        }

        // Actualizar contraseña
        usuario.setPassword(passwordEncoder.encode(passwordNuevo));
        usuarioRepository.save(usuario);

        log.info("Contraseña cambiada exitosamente para usuario: {}", usuario.getUsername());
    }

    /**
     * Resetear contraseña (solo para administradores)
     */
    public String resetearPassword(Long usuarioId) {
        log.debug("Reseteando contraseña para usuario ID: {}", usuarioId);

        validarIdNoNulo(usuarioId);

        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado con ID: " + usuarioId));

        // Generar contraseña temporal
        String passwordTemporal = generarPasswordTemporal();

        // Actualizar contraseña
        usuario.setPassword(passwordEncoder.encode(passwordTemporal));
        usuarioRepository.save(usuario);

        log.info("Contraseña reseteada para usuario: {}", usuario.getUsername());

        return passwordTemporal;
    }

    /**
     * Activar/Desactivar usuario
     */
    public Usuario cambiarEstado(Long id, boolean activo) {
        log.debug("Cambiando estado de usuario ID: {} a {}", id, activo ? "activo" : "inactivo");

        validarIdNoNulo(id);

        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado con ID: " + id));

        // No permitir desactivar al usuario admin principal
        if (!activo && "admin".equals(usuario.getUsername())) {
            throw new RuntimeException("No se puede desactivar el usuario administrador principal");
        }

        usuario.setActivo(activo);
        Usuario usuarioGuardado = usuarioRepository.save(usuario);

        log.info("Estado cambiado exitosamente para usuario: {} -> {}",
                usuario.getUsername(), activo ? "ACTIVO" : "INACTIVO");

        return usuarioGuardado;
    }

    /**
     * Eliminar usuario
     */
    public void eliminar(Long id) {
        log.debug("Eliminando usuario con ID: {}", id);

        validarIdNoNulo(id);

        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado con ID: " + id));

        // No permitir eliminar al usuario admin principal
        if ("admin".equals(usuario.getUsername())) {
            throw new RuntimeException("No se puede eliminar el usuario administrador principal");
        }

        // Verificar si tiene movimientos asociados
        if (usuarioRepository.hasMovimientos(id)) {
            throw new RuntimeException("No se puede eliminar el usuario '" + usuario.getUsername() +
                    "' porque tiene movimientos de inventario asociados. Considere desactivarlo en su lugar.");
        }

        usuarioRepository.deleteById(id);
        log.info("Usuario eliminado exitosamente: {}", usuario.getUsername());
    }

    /**
     * Autenticar usuario
     */
    @Transactional(readOnly = true)
    public Optional<Usuario> autenticar(String username, String password) {
        log.debug("Autenticando usuario: {}", username);

        validarUsernameNoVacio(username);
        validarPassword(password);

        Optional<Usuario> usuarioOpt = usuarioRepository.findByUsername(username.trim().toLowerCase());

        if (usuarioOpt.isPresent()) {
            Usuario usuario = usuarioOpt.get();

            // Verificar que esté activo
            if (!usuario.getActivo()) {
                log.warn("Intento de login con usuario inactivo: {}", username);
                return Optional.empty();
            }

            // Verificar contraseña
            if (passwordEncoder.matches(password, usuario.getPassword())) {
                log.info("Autenticación exitosa para usuario: {}", username);
                return Optional.of(usuario);
            } else {
                log.warn("Contraseña incorrecta para usuario: {}", username);
            }
        } else {
            log.warn("Usuario no encontrado: {}", username);
        }

        return Optional.empty();
    }

    /**
     * Verificar si existe username
     */
    @Transactional(readOnly = true)
    public boolean existeUsername(String username) {
        validarUsernameNoVacio(username);
        return usuarioRepository.existsByUsername(username.trim().toLowerCase());
    }

    /**
     * Verificar si existe email
     */
    @Transactional(readOnly = true)
    public boolean existeEmail(String email) {
        validarEmailNoVacio(email);
        return usuarioRepository.existsByEmail(email.trim().toLowerCase());
    }

    /**
     * Contar usuarios activos
     */
    @Transactional(readOnly = true)
    public Long contarUsuariosActivos() {
        return usuarioRepository.countUsuariosActivos();
    }

    /**
     * Obtener usuario requerido (lanza excepción si no existe)
     */
    @Transactional(readOnly = true)
    public Usuario obtenerRequerido(Long id) {
        return obtenerPorId(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado con ID: " + id));
    }

    // ===== MÉTODOS PRIVADOS =====

    private String generarPasswordTemporal() {
        // Generar password temporal de 8 caracteres
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        StringBuilder password = new StringBuilder();
        for (int i = 0; i < 8; i++) {
            password.append(chars.charAt((int) (Math.random() * chars.length())));
        }
        return password.toString();
    }

    // ===== MÉTODOS DE VALIDACIÓN =====

    private void validarUsuarioParaCreacion(Usuario usuario) {
        if (usuario == null) {
            throw new IllegalArgumentException("El usuario no puede ser nulo");
        }

        validarUsernameNoVacio(usuario.getUsername());
        validarUsernameFormato(usuario.getUsername());
        validarUsernameUnico(usuario.getUsername());

        validarNombreCompletoNoVacio(usuario.getNombreCompleto());
        validarLongitudNombreCompleto(usuario.getNombreCompleto());

        if (usuario.getEmail() != null) {
            validarEmailFormato(usuario.getEmail());
            validarEmailUnico(usuario.getEmail());
        }

        if (usuario.getRol() == null || usuario.getRol().getId() == null) {
            throw new IllegalArgumentException("El rol es obligatorio");
        }
    }

    private void validarUsuarioParaActualizacion(Usuario usuario) {
        if (usuario == null) {
            throw new IllegalArgumentException("El usuario no puede ser nulo");
        }

        validarUsernameNoVacio(usuario.getUsername());
        validarUsernameFormato(usuario.getUsername());

        validarNombreCompletoNoVacio(usuario.getNombreCompleto());
        validarLongitudNombreCompleto(usuario.getNombreCompleto());

        if (usuario.getEmail() != null) {
            validarEmailFormato(usuario.getEmail());
        }

        if (usuario.getRol() == null || usuario.getRol().getId() == null) {
            throw new IllegalArgumentException("El rol es obligatorio");
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

    private void validarUsernameNoVacio(String username) {
        if (username == null || username.trim().isEmpty()) {
            throw new IllegalArgumentException("El username no puede estar vacío");
        }
    }

    private void validarUsernameFormato(String username) {
        if (!USERNAME_PATTERN.matcher(username.trim()).matches()) {
            throw new IllegalArgumentException("El username debe tener entre 3-20 caracteres y solo puede contener letras, números, puntos, guiones y guiones bajos");
        }
    }

    private void validarUsernameUnico(String username) {
        if (usuarioRepository.existsByUsername(username.trim().toLowerCase())) {
            throw new RuntimeException("Ya existe un usuario con el username: " + username);
        }
    }

    private void validarNombreCompletoNoVacio(String nombreCompleto) {
        if (nombreCompleto == null || nombreCompleto.trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre completo no puede estar vacío");
        }
    }

    private void validarLongitudNombreCompleto(String nombreCompleto) {
        if (nombreCompleto.trim().length() > 100) {
            throw new IllegalArgumentException("El nombre completo no puede exceder 100 caracteres");
        }
        if (nombreCompleto.trim().length() < 2) {
            throw new IllegalArgumentException("El nombre completo debe tener al menos 2 caracteres");
        }
    }

    private void validarEmailNoVacio(String email) {
        if (email == null || email.trim().isEmpty()) {
            throw new IllegalArgumentException("El email no puede estar vacío");
        }
    }

    private void validarEmailFormato(String email) {
        if (!EMAIL_PATTERN.matcher(email.trim()).matches()) {
            throw new IllegalArgumentException("El formato del email no es válido");
        }
        if (email.trim().length() > 100) {
            throw new IllegalArgumentException("El email no puede exceder 100 caracteres");
        }
    }

    private void validarEmailUnico(String email) {
        if (usuarioRepository.existsByEmail(email.trim().toLowerCase())) {
            throw new RuntimeException("Ya existe un usuario con el email: " + email);
        }
    }

    private void validarPassword(String password) {
        if (password == null || password.isEmpty()) {
            throw new IllegalArgumentException("La contraseña no puede estar vacía");
        }
        if (password.length() < 6) {
            throw new IllegalArgumentException("La contraseña debe tener al menos 6 caracteres");
        }
        if (password.length() > 100) {
            throw new IllegalArgumentException("La contraseña no puede exceder 100 caracteres");
        }
    }
}