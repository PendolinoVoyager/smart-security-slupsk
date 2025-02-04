package com.kacper.iot_backend.auth.device_auth;

import com.kacper.iot_backend.device.DeviceService;
import com.kacper.iot_backend.exception.DeviceOwnerMismatchException;
import com.kacper.iot_backend.exception.InvalidTokenException;
import com.kacper.iot_backend.exception.WrongLoginCredentialsException;
import com.kacper.iot_backend.jwt.JWTService;
import com.kacper.iot_backend.user.User;
import com.kacper.iot_backend.user.UserService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;

import java.util.logging.Logger;

@Service
public class AuthDeviceService
{
    private final DeviceService deviceService;
    private final AuthenticationManager authenticationManager;
    private final JWTService jwtService;
    private final UserService userService;
    private final static Logger logger = Logger.getLogger(AuthDeviceService.class.getName());

    public AuthDeviceService(
            DeviceService deviceService,
            AuthenticationManager authenticationManager,
            JWTService jwtService,
            UserService userService
    ) {
        this.deviceService = deviceService;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.userService = userService;
    }

    public AuthDeviceResponse authenticateDevice(AuthDeviceRequest authDeviceRequest) {
        User deviceOwner = deviceService.getUserByDeviceUuIdOrThrow(authDeviceRequest.deviceUuid());
        User requestingUser = userService.getUserOrThrow(authDeviceRequest.email());
        checkOwnerOrThrow(deviceOwner, requestingUser);

        try {
            authenticationManager
                    .authenticate(
                            new UsernamePasswordAuthenticationToken(authDeviceRequest.email(),
                                    authDeviceRequest.password())
                    );
        } catch (BadCredentialsException e) {
            throw new WrongLoginCredentialsException("Wrong login credentials");
        } catch (RuntimeException e) {
            throw new RuntimeException("Critical error during authentication");
        }

        String deviceToken = jwtService.generateDeviceAccessToken(deviceOwner, authDeviceRequest.deviceUuid());
        String deviceRefreshToken = jwtService.generateDeviceRefreshToken(deviceOwner);

        return AuthDeviceResponse.builder()
                .token(deviceToken)
                .refreshToken(deviceRefreshToken)
                .build();

    }

    private void checkOwnerOrThrow(User deviceOwner, User requestingUser) {
        if (!deviceOwner.equals(requestingUser)) {
            logger.warning("Device owner and requesting user are not the same");
            throw new DeviceOwnerMismatchException("Device owner and requesting user are not the same");
        }

    }


    public AuthDeviceAccessTokenResponse refreshDeviceToken(String authorizationHeader, AuthDeviceRefreshRequest authDeviceRefreshRequest) {
        logger.info("\n\n ref_token: " + authDeviceRefreshRequest.refreshToken() + "\n\n");
        String deviceUUID = jwtService.extractDeviceUUID(authorizationHeader.substring(7));
        User deviceOwner = deviceService.getUserByDeviceUuIdOrThrow(deviceUUID);

        boolean isRefreshTokenValid = jwtService.isRefreshTokenValid(authDeviceRefreshRequest.refreshToken());

        if (!isRefreshTokenValid) {
            throw new InvalidTokenException("Invalid refresh token");
        }

        String deviceToken = jwtService.generateDeviceAccessToken(deviceOwner, deviceUUID);

        return AuthDeviceAccessTokenResponse.builder()
                .refreshedAccessToken(deviceToken)
                .build();
    }
}
