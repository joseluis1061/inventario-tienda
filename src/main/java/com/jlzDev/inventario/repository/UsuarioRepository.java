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

    // ===== MÉTODOS BÁSICOS DE BÚSQUEDA =====

    // Buscar usuario por username
    Optional<Usuario> findByUsername(String username);

    // Buscar usuario por email
    Optional<Usuario> findByEmail(String email);

    // ===== MÉTODOS REQUERIDOS POR UserDetailsServiceImpl =====

    // Buscar usuario por username y que esté activo
    Optional<Usuario> findByUsernameAndActivoTrue(String username);

    // Buscar usuario por email y que esté activo
    Optional<Usuario> findByEmailAndActivoTrue(String email);

    // ===== MÉTODOS DE VERIFICACIÓN =====

    // Verificar si existe username
    boolean existsByUsername(String username);

    // Verificar si existe email
    boolean existsByEmail(String email);

    // Verificar si existe username activo
    boolean existsByUsernameAndActivoTrue(String username);

    // Verificar si existe email activo
    boolean existsByEmailAndActivoTrue(String email);

    // ===== MÉTODOS POR ESTADO =====

    // Buscar usuarios activos
    List<Usuario> findByActivoTrue();

    // Buscar usuarios inactivos
    List<Usuario> findByActivoFalse();

    // ===== MÉTODOS POR ROL =====

    // Buscar usuarios por rol
    List<Usuario> findByRolId(Long rolId);

    // Buscar usuarios activos por rol
    List<Usuario> findByRolIdAndActivoTrue(Long rolId);

    // Buscar usuarios por nombre del rol
    @Query("SELECT u FROM Usuario u WHERE u.rol.nombre = :rolNombre")
    List<Usuario> findByRolNombre(@Param("rolNombre") String rolNombre);

    // Buscar usuarios por rol y estado
    @Query("SELECT u FROM Usuario u WHERE u.rol.nombre = :rolNombre AND u.activo = :activo")
    List<Usuario> findByRolNombreAndActivo(@Param("rolNombre") String rolNombre, @Param("activo") Boolean activo);

    // ===== MÉTODOS DE BÚSQUEDA AVANZADA =====

    // Buscar por nombre completo que contenga el texto
    List<Usuario> findByNombreCompletoContainingIgnoreCase(String nombreCompleto);

    // Buscar usuarios creados después de una fecha
    List<Usuario> findByFechaCreacionAfter(LocalDateTime fecha);

    // Buscar usuarios creados entre fechas
    List<Usuario> findByFechaCreacionBetween(LocalDateTime fechaInicio, LocalDateTime fechaFin);

    // Buscar por username o email (para login flexible)
    @Query("SELECT u FROM Usuario u WHERE (u.username = :usernameOrEmail OR u.email = :usernameOrEmail) AND u.activo = true")
    Optional<Usuario> findByUsernameOrEmailAndActivoTrue(@Param("usernameOrEmail") String usernameOrEmail);

    // ===== MÉTODOS DE CONTEO =====

    // Contar usuarios por rol
    @Query("SELECT COUNT(u) FROM Usuario u WHERE u.rol.id = :rolId")
    Long countByRolId(@Param("rolId") Long rolId);

    // Contar usuarios activos
    @Query("SELECT COUNT(u) FROM Usuario u WHERE u.activo = true")
    Long countUsuariosActivos();

    // Contar usuarios inactivos
    @Query("SELECT COUNT(u) FROM Usuario u WHERE u.activo = false")
    Long countUsuariosInactivos();

    // Contar usuarios por rol y estado
    @Query("SELECT COUNT(u) FROM Usuario u WHERE u.rol.nombre = :rolNombre AND u.activo = :activo")
    Long countByRolNombreAndActivo(@Param("rolNombre") String rolNombre, @Param("activo") Boolean activo);

    // ===== MÉTODOS RELACIONADOS CON MOVIMIENTOS =====

    // Obtener usuarios con movimientos
    @Query("SELECT DISTINCT u FROM Usuario u JOIN u.movimientos m")
    List<Usuario> findUsuariosConMovimientos();

    // Verificar si el usuario tiene movimientos
    @Query("SELECT CASE WHEN COUNT(m) > 0 THEN true ELSE false END FROM Movimiento m WHERE m.usuario.id = :usuarioId")
    boolean hasMovimientos(@Param("usuarioId") Long usuarioId);

    // Obtener usuarios que han hecho movimientos en un período
    @Query("SELECT DISTINCT u FROM Usuario u JOIN u.movimientos m WHERE m.fecha BETWEEN :fechaInicio AND :fechaFin")
    List<Usuario> findUsuariosConMovimientosEnPeriodo(@Param("fechaInicio") LocalDateTime fechaInicio,
                                                      @Param("fechaFin") LocalDateTime fechaFin);

    // ===== MÉTODOS PARA ADMINISTRACIÓN =====

    // Buscar usuarios administradores activos
    @Query("SELECT u FROM Usuario u WHERE u.rol.nombre = 'ADMIN' AND u.activo = true")
    List<Usuario> findAdministradoresActivos();

    // Buscar usuarios por múltiples criterios
    @Query("SELECT u FROM Usuario u WHERE " +
            "(:username IS NULL OR u.username LIKE %:username%) AND " +
            "(:email IS NULL OR u.email LIKE %:email%) AND " +
            "(:nombreCompleto IS NULL OR u.nombreCompleto LIKE %:nombreCompleto%) AND " +
            "(:rolId IS NULL OR u.rol.id = :rolId) AND " +
            "(:activo IS NULL OR u.activo = :activo)")
    List<Usuario> findByCriteriaMultiple(@Param("username") String username,
                                         @Param("email") String email,
                                         @Param("nombreCompleto") String nombreCompleto,
                                         @Param("rolId") Long rolId,
                                         @Param("activo") Boolean activo);

    // ===== MÉTODOS PARA ESTADÍSTICAS =====

    // Obtener usuarios más activos (por número de movimientos)
    @Query("SELECT u, COUNT(m) as movimientos FROM Usuario u " +
            "LEFT JOIN u.movimientos m " +
            "GROUP BY u " +
            "ORDER BY COUNT(m) DESC")
    List<Object[]> findUsuariosMasActivos();

    // Obtener últimos usuarios registrados
    @Query("SELECT u FROM Usuario u ORDER BY u.fechaCreacion DESC")
    List<Usuario> findUltimosUsuariosRegistrados();

    // ===== MÉTODOS DE SEGURIDAD =====

    // Verificar si un username está disponible (no existe o está inactivo)
    @Query("SELECT CASE WHEN COUNT(u) = 0 THEN true ELSE false END FROM Usuario u WHERE u.username = :username AND u.activo = true")
    boolean isUsernameAvailable(@Param("username") String username);

    // Verificar si un email está disponible (no existe o está inactivo)
    @Query("SELECT CASE WHEN COUNT(u) = 0 THEN true ELSE false END FROM Usuario u WHERE u.email = :email AND u.activo = true")
    boolean isEmailAvailable(@Param("email") String email);

    // Buscar usuarios que coincidan con username o email (para validación de duplicados)
    @Query("SELECT u FROM Usuario u WHERE u.username = :value OR u.email = :value")
    List<Usuario> findByUsernameOrEmail(@Param("value") String value);
}