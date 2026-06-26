package cl.duoc.pichangapp.users_service.service.impl;

import cl.duoc.pichangapp.users_service.dto.ChangePasswordRequest;
import cl.duoc.pichangapp.users_service.dto.JWTResponse;
import cl.duoc.pichangapp.users_service.dto.LoginRequest;
import cl.duoc.pichangapp.users_service.dto.RegisterRequest;
import cl.duoc.pichangapp.users_service.dto.ResendCodeRequest;
import cl.duoc.pichangapp.users_service.dto.PerfilPublicoDTO;
import cl.duoc.pichangapp.users_service.dto.UpdateProfileRequest;
import cl.duoc.pichangapp.users_service.dto.UserDTO;
import cl.duoc.pichangapp.users_service.dto.VerifyCodeRequest;
import cl.duoc.pichangapp.users_service.model.User; // <- cambia si tu User está en otro paquete
import cl.duoc.pichangapp.users_service.repository.UserRepository;
import cl.duoc.pichangapp.users_service.security.JwtProvider;
import cl.duoc.pichangapp.users_service.service.EmailService;
import cl.duoc.pichangapp.users_service.service.EventsServiceClient;
import cl.duoc.pichangapp.users_service.service.KarmaServiceClient;
import cl.duoc.pichangapp.users_service.service.NotificationServiceClient;
import cl.duoc.pichangapp.users_service.service.UserService;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

import cl.duoc.pichangapp.users_service.dto.AdminUserDTO;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Random;

/**
 * Implementación del servicio de usuarios sin excepciones personalizadas.
 * Usa ResponseStatusException para devolver códigos HTTP claros sin crear clases propias.
 */
