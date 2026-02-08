package com.kacper.iot_backend.webSocket;

import com.kacper.iot_backend.jwt.JWTService;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * Implementation of JwtVerifier that uses the existing JWTService.
 * This component validates JWT tokens during WebSocket handshake.
 */
@Component
public class JwtVerifierImpl implements WebSocketConfig.JwtVerifier {

    private final JWTService jwtService;

    public JwtVerifierImpl(JWTService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    public Optional<String> validateAndExtractUsername(String token) {
        try {
            if (jwtService.isTokenExpired(token)) {
                return Optional.empty();
            }
            String username = jwtService.extractUsername(token);
            return Optional.ofNullable(username);
        } catch (Exception e) {
            return Optional.empty();
        }
    }
}

