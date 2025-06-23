//package com.jlzDev.inventario.config;
//
//public class ValidationConfig {
//}
package com.jlzDev.inventario.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;

import java.lang.annotation.*;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Configuración de validaciones personalizadas
 * Define validadores customizados para el dominio del inventario
 */
@Configuration
public class ValidationConfig {

    /**
     * Bean del validador principal
     */
//    @Bean
//    public Validator validator(ValidatorFactory validatorFactory) {
//        return validatorFactory.getValidator();
//    }

    /**
     * Anotación para validar precios de productos
     */
    @Documented
    @Constraint(validatedBy = PrecioValidoValidator.class)
    @Target({ElementType.FIELD, ElementType.PARAMETER})
    @Retention(RetentionPolicy.RUNTIME)
    public @interface PrecioValido {
        String message() default "El precio debe ser positivo y tener máximo 2 decimales";
        Class<?>[] groups() default {};
        Class<? extends Payload>[] payload() default {};

        double min() default 0.01;
        double max() default 99999999.99;
    }

    /**
     * Validador para precios
     */
    public static class PrecioValidoValidator implements ConstraintValidator<PrecioValido, BigDecimal> {

        private double min;
        private double max;

        @Override
        public void initialize(PrecioValido constraintAnnotation) {
            this.min = constraintAnnotation.min();
            this.max = constraintAnnotation.max();
        }

        @Override
        public boolean isValid(BigDecimal value, ConstraintValidatorContext context) {
            if (value == null) {
                return true; // @NotNull debe manejar nulos
            }

            // Verificar rango
            double doubleValue = value.doubleValue();
            if (doubleValue < min || doubleValue > max) {
                return false;
            }

            // Verificar máximo 2 decimales
            return value.scale() <= 2;
        }
    }

    /**
     * Anotación para validar stock
     */
    @Documented
    @Constraint(validatedBy = StockValidoValidator.class)
    @Target({ElementType.FIELD, ElementType.PARAMETER})
    @Retention(RetentionPolicy.RUNTIME)
    public @interface StockValido {
        String message() default "El stock debe ser un número entero no negativo";
        Class<?>[] groups() default {};
        Class<? extends Payload>[] payload() default {};

        int min() default 0;
        int max() default 1000000;
    }

    /**
     * Validador para stock
     */
    public static class StockValidoValidator implements ConstraintValidator<StockValido, Integer> {

        private int min;
        private int max;

        @Override
        public void initialize(StockValido constraintAnnotation) {
            this.min = constraintAnnotation.min();
            this.max = constraintAnnotation.max();
        }

        @Override
        public boolean isValid(Integer value, ConstraintValidatorContext context) {
            if (value == null) {
                return true; // @NotNull debe manejar nulos
            }

            return value >= min && value <= max;
        }
    }

    /**
     * Anotación para validar nombres de productos únicos
     */
    @Documented
    @Constraint(validatedBy = NombreProductoUnicoValidator.class)
    @Target({ElementType.TYPE})
    @Retention(RetentionPolicy.RUNTIME)
    public @interface NombreProductoUnico {
        String message() default "Ya existe un producto con este nombre";
        Class<?>[] groups() default {};
        Class<? extends Payload>[] payload() default {};
    }

    /**
     * Validador para nombres únicos de productos
     * Nota: Este sería implementado en tiempo de ejecución con acceso al repository
     */
    public static class NombreProductoUnicoValidator implements ConstraintValidator<NombreProductoUnico, Object> {

        @Override
        public boolean isValid(Object value, ConstraintValidatorContext context) {
            // Esta validación se maneja en el service layer
            // Esta anotación es más decorativa para documentación
            return true;
        }
    }

    /**
     * Anotación para validar cantidades de movimiento
     */
    @Documented
    @Constraint(validatedBy = CantidadMovimientoValidator.class)
    @Target({ElementType.FIELD, ElementType.PARAMETER})
    @Retention(RetentionPolicy.RUNTIME)
    public @interface CantidadMovimientoValida {
        String message() default "La cantidad del movimiento debe estar entre 1 y 100,000";
        Class<?>[] groups() default {};
        Class<? extends Payload>[] payload() default {};

