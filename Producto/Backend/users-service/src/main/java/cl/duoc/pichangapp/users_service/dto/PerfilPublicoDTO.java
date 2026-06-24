package cl.duoc.pichangapp.users_service.dto;

/**
 * Perfil público de un usuario: lo que se muestra a OTROS usuarios.
 * No expone correo, id ni contraseña.
 */
public record PerfilPublicoDTO(
        String nombre,
        String apellido,
        Integer karmaScore,
        String categoriaKarma,
        Boolean historialVisible
) {}
