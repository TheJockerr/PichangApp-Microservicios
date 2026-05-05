package cl.duoc.pichangapp.api_gateway.filter;

import cl.duoc.pichangapp.api_gateway.security.JwtProvider;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;

@Component
public class JwtAuthenticationFilter implements GlobalFilter, Ordered {

    private final JwtProvider jwtProvider;
    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    // Rutas que no requieren token
    private final List<String> publicEndpoints = List.of(
            "/api/v1/auth/login",
            "/api/v1/auth/register",
            "/actuator/health",
            "/health",
            "/ws/**",
            "/swagger-ui/**",
            "/v3/api-docs/**"
    );

    public JwtAuthenticationFilter(JwtProvider jwtProvider) {
        this.jwtProvider = jwtProvider;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getURI().getPath();

        // 1. Validar si la ruta es pública
        boolean isPublic = publicEndpoints.stream().anyMatch(pattern -> pathMatcher.match(pattern, path));
        if (isPublic) {
            return chain.filter(exchange);
        }

        // 2. Extraer el token del header Authorization
        String authHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return onError(exchange, "No se encontró el token de autorización");
        }

        String token = authHeader.substring(7);

        // 3. Validar el token
        if (!jwtProvider.validateToken(token)) {
            return onError(exchange, "Token inválido o expirado");
        }

        // 4. Extraer userId y propagar en un nuevo header
        String userId = jwtProvider.getSubject(token);
        
        ServerHttpRequest modifiedRequest = exchange.getRequest().mutate()
                .header("X-User-Id", userId)
                .build();

        return chain.filter(exchange.mutate().request(modifiedRequest).build());
    }

    private Mono<Void> onError(ServerWebExchange exchange, String err) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(HttpStatus.UNAUTHORIZED);
        // El body de la respuesta se manejará mejor en el GlobalErrorHandler
        // Aquí solo cortamos el flujo retornando un Mono completado vacío tras setear el status.
        return response.setComplete();
    }

    @Override
    public int getOrder() {
        return -1; // Alta prioridad, se ejecuta antes del enrutamiento
    }
}
