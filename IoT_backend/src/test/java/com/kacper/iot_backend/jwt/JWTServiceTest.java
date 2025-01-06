package com.kacper.iot_backend.jwt;

import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.logging.Logger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JWTServiceTest
{
    private JWTService jwtService;
    private final static Logger logger = Logger.getLogger(JWTServiceTest.class.getName());
    @BeforeEach
    void setUp() throws Exception {
        Resource privateKeyResource = new ClassPathResource("secrets/private_key.pem");
        jwtService = new JWTService(privateKeyResource);
    }

    @Test
    void shouldGenerateToken() {
        UserDetails userDetails = new User("test@example.com", "Password.123", new ArrayList<>());

        String token = jwtService.generateToken(userDetails);
        logger.info("\nSuccessfully generated token: " + token);

        assertThat(token).isNotNull();
    }

    @Test
    void shouldGenerateRefreshToken() {
        UserDetails userDetails = new User("test@example.com", "Password.123", new ArrayList<>());
        HashMap<String, Object> claims = new HashMap<>();
        claims.put("role", "USER");

        String refreshToken = jwtService.generateRefreshToken(claims, userDetails);

        assertThat(refreshToken).isNotNull();
    }

    @Test
    void shouldExtractUsername() {
        UserDetails userDetails = new User("test@example.com", "Password.123", new ArrayList<>());
        String token = jwtService.generateToken(userDetails);

        String username = jwtService.extractUsername(token);

        assertThat(username).isEqualTo(userDetails.getUsername());
    }

    @Test
    void shouldValidateToken() {
        UserDetails userDetails = new User("test@example.com", "Password.123", new ArrayList<>());
        String token = jwtService.generateToken(userDetails);

        boolean isValid = jwtService.isTokenValid(token, userDetails);

        assertTrue(isValid);
    }

}
