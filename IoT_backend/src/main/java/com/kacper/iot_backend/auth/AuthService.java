package com.kacper.iot_backend.auth;

import com.kacper.iot_backend.exception.ResourceNotFoundException;
import com.kacper.iot_backend.jwt.JWTService;
import com.kacper.iot_backend.user.User;
import com.kacper.iot_backend.user.UserRepository;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.logging.Logger;

@Service
public class AuthService
{
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JWTService jwtService;
    private final AuthenticationManager authenticationManager;
    public Logger logger = Logger.getLogger(AuthService.class.getName());

    public AuthService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            JWTService jwtService, AuthenticationManager authenticationManager
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.authenticationManager = authenticationManager;
    }


    public AuthRegistrationResponse register(AuthRegistrationRequest authRegistrationRequest) {
        logger.info(authRegistrationRequest.toString());
        User user = User.builder()
                .name(authRegistrationRequest.name())
                .last_name(authRegistrationRequest.last_name())
                .email(authRegistrationRequest.email())
                .password(passwordEncoder.encode(authRegistrationRequest.password()))
                .role(authRegistrationRequest.role().toUpperCase())
                .created_at(new Date())
                .build();

        logger.info(user.toString());

        try {
            User savedUser = userRepository.save(user);
            return AuthRegistrationResponse.builder()
                    .name(savedUser.getName())
                    .lastName(savedUser.getLast_name())
                    .role(savedUser.getRole())
                    .email(savedUser.getEmail())
                    .build();
        } catch (Exception e) {
            throw new RuntimeException("Error xD");
        }

    }

    public AuthLoginResponse login(AuthLoginRequest authLoginRequest) {
        User user = userRepository.findByEmail(authLoginRequest.email())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        try {
            authenticationManager
                    .authenticate(new UsernamePasswordAuthenticationToken(authLoginRequest.email(), authLoginRequest.password()));
        } catch (Exception e) {
            throw new RuntimeException("Invalid email/password");
        }

        String token = jwtService.generateToken(user);
        return AuthLoginResponse.builder()
                .email(user.getEmail())
                .role(user.getRole())
                .token(token)
                .build();
    }


}
