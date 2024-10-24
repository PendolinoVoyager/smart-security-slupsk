package com.kacper.iot_backend.auth;

public record AuthLoginRequest(
        String email,
        String password
)
{}