@Service
@SuppressWarnings("null")
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;
    private final EmailService emailService;
    private final KarmaServiceClient karmaServiceClient;
    private final EventsServiceClient eventsServiceClient;
    private final NotificationServiceClient notificationServiceClient;

    public UserServiceImpl(UserRepository userRepository,
                           PasswordEncoder passwordEncoder,
                           JwtProvider jwtProvider,
                           EmailService emailService,
                           KarmaServiceClient karmaServiceClient,
                           EventsServiceClient eventsServiceClient,
                           NotificationServiceClient notificationServiceClient) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtProvider = jwtProvider;
        this.emailService = emailService;
        this.karmaServiceClient = karmaServiceClient;
        this.eventsServiceClient = eventsServiceClient;
        this.notificationServiceClient = notificationServiceClient;
    }

    @Override
    @Transactional
    public UserDTO register(RegisterRequest request) {
        // Normalizar correo
        String correo = request.correo().toLowerCase().trim();

        // Validaciones de negocio con ResponseStatusException (HTTP 409 / 400)
        if (userRepository.existsByCorreo(correo)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "El correo ya está registrado");
        }
        if (request.password() == null || request.password().length() < 6) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La contraseña debe tener al menos 6 caracteres");
        }
        if (request.nombre() == null || request.nombre().isBlank() ||
            request.apellido() == null || request.apellido().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Nombre y apellido son obligatorios");
        }

        // Construcción de la entidad
        User user = new User();
        user.setCorreo(correo);
        user.setContrasena(passwordEncoder.encode(request.password()));
        user.setNombre(request.nombre().trim());
        user.setApellido(request.apellido().trim());
        user.setEnabled(false);

        // Generar código de verificación de 6 dígitos
        String code = String.format("%06d", new Random().nextInt(1000000));
        user.setVerificationCode(code);
        user.setVerificationCodeExpiry(LocalDateTime.now().plusMinutes(5));

        User saved = userRepository.save(user);

        // Enviar correo de verificación
        emailService.sendVerificationEmail(saved.getCorreo(), code);

        return mapToDto(saved);
    }

    @Override
    public JWTResponse authenticate(LoginRequest request) {
        String correo = request.correo().toLowerCase().trim();

        // Buscar usuario; si no existe -> 401 Unauthorized
        User user = userRepository.findByCorreo(correo)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Credenciales inválidas"));

        // Verificar contraseña; si no coincide -> 401
        if (!passwordEncoder.matches(request.password(), user.getContrasena())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Credenciales inválidas");
        }

        // Verificar que la cuenta esté habilitada -> 403 Forbidden
        if (!user.isEnabled()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Cuenta no verificada");
        }

        String token = jwtProvider.generateToken(user.getId().toString(), user.getCorreo(), user.getRole());
        long expiresIn = jwtProvider.getExpirationMs();

        return new JWTResponse(token, "Bearer", expiresIn, mapToDto(user));
    }

    @Override
    public UserDTO getProfile(Integer id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado"));
        return mapToDto(user);
    }

    @Override
    @Transactional
    public UserDTO updateProfile(Integer id, UpdateProfileRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado"));

        if (request.nombre() != null && !request.nombre().isBlank()) {
            user.setNombre(request.nombre().trim());
        }
        if (request.apellido() != null && !request.apellido().isBlank()) {
            user.setApellido(request.apellido().trim());
        }

        User updated = userRepository.save(user);
        return mapToDto(updated);
    }

    @Override
    @Transactional
    public void changePassword(Integer id, ChangePasswordRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado"));

        // Verificar contraseña actual -> 401
        if (!passwordEncoder.matches(request.currentPassword(), user.getContrasena())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Contraseña actual incorrecta");
        }

        // Validar nueva contraseña -> 400
        if (request.newPassword() == null || request.newPassword().length() < 6) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La nueva contraseña debe tener al menos 6 caracteres");
        }

        user.setContrasena(passwordEncoder.encode(request.newPassword()));
        userRepository.save(user);
    }

    @Override
    @Transactional
    public boolean setHistorialVisible(Integer id, boolean visible) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado"));
        user.setHistorialVisible(visible);
        userRepository.save(user);
        return visible;
    }

    @Override
    public List<PerfilPublicoDTO> buscarUsuarios(String texto) {
        if (texto == null || texto.isBlank()) {
            return List.of();
        }
        String t = texto.trim();
        return userRepository
                .findByNombreContainingIgnoreCaseOrApellidoContainingIgnoreCase(t, t)
                .stream()
                .map(this::toPerfilPublico)
                .toList();
    }

    @Override
    public PerfilPublicoDTO getPerfilPublicoByCorreo(String correo) {
        User user = userRepository.findByCorreo(correo.toLowerCase().trim())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado"));
        return toPerfilPublico(user);
    }

    @Override
    @Transactional
    public void eliminarCuenta(Integer id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado"));

        // Orden estricto: eventos → karma → notificaciones → usuario.
        // Cada cliente hace log-and-continue para no bloquear el borrado si un servicio falla.
        eventsServiceClient.deleteUserEvents(id);
        karmaServiceClient.deleteKarma(id);
        notificationServiceClient.deleteUserNotifications(id);

        userRepository.delete(user);
    }

    // Construye el perfil público resolviendo el karma desde karma_service.
    private PerfilPublicoDTO toPerfilPublico(User u) {
        KarmaServiceClient.KarmaInfo info = karmaServiceClient.getKarmaInfo(u.getId());
        return new PerfilPublicoDTO(
                u.getCorreo(),
                u.getNombre(),
                u.getApellido(),
                info.karmaScore(),
                info.categoria(),
                u.getHistorialVisible()
        );
    }

    @Override
    @Transactional
    public void enableUser(Integer id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado"));
        user.setEnabled(true);
        userRepository.save(user);
    }

    @Override
    @Transactional
    public void verifyCode(VerifyCodeRequest request) {
        String correo = request.email().toLowerCase().trim();
        User user = userRepository.findByCorreo(correo)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado"));

        if (user.getVerificationCode() == null || !user.getVerificationCode().equals(request.code())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Código inválido");
        }

        if (LocalDateTime.now().isAfter(user.getVerificationCodeExpiry())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Código expirado, solicita uno nuevo");
        }

        user.setEnabled(true);
        user.setVerificationCode(null);
        user.setVerificationCodeExpiry(null);
        userRepository.save(user);
    }

    @Override
    @Transactional
    public void resendCode(ResendCodeRequest request) {
        String correo = request.email().toLowerCase().trim();
        User user = userRepository.findByCorreo(correo)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado"));

        if (user.isEnabled()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El usuario ya está verificado");
        }

        String code = String.format("%06d", new Random().nextInt(1000000));
        user.setVerificationCode(code);
        user.setVerificationCodeExpiry(LocalDateTime.now().plusMinutes(5));
        userRepository.save(user);

        emailService.sendVerificationEmail(user.getCorreo(), code);
    }

    @Override
    public Optional<UserDTO> findByCorreo(String correo) {
        return userRepository.findByCorreo(correo.toLowerCase().trim()).map(this::mapToDto);
    }

    @Override
    public boolean existsById(Integer id) {
        return userRepository.existsById(id);
    }

    // ======================= Administración (rol ADMIN) =======================

    @Override
    public List<AdminUserDTO> listAllUsers() {
        return userRepository.findAll().stream()
                .map(this::mapToAdminDto)
                .toList();
    }

    @Override
    public AdminUserDTO getUserForAdmin(Integer id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado"));
        return mapToAdminDto(user);
    }

    @Override
    @Transactional
    public void deleteUser(Integer id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado"));
        if ("ADMIN".equals(user.getRole())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "No se puede eliminar a un usuario administrador");
        }
        userRepository.delete(user);
    }

    // Mapeo entidad -> DTO (sin exponer id; el identificador visible es el correo)
    private UserDTO mapToDto(User u) {
        return new UserDTO(u.getCorreo(), u.getNombre(), u.getApellido(), u.isEnabled(), u.getRole());
    }

    // Mapeo entidad -> DTO administrativo (incluye id para uso interno del panel)
    private AdminUserDTO mapToAdminDto(User u) {
        return new AdminUserDTO(u.getId(), u.getCorreo(), u.getNombre(), u.getApellido(), u.isEnabled(), u.getRole());
    }
}

