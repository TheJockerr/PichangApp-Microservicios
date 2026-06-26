package cl.duoc.pichangapp.users_service.dto;

/**
 * Perfil público de un usuario: lo que se muestra a OTROS usuarios.
 * No expone id ni contraseña. El correo se incluye como clave única de identidad.
 */
public record PerfilPublicoDTO(
        String correo,
        String nombre,
        String apellido,
        Integer karmaScore,
        String categoriaKarma,
        Boolean historialVisible
) {}
