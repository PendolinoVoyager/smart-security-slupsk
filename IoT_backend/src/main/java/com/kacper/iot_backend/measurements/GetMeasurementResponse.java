package com.kacper.iot_backend.measurements;

public record GetMeasurementResponse(
        int id,
        String measurementType,
        double value,
        String timestamp
) {
}
