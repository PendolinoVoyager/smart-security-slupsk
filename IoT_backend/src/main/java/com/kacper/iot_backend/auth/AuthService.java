package com.kacper.iot_backend.auth;

import com.kacper.iot_backend.activation_token.ActivationToken;
import com.kacper.iot_backend.activation_token.ActivationTokenService;
import com.kacper.iot_backend.device.Device;
import com.kacper.iot_backend.device.DeviceRepository;
import com.kacper.iot_backend.exception.DeviceOwnerMismatchException;
import com.kacper.iot_backend.exception.InvalidTokenException;
import com.kacper.iot_backend.exception.ResourceNotFoundException;
import com.kacper.iot_backend.exception.WrongLoginCredentialsException;
import com.kacper.iot_backend.jwt.JWTService;
import com.kacper.iot_backend.mail.MailService;
import com.kacper.iot_backend.user.CustomUserDetailsService;
import com.kacper.iot_backend.user.User;
import com.kacper.iot_backend.user.UserService;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.MalformedJwtException;
import jakarta.mail.MessagingException;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;



@Service
public class AuthService
{
    private final JWTService jwtService;
    private final AuthenticationManager authenticationManager;
    private final MailService mailService;
    private final UserService userService;
    private final ActivationTokenService activationTokenService;
    private final CustomUserDetailsService customUserDetailsService;
    private final DeviceRepository deviceRepository;

    @Value("${security.allowed-ips}")
    private List<String> allowedIps;

    public AuthService(
            JWTService jwtService,
            AuthenticationManager authenticationManager,
            MailService mailService,
            UserService userService, ActivationTokenService activationTokenService, CustomUserDetailsService customUserDetailsService,
            DeviceRepository deviceRepository
    ) {
        this.jwtService = jwtService;
        this.authenticationManager = authenticationManager;
        this.mailService = mailService;
        this.userService = userService;
        this.activationTokenService = activationTokenService;
        this.customUserDetailsService = customUserDetailsService;
        this.deviceRepository = deviceRepository;
    }

    public Boolean isAllowedServiceIp(String ip) {
        return allowedIps.contains(ip);
    }
    public AuthRegistrationResponse register(
            AuthRegistrationRequest authRegistrationRequest
    ) throws MessagingException {
        User user = userService.createUser(authRegistrationRequest);
        ActivationToken activationToken = activationTokenService.createActivationToken(user);

        userService.saveUserAndToken(user, activationToken);
        sendVerificationMail(user, activationToken);

        return AuthRegistrationResponse.builder()
                .name(user.getName())
                .lastName(user.getLast_name())
                .role(user.getRole())
                .email(user.getEmail())
                .build();
    }


    public AuthLoginResponse login(
            AuthLoginRequest authLoginRequest
    ) {
        User user = userService.getUserOrThrow(authLoginRequest.email());
        userService.isUserEnabled(user);
        authenticateUser(authLoginRequest);
        String token = jwtService.generateToken(user);

        return AuthLoginResponse.builder()
                .email(user.getEmail())
                .role(user.getRole())
                .token(token)
                .build();
    }

    private void authenticateUser(AuthLoginRequest authLoginRequest) {
        try {
            authenticationManager
                    .authenticate(
                            new UsernamePasswordAuthenticationToken(authLoginRequest.email(),
                                    authLoginRequest.password())
                    );
        } catch (BadCredentialsException e) {
            throw new WrongLoginCredentialsException("Wrong login credentials");
        } catch (RuntimeException e) {
            throw new RuntimeException("Critical error during authentication");
        }
    }

    public void sendVerificationMail(User user, ActivationToken activationToken) throws MessagingException {
        try {
            mailService.sendVerificationMail(user, activationToken.getToken());
        } catch (MessagingException e) {
            throw new MessagingException("Error during sending verification mail");
        }
    }

    public AudioServerAuthUserResponse checkUserValidForServiceAndCheckOwnership(String ipAddr, AudioServerAuthUserRequest request) {
        if (!isAllowedServiceIp(ipAddr)) {
            throw new ResourceNotFoundException("Service not found");
        }
        Claims claims = null;
        try {
            claims = jwtService.extractAllClaims(request.token());
        }
        catch (MalformedJwtException e) {
            throw new InvalidTokenException("Invalid token");
        }
        if (claims == null) {
            throw new InvalidTokenException("Invalid token");
        }
        User user = userService.getUserOrThrow(claims.getSubject());
        Device device = deviceRepository.findById(request.deviceId()).orElseThrow(() -> new ResourceNotFoundException("Device not found"));
        if (!device.getUser().equals(user)) {
            throw new DeviceOwnerMismatchException("User does not own this device.");
        }
        return AudioServerAuthUserResponse.builder()
                .valid(true)
                .email(claims.getSubject())
                .build();
    }

}
