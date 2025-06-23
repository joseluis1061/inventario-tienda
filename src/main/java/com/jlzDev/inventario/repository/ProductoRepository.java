package com.jlzDev.inventario.repository;

import com.jlzDev.inventario.entity.Producto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ProductoRepository extends JpaRepository<Producto, Long> {

    // Buscar producto por nombre
    Optional<Producto> findByNombre(String nombre);

    // Verificar si existe producto por nombre
    boolean existsByNombre(String nombre);

    // Buscar productos por categoría
    List<Producto> findByCategoriaId(Long categoriaId);

    // Buscar productos que contengan el texto en el nombre
    List<Producto> findByNombreContainingIgnoreCase(String nombre);

    // Buscar productos con stock bajo (stock actual <= stock mínimo)
    @Query("SELECT p FROM Producto p WHERE p.stockActual <= p.stockMinimo")
    List<Producto> findProductosConStockBajo();

    // Buscar productos con stock crítico (stock actual = 0)
    @Query("SELECT p FROM Producto p WHERE p.stockActual = 0")
    List<Producto> findProductosConStockCritico();

    // Buscar productos por rango de precio
    List<Producto> findByPrecioBetween(BigDecimal precioMin, BigDecimal precioMax);

    // Buscar productos por stock mayor a una cantidad
    @Query("SELECT p FROM Producto p WHERE p.stockActual > :cantidad")
    List<Producto> findByStockActualGreaterThan(@Param("cantidad") Integer cantidad);

    // Buscar productos por categoría y con stock disponible
    @Query("SELECT p FROM Producto p WHERE p.categoria.id = :categoriaId AND p.stockActual > 0")
    List<Producto> findByCategoriaIdAndStockDisponible(@Param("categoriaId") Long categoriaId);

    // Obtener productos más vendidos (por cantidad de movimientos de salida)
    @Query("""
        SELECT p FROM Producto p 
        LEFT JOIN p.movimientos m 
        WHERE m.tipoMovimiento = 'SALIDA' 
        GROUP BY p 
        ORDER BY COUNT(m) DESC
    """)
    List<Producto> findProductosMasVendidos();

    // Contar productos por categoría
    @Query("SELECT COUNT(p) FROM Producto p WHERE p.categoria.id = :categoriaId")
    Long countByCategoriaId(@Param("categoriaId") Long categoriaId);

    // Verificar si el producto tiene movimientos
    @Query("SELECT CASE WHEN COUNT(m) > 0 THEN true ELSE false END FROM Movimiento m WHERE m.producto.id = :productoId")
    boolean hasMovimientos(@Param("productoId") Long productoId);

    // Buscar productos creados después de una fecha
    List<Producto> findByFechaCreacionAfter(LocalDateTime fecha);

    // Buscar productos actualizados después de una fecha
    List<Producto> findByFechaActualizacionAfter(LocalDateTime fecha);

    // Obtener estadísticas de stock
    @Query("""
        SELECT 
            COUNT(p) as total,
            COUNT(CASE WHEN p.stockActual <= p.stockMinimo THEN 1 END) as stockBajo,
            COUNT(CASE WHEN p.stockActual = 0 THEN 1 END) as sinStock
        FROM Producto p
    """)
    Object[] getEstadisticasStock();

    // Buscar productos por categoría y nombre
    @Query("SELECT p FROM Producto p WHERE p.categoria.id = :categoriaId AND p.nombre LIKE %:nombre%")
    List<Producto> findByCategoriaIdAndNombreContaining(@Param("categoriaId") Long categoriaId, @Param("nombre") String nombre);
}