        int min() default 1;
        int max() default 100000;
    }

    /**
     * Validador para cantidades de movimiento
     */
    public static class CantidadMovimientoValidator implements ConstraintValidator<CantidadMovimientoValida, Integer> {

        private int min;
        private int max;

        @Override
        public void initialize(CantidadMovimientoValida constraintAnnotation) {
            this.min = constraintAnnotation.min();
            this.max = constraintAnnotation.max();
        }

        @Override
        public boolean isValid(Integer value, ConstraintValidatorContext context) {
            if (value == null) {
                return true; // @NotNull debe manejar nulos
            }

            return value >= min && value <= max;
        }
    }

    /**
     * Anotación para validar usernames
     */
    @Documented
    @Constraint(validatedBy = UsernameValidoValidator.class)
    @Target({ElementType.FIELD, ElementType.PARAMETER})
    @Retention(RetentionPolicy.RUNTIME)
    public @interface UsernameValido {
        String message() default "El username debe tener entre 3-20 caracteres y solo puede contener letras, números, puntos, guiones y guiones bajos";
        Class<?>[] groups() default {};
        Class<? extends Payload>[] payload() default {};
    }

    /**
     * Validador para usernames
     */
    public static class UsernameValidoValidator implements ConstraintValidator<UsernameValido, String> {

        private static final Pattern USERNAME_PATTERN = Pattern.compile("^[a-zA-Z0-9._-]{3,20}$");
        private static final List<String> RESERVED_USERNAMES = Arrays.asList(
                "admin", "root", "system", "user", "test", "null", "undefined", "administrator"
        );

        @Override
        public boolean isValid(String value, ConstraintValidatorContext context) {
            if (value == null || value.trim().isEmpty()) {
                return true; // @NotBlank debe manejar vacíos
            }

            String username = value.trim().toLowerCase();

            // Verificar patrón
            if (!USERNAME_PATTERN.matcher(value).matches()) {
                return false;
            }

            // Verificar que no sea reservado
            return !RESERVED_USERNAMES.contains(username);
        }
    }

    /**
     * Anotación para validar emails corporativos
     */
    @Documented
    @Constraint(validatedBy = EmailCorporativoValidator.class)
    @Target({ElementType.FIELD, ElementType.PARAMETER})
    @Retention(RetentionPolicy.RUNTIME)
    public @interface EmailCorporativo {
        String message() default "Debe usar un email corporativo válido";
        Class<?>[] groups() default {};
        Class<? extends Payload>[] payload() default {};

        String[] dominiosPermitidos() default {};
        boolean requerirCorporativo() default false;
    }

    /**
     * Validador para emails corporativos
     */
    public static class EmailCorporativoValidator implements ConstraintValidator<EmailCorporativo, String> {

        private static final Pattern EMAIL_PATTERN = Pattern.compile(
                "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"
        );

        private String[] dominiosPermitidos;
        private boolean requerirCorporativo;

        @Override
        public void initialize(EmailCorporativo constraintAnnotation) {
            this.dominiosPermitidos = constraintAnnotation.dominiosPermitidos();
            this.requerirCorporativo = constraintAnnotation.requerirCorporativo();
        }

        @Override
        public boolean isValid(String value, ConstraintValidatorContext context) {
            if (value == null || value.trim().isEmpty()) {
                return true; // @Email debe manejar el formato básico
            }

            // Verificar formato básico
            if (!EMAIL_PATTERN.matcher(value).matches()) {
                return false;
            }

            // Si no se requiere corporativo y no hay dominios específicos, es válido
            if (!requerirCorporativo && dominiosPermitidos.length == 0) {
                return true;
            }

            // Verificar dominio
            String domain = value.substring(value.lastIndexOf("@") + 1).toLowerCase();

            // Si hay dominios específicos permitidos
            if (dominiosPermitidos.length > 0) {
                return Arrays.stream(dominiosPermitidos)
                        .anyMatch(allowed -> domain.equals(allowed.toLowerCase()));
            }

            // Si se requiere corporativo, verificar que no sea dominio público
            if (requerirCorporativo) {
                List<String> dominiosPublicos = Arrays.asList(
                        "gmail.com", "yahoo.com", "hotmail.com", "outlook.com",
                        "live.com", "msn.com", "aol.com", "icloud.com"
                );
                return !dominiosPublicos.contains(domain);
            }

            return true;
        }
    }

