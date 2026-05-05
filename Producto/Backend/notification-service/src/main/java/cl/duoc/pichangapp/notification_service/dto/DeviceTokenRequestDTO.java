package cl.duoc.pichangapp.notification_service.dto;

/**
 * DTO para registrar o actualizar un token de dispositivo FCM.
 */
public record DeviceTokenRequestDTO(
        String userId,  // ID del usuario dueño del dispositivo
        String token    // Token FCM del dispositivo
) {}
