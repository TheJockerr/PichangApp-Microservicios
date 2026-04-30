package cl.duoc.pichangapp.users_service.dto;

/**
 * DTO para actualizar el perfil del usuario.
 * Solo contiene los campos que el usuario puede modificar desde su perfil.
 */
public record UpdateProfileRequest(
        String nombre,   // Nuevo nombre (opcional)
        String apellido  // Nuevo apellido (opcional)
) {}

