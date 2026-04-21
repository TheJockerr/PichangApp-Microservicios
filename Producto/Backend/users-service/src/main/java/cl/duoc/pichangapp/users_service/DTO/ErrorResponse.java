package cl.duoc.pichangapp.users_service.DTO;

import java.time.Instant;

/**
 * DTO para respuestas de error uniformes.
 * Útil para que el frontend reciba siempre la misma estructura en caso de fallo.
 */
public record ErrorResponse(
        String message,    // Mensaje legible del error
        String code,       // Código corto del error (ej. "USER_NOT_FOUND")
        Instant timestamp  // Marca de tiempo del error
) {}
