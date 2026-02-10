package com.kacper.iot_backend.auth;

import lombok.Builder;

@Builder
public record AudioServerAuthUserResponse(
        Boolean valid,
        String email
) {
}
