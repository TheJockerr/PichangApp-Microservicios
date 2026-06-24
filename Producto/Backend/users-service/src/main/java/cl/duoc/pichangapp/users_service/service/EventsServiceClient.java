package cl.duoc.pichangapp.users_service.service;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Date;

/**
 * Cliente para events-service. Usa un JWT interno (subject = users-service),
 * mismo patrón que KarmaServiceClient.
 */
@Service
@Slf4j
@SuppressWarnings("null")
public class EventsServiceClient {

    @Value("${events.service.url:http://localhost:8084}")
    private String eventsServiceUrl;

    @Value("${app.jwt.secret}")
    private String jwtSecret;

    private final RestTemplate restTemplate;

    public EventsServiceClient(RestTemplateBuilder restTemplateBuilder) {
        this.restTemplate = restTemplateBuilder.build();
    }

    private HttpHeaders headers() {
        String token = Jwts.builder()
                .setSubject("users-service")
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 3600000))
                .signWith(Keys.hmacShaKeyFor(jwtSecret.getBytes()), SignatureAlgorithm.HS256)
                .compact();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(token);
        return headers;
    }

    /**
     * Cancela todos los eventos ACTIVO del usuario y elimina sus inscripciones.
     * No lanza: log-and-continue (el borrado de cuenta no debe bloquearse).
     */
    public void deleteUserEvents(Integer userId) {
        try {
            String url = eventsServiceUrl + "/api/v1/events/usuario/" + userId;
            restTemplate.exchange(url, HttpMethod.DELETE, new HttpEntity<>(headers()), Void.class);
            log.info("Eventos del usuario {} procesados (cancelados + inscripciones eliminadas)", userId);
        } catch (Exception e) {
            log.error("Error al eliminar eventos del usuario {}: {}", userId, e.getMessage());
        }
    }
}
