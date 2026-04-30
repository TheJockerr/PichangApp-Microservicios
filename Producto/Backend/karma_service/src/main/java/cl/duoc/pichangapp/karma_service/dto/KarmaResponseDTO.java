package cl.duoc.pichangapp.karma_service.dto;

public record KarmaResponseDTO(
        String userId,
        Integer karmaScore,
        String category
) {}
