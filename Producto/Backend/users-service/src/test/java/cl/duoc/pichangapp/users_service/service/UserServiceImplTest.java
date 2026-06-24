package cl.duoc.pichangapp.users_service.service;

import cl.duoc.pichangapp.users_service.dto.ChangePasswordRequest;
import cl.duoc.pichangapp.users_service.dto.PerfilPublicoDTO;
import cl.duoc.pichangapp.users_service.dto.UserDTO;
import cl.duoc.pichangapp.users_service.model.User;
import cl.duoc.pichangapp.users_service.repository.UserRepository;
import cl.duoc.pichangapp.users_service.security.JwtProvider;
import cl.duoc.pichangapp.users_service.service.impl.UserServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtProvider jwtProvider;

    @Mock
    private EmailService emailService;

    @Mock
    private KarmaServiceClient karmaServiceClient;

    @Mock
    private EventsServiceClient eventsServiceClient;

    @Mock
    private NotificationServiceClient notificationServiceClient;

    @InjectMocks
    private UserServiceImpl userService;

    private User mockUser;

    @BeforeEach
    void setUp() {
        mockUser = new User();
        mockUser.setId(1);
        mockUser.setCorreo("test@test.com");
        mockUser.setNombre("Test");
        mockUser.setApellido("User");
        mockUser.setEnabled(true);
        mockUser.setHistorialVisible(true);
    }

    @Test
    void testExistsById_UserExists_ReturnsTrue() {
        when(userRepository.existsById(1)).thenReturn(true);
        boolean exists = userService.existsById(1);
        assertTrue(exists);
        verify(userRepository).existsById(1);
    }

    @Test
    void testExistsById_UserDoesNotExist_ReturnsFalse() {
        when(userRepository.existsById(99)).thenReturn(false);
        boolean exists = userService.existsById(99);
        assertFalse(exists);
        verify(userRepository).existsById(99);
    }

    @Test
    void testFindByCorreo_UserExists_ReturnsDto() {
        when(userRepository.findByCorreo("test@test.com")).thenReturn(Optional.of(mockUser));
        Optional<UserDTO> result = userService.findByCorreo("test@test.com");

        assertTrue(result.isPresent());
        assertEquals("test@test.com", result.get().correo());
        verify(userRepository).findByCorreo("test@test.com");
    }

    @Test
    void testFindByCorreo_UserDoesNotExist_ReturnsEmpty() {
        when(userRepository.findByCorreo("notfound@test.com")).thenReturn(Optional.empty());
        Optional<UserDTO> result = userService.findByCorreo("notfound@test.com");

        assertFalse(result.isPresent());
        verify(userRepository).findByCorreo("notfound@test.com");
    }

    // ───────────────────────── Nuevos tests ─────────────────────────

    @Test
    void changePassword_Success() {
        ChangePasswordRequest req = new ChangePasswordRequest("oldPass", "newPass123");
        mockUser.setContrasena("hashedOld");
        when(userRepository.findById(1)).thenReturn(Optional.of(mockUser));
        when(passwordEncoder.matches("oldPass", "hashedOld")).thenReturn(true);
        when(passwordEncoder.encode("newPass123")).thenReturn("hashedNew");

        userService.changePassword(1, req);

        assertEquals("hashedNew", mockUser.getContrasena());
        verify(userRepository).save(mockUser);
    }

    @Test
    void changePassword_WrongCurrentPassword() {
        ChangePasswordRequest req = new ChangePasswordRequest("wrongPass", "newPass123");
        mockUser.setContrasena("hashedOld");
        when(userRepository.findById(1)).thenReturn(Optional.of(mockUser));
        when(passwordEncoder.matches("wrongPass", "hashedOld")).thenReturn(false);

        assertThrows(ResponseStatusException.class, () -> userService.changePassword(1, req));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void buscarUsuario_RetornaListaFiltrada() {
        when(userRepository.findByNombreContainingIgnoreCaseOrApellidoContainingIgnoreCase("Test", "Test"))
                .thenReturn(List.of(mockUser));
        when(karmaServiceClient.getKarmaInfo(1))
                .thenReturn(new KarmaServiceClient.KarmaInfo(80, "Excelente"));

        List<PerfilPublicoDTO> result = userService.buscarUsuarios("Test");

        assertEquals(1, result.size());
        assertEquals("Test", result.get(0).nombre());
        assertEquals(80, result.get(0).karmaScore());
        assertEquals("Excelente", result.get(0).categoriaKarma());
        // No expone correo ni id (el record PerfilPublicoDTO no los tiene).
    }

    @Test
    void eliminarCuenta_Success() {
        when(userRepository.findById(1)).thenReturn(Optional.of(mockUser));

        userService.eliminarCuenta(1);

        // Compensa en los tres servicios y borra el usuario, en orden.
        verify(eventsServiceClient).deleteUserEvents(1);
        verify(karmaServiceClient).deleteKarma(1);
        verify(notificationServiceClient).deleteUserNotifications(1);
        verify(userRepository).delete(mockUser);
    }
}
