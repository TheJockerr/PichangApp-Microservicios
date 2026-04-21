package cl.duoc.pichangapp.users_service.DTO;

/**
 * DTO para autenticación (login).
 * Contiene las credenciales que el usuario envía para obtener un token.
 */
public record LoginRequest(
        String correo,   // Correo electrónico usado para identificar al usuario
        String password  // Contraseña en texto plano para validar
) {}

