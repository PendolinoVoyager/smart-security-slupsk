package com.kacper.iot_backend.auth;

public record AuthRegistrationRequest(
        String name,
        String last_name,
        String email,
        String role,
        String password
) {
}
