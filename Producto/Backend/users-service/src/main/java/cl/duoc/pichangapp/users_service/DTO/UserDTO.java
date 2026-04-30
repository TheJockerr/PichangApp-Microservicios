package cl.duoc.pichangapp.users_service.dto;

/**
 * DTO que representa la información pública de un usuario.
 * Se utiliza para devolver datos al cliente sin exponer la contraseña ni datos de otros microservicios.
 */
public record UserDTO(
        Integer id,       // Identificador único del usuario
        String correo,    // Correo electrónico
        String nombre,    // Nombre
        String apellido,  // Apellido
        boolean enabled   // Estado de verificación de la cuenta
) {}



