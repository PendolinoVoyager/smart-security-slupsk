package com.kacper.iot_backend.activation_token;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kacper.iot_backend.config.SecurityConfig;
import com.kacper.iot_backend.exception.ResourceNotFoundException;
import com.kacper.iot_backend.exception.WrongLoginCredentialsException;
import com.kacper.iot_backend.jwt.JWTService;
import com.kacper.iot_backend.user.CustomUserDetailsService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ActivationTokenController.class)
@Import(SecurityConfig.class)
class ActivationTokenControllerTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(ActivationTokenControllerTest.class);

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ActivationTokenService activationTokenService;

    @MockBean
    private JWTService jwtService;

    @MockBean
    private CustomUserDetailsService customUserDetailsService;

    @Test
    void shouldEnableUserWithValidActivationToken() throws Exception {
        UserDetails userDetails = User.builder()
                .username("test@example.com")
                .password("password")
                .authorities(Collections.emptyList())
                .build();

        String mockedToken = "mocked-token";

        when(jwtService.generateToken(any(UserDetails.class))).thenReturn(mockedToken);
        when(jwtService.extractUsername(mockedToken)).thenReturn(userDetails.getUsername());
        when(customUserDetailsService.loadUserByUsername(userDetails.getUsername())).thenReturn(userDetails);
        when(jwtService.isTokenValid(mockedToken, userDetails)).thenReturn(true);

        ActivationTokenRequest request = new ActivationTokenRequest("test@example.com", "1234");
        doNothing().when(activationTokenService).enableUser(Mockito.any(ActivationTokenRequest.class));

        mockMvc.perform(post("/api/v1/activation-token/verify")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .header("Authorization", "Bearer " + mockedToken))
                .andExpect(status().isOk());

        verify(activationTokenService, times(1)).enableUser(Mockito.any(ActivationTokenRequest.class));
    }

    @Test
    void shouldReturnBadRequestWhenRequestIsInvalid() throws Exception {
        UserDetails userDetails = User.builder()
                .username("test@example.com")
                .password("password")
                .authorities(Collections.emptyList())
                .build();

        String mockedToken = "mocked-token";

        when(jwtService.generateToken(any(UserDetails.class))).thenReturn(mockedToken);
        when(jwtService.extractUsername(mockedToken)).thenReturn(userDetails.getUsername());
        when(customUserDetailsService.loadUserByUsername(userDetails.getUsername())).thenReturn(userDetails);
        when(jwtService.isTokenValid(mockedToken, userDetails)).thenReturn(true);

        ActivationTokenRequest request = new ActivationTokenRequest("", "");

        mockMvc.perform(post("/api/v1/activation-token/verify")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .header("Authorization", "Bearer " + mockedToken))
                .andExpect(status().isBadRequest());

        verify(activationTokenService, never()).enableUser(any(ActivationTokenRequest.class));
    }

    @Test
    void shouldReturnNotFoundWhenServiceThrowsResourceNotFoundException() throws Exception {
        UserDetails userDetails = User.builder()
                .username("test@example.com")
                .password("password")
                .authorities(Collections.emptyList())
                .build();

        String mockedToken = "mocked-token";

        when(jwtService.generateToken(any(UserDetails.class))).thenReturn(mockedToken);
        when(jwtService.extractUsername(mockedToken)).thenReturn(userDetails.getUsername());
        when(customUserDetailsService.loadUserByUsername(userDetails.getUsername())).thenReturn(userDetails);
        when(jwtService.isTokenValid(mockedToken, userDetails)).thenReturn(true);

        ActivationTokenRequest request = new ActivationTokenRequest("test@example.com", "1234");
        doThrow(new ResourceNotFoundException("Activation token not found"))
                .when(activationTokenService).enableUser(Mockito.any(ActivationTokenRequest.class));

        mockMvc.perform(post("/api/v1/activation-token/verify")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .header("Authorization", "Bearer " + mockedToken))
                .andExpect(status().isConflict());

        verify(activationTokenService, times(1)).enableUser(Mockito.any(ActivationTokenRequest.class));
    }

    @Test
    void shouldReturnUnauthorizedWhenServiceThrowsWrongLoginCredentialsException() throws Exception {
        UserDetails userDetails = User.builder()
                .username("test@example.com")
                .password("password")
                .authorities(Collections.emptyList())
                .build();

        String mockedToken = "mocked-token";

        when(jwtService.generateToken(any(UserDetails.class))).thenReturn(mockedToken);
        when(jwtService.extractUsername(mockedToken)).thenReturn(userDetails.getUsername());
        when(customUserDetailsService.loadUserByUsername(userDetails.getUsername())).thenReturn(userDetails);
        when(jwtService.isTokenValid(mockedToken, userDetails)).thenReturn(true);

        ActivationTokenRequest request = new ActivationTokenRequest("test@example.com", "1234");
        doThrow(new WrongLoginCredentialsException("Invalid credentials"))
                .when(activationTokenService).enableUser(Mockito.any(ActivationTokenRequest.class));

        mockMvc.perform(post("/api/v1/activation-token/verify")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .header("Authorization", "Bearer " + mockedToken))
                .andExpect(status().isUnauthorized());

        verify(activationTokenService, times(1)).enableUser(Mockito.any(ActivationTokenRequest.class));
    }
}
