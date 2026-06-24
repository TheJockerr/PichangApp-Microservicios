package cl.duoc.pichangapp.users_service.dto;

/**
 * Body para actualizar la visibilidad del historial de karma.
 * { "visible": true/false }
 */
public record HistorialVisibleRequest(Boolean visible) {}
