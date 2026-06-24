package cl.duoc.pichangapp.karma_service.service;

import cl.duoc.pichangapp.karma_service.dto.CheckInEventDTO;
import cl.duoc.pichangapp.karma_service.dto.KarmaHistoryDTO;
import cl.duoc.pichangapp.karma_service.dto.KarmaResponseDTO;
import cl.duoc.pichangapp.karma_service.dto.KarmaUpdateRequestDTO;
import cl.duoc.pichangapp.karma_service.exception.UserNotFoundException;
import cl.duoc.pichangapp.karma_service.model.KarmaHistory;
import cl.duoc.pichangapp.karma_service.model.KarmaScore;
import cl.duoc.pichangapp.karma_service.repository.KarmaHistoryRepository;
import cl.duoc.pichangapp.karma_service.repository.KarmaScoreRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;

import java.util.ArrayList;
import java.util.List;

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
@SuppressWarnings("null")
public class KarmaService {

    private final KarmaScoreRepository karmaScoreRepository;
    private final KarmaHistoryRepository karmaHistoryRepository;
    private final RestTemplate restTemplate;

    @Value("${users.service.url}")
    private String usersServiceUrl;

    private static final int INITIAL_KARMA = 50;
    private static final int MIN_KARMA = 0;

    @Transactional
    public KarmaResponseDTO getKarmaByUserId(String userId) {
        KarmaScore score = karmaScoreRepository.findByUserId(userId)
                .orElseGet(() -> initializeKarma(userId));
        return buildResponseDTO(score);
    }

    /**
     * Elimina el KarmaScore (y su historial por cascade/orphanRemoval) de un usuario.
     * Idempotente: si el usuario no tiene karma, no hace nada.
     * Lo usa users-service al eliminar una cuenta.
     */
    @Transactional
    public void deleteKarmaByUserId(String userId) {
        karmaScoreRepository.findByUserId(userId)
                .ifPresent(karmaScoreRepository::delete);
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

    /**
     * Ajuste manual de karma por un administrador.
     * Establece el puntaje de forma absoluta, registra el movimiento en el historial
     * con motivo ADMIN_ADJUSTMENT y recalcula la categoría automáticamente.
     */
    @Transactional
    public KarmaResponseDTO adminAdjustKarma(String userId, cl.duoc.pichangapp.karma_service.dto.AdminKarmaAdjustmentDTO dto) {
        if (dto.newKarmaScore() == null || dto.newKarmaScore() < MIN_KARMA) {
            throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.BAD_REQUEST,
                    "El nuevo puntaje de karma debe ser un valor mayor o igual a " + MIN_KARMA);
        }

        KarmaScore score = getOrCreateKarmaScore(userId);
        int previousScore = score.getKarmaScore();
        int newScore = dto.newKarmaScore();
        int delta = newScore - previousScore;

        score.setKarmaScore(newScore);
        karmaScoreRepository.save(score);

        String detail = (dto.reason() != null && !dto.reason().isBlank()) ? dto.reason() : "Ajuste manual por administrador";
        KarmaHistory history = KarmaHistory.builder()
                .karmaScore(score)
                .amount(delta)
                .reason("ADMIN_ADJUSTMENT: " + detail)
                .build();
        karmaHistoryRepository.save(history);

        return buildResponseDTO(score);
    }

    private void validateUserExistsInUsersService(String userId) {
        try {
            // Validar que el userId sea un número entero válido
            if (!userId.matches("\\d+")) {
                throw new UserNotFoundException("El ID de usuario (" + userId + ") no es válido para el sistema.");
            }

            ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attrs != null) {
                String authHeader = attrs.getRequest().getHeader(HttpHeaders.AUTHORIZATION);
                HttpHeaders headers = new HttpHeaders();
                if (authHeader != null) {
                    headers.set(HttpHeaders.AUTHORIZATION, authHeader);
                }
                HttpEntity<?> entity = new HttpEntity<>(headers);

                String url = usersServiceUrl + "/api/v1/users/" + userId + "/exists";
                ResponseEntity<Boolean> response = restTemplate.exchange(
                        url,
                        HttpMethod.GET,
                        entity,
                        Boolean.class);

                if (response.getBody() == null || !Boolean.TRUE.equals(response.getBody())) {
                    throw new UserNotFoundException("Usuario con ID " + userId + " no existe en users-service.");
                }
            }
        } catch (NumberFormatException ex) {
            throw new UserNotFoundException("El ID de usuario (" + userId + ") no es válido para el sistema.");
        } catch (HttpClientErrorException.NotFound e) {
            throw new UserNotFoundException("Usuario con ID " + userId + " no existe en users-service.");
        } catch (HttpClientErrorException.Unauthorized | HttpClientErrorException.Forbidden e) {
            throw new RuntimeException(
                    "No autorizado para consultar users-service. Asegúrate de enviar un Token JWT válido.");
        } catch (UserNotFoundException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Error al comunicar con users-service para validar usuario: " + e.getMessage(),
                    e);
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
        List<KarmaHistoryDTO> historyDto = new ArrayList<>();
        if (score.getHistory() != null) {
            historyDto = score.getHistory().stream()
                    .map(h -> new KarmaHistoryDTO(h.getAmount(), h.getReason(), h.getCreatedAt()))
                    .sorted((h1, h2) -> h2.createdAt().compareTo(h1.createdAt()))
                    .toList();
        }
        return new KarmaResponseDTO(score.getUserId(), score.getKarmaScore(), category, historyDto);
    }

    private String determineCategory(int score) {
        if (score >= 80)
            return "Excelente";
        if (score >= 60)
            return "Bueno";
        if (score >= 40)
            return "Regular";
        return "Bajo";
    }
}
