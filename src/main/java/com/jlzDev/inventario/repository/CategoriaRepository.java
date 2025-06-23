package com.jlzDev.inventario.repository;

import com.jlzDev.inventario.entity.Categoria;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategoriaRepository extends JpaRepository<Categoria, Long> {

    // Buscar categoría por nombre
    Optional<Categoria> findByNombre(String nombre);

    // Verificar si existe una categoría por nombre
    boolean existsByNombre(String nombre);

    // Buscar categoría por nombre ignorando mayúsculas/minúsculas
    Optional<Categoria> findByNombreIgnoreCase(String nombre);

    // Buscar categorías que contengan el texto en el nombre
    List<Categoria> findByNombreContainingIgnoreCase(String nombre);

    // Contar productos por categoría
    @Query("SELECT COUNT(p) FROM Producto p WHERE p.categoria.id = :categoriaId")
    Long countProductosByCategoriaId(@Param("categoriaId") Long categoriaId);

    // Verificar si la categoría tiene productos asociados
    @Query("SELECT CASE WHEN COUNT(p) > 0 THEN true ELSE false END FROM Producto p WHERE p.categoria.id = :categoriaId")
    boolean hasProductosAsociados(@Param("categoriaId") Long categoriaId);

    // Obtener categorías con productos
    @Query("SELECT DISTINCT c FROM Categoria c JOIN c.productos p")
    List<Categoria> findCategoriasConProductos();

    // Obtener categorías sin productos
    @Query("SELECT c FROM Categoria c WHERE c.productos IS EMPTY")
    List<Categoria> findCategoriasSinProductos();
}