package com.kacper.iot_backend.measurements;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.Date;

public record AddMeasurementRequest(
        @NotNull(message = "Device ID is required")
        int deviceId,

        @NotBlank(message = "MeasurementType is required")
        String measurementType,

        @NotNull(message = "Value is required")
        double value,

        @NotNull(message = "Timestamp is required")
        Date timestamp
) { }
