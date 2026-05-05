package cl.duoc.pichangapp.notification_service.dto;

import java.time.Instant;

/**
 * DTO para respuestas de error uniformes.
 * Copiado del karma_service para consistencia.
 */
public record ErrorResponse(
        String message,    // Mensaje legible del error
        String code,       // Código corto del error
        Instant timestamp  // Marca de tiempo del error
) {}
