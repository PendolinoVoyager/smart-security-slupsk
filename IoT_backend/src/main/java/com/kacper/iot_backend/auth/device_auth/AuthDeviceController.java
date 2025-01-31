package com.kacper.iot_backend.auth.device_auth;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.logging.Logger;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthDeviceController
{
    private final AuthDeviceService authDeviceService;
    private final static Logger logger = Logger.getLogger(AuthDeviceController.class.getName());

    public AuthDeviceController(AuthDeviceService authDeviceService) {
        this.authDeviceService = authDeviceService;
    }


    @PostMapping("/device")
    public AuthDeviceResponse authDevice(@RequestBody AuthDeviceRequest authDeviceRequest) {
        return authDeviceService.authenticateDevice(authDeviceRequest);
    }

    @PostMapping("/device/refresh")
    public AuthDeviceAccessTokenResponse refreshDeviceToken(@RequestHeader("Authorization") String authorizationHeader, @RequestBody AuthDeviceRefreshRequest authDeviceRefreshRequest) {
        return authDeviceService.refreshDeviceToken(authorizationHeader, authDeviceRefreshRequest);
    }


}
