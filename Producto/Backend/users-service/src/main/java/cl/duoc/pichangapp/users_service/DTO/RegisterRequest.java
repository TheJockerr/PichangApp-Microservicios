package cl.duoc.pichangapp.users_service.DTO;

/**
 * DTO para registrar un nuevo usuario.
 * Contiene los datos mínimos requeridos por el sistema para crear una cuenta.
 * La contraseña se recibe en texto plano y se debe encriptar en el service antes de persistir.
 */
public record RegisterRequest(
        String correo,    // Correo electrónico del usuario (usado como login)
        String password,  // Contraseña en texto plano (se encripta en el service)
        String nombre,    // Nombre del usuario
        String apellido   // Apellido del usuario
) {}
