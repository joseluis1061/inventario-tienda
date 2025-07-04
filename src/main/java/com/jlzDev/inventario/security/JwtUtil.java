package com.jlzDev.inventario.security;

import com.jlzDev.inventario.dto.response.TokenResponse;
import com.jlzDev.inventario.entity.Usuario;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.function.Function;

/**
 * Utilidad para manejo de JWT (JSON Web Tokens)
 * Genera, valida y extrae información de tokens de acceso y refresh
 */
@Component
@Slf4j
public class JwtUtil {

    // Claims personalizados
    private static final String CLAIM_USER_ID = "userId";
    private static final String CLAIM_USERNAME = "username";
    private static final String CLAIM_ROLE = "role";
    private static final String CLAIM_PERMISSIONS = "permissions";
    private static final String CLAIM_SESSION_ID = "sessionId";
    private static final String CLAIM_DEVICE_INFO = "deviceInfo";
    private static final String CLAIM_TOKEN_TYPE = "tokenType";

    // Configuración de expiración
    private final int jwtExpirationHours;
    private final int refreshExpirationDays;
    private final SecretKey secretKey;

    /**
     * Enum para tipos de token
     */
    public enum TokenType {
        ACCESS("ACCESS"),
        REFRESH("REFRESH");

        private final String value;

        TokenType(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }
    }

    /**
     * Constructor con inyección de propiedades
     */
    public JwtUtil(@Value("${app.security.jwt.expiration-hours:24}") int jwtExpirationHours,
                   @Value("${app.security.jwt.refresh-expiration-days:30}") int refreshExpirationDays,
                   @Value("${app.security.jwt.secret:}") String jwtSecret) {

        this.jwtExpirationHours = jwtExpirationHours;
        this.refreshExpirationDays = refreshExpirationDays;

        // Usar el secreto del application.properties o el hardcoded como fallback
        String secret = (jwtSecret != null && !jwtSecret.trim().isEmpty())
                ? jwtSecret
                : "mi-clave-secreta-super-segura-para-jwt-inventario-2024";

        // Generar clave segura desde el string
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes());

