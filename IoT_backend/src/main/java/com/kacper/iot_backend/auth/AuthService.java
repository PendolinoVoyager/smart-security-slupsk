package com.kacper.iot_backend.auth;

import com.kacper.iot_backend.user.User;
import com.kacper.iot_backend.user.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
public class AuthService
{
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder
            ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }


    public AuthRegistrationResponse register(AuthRegistrationRequest authRegistrationRequest) {
        User user = User.builder()
                .name(authRegistrationRequest.name())
                .last_name(authRegistrationRequest.last_name())
                .email(authRegistrationRequest.email())
                .password(passwordEncoder.encode(authRegistrationRequest.password()))
                .role(authRegistrationRequest.role().toUpperCase())
                .created_at(new Date())
                .build();

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
}
