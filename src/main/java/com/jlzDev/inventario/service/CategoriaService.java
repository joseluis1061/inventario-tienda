package com.jlzDev.inventario.service;

import com.jlzDev.inventario.entity.Categoria;
import com.jlzDev.inventario.repository.CategoriaRepository;
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
public class CategoriaService {

    private final CategoriaRepository categoriaRepository;

    /**
     * Obtener todas las categorías
     */
    @Transactional(readOnly = true)
    public List<Categoria> obtenerTodas() {
        log.debug("Obteniendo todas las categorías");
        return categoriaRepository.findAll();
    }

    /**
     * Obtener categoría por ID
     */
    @Transactional(readOnly = true)
    public Optional<Categoria> obtenerPorId(Long id) {
        log.debug("Obteniendo categoría por ID: {}", id);
        validarIdNoNulo(id);
        return categoriaRepository.findById(id);
    }

    /**
     * Obtener categoría por nombre
     */
    @Transactional(readOnly = true)
    public Optional<Categoria> obtenerPorNombre(String nombre) {
        log.debug("Obteniendo categoría por nombre: {}", nombre);
        validarNombreNoVacio(nombre);
        return categoriaRepository.findByNombreIgnoreCase(nombre.trim());
    }

    /**
     * Buscar categorías por nombre que contenga el texto
     */
    @Transactional(readOnly = true)
    public List<Categoria> buscarPorNombre(String nombre) {
        log.debug("Buscando categorías que contengan: {}", nombre);
        validarNombreNoVacio(nombre);
        return categoriaRepository.findByNombreContainingIgnoreCase(nombre.trim());
    }

    /**
     * Obtener categorías que tienen productos asociados
     */
    @Transactional(readOnly = true)
    public List<Categoria> obtenerCategoriasConProductos() {
        log.debug("Obteniendo categorías con productos");
        return categoriaRepository.findCategoriasConProductos();
    }

    /**
     * Obtener categorías que NO tienen productos asociados
     */
    @Transactional(readOnly = true)
    public List<Categoria> obtenerCategoriasSinProductos() {
        log.debug("Obteniendo categorías sin productos");
        return categoriaRepository.findCategoriasSinProductos();
    }

    /**
     * Crear nueva categoría
     */
    public Categoria crear(Categoria categoria) {
        log.debug("Creando nueva categoría: {}", categoria.getNombre());

        // Validaciones de negocio
        validarCategoriaParaCreacion(categoria);

        // Normalizar nombre (mantener formato original pero limpio)
        categoria.setNombre(categoria.getNombre().trim());

        if (categoria.getDescripcion() != null) {
            categoria.setDescripcion(categoria.getDescripcion().trim());
        }

        Categoria categoriaGuardada = categoriaRepository.save(categoria);
        log.info("Categoría creada exitosamente con ID: {}", categoriaGuardada.getId());

        return categoriaGuardada;
    }

    /**
     * Actualizar categoría existente
     */
    public Categoria actualizar(Long id, Categoria categoriaActualizada) {
        log.debug("Actualizando categoría con ID: {}", id);

        validarIdNoNulo(id);
        validarCategoriaParaActualizacion(categoriaActualizada);

        Categoria categoriaExistente = categoriaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Categoría no encontrada con ID: " + id));

        // Verificar si el nuevo nombre ya existe (excluyendo la categoría actual)
        if (!categoriaExistente.getNombre().equalsIgnoreCase(categoriaActualizada.getNombre())) {
            validarNombreUnico(categoriaActualizada.getNombre());
        }

        // Actualizar campos
        categoriaExistente.setNombre(categoriaActualizada.getNombre().trim());

        if (categoriaActualizada.getDescripcion() != null) {
            categoriaExistente.setDescripcion(categoriaActualizada.getDescripcion().trim());
        } else {
            categoriaExistente.setDescripcion(null);
        }

        Categoria categoriaGuardada = categoriaRepository.save(categoriaExistente);
        log.info("Categoría actualizada exitosamente: {}", categoriaGuardada.getNombre());

        return categoriaGuardada;
    }

    /**
     * Eliminar categoría por ID
     */
    public void eliminar(Long id) {
        log.debug("Eliminando categoría con ID: {}", id);

        validarIdNoNulo(id);

        // Verificar que la categoría existe
        Categoria categoria = categoriaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Categoría no encontrada con ID: " + id));

        // Validar que no tenga productos asociados
        if (categoriaRepository.hasProductosAsociados(id)) {
            Long cantidadProductos = categoriaRepository.countProductosByCategoriaId(id);
            throw new RuntimeException("No se puede eliminar la categoría '" + categoria.getNombre() +
                    "' porque tiene " + cantidadProductos + " producto(s) asociado(s)");
        }

        categoriaRepository.deleteById(id);
        log.info("Categoría eliminada exitosamente: {}", categoria.getNombre());
    }