        log.info("JwtUtil inicializado - Expiración: {}h, Refresh: {}d", jwtExpirationHours, refreshExpirationDays);
    }

    /**
     * Generar access token para un usuario
     */
    public String generateAccessToken(Usuario usuario, String sessionId, String deviceInfo) {
        log.debug("Generando access token para usuario: {}", usuario.getUsername());

        Map<String, Object> claims = new HashMap<>();
        claims.put(CLAIM_USER_ID, usuario.getId());
        claims.put(CLAIM_USERNAME, usuario.getUsername());
        claims.put(CLAIM_ROLE, usuario.getRol().getNombre());
        claims.put(CLAIM_PERMISSIONS, determinarPermisos(usuario.getRol().getNombre()));
        claims.put(CLAIM_TOKEN_TYPE, TokenType.ACCESS.getValue());
        claims.put(CLAIM_SESSION_ID, sessionId);
        claims.put(CLAIM_DEVICE_INFO, deviceInfo);

        Date expirationDate = Date.from(
                LocalDateTime.now()
                        .plusHours(jwtExpirationHours)
                        .atZone(ZoneId.systemDefault())
                        .toInstant()
        );

        String token = Jwts.builder()
                .setClaims(claims)
                .setSubject(usuario.getUsername())
                .setIssuedAt(new Date())
                .setExpiration(expirationDate)
                .setIssuer("inventario-api")
                .setAudience("inventario-clients")
                .setId(UUID.randomUUID().toString())
                .signWith(secretKey, SignatureAlgorithm.HS512)
                .compact();

        log.info("Access token generado exitosamente para usuario: {}", usuario.getUsername());
        return token;
    }

    /**
     * Generar refresh token para un usuario
     */
    public String generateRefreshToken(Usuario usuario, String sessionId, String deviceInfo) {
        log.debug("Generando refresh token para usuario: {}", usuario.getUsername());

        Map<String, Object> claims = new HashMap<>();
        claims.put(CLAIM_USER_ID, usuario.getId());
        claims.put(CLAIM_USERNAME, usuario.getUsername());
        claims.put(CLAIM_ROLE, usuario.getRol().getNombre());
        claims.put(CLAIM_TOKEN_TYPE, TokenType.REFRESH.getValue());
        claims.put(CLAIM_SESSION_ID, sessionId);
        claims.put(CLAIM_DEVICE_INFO, deviceInfo);

        Date expirationDate = Date.from(
                LocalDateTime.now()
                        .plusDays(refreshExpirationDays)
                        .atZone(ZoneId.systemDefault())
                        .toInstant()
        );

        String token = Jwts.builder()
                .setClaims(claims)
                .setSubject(usuario.getUsername())
                .setIssuedAt(new Date())
                .setExpiration(expirationDate)
                .setIssuer("inventario-api")
                .setAudience("inventario-clients")
                .setId(UUID.randomUUID().toString())
                .signWith(secretKey, SignatureAlgorithm.HS512)
                .compact();

        log.info("Refresh token generado exitosamente para usuario: {}", usuario.getUsername());
        return token;
    }

    /**
     * Extraer username del token
     */
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    /**
     * Extraer fecha de expiración del token
     */
    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    /**
     * Extraer fecha de expiración como LocalDateTime
     */
    public LocalDateTime extractExpirationAsLocalDateTime(String token) {
        Date expiration = extractExpiration(token);
        return expiration.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
    }

    /**
     * Extraer session ID del token
     */
    public String extractSessionId(String token) {
        return extractClaim(token, claims -> claims.get(CLAIM_SESSION_ID, String.class));
    }

    /**
     * Extraer user ID del token
     */
    public Long extractUserId(String token) {
        return extractClaim(token, claims -> claims.get(CLAIM_USER_ID, Long.class));
    }

    /**
     * Extraer rol del token
     */
    public String extractRole(String token) {
        return extractClaim(token, claims -> claims.get(CLAIM_ROLE, String.class));
    }

    /**
     * Extraer tipo de token (ACCESS/REFRESH)
     */
    public TokenType extractTokenType(String token) {
        String type = extractClaim(token, claims -> claims.get(CLAIM_TOKEN_TYPE, String.class));
        return "REFRESH".equals(type) ? TokenType.REFRESH : TokenType.ACCESS;
    }

    /**
     * Extraer claim específico del token
     */
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    /**
     * Extraer todos los claims del token
     */
    private Claims extractAllClaims(String token) {
        try {
            return Jwts.parser()
                    .setSigningKey(secretKey)
                    .parseClaimsJws(token)
                    .getBody();
        } catch (Exception e) {
            log.warn("Error al extraer claims del token: {}", e.getMessage());
            throw e;
        }
    }

    /**
     * Verificar si el token está expirado
     */
    public Boolean isTokenExpired(String token) {
        try {
            Date expiration = extractExpiration(token);
            return expiration.before(new Date());
        } catch (Exception e) {
            log.warn("Error al verificar expiración del token: {}", e.getMessage());
            return true; // Si hay error, considerar como expirado
        }
    }

    /**
     * Validar token contra un usuario específico
     */
    public Boolean validateToken(String token, String username) {
        try {
            final String tokenUsername = extractUsername(token);
            return (tokenUsername.equals(username) && !isTokenExpired(token));
        } catch (Exception e) {
            log.warn("Error al validar token para usuario {}: {}", username, e.getMessage());
            return false;
        }
    }

    /**
     * Validar token sin verificar usuario específico
     */
    public TokenResponse validateToken(String token) {
        try {
            // Verificar formato básico
            if (token == null || token.trim().isEmpty()) {
                return TokenResponse.tokenInvalido("Token vacío");
            }

            // Remover prefijo Bearer si existe
            token = cleanToken(token);

            // Extraer información del token
            String username = extractUsername(token);
            String role = extractRole(token);
            String sessionId = extractSessionId(token);
            LocalDateTime expiresAt = extractExpirationAsLocalDateTime(token);

            // Verificar si está expirado
            if (isTokenExpired(token)) {
                return TokenResponse.tokenExpirado(username, expiresAt);
            }

            // Token válido
            return TokenResponse.tokenValido(username, role, expiresAt, sessionId);

        } catch (ExpiredJwtException e) {
            log.debug("Token expirado: {}", e.getMessage());
            String username = e.getClaims().getSubject();
            Date expiration = e.getClaims().getExpiration();
            LocalDateTime expiresAt = expiration.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
            return TokenResponse.tokenExpirado(username, expiresAt);

        } catch (UnsupportedJwtException e) {
            log.warn("Token no soportado: {}", e.getMessage());
            return TokenResponse.tokenInvalido("Formato de token no soportado");

        } catch (MalformedJwtException e) {
            log.warn("Token malformado: {}", e.getMessage());
            return TokenResponse.tokenInvalido("Token malformado");

        } catch (SignatureException e) {
            log.warn("Firma de token inválida: {}", e.getMessage());
            return TokenResponse.tokenInvalido("Firma inválida");

        } catch (IllegalArgumentException e) {
            log.warn("Token inválido: {}", e.getMessage());
            return TokenResponse.tokenInvalido("Token inválido");

        } catch (Exception e) {
            log.error("Error inesperado validando token: {}", e.getMessage());
            return TokenResponse.tokenInvalido("Error de validación");
        }
    }

    /**
     * Verificar si es un refresh token válido
     */
    public boolean isValidRefreshToken(String token) {
        try {
            TokenResponse validation = validateToken(token);
            if (!validation.esExitoso()) {
                return false;
            }

            TokenType tokenType = extractTokenType(token);
            return tokenType == TokenType.REFRESH;

        } catch (Exception e) {
            log.warn("Error validando refresh token: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Refrescar access token usando refresh token
     */
    public String refreshAccessToken(String refreshToken, Usuario usuario, String sessionId) {
        try {
            // Validar que sea un refresh token válido
            if (!isValidRefreshToken(refreshToken)) {
                throw new RuntimeException("Refresh token inválido");
            }

            // Extraer device info del refresh token
            String deviceInfo = extractClaim(refreshToken, claims ->
                    claims.get(CLAIM_DEVICE_INFO, String.class));

            // Generar nuevo access token
            return generateAccessToken(usuario, sessionId, deviceInfo);

        } catch (Exception e) {
            log.error("Error refrescando access token: {}", e.getMessage());
            throw new RuntimeException("Error al refrescar token", e);
        }
    }

    /**
     * Generar session ID único
     */
    public String generateSessionId(String username) {
        return username + "_" + System.currentTimeMillis() + "_" + UUID.randomUUID().toString().substring(0, 8);
    }

    /**
     * Obtener tiempo restante del token en segundos
     */
    public Long getTokenRemainingTime(String token) {
        try {
            LocalDateTime expiresAt = extractExpirationAsLocalDateTime(token);
            LocalDateTime now = LocalDateTime.now();

            if (expiresAt.isBefore(now)) {
                return 0L;
            }

            return java.time.Duration.between(now, expiresAt).getSeconds();

        } catch (Exception e) {
            log.warn("Error calculando tiempo restante del token: {}", e.getMessage());
            return 0L;
        }
    }

    /**
     * Obtener configuración de expiración
     */
    public Map<String, Object> getExpirationConfig() {
        Map<String, Object> config = new HashMap<>();
        config.put("accessTokenHours", jwtExpirationHours);
        config.put("refreshTokenDays", refreshExpirationDays);
        config.put("accessTokenSeconds", jwtExpirationHours * 3600);
        config.put("refreshTokenSeconds", refreshExpirationDays * 24 * 3600);
        return config;
    }

    // ===== MÉTODOS PRIVADOS =====

    /**
     * Limpiar token removiendo prefijo Bearer si existe
     */
    private String cleanToken(String token) {
        if (token.startsWith("Bearer ")) {
            return token.substring(7);
        }
        return token;
    }

    /**
     * Determinar permisos basados en el rol
     */
    private String[] determinarPermisos(String roleName) {
        switch (roleName.toUpperCase()) {
            case "ADMIN":
                return new String[]{"READ", "WRITE", "DELETE", "ADMIN", "REPORTS", "USERS"};
            case "GERENTE":
                return new String[]{"READ", "WRITE", "REPORTS", "INVENTORY"};
            case "EMPLEADO":
                return new String[]{"READ", "INVENTORY"};
            default:
                return new String[]{"READ"};
        }
    }
}