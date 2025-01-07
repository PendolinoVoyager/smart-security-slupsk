package com.kacper.iot_backend.auth.device_auth;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthDeviceController
{
    private final AuthDeviceService authDeviceService;

    public AuthDeviceController(AuthDeviceService authDeviceService) {
        this.authDeviceService = authDeviceService;
    }

    @PostMapping("/device")
    public AuthDeviceResponse authDevice(@RequestBody AuthDeviceRequest authDeviceRequest) {
        return authDeviceService.authenticateDevice(authDeviceRequest);
    }
}
