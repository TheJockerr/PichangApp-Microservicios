package cl.duoc.pichangapp.api_gateway.filter;

import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class RateLimitingFilter implements GlobalFilter, Ordered {

    // Mapa concurrente para almacenar los "buckets" de tokens por dirección IP
    private final Map<String, TokenBucket> buckets = new ConcurrentHashMap<>();
    
    // Configuración: 100 peticiones por minuto
    private final int capacity = 100;
    private final int refillRate = 100; // tokens por minuto
    
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String ip = exchange.getRequest().getRemoteAddress() != null 
                ? exchange.getRequest().getRemoteAddress().getAddress().getHostAddress() 
                : "unknown";

        TokenBucket bucket = buckets.computeIfAbsent(ip, k -> new TokenBucket(capacity, refillRate));

        if (bucket.tryConsume()) {
            return chain.filter(exchange);
        } else {
            ServerHttpResponse response = exchange.getResponse();
            response.setStatusCode(HttpStatus.TOO_MANY_REQUESTS);
            return response.setComplete();
        }
    }

    @Override
    public int getOrder() {
        return -2; // Ejecutar antes que el JwtAuthenticationFilter para evitar que peticiones excesivas saturen el JWT validator
    }

    // Implementación simple de Token Bucket en memoria
    private static class TokenBucket {
        private final int capacity;
        private final int refillRate; // por minuto
        private int tokens;
        private Instant lastRefill;

        public TokenBucket(int capacity, int refillRate) {
            this.capacity = capacity;
            this.refillRate = refillRate;
            this.tokens = capacity;
            this.lastRefill = Instant.now();
        }

        public synchronized boolean tryConsume() {
            refill();
            if (tokens > 0) {
                tokens--;
                return true;
            }
            return false;
        }

        private void refill() {
            Instant now = Instant.now();
            long minutesPassed = java.time.Duration.between(lastRefill, now).toMinutes();
            if (minutesPassed > 0) {
                tokens = Math.min(capacity, tokens + (int)(minutesPassed * refillRate));
                lastRefill = now;
            }
        }
    }
}
