package cl.duoc.pichangapp.users_service.controller;


import cl.duoc.pichangapp.users_service.dto.*;
import cl.duoc.pichangapp.users_service.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controlador de autenticación y gestión básica de perfil.
 * El controller no contiene lógica de negocio; delega todo al UserService.
 */
@RestController
@RequestMapping("/api/v1")
public class AuthController {

    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    /**
     * Registro de usuario.
     * POST /api/auth/register
     */
    @PostMapping("/auth/register")
    public ResponseEntity<UserDTO> register(@RequestBody RegisterRequest request) {
        UserDTO created = userService.register(request);
        return ResponseEntity.status(201).body(created);
    }

    /**
     * Login de usuario.
     * POST /api/auth/login
     */
    @PostMapping("/auth/login")
    public ResponseEntity<JWTResponse> login(@RequestBody LoginRequest request) {
        JWTResponse jwt = userService.authenticate(request);
        return ResponseEntity.ok(jwt);
    }

    /**
     * Obtener perfil público por id.
     * GET /api/users/{id}
     */
    @GetMapping("/users/{id}")
    public ResponseEntity<UserDTO> getProfile(@PathVariable Integer id) {
        UserDTO dto = userService.getProfile(id);
        return ResponseEntity.ok(dto);
    }

    /**
     * Actualizar perfil (nombre/apellido).
     * PUT /api/users/{id}
     */
    @PutMapping("/users/{id}")
    public ResponseEntity<UserDTO> updateProfile(@PathVariable Integer id,
                                                 @RequestBody UpdateProfileRequest request) {
        UserDTO updated = userService.updateProfile(id, request);
        return ResponseEntity.ok(updated);
    }

    /**
     * Cambiar contraseña.
     * PUT /api/users/{id}/password
     */
    @PutMapping("/users/{id}/password")
    public ResponseEntity<Void> changePassword(@PathVariable Integer id,
                                               @RequestBody ChangePasswordRequest request) {
        userService.changePassword(id, request);
        return ResponseEntity.noContent().build();
    }

    /**
     * Habilitar usuario (por ejemplo, endpoint que llamaría el proceso de verificación).
     * POST /api/auth/enable/{id}
     */
    @PostMapping("/auth/enable/{id}")
    public ResponseEntity<Void> enableUser(@PathVariable Integer id) {
        userService.enableUser(id);
        return ResponseEntity.noContent().build();
    }
}


