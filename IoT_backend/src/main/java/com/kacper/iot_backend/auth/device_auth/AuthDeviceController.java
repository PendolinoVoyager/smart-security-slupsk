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

    // jwt user -> filter -> jwt device -> filter -> device controller
    // authenticated with jwt user
    @PostMapping("/device")
    public AuthDeviceResponse authDevice(@RequestBody AuthDeviceRequest authDeviceRequest) {
        return authDeviceService.authenticateDevice(authDeviceRequest);
    }

    // kupuje urzadzenie
    // wchodze na localhost
    // urzadzenie o danym uuid jest juz przypisane do usera
    // user sie loguje na urzadzeniu mailem i haslem
    // dostaje token z rola DEVICE
    // token zapisac w chuj bezpiecznym miejscu
}
