package cl.duoc.pichangapp.karma_service.dto;

public record CheckInEventDTO(
        String userId,
        String eventId,
        String location // Opcional, para logs o geocerca
) {}
