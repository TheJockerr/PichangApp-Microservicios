package cl.duoc.pichangapp.notification_service.dto;

/**
 * DTO para solicitar el envío de una notificación a un usuario.
 */
public record NotificationRequestDTO(
        String userId,  // ID del usuario destinatario
        String title,   // Título de la notificación
        String body,    // Cuerpo del mensaje
        String type     // Tipo de notificación (debe coincidir con NotificationType)
) {}
