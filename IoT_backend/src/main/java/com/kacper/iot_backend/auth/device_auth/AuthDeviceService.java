package com.kacper.iot_backend.auth.device_auth;

import com.kacper.iot_backend.device.DeviceService;
import com.kacper.iot_backend.jwt.JWTService;
import com.kacper.iot_backend.user.User;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;

import java.util.logging.Logger;

@Service
public class AuthDeviceService
{
    private final DeviceService deviceService;
    private final AuthenticationManager authenticationManager;
    private final JWTService jwtService;
    private final static Logger logger = Logger.getLogger(AuthDeviceService.class.getName());

    public AuthDeviceService(
            DeviceService deviceService,
            AuthenticationManager authenticationManager,
            JWTService jwtService
    ) {
        this.deviceService = deviceService;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
    }

    // It is ONLY for testing purposes
    public AuthDeviceResponse authenticateDevice(AuthDeviceRequest authDeviceRequest) {
        User user = deviceService.getUserByDevice(authDeviceRequest.deviceId());


        String token = jwtService.generatePermanentDeviceToken(user.getEmail());

        return new AuthDeviceResponse(token);
    }
}
