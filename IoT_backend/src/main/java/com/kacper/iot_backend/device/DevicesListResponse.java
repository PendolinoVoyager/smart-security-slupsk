package com.kacper.iot_backend.device;

public record DevicesListResponse(
        Integer id,
        String address,
        String deviceName,
        String uuid

) {
}
