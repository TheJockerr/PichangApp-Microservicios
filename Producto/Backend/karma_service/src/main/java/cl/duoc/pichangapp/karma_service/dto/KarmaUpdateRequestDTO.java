package cl.duoc.pichangapp.karma_service.dto;

public record KarmaUpdateRequestDTO(
        String userId,
        String eventId,
        String organizerId,
        boolean isPositiveValidation // true = +5, false = -5
) {}
