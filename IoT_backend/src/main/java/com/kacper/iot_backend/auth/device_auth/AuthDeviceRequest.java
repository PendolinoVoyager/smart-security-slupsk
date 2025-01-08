package com.kacper.iot_backend.auth.device_auth;

public record AuthDeviceRequest(
        String deviceUuid,
        String email,
        String password
) {
}
