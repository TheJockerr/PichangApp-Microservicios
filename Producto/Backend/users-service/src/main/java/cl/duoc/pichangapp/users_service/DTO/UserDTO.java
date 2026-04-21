package cl.duoc.pichangapp.users_service.DTO;

import java.time.Instant;

/**
 * DTO que representa la información pública de un usuario.
 * Se utiliza para devolver datos al cliente sin exponer la contraseña.
 */
public record UserDTO(
        Integer id,         // Identificador numérico autoincremental (PK)
        String correo,      // Correo del usuario
        String nombre,      // Nombre
        String apellido,    // Apellido
        boolean enabled,    // Si la cuenta está verificada/activa
        Integer karmaScore, // Puntaje de reputación (Karma)
        Instant createdAt,  // Fecha de creación del registro
        Instant updatedAt   // Fecha de última actualización (puede ser null)
) {}