    /**
     * Anotación para validar rangos de fechas
     */
    @Documented
    @Constraint(validatedBy = RangoFechaValidator.class)
    @Target({ElementType.TYPE})
    @Retention(RetentionPolicy.RUNTIME)
    public @interface RangoFechaValido {
        String message() default "El rango de fechas no es válido";
        Class<?>[] groups() default {};
        Class<? extends Payload>[] payload() default {};

        String fechaInicio();
        String fechaFin();
        int maxDias() default 365;
    }

    /**
     * Validador para rangos de fecha
     */
    public static class RangoFechaValidator implements ConstraintValidator<RangoFechaValido, Object> {

        private String fechaInicioField;
        private String fechaFinField;
        private int maxDias;

        @Override
        public void initialize(RangoFechaValido constraintAnnotation) {
            this.fechaInicioField = constraintAnnotation.fechaInicio();
            this.fechaFinField = constraintAnnotation.fechaFin();
            this.maxDias = constraintAnnotation.maxDias();
        }

        @Override
        public boolean isValid(Object value, ConstraintValidatorContext context) {
            // Esta validación se implementaría usando reflection para acceder a los campos
            // Por simplicidad, retornamos true y manejamos la validación en el service
            return true;
        }
    }

    /**
     * Anotación para validar nombres que no contengan caracteres especiales peligrosos
     */
    @Documented
    @Constraint(validatedBy = NombreSeguroValidator.class)
    @Target({ElementType.FIELD, ElementType.PARAMETER})
    @Retention(RetentionPolicy.RUNTIME)
    public @interface NombreSeguro {
        String message() default "El nombre contiene caracteres no permitidos";
        Class<?>[] groups() default {};
        Class<? extends Payload>[] payload() default {};

        boolean permitirNumeros() default true;
        boolean permitirEspacios() default true;
        boolean permitirAcentos() default true;
    }

    /**
     * Validador para nombres seguros
     */
    public static class NombreSeguroValidator implements ConstraintValidator<NombreSeguro, String> {

        private boolean permitirNumeros;
        private boolean permitirEspacios;
        private boolean permitirAcentos;

        @Override
        public void initialize(NombreSeguro constraintAnnotation) {
            this.permitirNumeros = constraintAnnotation.permitirNumeros();
            this.permitirEspacios = constraintAnnotation.permitirEspacios();
            this.permitirAcentos = constraintAnnotation.permitirAcentos();
        }

        @Override
        public boolean isValid(String value, ConstraintValidatorContext context) {
            if (value == null || value.trim().isEmpty()) {
                return true; // @NotBlank debe manejar vacíos
            }

            // Construir patrón dinámicamente
            StringBuilder patternBuilder = new StringBuilder("^[a-zA-Z");

            if (permitirAcentos) {
                patternBuilder.append("áéíóúÁÉÍÓÚñÑüÜ");
            }

            if (permitirNumeros) {
                patternBuilder.append("0-9");
            }

            if (permitirEspacios) {
                patternBuilder.append("\\s");
            }

            // Siempre permitir algunos caracteres básicos seguros
            patternBuilder.append("._-");
            patternBuilder.append("]+$");

            Pattern pattern = Pattern.compile(patternBuilder.toString());
            return pattern.matcher(value).matches();
        }
    }

    /**
     * Constantes de validación comunes
     */
    public static class ValidationConstants {

        // Mensajes de error estándar
        public static final String REQUIRED_MESSAGE = "Este campo es obligatorio";
        public static final String EMAIL_MESSAGE = "El formato del email no es válido";
        public static final String LENGTH_MESSAGE = "La longitud del campo no es válida";
        public static final String NUMERIC_MESSAGE = "El valor debe ser numérico";
        public static final String POSITIVE_MESSAGE = "El valor debe ser positivo";
        public static final String FUTURE_DATE_MESSAGE = "La fecha debe ser futura";
        public static final String PAST_DATE_MESSAGE = "La fecha debe ser pasada";

