package cl.duoc.pichangapp.users_service.dto;

/**
 * DTO para cambiar la contraseña de un usuario.
 * El service debe verificar la contraseña actual antes de aceptar la nueva.
 */
public record ChangePasswordRequest(
        String currentPassword, // Contraseña actual en texto plano (para verificar)
        String newPassword      // Nueva contraseña en texto plano (se encripta en el service)
) {}