    /**
     * Eliminar categoría de forma segura (mover productos a otra categoría)
     */
    public void eliminarConReubicacion(Long categoriaEliminarId, Long categoriaNuevaId) {
        log.debug("Eliminando categoría {} y reubicando productos a categoría {}",
                categoriaEliminarId, categoriaNuevaId);

        validarIdNoNulo(categoriaEliminarId);
        validarIdNoNulo(categoriaNuevaId);

        if (categoriaEliminarId.equals(categoriaNuevaId)) {
            throw new IllegalArgumentException("La categoría de destino no puede ser la misma que se va a eliminar");
        }

        // Verificar que ambas categorías existen
        Categoria categoriaEliminar = categoriaRepository.findById(categoriaEliminarId)
                .orElseThrow(() -> new RuntimeException("Categoría a eliminar no encontrada con ID: " + categoriaEliminarId));

        Categoria categoriaNueva = categoriaRepository.findById(categoriaNuevaId)
                .orElseThrow(() -> new RuntimeException("Categoría de destino no encontrada con ID: " + categoriaNuevaId));

        // Esta operación requeriría coordinación con ProductoService
        // Por ahora lanzamos una excepción indicando que se debe hacer manualmente
        if (categoriaRepository.hasProductosAsociados(categoriaEliminarId)) {
            throw new RuntimeException("Para eliminar esta categoría, primero debe reasignar manualmente los productos a la categoría: " + categoriaNueva.getNombre());
        }

        // Si no hay productos, proceder con eliminación normal
        eliminar(categoriaEliminarId);
    }

    /**
     * Verificar si existe una categoría por nombre
     */
    @Transactional(readOnly = true)
    public boolean existe(String nombre) {
        validarNombreNoVacio(nombre);
        return categoriaRepository.existsByNombre(nombre.trim());
    }

    /**
     * Contar productos por categoría
     */
    @Transactional(readOnly = true)
    public Long contarProductos(Long categoriaId) {
        validarIdNoNulo(categoriaId);
        return categoriaRepository.countProductosByCategoriaId(categoriaId);
    }

    /**
     * Verificar si la categoría tiene productos asociados
     */
    @Transactional(readOnly = true)
    public boolean tieneProductosAsociados(Long categoriaId) {
        validarIdNoNulo(categoriaId);
        return categoriaRepository.hasProductosAsociados(categoriaId);
    }

    /**
     * Obtener categoría requerida (lanza excepción si no existe)
     */
    @Transactional(readOnly = true)
    public Categoria obtenerRequerida(Long id) {
        return obtenerPorId(id)
                .orElseThrow(() -> new RuntimeException("Categoría no encontrada con ID: " + id));
    }

    // ===== MÉTODOS DE VALIDACIÓN =====

    private void validarCategoriaParaCreacion(Categoria categoria) {
        if (categoria == null) {
            throw new IllegalArgumentException("La categoría no puede ser nula");
        }

        validarNombreNoVacio(categoria.getNombre());
        validarNombreUnico(categoria.getNombre());
        validarLongitudNombre(categoria.getNombre());

        if (categoria.getDescripcion() != null) {
            validarLongitudDescripcion(categoria.getDescripcion());
        }
    }

    private void validarCategoriaParaActualizacion(Categoria categoria) {
        if (categoria == null) {
            throw new IllegalArgumentException("La categoría no puede ser nula");
        }

        validarNombreNoVacio(categoria.getNombre());
        validarLongitudNombre(categoria.getNombre());

        if (categoria.getDescripcion() != null) {
            validarLongitudDescripcion(categoria.getDescripcion());
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
            throw new IllegalArgumentException("El nombre de la categoría no puede estar vacío");
        }
    }

    private void validarNombreUnico(String nombre) {
        if (categoriaRepository.existsByNombre(nombre.trim())) {
            throw new RuntimeException("Ya existe una categoría con el nombre: " + nombre);
        }
    }

    private void validarLongitudNombre(String nombre) {
        if (nombre.trim().length() > 100) {
            throw new IllegalArgumentException("El nombre de la categoría no puede exceder 100 caracteres");
        }
        if (nombre.trim().length() < 2) {
            throw new IllegalArgumentException("El nombre de la categoría debe tener al menos 2 caracteres");
        }
    }

    private void validarLongitudDescripcion(String descripcion) {
        if (descripcion.trim().length() > 255) {
            throw new IllegalArgumentException("La descripción no puede exceder 255 caracteres");
        }
    }
}