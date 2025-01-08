package com.kacper.iot_backend.auth.device_auth;

import lombok.Builder;

@Builder
public record AuthDeviceResponse(
        String token
) {
}
