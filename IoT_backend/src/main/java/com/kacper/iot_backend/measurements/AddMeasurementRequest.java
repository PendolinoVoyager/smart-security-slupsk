package com.kacper.iot_backend.measurements;

import jakarta.validation.constraints.NotBlank;

import java.util.Date;

public record AddMeasurementRequest(
        @NotBlank(message = "Device ID is required")
        int deviceId,

        @NotBlank(message = "Temperature is required")
        String temperature,

        @NotBlank(message = "Humidity is required")
        String humidity,

        @NotBlank(message = "Timestamp is required")
        Date timestamp
) { }
