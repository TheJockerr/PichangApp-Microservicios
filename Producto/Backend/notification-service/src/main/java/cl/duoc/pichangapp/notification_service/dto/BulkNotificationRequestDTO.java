package cl.duoc.pichangapp.notification_service.dto;

import java.util.List;

/**
 * DTO para solicitar el envío masivo de notificaciones a múltiples usuarios.
 */
public record BulkNotificationRequestDTO(
        List<String> userIds,  // Lista de IDs de usuarios destinatarios
        String title,          // Título de la notificación
        String body,           // Cuerpo del mensaje
        String type            // Tipo de notificación (debe coincidir con NotificationType)
) {}
