package com.kacper.iot_backend.auth;

import jakarta.mail.MessagingException;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController
{
    private static final Logger LOGGER = LoggerFactory.getLogger(AuthController.class);
    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<AuthRegistrationResponse> register(
            @Valid @RequestBody AuthRegistrationRequest authRegistrationRequest
    ) throws MessagingException {
        return new ResponseEntity<>(authService.register(authRegistrationRequest), HttpStatus.CREATED);
    }

    @PostMapping("/login")
    public AuthLoginResponse login(
            @Valid @RequestBody AuthLoginRequest authLoginRequest
    ) {
        return authService.login(authLoginRequest);
    }
}
