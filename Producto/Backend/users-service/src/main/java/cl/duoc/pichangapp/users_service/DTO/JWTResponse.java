package cl.duoc.pichangapp.users_service.dto;


/**
 * DTO devuelto tras un login exitoso.
 * Incluye el token JWT y la información pública del usuario autenticado.
 */
public record JWTResponse(
        String token,    // Token JWT generado
        String type,     // Tipo de token, por ejemplo "Bearer"
        long expiresIn,  // Tiempo en milisegundos hasta la expiración del token
        UserDTO user     // Información pública del usuario autenticado
) {}


