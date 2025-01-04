package com.kacper.iot_backend.activation_token;

import com.kacper.iot_backend.exception.ResourceNotFoundException;
import com.kacper.iot_backend.exception.WrongLoginCredentialsException;
import com.kacper.iot_backend.user.User;
import com.kacper.iot_backend.user.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;


import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class ActivationTokenServiceTest
{

    @Mock
    private UserService userService;

    @Mock
    private ActivationTokenRepository activationTokenRepository;

    @InjectMocks
    private ActivationTokenService activationTokenService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void createActivationTokenShouldReturnValidToken() {
        User user = new User();
        user.setId(1);

        ActivationToken activationToken = activationTokenService.createActivationToken(user);

        assertNotNull(activationToken);
        assertEquals(4, activationToken.getToken().length());
        assertEquals(user, activationToken.getUser());
        assertNotNull(activationToken.getCreatedAt());
    }

    @Test
    void enableUserShouldEnableTheUserWhenTokenIsValid() {
        User user = new User();
        user.setId(1);
        user.setEmail("test@example.com");
        user.setEnabled(false);

        ActivationToken activationToken = new ActivationToken();
        activationToken.setToken("1234");
        activationToken.setUser(user);
        user.setActivationToken(activationToken);

        when(userService.getUserOrThrow("test@example.com")).thenReturn(user);

        ActivationTokenRequest request = new ActivationTokenRequest("test@example.com", "1234");

        activationTokenService.enableUser(request);

        assertTrue(user.isEnabled());
        verify(activationTokenRepository, times(1)).deleteByUserId(1);
        verify(userService, times(1)).saveUser(user);
    }


    @Test
    void enableUserShouldThrowExceptionWhenTokenIsInvalid() {
        User user = new User();
        user.setId(1);
        user.setEmail("test@example.com");

        ActivationToken activationToken = new ActivationToken();
        activationToken.setToken("1234");
        activationToken.setUser(user);
        user.setActivationToken(activationToken);

        when(userService.getUserOrThrow("test@example.com")).thenReturn(user);

        ActivationTokenRequest request = new ActivationTokenRequest("test@example.com", "5678");

        WrongLoginCredentialsException exception = assertThrows(
                WrongLoginCredentialsException.class,
                () -> activationTokenService.enableUser(request)
        );

        assertEquals("Invalid activation token", exception.getMessage());
        verify(activationTokenRepository, never()).deleteByUserId(any());
        verify(userService, never()).saveUser(any());
    }


    @Test
    void enableUserShouldThrowExceptionWhenActivationTokenIsMissing() {
        User user = new User();
        user.setId(1);
        user.setEmail("test@example.com");
        user.setActivationToken(null);

        when(userService.getUserOrThrow("test@example.com")).thenReturn(user);

        ActivationTokenRequest request = new ActivationTokenRequest("test@example.com", "1234");

        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> activationTokenService.enableUser(request)
        );

        assertEquals("Activation token not found", exception.getMessage());
        verify(activationTokenRepository, never()).deleteByUserId(any());
        verify(userService, never()).saveUser(any());
    }


    @Test
    void generateActivationTokenShouldReturnValidToken() {
        String token = activationTokenService.createActivationToken(new User()).getToken();

        assertNotNull(token);
        assertEquals(4, token.length());
        assertTrue(token.chars().allMatch(Character::isDigit));
    }
}
