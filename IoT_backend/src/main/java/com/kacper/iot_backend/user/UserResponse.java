package com.kacper.iot_backend.user;

import lombok.Builder;

@Builder
public record UserResponse(
        String email,
        String name,
        String lastName,
        String role
) {
}
