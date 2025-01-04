package com.kacper.iot_backend.reset_password_token;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kacper.iot_backend.config.SecurityConfig;
import com.kacper.iot_backend.jwt.JWTService;
import com.kacper.iot_backend.user.CustomUserDetailsService;
import com.kacper.iot_backend.utils.DefaultResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Import(SecurityConfig.class)
@WebMvcTest(ResetPasswordTokenController.class)
class ResetPasswordTokenControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ResetPasswordTokenService resetPasswordTokenService;

    @MockBean
    private JWTService jwtService;

    @MockBean
    private CustomUserDetailsService customUserDetailsService;

    @Autowired
    private ObjectMapper objectMapper;

    private final String mockedToken = "mocked-token";

    @BeforeEach
    void setUp() {
        UserDetails userDetails = User.builder()
                .username("test@example.com")
                .password("password")
                .authorities(Collections.emptyList())
                .build();

        Mockito.when(jwtService.extractUsername(mockedToken)).thenReturn(userDetails.getUsername());
        Mockito.when(customUserDetailsService.loadUserByUsername(userDetails.getUsername())).thenReturn(userDetails);
        Mockito.when(jwtService.isTokenValid(mockedToken, userDetails)).thenReturn(true);
    }

    @Test
    void shouldSendResetPasswordToken() throws Exception {
        ResetPasswordRequest request = new ResetPasswordRequest("test@example.com");
        DefaultResponse response = DefaultResponse.builder()
                .message("Reset password token has been sent")
                .build();

        Mockito.when(resetPasswordTokenService.sendResetPasswordToken(any(ResetPasswordRequest.class)))
                .thenReturn(response);

        mockMvc.perform(post("/api/v1/reset-password-token/send")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + mockedToken)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Reset password token has been sent")));
    }

    @Test
    void shouldVerifyResetPasswordToken() throws Exception {
        ResetPasswordVerifyRequest request = new ResetPasswordVerifyRequest(
                "test@example.com",
                "123456",
                "NewPassword1!"
        );
        DefaultResponse response = DefaultResponse.builder()
                .message("Password has been reset")
                .build();

        Mockito.when(resetPasswordTokenService.resetPasswordToken(any(ResetPasswordVerifyRequest.class)))
                .thenReturn(response);

        mockMvc.perform(post("/api/v1/reset-password-token/reset")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + mockedToken)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Password has been reset")));
    }

}
