package cl.duoc.pichangapp.api_gateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;

@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

    @Bean
    public SecurityWebFilterChain springSecurityFilterChain(ServerHttpSecurity http) {
        http
            .csrf(csrf -> csrf.disable()) // Deshabilitar CSRF para APIs REST
            .cors(org.springframework.security.config.Customizer.withDefaults()) // Usa la configuración de CorsWebFilter provista en CorsConfig
            .authorizeExchange(exchanges -> exchanges
                // Permitir todo el trafico a nivel de Spring Security.
                // La autorización y validación del JWT la haremos manualmente
                // en nuestro propio JwtAuthenticationFilter (GlobalFilter)
                .anyExchange().permitAll()
            );
        return http.build();
    }
}
