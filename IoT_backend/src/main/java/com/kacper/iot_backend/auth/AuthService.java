package com.kacper.iot_backend.auth;

import com.kacper.iot_backend.activation_token.ActivationToken;
import com.kacper.iot_backend.activation_token.ActivationTokenRepository;
import com.kacper.iot_backend.exception.ResourceAlreadyExistException;
import com.kacper.iot_backend.exception.ResourceNotFoundException;
import com.kacper.iot_backend.exception.UserNotEnabledException;
import com.kacper.iot_backend.exception.WrongLoginCredentialsException;
import com.kacper.iot_backend.jwt.JWTService;
import com.kacper.iot_backend.user.User;
import com.kacper.iot_backend.user.UserRepository;
import jakarta.persistence.EntityExistsException;
import jakarta.persistence.TransactionRequiredException;
import org.apache.coyote.ActionCode;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.orm.jpa.JpaSystemException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.Date;
import java.util.logging.Logger;

@Service
public class AuthService
{
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JWTService jwtService;
    private final AuthenticationManager authenticationManager;
    private final ActivationTokenRepository activationTokenRepository;

    public Logger logger = Logger.getLogger(AuthService.class.getName());

    public AuthService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            JWTService jwtService,
            AuthenticationManager authenticationManager,
            ActivationTokenRepository activationTokenRepository
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.authenticationManager = authenticationManager;
        this.activationTokenRepository = activationTokenRepository;
    }

    public AuthRegistrationResponse register(
            AuthRegistrationRequest authRegistrationRequest
    ) {
        User user = createUser(authRegistrationRequest);
        ActivationToken activationToken = createActivationToken(user);
        saveUserAndToken(user, activationToken);

        return AuthRegistrationResponse.builder()
                .name(user.getName())
                .lastName(user.getLast_name())
                .role(user.getRole())
                .email(user.getEmail())
                .build();
    }


    public AuthLoginResponse login(
            AuthLoginRequest authLoginRequest
    ) {
        User user = getUser(authLoginRequest);
        isUserEnabled(user);
        authenticateUser(authLoginRequest);
        String token = jwtService.generateToken(user);

        return AuthLoginResponse.builder()
                .email(user.getEmail())
                .role(user.getRole())
                .token(token)
                .build();
    }

    private String generateActivationToken() {
        SecureRandom random = new SecureRandom();
        StringBuilder token = new StringBuilder(4);

        for (int i = 0; i < 4; i++) {
            int index = random.nextInt(10);
            token.append(index);
        }

        return token.toString();
    }

    private User getUser(AuthLoginRequest authLoginRequest) {
        return userRepository.findByEmail(authLoginRequest.email())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    private void isUserEnabled(User user) {
        if (!user.isEnabled()) {
            throw new UserNotEnabledException("User not enabled");
        }
    }

    private void authenticateUser(AuthLoginRequest authLoginRequest) {
        try {
            authenticationManager
                    .authenticate(
                            new UsernamePasswordAuthenticationToken(authLoginRequest.email(),
                                    authLoginRequest.password())
                    );
        } catch (BadCredentialsException e) {
            throw new WrongLoginCredentialsException("Wrong login credentials");
        } catch (RuntimeException e) {
            throw new RuntimeException("Critical error during authentication");
        }
    }

    private User createUser(AuthRegistrationRequest authRegistrationRequest) {
        return User.builder()
                .isEnabled(false)
                .name(authRegistrationRequest.name())
                .last_name(authRegistrationRequest.last_name())
                .email(authRegistrationRequest.email())
                .password(passwordEncoder.encode(authRegistrationRequest.password()))
                .role(authRegistrationRequest.role().toUpperCase())
                .created_at(new Date())
                .build();
    }

    private ActivationToken createActivationToken(User user) {
        return ActivationToken.builder()
                .token(generateActivationToken())
                .createdAt(new Date())
                .user(user)
                .build();
    }

    private void saveUserAndToken(User user, ActivationToken activationToken) {
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


}
