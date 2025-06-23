package com.jlzDev.inventario.repository;

import com.jlzDev.inventario.entity.Rol;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RolRepository extends JpaRepository<Rol, Long> {

    // Buscar rol por nombre
    Optional<Rol> findByNombre(String nombre);

    // Verificar si existe un rol por nombre
    boolean existsByNombre(String nombre);

    // Buscar rol por nombre ignorando mayúsculas/minúsculas
    Optional<Rol> findByNombreIgnoreCase(String nombre);

    // Contar usuarios activos por rol
    @Query("SELECT COUNT(u) FROM Usuario u WHERE u.rol.id = :rolId AND u.activo = true")
    Long countUsuariosActivosByRolId(Long rolId);

    // Verificar si el rol tiene usuarios asociados
    @Query("SELECT CASE WHEN COUNT(u) > 0 THEN true ELSE false END FROM Usuario u WHERE u.rol.id = :rolId")
    boolean hasUsuariosAsociados(Long rolId);
}