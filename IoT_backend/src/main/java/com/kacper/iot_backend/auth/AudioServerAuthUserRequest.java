package com.kacper.iot_backend.auth;

import jakarta.validation.constraints.*;

public record AudioServerAuthUserRequest(
        @NotNull(message = "token is required")
        String token,

        @NotNull(message = "deviceId is required")
        Integer deviceId

) {
}
