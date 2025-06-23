package com.jlzDev.inventario.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

/**
 * Configuración de CORS (Cross-Origin Resource Sharing)
 * Versión simplificada para desarrollo sin conflictos
 */
@Configuration
public class CorsConfig {

    /**
     * Configuración única de CORS usando CorsConfigurationSource
     * Esta es la única configuración CORS que debe existir
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        // Para desarrollo: permitir todos los orígenes
        configuration.setAllowedOrigins(Arrays.asList("*"));

        // Métodos HTTP permitidos
        configuration.setAllowedMethods(Arrays.asList(
                "GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS", "HEAD"
        ));

        // Headers permitidos
        configuration.setAllowedHeaders(Arrays.asList("*"));

        // Headers expuestos
        configuration.setExposedHeaders(Arrays.asList(
                "Access-Control-Allow-Origin",
                "Content-Disposition"
        ));

        // IMPORTANTE: Deshabilitar credenciales para poder usar "*"
        configuration.setAllowCredentials(false);

        // Cache para preflight requests
        configuration.setMaxAge(3600L);

        // Aplicar configuración a todas las rutas
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);

        return source;
    }
}