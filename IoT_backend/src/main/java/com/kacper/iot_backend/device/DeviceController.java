package com.kacper.iot_backend.device;


import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.kacper.iot_backend.exception.ResourceNotFoundException;

import java.util.List;
import org.springframework.web.bind.annotation.RequestParam;


@RequestMapping("/api/v1/device")
@RestController
public class DeviceController
{
    private final DeviceService deviceService;

    public DeviceController(DeviceService deviceService) {
        this.deviceService = deviceService;
    }

    @GetMapping("/")
    public List<DevicesListResponse> getUserDevices(
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        return deviceService.getUserDevices(userDetails);
    }

    @GetMapping("/{uuid}")
    public DevicesListResponse getUserDeviceByUuid(@AuthenticationPrincipal UserDetails userDetails,
        @PathVariable String uuid) {
        return deviceService.getByUuid(uuid);
    }

}
