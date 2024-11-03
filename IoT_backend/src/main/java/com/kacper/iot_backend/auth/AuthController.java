package com.kacper.iot_backend.auth;

import jakarta.mail.MessagingException;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController
{
    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public AuthRegistrationResponse register(
            @Valid @RequestBody AuthRegistrationRequest authRegistrationRequest
    ) throws MessagingException {
        return authService.register(authRegistrationRequest);
    }

    @PostMapping("/login")
    public AuthLoginResponse login(
            @Valid @RequestBody AuthLoginRequest authLoginRequest
    ) {
        return authService.login(authLoginRequest);
    }
}
