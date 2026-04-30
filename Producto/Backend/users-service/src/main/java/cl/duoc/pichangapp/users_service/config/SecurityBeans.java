package cl.duoc.pichangapp.users_service.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Beans de seguridad reutilizables.
 * Aquí definimos el PasswordEncoder que usará la aplicación para hashear contraseñas.
 */
@Configuration
public class SecurityBeans {

    /**
     * Bean de PasswordEncoder basado en BCrypt.
     * Se inyecta en los servicios que necesiten encriptar o verificar contraseñas.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}

