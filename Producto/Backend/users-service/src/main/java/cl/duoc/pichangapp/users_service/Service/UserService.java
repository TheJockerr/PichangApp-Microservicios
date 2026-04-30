package cl.duoc.pichangapp.users_service.service;

import cl.duoc.pichangapp.users_service.dto.*;

import java.util.Optional;

/**
 * Contrato del servicio de usuarios.
 * Define las operaciones de negocio que el controller llamará.
 */
public interface UserService {

    /**
     * Registra un nuevo usuario.
     * - Valida que el correo no exista.
     * - Hashea la contraseña.
     * - Inicializa enabled = false.
     * - Retorna UserDto (sin contraseña).
     */
    UserDTO register(RegisterRequest request);

    /**
     * Autentica un usuario y retorna un JWT si las credenciales son válidas.
     */
    JWTResponse authenticate(LoginRequest request);

    /**
     * Obtiene el perfil público del usuario por id.
     */
    UserDTO getProfile(Integer id);

    /**
     * Actualiza el perfil (nombre, apellido).
     */
    UserDTO updateProfile(Integer id, UpdateProfileRequest request);

    /**
     * Cambia la contraseña del usuario (verificando la actual).
     */
    void changePassword(Integer id, ChangePasswordRequest request);

    /**
     * Habilita la cuenta del usuario (por ejemplo tras verificación por email).
     */
    void enableUser(Integer id);

    /**
     * Busca usuario por correo y devuelve UserDto si existe.
     */
    Optional<UserDTO> findByCorreo(String correo);

    /**
     * Verifica si un usuario existe por su ID.
     */
    boolean existsById(Integer id);
}


