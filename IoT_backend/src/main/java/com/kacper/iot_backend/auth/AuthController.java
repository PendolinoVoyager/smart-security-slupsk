package com.kacper.iot_backend.auth;

import jakarta.mail.MessagingException;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
        LOGGER.info("Received request to register user: {}", authRegistrationRequest);
        return new ResponseEntity<>(authService.register(authRegistrationRequest), HttpStatus.CREATED);
    }

    @PostMapping("/login")
    public AuthLoginResponse login(
            @Valid @RequestBody AuthLoginRequest authLoginRequest
    ) {
        return authService.login(authLoginRequest);
    }

    /// Shit endpoint - dont use pls
    @GetMapping("is-token-valid")
    public ResponseEntity<IsTokenValidResponse> isTokenValid(@Valid @RequestBody IsTokenValidRequest isTokenValidRequest) {
        boolean isValid = authService.isTokenValid(isTokenValidRequest);
        IsTokenValidResponse response = new IsTokenValidResponse(isValid);
        return ResponseEntity.ok(response);
    }
}
