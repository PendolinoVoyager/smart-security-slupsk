package com.kacper.iot_backend.auth;

import com.kacper.iot_backend.activation_token.ActivationToken;
import com.kacper.iot_backend.activation_token.ActivationTokenService;
import com.kacper.iot_backend.exception.InvalidTokenException;
import com.kacper.iot_backend.exception.WrongLoginCredentialsException;
import com.kacper.iot_backend.jwt.JWTService;
import com.kacper.iot_backend.mail.MailService;
import com.kacper.iot_backend.user.CustomUserDetailsService;
import com.kacper.iot_backend.user.User;
import com.kacper.iot_backend.user.UserService;
import jakarta.mail.MessagingException;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
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


    public AuthService(
            JWTService jwtService,
            AuthenticationManager authenticationManager,
            MailService mailService,
            UserService userService, ActivationTokenService activationTokenService, CustomUserDetailsService customUserDetailsService
    ) {
        this.jwtService = jwtService;
        this.authenticationManager = authenticationManager;
        this.mailService = mailService;
        this.userService = userService;
        this.activationTokenService = activationTokenService;
        this.customUserDetailsService = customUserDetailsService;
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

    public boolean isTokenValid(IsTokenValidRequest isTokenValidRequest) {
        var tokenType = isTokenValidRequest.tokenType();

        if (tokenType.equals("Device")) {
            var isDeviceToken = jwtService.isDeviceToken(isTokenValidRequest.token());
            if (!isDeviceToken) {
                // zmienic na inny exception co rzuca bad request
                throw new InvalidTokenException("TokenType was set to Device but provided token is not a device token");
            }
        }

        var userDetails = customUserDetailsService.loadUserByUsername(
                jwtService.extractUsername(isTokenValidRequest.token())
        );

        return jwtService.isTokenValid(isTokenValidRequest.token(), userDetails);
    }
}
