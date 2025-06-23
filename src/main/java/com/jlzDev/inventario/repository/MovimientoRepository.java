package com.jlzDev.inventario.repository;

import com.jlzDev.inventario.entity.Movimiento;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface MovimientoRepository extends JpaRepository<Movimiento, Long> {

    // Buscar movimientos por producto
    List<Movimiento> findByProductoId(Long productoId);

    // Buscar movimientos por usuario
    List<Movimiento> findByUsuarioId(Long usuarioId);

    // Buscar movimientos por tipo
    List<Movimiento> findByTipoMovimiento(Movimiento.TipoMovimiento tipoMovimiento);

    // Buscar movimientos por producto y tipo
    List<Movimiento> findByProductoIdAndTipoMovimiento(Long productoId, Movimiento.TipoMovimiento tipoMovimiento);

    // Buscar movimientos por rango de fechas
    List<Movimiento> findByFechaBetween(LocalDateTime fechaInicio, LocalDateTime fechaFin);

    // Buscar movimientos por producto en un rango de fechas
    List<Movimiento> findByProductoIdAndFechaBetween(Long productoId, LocalDateTime fechaInicio, LocalDateTime fechaFin);

    // Buscar movimientos por usuario en un rango de fechas
    List<Movimiento> findByUsuarioIdAndFechaBetween(Long usuarioId, LocalDateTime fechaInicio, LocalDateTime fechaFin);

    // Obtener historial paginado ordenado por fecha descendente
    Page<Movimiento> findAllByOrderByFechaDesc(Pageable pageable);

    // Obtener movimientos recientes (últimos N días)
    @Query("SELECT m FROM Movimiento m WHERE m.fecha >= :fecha ORDER BY m.fecha DESC")
    List<Movimiento> findMovimientosRecientes(@Param("fecha") LocalDateTime fecha);

    // Sumar entradas por producto
    @Query("SELECT COALESCE(SUM(m.cantidad), 0) FROM Movimiento m WHERE m.producto.id = :productoId AND m.tipoMovimiento = 'ENTRADA'")
    Integer sumEntradasByProductoId(@Param("productoId") Long productoId);

    // Sumar salidas por producto
    @Query("SELECT COALESCE(SUM(m.cantidad), 0) FROM Movimiento m WHERE m.producto.id = :productoId AND m.tipoMovimiento = 'SALIDA'")
    Integer sumSalidasByProductoId(@Param("productoId") Long productoId);

    // Contar movimientos por tipo
    @Query("SELECT COUNT(m) FROM Movimiento m WHERE m.tipoMovimiento = :tipo")
    Long countByTipoMovimiento(@Param("tipo") Movimiento.TipoMovimiento tipo);

    // Obtener movimientos por categoría de producto
    @Query("""
        SELECT m FROM Movimiento m 
        JOIN m.producto p 
        WHERE p.categoria.id = :categoriaId 
        ORDER BY m.fecha DESC
    """)
    List<Movimiento> findByProductoCategoriaId(@Param("categoriaId") Long categoriaId);

    // Obtener últimos movimientos por usuario
    @Query("SELECT m FROM Movimiento m WHERE m.usuario.id = :usuarioId ORDER BY m.fecha DESC")
    Page<Movimiento> findUltimosMovimientosByUsuario(@Param("usuarioId") Long usuarioId, Pageable pageable);

    // Estadísticas de movimientos por periodo
    @Query("""
        SELECT 
            m.tipoMovimiento,
            COUNT(m) as cantidad,
            SUM(m.cantidad) as totalUnidades
        FROM Movimiento m 
        WHERE m.fecha BETWEEN :fechaInicio AND :fechaFin 
        GROUP BY m.tipoMovimiento
    """)
    List<Object[]> getEstadisticasPorPeriodo(@Param("fechaInicio") LocalDateTime fechaInicio,
                                             @Param("fechaFin") LocalDateTime fechaFin);

    // Productos más movidos en un periodo
    @Query("""
        SELECT 
            p.nombre,
            COUNT(m) as totalMovimientos,
            SUM(CASE WHEN m.tipoMovimiento = 'ENTRADA' THEN m.cantidad ELSE 0 END) as entradas,
            SUM(CASE WHEN m.tipoMovimiento = 'SALIDA' THEN m.cantidad ELSE 0 END) as salidas
        FROM Movimiento m 
        JOIN m.producto p 
        WHERE m.fecha BETWEEN :fechaInicio AND :fechaFin 
        GROUP BY p.id, p.nombre 
        ORDER BY COUNT(m) DESC
    """)
    List<Object[]> getProductosMasMovidos(@Param("fechaInicio") LocalDateTime fechaInicio,
                                          @Param("fechaFin") LocalDateTime fechaFin);

    // Buscar por motivo que contenga texto
    List<Movimiento> findByMotivoContainingIgnoreCase(String motivo);
}