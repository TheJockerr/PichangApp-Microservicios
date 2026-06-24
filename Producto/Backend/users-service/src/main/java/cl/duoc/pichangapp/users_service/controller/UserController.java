package cl.duoc.pichangapp.users_service.controller;

import cl.duoc.pichangapp.users_service.dto.ChangePasswordRequest;
import cl.duoc.pichangapp.users_service.dto.HistorialVisibleRequest;
import cl.duoc.pichangapp.users_service.dto.PerfilPublicoDTO;
import cl.duoc.pichangapp.users_service.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    /** Extrae el userId (subject) del JWT autenticado. */
    private Integer getLoggedUserId() {
        return Integer.parseInt(SecurityContextHolder.getContext().getAuthentication().getName());
    }

    @GetMapping("/{userId}/exists")
    public ResponseEntity<Boolean> userExists(@PathVariable Integer userId) {
        boolean exists = userService.existsById(userId);
        return ResponseEntity.ok(exists);
    }

    /**
     * Cambia la contraseña del usuario autenticado (userId desde el JWT).
     * PUT /api/v1/users/change-password
     */
    @PutMapping("/change-password")
    public ResponseEntity<String> changePassword(@RequestBody ChangePasswordRequest request) {
        userService.changePassword(getLoggedUserId(), request);
        return ResponseEntity.ok("Contraseña actualizada correctamente");
    }

    /**
     * Actualiza la visibilidad del historial de karma del usuario autenticado.
     * PUT /api/v1/users/historial-visible
     */
    @PutMapping("/historial-visible")
    public ResponseEntity<Map<String, Object>> setHistorialVisible(@RequestBody HistorialVisibleRequest request) {
        boolean nuevo = userService.setHistorialVisible(getLoggedUserId(), Boolean.TRUE.equals(request.visible()));
        return ResponseEntity.ok(Map.of("historialVisible", nuevo));
    }

    /**
     * Busca usuarios por nombre o apellido. Devuelve perfiles públicos.
     * GET /api/v1/users/buscar?nombre={texto}
     */
    @GetMapping("/buscar")
    public ResponseEntity<List<PerfilPublicoDTO>> buscarUsuarios(@RequestParam String nombre) {
        return ResponseEntity.ok(userService.buscarUsuarios(nombre));
    }

    /**
     * Perfil público por correo (uso interno: ver el creador de un evento).
     * GET /api/v1/users/perfil-publico/{correo}
     */
    @GetMapping("/perfil-publico/{correo}")
    public ResponseEntity<PerfilPublicoDTO> perfilPublico(@PathVariable String correo) {
        return ResponseEntity.ok(userService.getPerfilPublicoByCorreo(correo));
    }

    /**
     * Elimina la cuenta propia del usuario autenticado.
     * DELETE /api/v1/users/cuenta
     */
    @DeleteMapping("/cuenta")
    public ResponseEntity<String> eliminarCuenta() {
        userService.eliminarCuenta(getLoggedUserId());
        return ResponseEntity.ok("Cuenta eliminada correctamente");
    }
}
