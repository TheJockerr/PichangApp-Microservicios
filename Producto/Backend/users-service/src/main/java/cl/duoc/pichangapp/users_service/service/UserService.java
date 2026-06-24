package cl.duoc.pichangapp.users_service.service;

import cl.duoc.pichangapp.users_service.dto.*;

import java.util.List;
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
     * Actualiza la visibilidad del historial de karma. Devuelve el estado nuevo.
     */
    boolean setHistorialVisible(Integer id, boolean visible);

    /**
     * Busca usuarios por nombre o apellido y devuelve sus perfiles públicos.
     */
    List<PerfilPublicoDTO> buscarUsuarios(String texto);

    /**
     * Obtiene el perfil público de un usuario a partir de su correo (uso interno).
     */
    PerfilPublicoDTO getPerfilPublicoByCorreo(String correo);

    /**
     * Elimina la cuenta propia del usuario y compensa en los demás servicios
     * (cancela eventos, borra karma y notificaciones).
     */
    void eliminarCuenta(Integer id);

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

    /**
     * Verifica el código de correo.
     */
    void verifyCode(VerifyCodeRequest request);

    /**
     * Reenvía el código de verificación por correo.
     */
    void resendCode(ResendCodeRequest request);

    // ======================= Administración (rol ADMIN) =======================

    /**
     * Lista todos los usuarios para el panel de administración.
     */
    List<AdminUserDTO> listAllUsers();

    /**
     * Obtiene el detalle de un usuario por su id (uso administrativo).
     */
    AdminUserDTO getUserForAdmin(Integer id);

    /**
     * Elimina un usuario por su id (uso administrativo).
     */
    void deleteUser(Integer id);
}


