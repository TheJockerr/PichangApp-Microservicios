package cl.duoc.pichangapp.api_gateway.exception;

import org.springframework.boot.web.reactive.error.ErrorWebExceptionHandler;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
@Order(-2) // Alta prioridad para atrapar errores antes del default
public class GlobalErrorHandler implements ErrorWebExceptionHandler {

    @Override
    public Mono<Void> handle(ServerWebExchange exchange, Throwable ex) {
        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
        
        if (ex instanceof ResponseStatusException responseStatusException) {
            status = HttpStatus.resolve(responseStatusException.getStatusCode().value());
        }

        // Si ya hay un status asignado a la respuesta (ej. por RateLimiting o JwtAuthentication), usar ese
        if (exchange.getResponse().getStatusCode() != null) {
            status = HttpStatus.resolve(exchange.getResponse().getStatusCode().value());
        }

        exchange.getResponse().setStatusCode(status);
        exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);

        String jsonResponse = String.format(
                "{\"error\": \"%s\", \"status\": %d, \"message\": \"%s\"}",
                status != null ? status.getReasonPhrase() : "Unknown",
                status != null ? status.value() : 500,
                ex.getMessage() != null ? ex.getMessage().replace("\"", "'") : "Error procesando la solicitud en el Gateway"
        );

        byte[] bytes = jsonResponse.getBytes();
        return exchange.getResponse().writeWith(Mono.just(exchange.getResponse().bufferFactory().wrap(bytes)));
    }
}
