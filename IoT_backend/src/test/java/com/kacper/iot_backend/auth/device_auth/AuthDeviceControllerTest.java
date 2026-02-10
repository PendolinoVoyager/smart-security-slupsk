package com.kacper.iot_backend.auth.device_auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class AuthDeviceControllerTest {

    @Mock
    private AuthDeviceService authDeviceService;

    @InjectMocks
    private AuthDeviceController authDeviceController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(authDeviceController).build();
        objectMapper = new ObjectMapper();
    }

    // ===================== AUTH DEVICE ENDPOINT TESTS =====================

    @Test
    void shouldAuthenticateDeviceSuccessfully() throws Exception {
        // Given
        AuthDeviceRequest request = new AuthDeviceRequest(
                "device-uuid-123",
                "test@example.com",
                "password123"
        );

        AuthDeviceResponse response = AuthDeviceResponse.builder()
                .token("device-access-token")
                .refreshToken("device-refresh-token")
                .build();

        when(authDeviceService.authenticateDevice(any(AuthDeviceRequest.class))).thenReturn(response);

        // When & Then
        mockMvc.perform(post("/api/v1/auth/device")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("device-access-token"))
                .andExpect(jsonPath("$.refreshToken").value("device-refresh-token"));

        verify(authDeviceService).authenticateDevice(any(AuthDeviceRequest.class));
    }

    @Test
    void shouldCallServiceWithCorrectRequest() throws Exception {
        // Given
        AuthDeviceRequest request = new AuthDeviceRequest(
                "test-uuid",
                "user@test.com",
                "testpass"
        );

        AuthDeviceResponse response = AuthDeviceResponse.builder()
                .token("token")
                .refreshToken("refresh")
                .build();

        when(authDeviceService.authenticateDevice(any(AuthDeviceRequest.class))).thenReturn(response);

        // When
        mockMvc.perform(post("/api/v1/auth/device")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        // Then
        verify(authDeviceService).authenticateDevice(argThat(req ->
                req.deviceUuid().equals("test-uuid") &&
                req.email().equals("user@test.com") &&
                req.password().equals("testpass")
        ));
    }

    @Test
    void shouldReturnJsonResponse() throws Exception {
        // Given
        AuthDeviceRequest request = new AuthDeviceRequest("uuid", "email@test.com", "pass");
        AuthDeviceResponse response = AuthDeviceResponse.builder()
                .token("token")
                .refreshToken("refresh")
                .build();

        when(authDeviceService.authenticateDevice(any())).thenReturn(response);

        // When & Then
        mockMvc.perform(post("/api/v1/auth/device")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    // ===================== REFRESH DEVICE TOKEN ENDPOINT TESTS =====================

    @Test
    void shouldRefreshDeviceTokenSuccessfully() throws Exception {
        // Given
        AuthDeviceRefreshRequest request = AuthDeviceRefreshRequest.builder()
                .refreshToken("valid-refresh-token")
                .build();

        AuthDeviceAccessTokenResponse response = AuthDeviceAccessTokenResponse.builder()
                .refreshedAccessToken("new-access-token")
                .build();

        when(authDeviceService.refreshDeviceToken(anyString(), any(AuthDeviceRefreshRequest.class)))
                .thenReturn(response);

        // When & Then
        mockMvc.perform(post("/api/v1/auth/device/refresh")
                        .header("Authorization", "Bearer old-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.refreshedAccessToken").value("new-access-token"));

        verify(authDeviceService).refreshDeviceToken(anyString(), any(AuthDeviceRefreshRequest.class));
    }

    @Test
    void shouldPassAuthorizationHeaderToService() throws Exception {
        // Given
        AuthDeviceRefreshRequest request = AuthDeviceRefreshRequest.builder()
                .refreshToken("refresh")
                .build();

        AuthDeviceAccessTokenResponse response = AuthDeviceAccessTokenResponse.builder()
                .refreshedAccessToken("new-token")
                .build();

        when(authDeviceService.refreshDeviceToken(anyString(), any())).thenReturn(response);

        // When
        mockMvc.perform(post("/api/v1/auth/device/refresh")
                        .header("Authorization", "Bearer my-special-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        // Then
        verify(authDeviceService).refreshDeviceToken(eq("Bearer my-special-token"), any());
    }

    @Test
    void shouldPassRefreshTokenInRequestBody() throws Exception {
        // Given
        AuthDeviceRefreshRequest request = AuthDeviceRefreshRequest.builder()
                .refreshToken("my-refresh-token-value")
                .build();

        AuthDeviceAccessTokenResponse response = AuthDeviceAccessTokenResponse.builder()
                .refreshedAccessToken("new-token")
                .build();

        when(authDeviceService.refreshDeviceToken(anyString(), any())).thenReturn(response);

        // When
        mockMvc.perform(post("/api/v1/auth/device/refresh")
                        .header("Authorization", "Bearer token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        // Then
        verify(authDeviceService).refreshDeviceToken(anyString(), argThat(req ->
                req.refreshToken().equals("my-refresh-token-value")
        ));
    }

    // ===================== REQUEST BODY TESTS =====================

    @Test
    void shouldHandleRequestWithAllFields() throws Exception {
        // Given
        String requestJson = """
                {
                    "deviceUuid": "uuid-123",
                    "email": "test@test.com",
                    "password": "secret123"
                }
                """;

        AuthDeviceResponse response = AuthDeviceResponse.builder()
                .token("token")
                .refreshToken("refresh")
                .build();

        when(authDeviceService.authenticateDevice(any())).thenReturn(response);

        // When & Then
        mockMvc.perform(post("/api/v1/auth/device")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isOk());
    }

    @Test
    void shouldHandleRefreshRequestWithRefreshToken() throws Exception {
        // Given
        String requestJson = """
                {
                    "refreshToken": "refresh-token-value"
                }
                """;

        AuthDeviceAccessTokenResponse response = AuthDeviceAccessTokenResponse.builder()
                .refreshedAccessToken("new-token")
                .build();

        when(authDeviceService.refreshDeviceToken(anyString(), any())).thenReturn(response);

        // When & Then
        mockMvc.perform(post("/api/v1/auth/device/refresh")
                        .header("Authorization", "Bearer token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isOk());
    }

    // ===================== RESPONSE STRUCTURE TESTS =====================

    @Test
    void shouldReturnBothTokenAndRefreshTokenInAuthResponse() throws Exception {
        // Given
        AuthDeviceRequest request = new AuthDeviceRequest("uuid", "email@test.com", "pass");
        AuthDeviceResponse response = AuthDeviceResponse.builder()
                .token("access-token-12345")
                .refreshToken("refresh-token-67890")
                .build();

        when(authDeviceService.authenticateDevice(any())).thenReturn(response);

        // When & Then
        mockMvc.perform(post("/api/v1/auth/device")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists())
                .andExpect(jsonPath("$.refreshToken").exists())
                .andExpect(jsonPath("$.token").value("access-token-12345"))
                .andExpect(jsonPath("$.refreshToken").value("refresh-token-67890"));
    }

    @Test
    void shouldReturnOnlyRefreshedAccessTokenInRefreshResponse() throws Exception {
        // Given
        AuthDeviceRefreshRequest request = AuthDeviceRefreshRequest.builder()
                .refreshToken("refresh")
                .build();

        AuthDeviceAccessTokenResponse response = AuthDeviceAccessTokenResponse.builder()
                .refreshedAccessToken("new-access-token-abc")
                .build();

        when(authDeviceService.refreshDeviceToken(anyString(), any())).thenReturn(response);

        // When & Then
        mockMvc.perform(post("/api/v1/auth/device/refresh")
                        .header("Authorization", "Bearer old-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.refreshedAccessToken").exists())
                .andExpect(jsonPath("$.refreshedAccessToken").value("new-access-token-abc"));
    }

    // ===================== ENDPOINT PATH TESTS =====================

    @Test
    void shouldRespondToCorrectAuthEndpoint() throws Exception {
        // Given
        AuthDeviceRequest request = new AuthDeviceRequest("uuid", "email@test.com", "pass");
        AuthDeviceResponse response = AuthDeviceResponse.builder()
                .token("token")
                .refreshToken("refresh")
                .build();

        when(authDeviceService.authenticateDevice(any())).thenReturn(response);

        // When & Then
        mockMvc.perform(post("/api/v1/auth/device")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    void shouldRespondToCorrectRefreshEndpoint() throws Exception {
        // Given
        AuthDeviceRefreshRequest request = AuthDeviceRefreshRequest.builder()
                .refreshToken("refresh")
                .build();

        AuthDeviceAccessTokenResponse response = AuthDeviceAccessTokenResponse.builder()
                .refreshedAccessToken("token")
                .build();

        when(authDeviceService.refreshDeviceToken(anyString(), any())).thenReturn(response);

        // When & Then
        mockMvc.perform(post("/api/v1/auth/device/refresh")
                        .header("Authorization", "Bearer token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    // ===================== VERIFICATION TESTS =====================

    @Test
    void shouldCallAuthenticateDeviceExactlyOnce() throws Exception {
        // Given
        AuthDeviceRequest request = new AuthDeviceRequest("uuid", "email@test.com", "pass");
        AuthDeviceResponse response = AuthDeviceResponse.builder()
                .token("token")
                .refreshToken("refresh")
                .build();

        when(authDeviceService.authenticateDevice(any())).thenReturn(response);

        // When
        mockMvc.perform(post("/api/v1/auth/device")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        // Then
        verify(authDeviceService, times(1)).authenticateDevice(any());
    }

    @Test
    void shouldCallRefreshDeviceTokenExactlyOnce() throws Exception {
        // Given
        AuthDeviceRefreshRequest request = AuthDeviceRefreshRequest.builder()
                .refreshToken("refresh")
                .build();

        AuthDeviceAccessTokenResponse response = AuthDeviceAccessTokenResponse.builder()
                .refreshedAccessToken("token")
                .build();

        when(authDeviceService.refreshDeviceToken(anyString(), any())).thenReturn(response);

        // When
        mockMvc.perform(post("/api/v1/auth/device/refresh")
                        .header("Authorization", "Bearer token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        // Then
        verify(authDeviceService, times(1)).refreshDeviceToken(anyString(), any());
    }
}

