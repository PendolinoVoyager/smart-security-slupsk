package com.kacper.iot_backend.user;

import com.kacper.iot_backend.activation_token.ActivationToken;
import com.kacper.iot_backend.activation_token.ActivationTokenRepository;
import com.kacper.iot_backend.auth.AuthRegistrationRequest;
import com.kacper.iot_backend.exception.ResourceAlreadyExistException;
import com.kacper.iot_backend.exception.ResourceNotFoundException;
import com.kacper.iot_backend.exception.UserNotEnabledException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

class UserServiceTest {

    private UserRepository userRepository;
    private PasswordEncoder passwordEncoder;
    private ActivationTokenRepository activationTokenRepository;
    private UserService userService;

    @BeforeEach
    void setUp() {
        userRepository = mock(UserRepository.class);
        passwordEncoder = mock(PasswordEncoder.class);
        activationTokenRepository = mock(ActivationTokenRepository.class);
        userService = new UserService(userRepository, passwordEncoder, activationTokenRepository);
    }

    @Test
    void shouldGetUserOrThrow() {
        User user = new User();
        user.setEmail("test@example.com");
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));

        User result = userService.getUserOrThrow("test@example.com");

        assertThat(result).isEqualTo(user);
    }

    @Test
    void shouldThrowExceptionWhenUserNotFound() {
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getUserOrThrow("test@example.com"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("User not found");
    }

    @Test
    void shouldThrowExceptionWhenUserNotEnabled() {
        User user = new User();
        user.setEnabled(false);

        assertThatThrownBy(() -> userService.isUserEnabled(user))
                .isInstanceOf(UserNotEnabledException.class)
                .hasMessage("User not enabled");
    }

    @Test
    void shouldCreateUser() {
        AuthRegistrationRequest request = new AuthRegistrationRequest("Test", "Test", "test@example.com", "password");
        when(passwordEncoder.encode("password")).thenReturn("encodedPassword");

        User user = userService.createUser(request);

        assertThat(user.getEmail()).isEqualTo("test@example.com");
        assertThat(user.getPassword()).isEqualTo("encodedPassword");
        assertThat(user.getRole()).isEqualTo("USER");
        assertThat(user.isEnabled()).isFalse();
    }

    @Test
    void shouldSaveUserAndToken() {
        User user = new User();
        ActivationToken activationToken = new ActivationToken();

        userService.saveUserAndToken(user, activationToken);

        verify(userRepository).save(user);
        verify(activationTokenRepository).save(activationToken);
    }

    @Test
    void shouldThrowExceptionWhenUserAlreadyExists() {
        User user = new User();
        ActivationToken activationToken = new ActivationToken();
        doThrow(DataIntegrityViolationException.class).when(userRepository).save(any());

        assertThatThrownBy(() -> userService.saveUserAndToken(user, activationToken))
                .isInstanceOf(ResourceAlreadyExistException.class)
                .hasMessage("Email already exists or other integrity violation");
    }

    @Test
    void shouldSaveUser() {
        User user = new User();

        userService.saveUser(user);

        verify(userRepository).save(user);
    }

    @Test
    void shouldSetNewPassword() {
        User user = new User();
        when(passwordEncoder.encode("newPassword")).thenReturn("encodedPassword");

        userService.setNewPassword(user, "newPassword");

        assertThat(user.getPassword()).isEqualTo("encodedPassword");
        verify(userRepository).save(user);
    }
}
