package com.jlzDev.inventario.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Configuración independiente para PasswordEncoder
 * Separada de SecurityConfig para evitar dependencias circulares
 */
@Configuration
public class PasswordEncoderConfig {

    /**
     * Bean para encriptar contraseñas con BCrypt
     * Fuerza de encriptación: 12 (más seguro que el default 10)
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }
}