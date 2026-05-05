package cl.duoc.pichangapp.notification_service.dto;

import java.time.Instant;

/**
 * DTO para la respuesta de notificación (historial).
 */
public record NotificationResponseDTO(
        Integer id,
        String userId,
        String title,
        String body,
        String type,
        String status,
        Instant createdAt
) {}
