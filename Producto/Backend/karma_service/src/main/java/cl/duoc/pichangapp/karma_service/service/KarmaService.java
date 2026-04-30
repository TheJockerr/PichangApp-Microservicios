package cl.duoc.pichangapp.karma_service.service;

import cl.duoc.pichangapp.karma_service.dto.CheckInEventDTO;
import cl.duoc.pichangapp.karma_service.dto.KarmaResponseDTO;
import cl.duoc.pichangapp.karma_service.dto.KarmaUpdateRequestDTO;
import cl.duoc.pichangapp.karma_service.exception.UserNotFoundException;
import cl.duoc.pichangapp.karma_service.model.KarmaHistory;
import cl.duoc.pichangapp.karma_service.model.KarmaScore;
import cl.duoc.pichangapp.karma_service.repository.KarmaHistoryRepository;
import cl.duoc.pichangapp.karma_service.repository.KarmaScoreRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Service
@RequiredArgsConstructor
public class KarmaService {

    private final KarmaScoreRepository karmaScoreRepository;
    private final KarmaHistoryRepository karmaHistoryRepository;
    private final RestTemplate restTemplate;

    private static final int INITIAL_KARMA = 100;
    private static final int MIN_KARMA = 0;
    private static final String USERS_SERVICE_URL = "http://localhost:8080/api/v1/users/";

    @Transactional
    public KarmaResponseDTO getKarmaByUserId(String userId) {
        KarmaScore score = karmaScoreRepository.findByUserId(userId)
                .orElseGet(() -> initializeKarma(userId));
        return buildResponseDTO(score);
    }

    @Transactional
    public KarmaResponseDTO processCheckIn(CheckInEventDTO dto) {
        KarmaScore score = getOrCreateKarmaScore(dto.userId());
        updateKarma(score, 10, "Check-in exitoso en evento: " + dto.eventId());
        return buildResponseDTO(score);
    }

    @Transactional
    public KarmaResponseDTO processAbsence(String userId, String eventId) {
        KarmaScore score = getOrCreateKarmaScore(userId);
        updateKarma(score, -15, "Inasistencia sin aviso al evento: " + eventId);
        return buildResponseDTO(score);
    }

    @Transactional
    public KarmaResponseDTO processOrganizerValidation(KarmaUpdateRequestDTO dto) {
        KarmaScore score = getOrCreateKarmaScore(dto.userId());
        int amount = dto.isPositiveValidation() ? 5 : -5;
        String actionStr = dto.isPositiveValidation() ? "positiva" : "negativa";
        String reason = String.format("Validación %s del organizador (%s) para el evento: %s", 
                actionStr, dto.organizerId(), dto.eventId());
        
        updateKarma(score, amount, reason);
        return buildResponseDTO(score);
    }

    private void validateUserExistsInUsersService(String userId) {
        try {
            int id = Integer.parseInt(userId);
            
            ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attrs != null) {
                String authHeader = attrs.getRequest().getHeader(HttpHeaders.AUTHORIZATION);
                HttpHeaders headers = new HttpHeaders();
                if (authHeader != null) {
                    headers.set(HttpHeaders.AUTHORIZATION, authHeader);
                }
                HttpEntity<?> entity = new HttpEntity<>(headers);
                
                ResponseEntity<Boolean> response = restTemplate.exchange(
                        USERS_SERVICE_URL + id + "/exists",
                        HttpMethod.GET,
                        entity,
                        Boolean.class
                );
                
                if (response.getBody() == null || !Boolean.TRUE.equals(response.getBody())) {
                    throw new UserNotFoundException("Usuario con ID " + userId + " no existe en users-service.");
                }
            }
        } catch (NumberFormatException ex) {
            throw new UserNotFoundException("El ID de usuario (" + userId + ") no es válido para el sistema.");
        } catch (HttpClientErrorException.NotFound e) {
            throw new UserNotFoundException("Usuario con ID " + userId + " no existe en users-service.");
        } catch (HttpClientErrorException.Unauthorized | HttpClientErrorException.Forbidden e) {
            // Ignorar en testing o lanzar si estrictamente necesario, pero para evitar trabas:
            throw new RuntimeException("No autorizado para consultar users-service. Asegúrate de enviar un Token JWT válido.");
        } catch (Exception e) {
             // Si el microservicio está caído, podríamos lanzar excepción o permitir la creación.
             // Para microservicios es mejor fallar rápido (Fail Fast)
             if (e instanceof UserNotFoundException || e.getMessage().contains("No autorizado")) {
                 throw e;
             }
             throw new RuntimeException("Error al comunicar con users-service para validar usuario", e);
        }
    }

    private KarmaScore initializeKarma(String userId) {
        validateUserExistsInUsersService(userId);
        KarmaScore score = KarmaScore.builder()
                .userId(userId)
                .karmaScore(INITIAL_KARMA)
                .build();
        return karmaScoreRepository.save(score);
    }

    private KarmaScore getOrCreateKarmaScore(String userId) {
        return karmaScoreRepository.findByUserId(userId)
                .orElseGet(() -> initializeKarma(userId));
    }

    private void updateKarma(KarmaScore score, int amount, String reason) {
        int newScore = score.getKarmaScore() + amount;
        if (newScore < MIN_KARMA) {
            newScore = MIN_KARMA;
        }
        
        score.setKarmaScore(newScore);
        karmaScoreRepository.save(score);

        KarmaHistory history = KarmaHistory.builder()
                .karmaScore(score)
                .amount(amount)
                .reason(reason)
                .build();
        karmaHistoryRepository.save(history);
    }

    private KarmaResponseDTO buildResponseDTO(KarmaScore score) {
        String category = determineCategory(score.getKarmaScore());
        return new KarmaResponseDTO(score.getUserId(), score.getKarmaScore(), category);
    }

    private String determineCategory(int score) {
        if (score >= 80) return "Excelente";
        if (score >= 60) return "Bueno";
        if (score >= 40) return "Regular";
        return "Bajo";
    }
}
