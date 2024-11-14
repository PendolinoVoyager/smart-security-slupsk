package com.kacper.iot_backend.utils;

import lombok.Builder;

@Builder
public record DefaultResponse(
        String message
) {
}
