package com.kacper.iot_backend.device;

import org.springframework.stereotype.Service;

import java.util.function.Function;

@Service
public class DeviceListResponseMapper implements Function<Device, DevicesListResponse>
{
    @Override
    public DevicesListResponse apply(Device device) {
        return new DevicesListResponse(
                device.getId(),
                device.getAddress(),
                device.getDeviceName(),
                device.getUuid()
        );
    }
}
