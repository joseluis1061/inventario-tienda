package com.jlzDev.inventario.service;

import com.jlzDev.inventario.entity.Rol;
import com.jlzDev.inventario.repository.RolRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class RolService {

    private final RolRepository rolRepository;

    /**
     * Obtener todos los roles
     */
    @Transactional(readOnly = true)
    public List<Rol> obtenerTodos() {
        log.debug("Obteniendo todos los roles");
        return rolRepository.findAll();
    }

    /**
     * Obtener rol por ID
     */
    @Transactional(readOnly = true)
    public Optional<Rol> obtenerPorId(Long id) {
        log.debug("Obteniendo rol por ID: {}", id);
        validarIdNoNulo(id);
        return rolRepository.findById(id);
    }

    /**
     * Obtener rol por nombre
     */
    @Transactional(readOnly = true)
    public Optional<Rol> obtenerPorNombre(String nombre) {
        log.debug("Obteniendo rol por nombre: {}", nombre);
        validarNombreNoVacio(nombre);
        return rolRepository.findByNombreIgnoreCase(nombre.trim());
    }

    /**
     * Crear nuevo rol
     */
    public Rol crear(Rol rol) {
        log.debug("Creando nuevo rol: {}", rol.getNombre());

        // Validaciones de negocio
        validarRolParaCreacion(rol);

        // Normalizar nombre
        rol.setNombre(rol.getNombre().trim().toUpperCase());

        if (rol.getDescripcion() != null) {
            rol.setDescripcion(rol.getDescripcion().trim());
        }

        Rol rolGuardado = rolRepository.save(rol);
        log.info("Rol creado exitosamente con ID: {}", rolGuardado.getId());

        return rolGuardado;
    }

    /**
     * Actualizar rol existente
     */
    public Rol actualizar(Long id, Rol rolActualizado) {
        log.debug("Actualizando rol con ID: {}", id);

        validarIdNoNulo(id);
        validarRolParaActualizacion(rolActualizado);

        Rol rolExistente = rolRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Rol no encontrado con ID: " + id));

        // Verificar si el nuevo nombre ya existe (excluyendo el rol actual)
        if (!rolExistente.getNombre().equalsIgnoreCase(rolActualizado.getNombre())) {
            validarNombreUnico(rolActualizado.getNombre());
        }

        // Actualizar campos
        rolExistente.setNombre(rolActualizado.getNombre().trim().toUpperCase());

        if (rolActualizado.getDescripcion() != null) {
            rolExistente.setDescripcion(rolActualizado.getDescripcion().trim());
        }

        Rol rolGuardado = rolRepository.save(rolExistente);
        log.info("Rol actualizado exitosamente: {}", rolGuardado.getNombre());

        return rolGuardado;
    }

    /**
     * Eliminar rol por ID
     */
    public void eliminar(Long id) {
        log.debug("Eliminando rol con ID: {}", id);

        validarIdNoNulo(id);

        // Verificar que el rol existe
        Rol rol = rolRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Rol no encontrado con ID: " + id));

        // Validar que no tenga usuarios asociados
        if (rolRepository.hasUsuariosAsociados(id)) {
            throw new RuntimeException("No se puede eliminar el rol '" + rol.getNombre() +
                    "' porque tiene usuarios asociados");
        }

        // Validar que no sea un rol del sistema (ADMIN, GERENTE, EMPLEADO)
        validarRolNoEsSistema(rol.getNombre());

        rolRepository.deleteById(id);
        log.info("Rol eliminado exitosamente: {}", rol.getNombre());
    }

    /**
     * Verificar si existe un rol por nombre
     */
    @Transactional(readOnly = true)
    public boolean existe(String nombre) {
        validarNombreNoVacio(nombre);
        return rolRepository.existsByNombre(nombre.trim().toUpperCase());
    }

    /**
     * Contar usuarios activos por rol
     */
    @Transactional(readOnly = true)
    public Long contarUsuariosActivos(Long rolId) {
        validarIdNoNulo(rolId);
        return rolRepository.countUsuariosActivosByRolId(rolId);
    }

    // ===== MÉTODOS DE VALIDACIÓN =====

    private void validarRolParaCreacion(Rol rol) {
        if (rol == null) {
            throw new IllegalArgumentException("El rol no puede ser nulo");
        }

        validarNombreNoVacio(rol.getNombre());
        validarNombreUnico(rol.getNombre());
        validarLongitudNombre(rol.getNombre());

        if (rol.getDescripcion() != null) {
            validarLongitudDescripcion(rol.getDescripcion());
        }
    }

    private void validarRolParaActualizacion(Rol rol) {
        if (rol == null) {
            throw new IllegalArgumentException("El rol no puede ser nulo");
        }

        validarNombreNoVacio(rol.getNombre());
        validarLongitudNombre(rol.getNombre());

        if (rol.getDescripcion() != null) {
            validarLongitudDescripcion(rol.getDescripcion());
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
            throw new IllegalArgumentException("El nombre del rol no puede estar vacío");
        }
    }

    private void validarNombreUnico(String nombre) {
        if (rolRepository.existsByNombre(nombre.trim().toUpperCase())) {
            throw new RuntimeException("Ya existe un rol con el nombre: " + nombre);
        }
    }

    private void validarLongitudNombre(String nombre) {
        if (nombre.trim().length() > 50) {
            throw new IllegalArgumentException("El nombre del rol no puede exceder 50 caracteres");
        }
    }

    private void validarLongitudDescripcion(String descripcion) {
        if (descripcion.trim().length() > 255) {
            throw new IllegalArgumentException("La descripción no puede exceder 255 caracteres");
        }
    }

    private void validarRolNoEsSistema(String nombre) {
        String nombreUpper = nombre.toUpperCase();
        if ("ADMIN".equals(nombreUpper) || "GERENTE".equals(nombreUpper) || "EMPLEADO".equals(nombreUpper)) {
            throw new RuntimeException("No se puede eliminar un rol del sistema: " + nombre);
        }
    }
}