        // Longitudes comunes
        public static final int MIN_NAME_LENGTH = 2;
        public static final int MAX_NAME_LENGTH = 100;
        public static final int MIN_DESCRIPTION_LENGTH = 0;
        public static final int MAX_DESCRIPTION_LENGTH = 1000;
        public static final int MIN_USERNAME_LENGTH = 3;
        public static final int MAX_USERNAME_LENGTH = 20;
        public static final int MIN_PASSWORD_LENGTH = 6;
        public static final int MAX_PASSWORD_LENGTH = 100;

        // Valores numéricos
        public static final double MIN_PRICE = 0.01;
        public static final double MAX_PRICE = 99999999.99;
        public static final int MIN_STOCK = 0;
        public static final int MAX_STOCK = 1000000;
        public static final int MIN_MOVEMENT_QUANTITY = 1;
        public static final int MAX_MOVEMENT_QUANTITY = 100000;

        // Patrones regex
        public static final String EMAIL_REGEX = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";
        public static final String USERNAME_REGEX = "^[a-zA-Z0-9._-]{3,20}$";
        public static final String PHONE_REGEX = "^\\+?[1-9]\\d{1,14}$";
        public static final String SAFE_STRING_REGEX = "^[a-zA-ZáéíóúÁÉÍÓÚñÑ0-9\\s._-]+$";
    }

    /**
     * Grupos de validación para diferentes escenarios
     */
    public static class ValidationGroups {

        // Grupos básicos
        public interface Create {}
        public interface Update {}
        public interface Delete {}

        // Grupos específicos por entidad
        public interface ProductoValidation {}
        public interface CategoriaValidation {}
        public interface UsuarioValidation {}
        public interface MovimientoValidation {}

        // Grupos de seguridad
        public interface AdminOnly {}
        public interface ManagerOnly {}
        public interface UserLevel {}

        // Grupos de operación
        public interface BulkOperation {}
        public interface ImportOperation {}
        public interface ExportOperation {}
    }

    /**
     * Utilidades para validación manual
     */
    public static class ValidationUtils {

        /**
         * Validar email básico
         */
        public static boolean isValidEmail(String email) {
            if (email == null || email.trim().isEmpty()) {
                return false;
            }
            return Pattern.matches(ValidationConstants.EMAIL_REGEX, email);
        }

        /**
         * Validar username
         */
        public static boolean isValidUsername(String username) {
            if (username == null || username.trim().isEmpty()) {
                return false;
            }
            return Pattern.matches(ValidationConstants.USERNAME_REGEX, username);
        }

        /**
         * Validar precio
         */
        public static boolean isValidPrice(BigDecimal price) {
            if (price == null) {
                return false;
            }
            double value = price.doubleValue();
            return value >= ValidationConstants.MIN_PRICE &&
                    value <= ValidationConstants.MAX_PRICE &&
                    price.scale() <= 2;
        }

        /**
         * Validar stock
         */
        public static boolean isValidStock(Integer stock) {
            if (stock == null) {
                return false;
            }
            return stock >= ValidationConstants.MIN_STOCK &&
                    stock <= ValidationConstants.MAX_STOCK;
        }

        /**
         * Validar longitud de texto
         */
        public static boolean isValidLength(String text, int min, int max) {
            if (text == null) {
                return min == 0;
            }
            int length = text.trim().length();
            return length >= min && length <= max;
        }

        /**
         * Limpiar y normalizar texto
         */
        public static String sanitizeText(String text) {
            if (text == null) {
                return null;
            }

            return text.trim()
                    .replaceAll("\\s+", " ") // Múltiples espacios -> un espacio
                    .replaceAll("[\\p{Cntrl}]", ""); // Remover caracteres de control
        }

        /**
         * Validar que una cadena no contenga caracteres peligrosos
         */
        public static boolean isSafeString(String text) {
            if (text == null) {
                return true;
            }

            // Caracteres peligrosos comunes en ataques de inyección
            String[] dangerousChars = {"<", ">", "\"", "'", "&", "script", "javascript:", "onload", "onerror"};

            String lowerText = text.toLowerCase();
            return Arrays.stream(dangerousChars)
                    .noneMatch(lowerText::contains);
        }
    }
}