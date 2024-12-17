package com.kacper.iot_backend.user;

import com.kacper.iot_backend.activation_token.ActivationToken;
import com.kacper.iot_backend.activation_token.ActivationTokenRepository;
import com.kacper.iot_backend.auth.AuthRegistrationRequest;
import com.kacper.iot_backend.exception.ResourceAlreadyExistException;
import com.kacper.iot_backend.exception.ResourceNotFoundException;
import com.kacper.iot_backend.exception.UserNotEnabledException;
import jakarta.persistence.EntityExistsException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

@Service
public class UserService
{
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final ActivationTokenRepository activationTokenRepository;

    public UserService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder, ActivationTokenRepository activationTokenRepository
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.activationTokenRepository = activationTokenRepository;
    }

    public User getUserOrThrow(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    public void isUserEnabled(User user) {
        if (!user.isEnabled()) {
            throw new UserNotEnabledException("User not enabled");
        }
    }

    public User createUser(AuthRegistrationRequest authRegistrationRequest) {
        return User.builder()
                .enabled(false)
                .name(authRegistrationRequest.name())
                .last_name(authRegistrationRequest.last_name())
                .email(authRegistrationRequest.email())
                .password(passwordEncoder.encode(authRegistrationRequest.password()))
                .role("USER")
                .created_at(new Date())
                .build();
    }

    @Transactional
    public void saveUserAndToken(User user, ActivationToken activationToken) {
        try {
            userRepository.save(user);
            activationTokenRepository.save(activationToken);
        } catch (DataIntegrityViolationException e) {
            throw new ResourceAlreadyExistException("Email already exists or other integrity violation");
        } catch (EntityExistsException e) {
            throw new ResourceAlreadyExistException("Entity already exists");
        } catch (RuntimeException e) {
            throw new RuntimeException("Critical error during registration");
        }
    }

    public void saveUser(User user) {
        userRepository.save(user);
    }

    public void setNewPassword(User user, String newPassword) {
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }
}
