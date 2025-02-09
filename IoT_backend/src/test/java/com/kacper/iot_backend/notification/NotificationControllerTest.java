package com.kacper.iot_backend.notification;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kacper.iot_backend.config.SecurityConfig;
import com.kacper.iot_backend.jwt.JWTService;
import com.kacper.iot_backend.user.CustomUserDetailsService;
import com.kacper.iot_backend.utils.DefaultResponse;
import org.junit.jupiter.api.Test;
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

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Import(SecurityConfig.class)
@WebMvcTest(NotificationController.class)
class NotificationControllerTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(NotificationControllerTest.class);

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private NotificationService notificationService;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private JWTService jwtService;

    @MockBean
    private CustomUserDetailsService customUserDetailsService;

    @Test
    void shouldReturnNotificationsForUser() throws Exception {
        LOGGER.info("Started test shouldReturnNotificationsForUser");

        UserDetails userDetails = User.builder()
                .username("test@example.com")
                .password("password")
                .authorities(Collections.emptyList())
                .build();

        String mockedToken = "mocked-token";
        when(jwtService.extractUsername(mockedToken)).thenReturn(userDetails.getUsername());
        when(customUserDetailsService.loadUserByUsername(userDetails.getUsername())).thenReturn(userDetails);
        when(jwtService.isTokenValid(mockedToken, userDetails)).thenReturn(true);

        NotificationPageResponse response = new NotificationPageResponse(0, 1, Collections.emptyList());
        when(notificationService.getNotifications(any(UserDetails.class), any())).thenReturn(response);

        LOGGER.info("Starting API test /api/v1/notification");
        mockMvc.perform(get("/api/v1/notification")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + mockedToken))
                .andExpect(status().isOk());

        verify(notificationService, times(1)).getNotifications(any(UserDetails.class), any());
        LOGGER.info("Finished test shouldReturnNotificationsForUser");
    }

    @Test
    void shouldAddNotificationSuccessfully() throws Exception {
        LOGGER.info("Started test shouldAddNotificationSuccessfully");

        String mockedToken = "mocked-token";
        String userEmail = "test@example.com";

        UserDetails userDetails = User.builder()
                .username(userEmail)
                .password("password")
                .authorities(Collections.emptyList())
                .build();

        NotificationRequest request = new NotificationRequest("INFO", "New Notification");
        DefaultResponse response = new DefaultResponse("Notification added successfully");

        when(jwtService.extractUsername(mockedToken)).thenReturn(userEmail);
        when(customUserDetailsService.loadUserByUsername(userEmail)).thenReturn(userDetails);
        when(jwtService.isTokenValid(mockedToken, userDetails)).thenReturn(true);

        when(notificationService.addNotification(eq("Bearer " + mockedToken), any(NotificationRequest.class)))
                .thenReturn(response);

        LOGGER.info("Starting API test /api/v1/notification/");
        mockMvc.perform(post("/api/v1/notification/")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + mockedToken)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Notification added successfully"));

        verify(notificationService, times(1)).addNotification(eq("Bearer " + mockedToken), any(NotificationRequest.class));
        LOGGER.info("Finished test shouldAddNotificationSuccessfully");
    }


    @Test
    void shouldReturnForbiddenWhenTokenIsInvalid() throws Exception {
        LOGGER.info("Started test shouldReturnForbiddenWhenTokenIsInvalid");

        String invalidToken = "invalid-token";

        UserDetails invalidUserDetails = User.builder()
                .username("unknown@example.com")
                .password("invalid-password")
                .authorities(Collections.emptyList())
                .build();

        when(jwtService.extractUsername(invalidToken)).thenReturn(invalidUserDetails.getUsername());
        when(customUserDetailsService.loadUserByUsername(invalidUserDetails.getUsername())).thenReturn(invalidUserDetails);
        when(jwtService.isTokenValid(invalidToken, invalidUserDetails)).thenReturn(false);

        LOGGER.info("Starting API test /api/v1/notification/ with invalid token");
        mockMvc.perform(get("/api/v1/notification")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + invalidToken))
                .andExpect(status().isForbidden());

        verify(notificationService, never()).getNotifications(any(UserDetails.class), any());
        LOGGER.info("Finished test shouldReturnForbiddenWhenTokenIsInvalid");
    }
}
