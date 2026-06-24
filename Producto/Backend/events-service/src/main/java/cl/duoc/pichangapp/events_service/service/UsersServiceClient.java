package cl.duoc.pichangapp.events_service.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * Cliente liviano para resolver datos públicos de usuarios (p. ej. el correo del
 * organizador) desde el users-service. Reenvía el JWT de la petición entrante para
 * que el users-service autorice la consulta.
 */
@Service
@SuppressWarnings("unchecked")
public class UsersServiceClient {

    private static final Logger log = LoggerFactory.getLogger(UsersServiceClient.class);

    private final RestTemplate restTemplate;
    private final String usersServiceUrl;

    public UsersServiceClient(RestTemplateBuilder restTemplateBuilder,
                              @Value("${users.service.url}") String usersServiceUrl) {
        this.restTemplate = restTemplateBuilder.build();
        this.usersServiceUrl = usersServiceUrl;
    }

    /**
     * Devuelve el correo del usuario con el id dado, o {@code null} si no se puede resolver.
     * No lanza excepción: el listado administrativo no debe romperse por un usuario huérfano.
     */
    public String getEmailByUserId(Integer userId) {
        if (userId == null) {
            return null;
        }
        try {
            HttpHeaders headers = new HttpHeaders();
            ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attrs != null) {
                String authHeader = attrs.getRequest().getHeader(HttpHeaders.AUTHORIZATION);
                if (authHeader != null) {
                    headers.set(HttpHeaders.AUTHORIZATION, authHeader);
                }
            }
            HttpEntity<?> entity = new HttpEntity<>(headers);
            String url = usersServiceUrl + "/api/v1/users/" + userId;
            ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                    url, HttpMethod.GET, entity,
                    (Class<Map<String, Object>>) (Class<?>) Map.class);
            Map<String, Object> body = response.getBody();
            if (body != null && body.get("correo") != null) {
                return body.get("correo").toString();
            }
        } catch (Exception e) {
            log.warn("No se pudo resolver el correo del usuario {}: {}", userId, e.getMessage());
        }
        return null;
    }

    /**
     * Devuelve "nombre apellido" del usuario, o "Organizador" como fallback si no se
     * puede resolver (el listado de eventos no debe romperse por esto).
     */
    public String getNombreCreador(Integer userId) {
        if (userId == null) {
            return "Organizador";
        }
        try {
            HttpHeaders headers = new HttpHeaders();
            ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attrs != null) {
                String authHeader = attrs.getRequest().getHeader(HttpHeaders.AUTHORIZATION);
                if (authHeader != null) {
                    headers.set(HttpHeaders.AUTHORIZATION, authHeader);
                }
            }
            HttpEntity<?> entity = new HttpEntity<>(headers);
            String url = usersServiceUrl + "/api/v1/users/" + userId;
            ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                    url, HttpMethod.GET, entity,
                    (Class<Map<String, Object>>) (Class<?>) Map.class);
            Map<String, Object> body = response.getBody();
            if (body != null) {
                String nombre = body.get("nombre") != null ? body.get("nombre").toString() : "";
                String apellido = body.get("apellido") != null ? body.get("apellido").toString() : "";
                String completo = (nombre + " " + apellido).trim();
                if (!completo.isBlank()) {
                    return completo;
                }
            }
        } catch (Exception e) {
            log.warn("No se pudo resolver el nombre del usuario {}: {}", userId, e.getMessage());
        }
        return "Organizador";
    }
}
