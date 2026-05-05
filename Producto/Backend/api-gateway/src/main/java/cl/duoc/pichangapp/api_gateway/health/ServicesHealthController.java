package cl.duoc.pichangapp.api_gateway.health;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@RestController
public class ServicesHealthController {

    private final WebClient webClient;
    
    @Value("${USERS_SERVICE_URL:http://localhost:8083}")
    private String usersServiceUrl;
    
    @Value("${KARMA_SERVICE_URL:http://localhost:8081}")
    private String karmaServiceUrl;
    
    @Value("${NOTIFICATION_SERVICE_URL:http://localhost:8082}")
    private String notificationServiceUrl;

    public ServicesHealthController(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.build();
    }

    @GetMapping("/health")
    public Mono<ResponseEntity<Map<String, Object>>> checkHealth() {
        Map<String, Object> response = new ConcurrentHashMap<>();
        response.put("gateway", "UP");

        Mono<String> usersHealth = checkServiceHealth(usersServiceUrl, "users-service");
        Mono<String> karmaHealth = checkServiceHealth(karmaServiceUrl, "karma-service");
        Mono<String> notificationHealth = checkServiceHealth(notificationServiceUrl, "notification-service");

        // Mono.zip espera a que todas las peticiones terminen concurrentemente
        return Mono.zip(usersHealth, karmaHealth, notificationHealth)
                .map(tuple -> {
                    response.put("users-service", tuple.getT1());
                    response.put("karma-service", tuple.getT2());
                    response.put("notification-service", tuple.getT3());
                    
                    boolean allUp = tuple.getT1().equals("UP") && 
                                    tuple.getT2().equals("UP") && 
                                    tuple.getT3().equals("UP");
                                    
                    return ResponseEntity.status(allUp ? 200 : 503).body(response);
                });
    }

    private Mono<String> checkServiceHealth(String url, String serviceName) {
        return webClient.get()
                .uri(url + "/actuator/health")
                .retrieve()
                .bodyToMono(new org.springframework.core.ParameterizedTypeReference<Map<String, Object>>() {})
                .map(body -> body != null && body.containsKey("status") ? body.get("status").toString() : "DOWN")
                .onErrorResume(e -> Mono.just("DOWN (" + e.getMessage() + ")"));
    }
}
