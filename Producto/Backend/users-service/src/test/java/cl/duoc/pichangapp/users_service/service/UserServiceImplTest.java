package cl.duoc.pichangapp.users_service.service;

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

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtProvider jwtProvider;

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
        assertEquals(1, result.get().id());
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
}
