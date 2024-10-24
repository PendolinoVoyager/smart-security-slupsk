package com.kacper.iot_backend.auth;

import lombok.Builder;

@Builder
public record AuthRegistrationResponse(
        String email,
        String name,
        String lastName,
        String role
) {
}
