package com.jlzDev.inventario.repository;

import com.jlzDev.inventario.entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    // Buscar usuario por username
    Optional<Usuario> findByUsername(String username);

    // Buscar usuario por email
    Optional<Usuario> findByEmail(String email);

    // Verificar si existe username
    boolean existsByUsername(String username);

    // Verificar si existe email
    boolean existsByEmail(String email);

    // Buscar usuarios activos
    List<Usuario> findByActivoTrue();

    // Buscar usuarios inactivos
    List<Usuario> findByActivoFalse();

    // Buscar usuarios por rol
    List<Usuario> findByRolId(Long rolId);

    // Buscar usuarios activos por rol
    List<Usuario> findByRolIdAndActivoTrue(Long rolId);

    // Buscar por nombre completo que contenga el texto
    List<Usuario> findByNombreCompletoContainingIgnoreCase(String nombreCompleto);

    // Buscar usuarios creados después de una fecha
    List<Usuario> findByFechaCreacionAfter(LocalDateTime fecha);

    // Contar usuarios por rol
    @Query("SELECT COUNT(u) FROM Usuario u WHERE u.rol.id = :rolId")
    Long countByRolId(@Param("rolId") Long rolId);

    // Contar usuarios activos
    @Query("SELECT COUNT(u) FROM Usuario u WHERE u.activo = true")
    Long countUsuariosActivos();

    // Obtener usuarios con movimientos
    @Query("SELECT DISTINCT u FROM Usuario u JOIN u.movimientos m")
    List<Usuario> findUsuariosConMovimientos();

    // Buscar usuarios por rol y estado
    @Query("SELECT u FROM Usuario u WHERE u.rol.nombre = :rolNombre AND u.activo = :activo")
    List<Usuario> findByRolNombreAndActivo(@Param("rolNombre") String rolNombre, @Param("activo") Boolean activo);

    // Verificar si el usuario tiene movimientos
    @Query("SELECT CASE WHEN COUNT(m) > 0 THEN true ELSE false END FROM Movimiento m WHERE m.usuario.id = :usuarioId")
    boolean hasMovimientos(@Param("usuarioId") Long usuarioId);
}