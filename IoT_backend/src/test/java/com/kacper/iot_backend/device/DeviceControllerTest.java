package com.kacper.iot_backend.device;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kacper.iot_backend.config.SecurityConfig;
import com.kacper.iot_backend.jwt.JWTService;
import com.kacper.iot_backend.user.CustomUserDetailsService;
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
import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Import(SecurityConfig.class)
@WebMvcTest(DeviceController.class)
class DeviceControllerTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(DeviceControllerTest.class);

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private DeviceService deviceService;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private JWTService jwtService;

    @MockBean
    private CustomUserDetailsService customUserDetailsService;

    @Test
    void shouldReturnUserDevicesWhenDevicesExist() throws Exception {
        LOGGER.info("Started test shouldReturnUserDevicesWhenDevicesExist");

        UserDetails userDetails = User.builder()
                .username("test@example.com")
                .password("password")
                .authorities(Collections.emptyList())
                .build();

        String mockedToken = "mocked-token";
        when(jwtService.extractUsername(mockedToken)).thenReturn(userDetails.getUsername());
        when(customUserDetailsService.loadUserByUsername(userDetails.getUsername())).thenReturn(userDetails);
        when(jwtService.isTokenValid(mockedToken, userDetails)).thenReturn(true);

        DevicesListResponse response1 = new DevicesListResponse(1, "Grove street 1", "Device1", "uuid1");
        DevicesListResponse response2 = new DevicesListResponse(2, "Grove street 2", "Device2", "uuid2");

        when(deviceService.getUserDevices(any(UserDetails.class))).thenReturn(List.of(response1, response2));

        LOGGER.info("Starting API test /api/v1/device/");
        mockMvc.perform(get("/api/v1/device/")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + mockedToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].deviceName").value("Device1"))
                .andExpect(jsonPath("$[1].uuid").value("uuid2"));

        verify(deviceService, times(1)).getUserDevices(any(UserDetails.class));
        LOGGER.info("Finished test shouldReturnUserDevicesWhenDevicesExist");
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

        LOGGER.info("Starting API test /api/v1/device/ with invalid token");
        mockMvc.perform(get("/api/v1/device/")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + invalidToken))
                .andExpect(status().isForbidden());

        verify(deviceService, never()).getUserDevices(any(UserDetails.class));
        LOGGER.info("Finished test shouldReturnForbiddenWhenTokenIsInvalid");
    }

}
