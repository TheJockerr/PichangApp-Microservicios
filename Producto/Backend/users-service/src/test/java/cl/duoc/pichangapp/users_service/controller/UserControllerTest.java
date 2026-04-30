package cl.duoc.pichangapp.users_service.controller;

import cl.duoc.pichangapp.users_service.service.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserControllerTest {

    @Mock
    private UserService userService;

    @InjectMocks
    private UserController userController;

    @Test
    void testUserExists_WhenExists_ReturnsTrue() {
        when(userService.existsById(1)).thenReturn(true);
        ResponseEntity<Boolean> response = userController.userExists(1);
        
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody());
    }

    @Test
    void testUserExists_WhenNotExists_ReturnsFalse() {
        when(userService.existsById(99)).thenReturn(false);
        ResponseEntity<Boolean> response = userController.userExists(99);
        
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(Boolean.FALSE, response.getBody());
    }
}
