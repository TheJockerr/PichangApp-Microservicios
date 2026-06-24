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
 * Cliente para notification-service. Usa un JWT interno (subject = users-service),
 * mismo patrón que KarmaServiceClient.
 */
@Service
@Slf4j
@SuppressWarnings("null")
public class NotificationServiceClient {

    @Value("${notification.service.url:http://localhost:8081}")
    private String notificationServiceUrl;

    @Value("${app.jwt.secret}")
    private String jwtSecret;

    private final RestTemplate restTemplate;

    public NotificationServiceClient(RestTemplateBuilder restTemplateBuilder) {
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
     * Elimina el historial de notificaciones del usuario.
     * No lanza: log-and-continue.
     */
    public void deleteUserNotifications(Integer userId) {
        try {
            String url = notificationServiceUrl + "/api/v1/notifications/" + userId;
            restTemplate.exchange(url, HttpMethod.DELETE, new HttpEntity<>(headers()), Void.class);
            log.info("Notificaciones del usuario {} eliminadas", userId);
        } catch (Exception e) {
            log.error("Error al eliminar notificaciones del usuario {}: {}", userId, e.getMessage());
        }
    }
}
