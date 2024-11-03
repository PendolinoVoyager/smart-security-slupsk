package com.kacper.iot_backend.activation_token;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/activation-token")
public class ActivationTokenController
{
    private final ActivationTokenService activationTokenService;

    public ActivationTokenController(ActivationTokenService activationTokenService) {
        this.activationTokenService = activationTokenService;
    }

    @PostMapping("/verify")
    public ResponseEntity<String> verifyActivationToken(
            @RequestBody ActivationTokenRequest activationTokenRequest
    ) {
        activationTokenService.enableUser(activationTokenRequest);
        return new ResponseEntity<>("User has been enabled", HttpStatus.OK);
    }
}
