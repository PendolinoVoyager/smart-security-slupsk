package com.kacper.iot_backend.activation_token;

public record ActivationTokenRequest(
        String email,
        String activationToken
) {
}
