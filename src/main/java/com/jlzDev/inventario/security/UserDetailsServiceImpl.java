package com.jlzDev.inventario.security;

import com.jlzDev.inventario.entity.Usuario;
import com.jlzDev.inventario.repository.UsuarioRepository;
import com.jlzDev.inventario.service.UsuarioService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Optional;

/**
 * Implementación de UserDetailsService para Spring Security
 * Conecta Spring Security con nuestro sistema de usuarios y roles
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UsuarioService usuarioService;
    private final UsuarioRepository usuarioRepository;

    /**
     * Cargar usuario por username para Spring Security
     * Este método es llamado automáticamente por Spring Security durante el login
     */
    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        log.debug("Cargando usuario por username: {}", username);

        try {
            // Buscar usuario en la base de datos que esté activo
            Optional<Usuario> usuarioOpt = usuarioRepository.findByUsernameAndActivoTrue(username);

            if (usuarioOpt.isEmpty()) {
                log.warn("Usuario no encontrado o inactivo: {}", username);
                throw new UsernameNotFoundException("Usuario no encontrado: " + username);
            }

            Usuario usuario = usuarioOpt.get();

            // Crear y retornar UserDetails
            UserDetails userDetails = createUserDetails(usuario);

            log.info("Usuario cargado exitosamente: {} con rol: {}",
                    username, usuario.getRol().getNombre());

            return userDetails;

        } catch (UsernameNotFoundException e) {
            // Re-lanzar excepciones de usuario no encontrado
            throw e;
        } catch (Exception e) {
            log.error("Error inesperado al cargar usuario {}: {}", username, e.getMessage());
            throw new UsernameNotFoundException("Error al cargar usuario: " + username, e);
        }
    }

    /**
     * Cargar usuario por username o email
     * Método adicional para flexibilidad en el login
     */
    public UserDetails loadUserByUsernameOrEmail(String usernameOrEmail) throws UsernameNotFoundException {
        log.debug("Intentando cargar usuario por username o email: {}", usernameOrEmail);

        try {
            // Usar el método específico del repository que busca por ambos campos
            Optional<Usuario> usuarioOpt = usuarioRepository.findByUsernameOrEmailAndActivoTrue(usernameOrEmail);

            if (usuarioOpt.isEmpty()) {
                log.warn("Usuario no encontrado por username o email: {}", usernameOrEmail);
                throw new UsernameNotFoundException("Usuario no encontrado: " + usernameOrEmail);
            }

            Usuario usuario = usuarioOpt.get();
            UserDetails userDetails = createUserDetails(usuario);

            log.info("Usuario cargado exitosamente por username/email: {} (username real: {}) con rol: {}",
                    usernameOrEmail, usuario.getUsername(), usuario.getRol().getNombre());

            return userDetails;

        } catch (UsernameNotFoundException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error inesperado al cargar usuario por username/email {}: {}", usernameOrEmail, e.getMessage());
            throw new UsernameNotFoundException("Error al cargar usuario: " + usernameOrEmail, e);
        }
    }

    /**
     * Crear UserDetails desde nuestra entidad Usuario
     */
    private UserDetails createUserDetails(Usuario usuario) {
        // Crear autoridades (roles y permisos)
        Collection<GrantedAuthority> authorities = createAuthorities(usuario);

        // Crear y retornar UserDetails usando la clase User de Spring Security
        return User.builder()
                .username(usuario.getUsername())
                .password(usuario.getPassword()) // Ya está encriptado
                .authorities(authorities)
                .accountExpired(false) // Nuestro sistema no maneja expiración de cuenta
                .accountLocked(!usuario.getActivo())  // Usuario inactivo = bloqueado
                .credentialsExpired(false) // Nuestro sistema no maneja expiración de credenciales
                .disabled(!usuario.getActivo()) // Usuario inactivo = disabled
                .build();
    }

    /**
     * Crear autoridades (roles y permisos) para Spring Security
     */
    private Collection<GrantedAuthority> createAuthorities(Usuario usuario) {
        Collection<GrantedAuthority> authorities = new ArrayList<>();

        // Agregar rol principal con prefijo ROLE_
        String roleName = "ROLE_" + usuario.getRol().getNombre().toUpperCase();
        authorities.add(new SimpleGrantedAuthority(roleName));

        // Agregar rol sin prefijo para compatibilidad
        authorities.add(new SimpleGrantedAuthority(usuario.getRol().getNombre().toUpperCase()));

        // Agregar permisos específicos basados en el rol
        Collection<GrantedAuthority> permissions = createPermissionAuthorities(usuario.getRol().getNombre());
        authorities.addAll(permissions);

        log.debug("Autoridades creadas para usuario {}: {}", usuario.getUsername(), authorities);

        return authorities;
    }

    /**
     * Crear autoridades de permisos específicos basados en el rol
     */
    private Collection<GrantedAuthority> createPermissionAuthorities(String roleName) {
        Collection<GrantedAuthority> permissions = new ArrayList<>();

        switch (roleName.toUpperCase()) {
            case "ADMIN":
                // Administrador tiene todos los permisos
                permissions.add(new SimpleGrantedAuthority("READ"));
                permissions.add(new SimpleGrantedAuthority("WRITE"));
                permissions.add(new SimpleGrantedAuthority("DELETE"));
                permissions.add(new SimpleGrantedAuthority("ADMIN"));
                permissions.add(new SimpleGrantedAuthority("REPORTS"));
                permissions.add(new SimpleGrantedAuthority("USERS"));
                permissions.add(new SimpleGrantedAuthority("INVENTORY"));
                break;

            case "GERENTE":
                // Gerente tiene permisos de lectura, escritura, reportes e inventario
                permissions.add(new SimpleGrantedAuthority("READ"));
                permissions.add(new SimpleGrantedAuthority("WRITE"));
                permissions.add(new SimpleGrantedAuthority("REPORTS"));
                permissions.add(new SimpleGrantedAuthority("INVENTORY"));
                break;

            case "EMPLEADO":
                // Empleado solo tiene permisos de lectura e inventario básico
                permissions.add(new SimpleGrantedAuthority("READ"));
                permissions.add(new SimpleGrantedAuthority("INVENTORY"));
                break;

            default:
                // Rol desconocido, solo lectura
                permissions.add(new SimpleGrantedAuthority("READ"));
                log.warn("Rol desconocido: {}. Asignando solo permiso de lectura", roleName);
                break;
        }

        return permissions;
    }

    /**
     * Verificar si un usuario existe y está activo
     * Útil para validaciones adicionales
     */
    public boolean existsAndIsActive(String username) {
        try {
            return usuarioRepository.existsByUsernameAndActivoTrue(username);
        } catch (Exception e) {
            log.error("Error al verificar existencia y estado del usuario {}: {}", username, e.getMessage());
            return false;
        }
    }

    /**
     * Obtener Usuario entity desde username
     * Útil para obtener información completa del usuario
     */
    public Optional<Usuario> getUsuarioByUsername(String username) {
        try {
            return usuarioRepository.findByUsernameAndActivoTrue(username);
        } catch (Exception e) {
            log.error("Error al obtener usuario por username {}: {}", username, e.getMessage());
            return Optional.empty();
        }
    }

    /**
     * Verificar si un usuario tiene un rol específico
     */
    public boolean hasRole(String username, String role) {
        try {
            Optional<Usuario> usuarioOpt = usuarioRepository.findByUsernameAndActivoTrue(username);
            if (usuarioOpt.isPresent()) {
                String userRole = usuarioOpt.get().getRol().getNombre().toUpperCase();
                return userRole.equals(role.toUpperCase());
            }
            return false;
        } catch (Exception e) {
            log.error("Error al verificar rol para usuario {}: {}", username, e.getMessage());
            return false;
        }
    }

    /**
     * Verificar si un usuario tiene permisos de administrador
     */
    public boolean isAdmin(String username) {
        return hasRole(username, "ADMIN");
    }

    /**
     * Verificar si un usuario tiene permisos de gerente o superior
     */
    public boolean isManagerOrAbove(String username) {
        return hasRole(username, "ADMIN") || hasRole(username, "GERENTE");
    }

    /**
     * Obtener lista de autoridades como strings
     * Útil para logging y debugging
     */
    public String[] getAuthoritiesAsStrings(String username) {
        try {
            UserDetails userDetails = loadUserByUsername(username);
            return userDetails.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .toArray(String[]::new);
        } catch (Exception e) {
            log.error("Error al obtener autoridades para usuario {}: {}", username, e.getMessage());
            return new String[]{"ROLE_EMPLEADO"}; // default fallback
        }
    }

    /**
     * Recargar UserDetails después de cambios en el usuario
     * Útil después de actualizaciones de rol o estado
     */
    public UserDetails reloadUserDetails(String username) throws UsernameNotFoundException {
        log.debug("Recargando UserDetails para usuario: {}", username);
        return loadUserByUsername(username);
    }
}