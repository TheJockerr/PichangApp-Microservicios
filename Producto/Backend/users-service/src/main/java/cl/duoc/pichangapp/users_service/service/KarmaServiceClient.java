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
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Date;
import java.util.Map;

/**
 * Cliente para karma_service. Usa un JWT interno (subject = users-service) firmado
 * con el secreto compartido, igual que el patrón de events-service.
 */
@Service
@Slf4j
@SuppressWarnings({"null", "unchecked"})
public class KarmaServiceClient {

    @Value("${karma.service.url:http://localhost:8082}")
    private String karmaServiceUrl;

    @Value("${app.jwt.secret}")
    private String jwtSecret;

    private final RestTemplate restTemplate;

    public KarmaServiceClient(RestTemplateBuilder restTemplateBuilder) {
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

    /** Puntaje y categoría de karma para construir el perfil público. */
    public record KarmaInfo(Integer karmaScore, String categoria) {}

    public KarmaInfo getKarmaInfo(Integer userId) {
        try {
            String url = karmaServiceUrl + "/api/v1/karma/" + userId;
            ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                    url, HttpMethod.GET, new HttpEntity<>(headers()),
                    (Class<Map<String, Object>>) (Class<?>) Map.class);
            Map<String, Object> body = response.getBody();
            if (body != null) {
                Integer score = body.get("karmaScore") instanceof Number n ? n.intValue() : null;
                String categoria = body.get("category") != null ? body.get("category").toString() : null;
                return new KarmaInfo(score, categoria);
            }
        } catch (Exception e) {
            log.warn("No se pudo obtener karma del usuario {}: {}", userId, e.getMessage());
        }
        return new KarmaInfo(null, null);
    }

    /** Elimina el karma del usuario (borrado de cuenta). No lanza: log-and-continue. */
    public void deleteKarma(Integer userId) {
        try {
            String url = karmaServiceUrl + "/api/v1/karma/" + userId;
            restTemplate.exchange(url, HttpMethod.DELETE, new HttpEntity<>(headers()), Void.class);
            log.info("Karma eliminado para usuario {}", userId);
        } catch (Exception e) {
            log.error("Error al eliminar karma del usuario {}: {}", userId, e.getMessage());
        }
    }
}
