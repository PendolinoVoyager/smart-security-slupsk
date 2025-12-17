package com.kacper.iot_backend.auth;

import jakarta.validation.constraints.NotBlank;

public record IsTokenValidRequest(
        @NotBlank(message = "Token is required")
        String token,

        @NotBlank(message = "Token type is required")
        String tokenType
) {}
