package com.kacper.iot_backend.auth.device_auth;

import com.kacper.iot_backend.device.Device;
import com.kacper.iot_backend.device.DeviceService;
import com.kacper.iot_backend.exception.DeviceOwnerMismatchException;
import com.kacper.iot_backend.exception.InvalidTokenException;
import com.kacper.iot_backend.exception.WrongLoginCredentialsException;
import com.kacper.iot_backend.jwt.JWTService;
import com.kacper.iot_backend.user.User;
import com.kacper.iot_backend.user.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthDeviceServiceTest {

    @Mock
    private DeviceService deviceService;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JWTService jwtService;

    @Mock
    private UserService userService;

    @InjectMocks
    private AuthDeviceService authDeviceService;

    private User user;
    private Device device;
    private AuthDeviceRequest authDeviceRequest;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .id(1)
                .name("Test")
                .last_name("User")
                .email("test@example.com")
                .password("encodedPassword")
                .role("USER")
                .enabled(true)
                .created_at(new Date())
                .build();

        device = Device.builder()
                .id(1)
                .uuid("device-uuid-123")
                .deviceName("Test Device")
                .address("Test Address")
                .user(user)
                .build();

        authDeviceRequest = new AuthDeviceRequest(
                "device-uuid-123",
                "test@example.com",
                "password123"
        );
    }

    // ===================== AUTHENTICATE DEVICE TESTS =====================

    @Test
    void shouldAuthenticateDeviceSuccessfully() {
        // Given
        when(deviceService.getByUuid("device-uuid-123")).thenReturn(device);
        when(userService.getUserOrThrow("test@example.com")).thenReturn(user);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(new UsernamePasswordAuthenticationToken(user, null));
        when(jwtService.generateDeviceAccessToken(user, device)).thenReturn("device-access-token");
        when(jwtService.generateDeviceRefreshToken(user)).thenReturn("device-refresh-token");

        // When
        AuthDeviceResponse response = authDeviceService.authenticateDevice(authDeviceRequest);

        // Then
        assertNotNull(response);
        assertEquals("device-access-token", response.token());
        assertEquals("device-refresh-token", response.refreshToken());

        verify(deviceService).getByUuid("device-uuid-123");
        verify(userService).getUserOrThrow("test@example.com");
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(jwtService).generateDeviceAccessToken(user, device);
        verify(jwtService).generateDeviceRefreshToken(user);
    }

    @Test
    void shouldThrowExceptionWhenDeviceOwnerMismatch() {
        // Given
        User differentUser = User.builder()
                .id(2)
                .name("Different")
                .last_name("User")
                .email("different@example.com")
                .password("password")
                .role("USER")
                .enabled(true)
                .created_at(new Date())
                .build();

        when(deviceService.getByUuid("device-uuid-123")).thenReturn(device);
        when(userService.getUserOrThrow("test@example.com")).thenReturn(differentUser);

        // When & Then
        assertThrows(DeviceOwnerMismatchException.class,
            () -> authDeviceService.authenticateDevice(authDeviceRequest));

        verify(deviceService).getByUuid("device-uuid-123");
        verify(userService).getUserOrThrow("test@example.com");
        verify(authenticationManager, never()).authenticate(any());
    }

    @Test
    void shouldThrowExceptionWhenBadCredentials() {
        // Given
        when(deviceService.getByUuid("device-uuid-123")).thenReturn(device);
        when(userService.getUserOrThrow("test@example.com")).thenReturn(user);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Bad credentials"));

        // When & Then
        assertThrows(WrongLoginCredentialsException.class,
            () -> authDeviceService.authenticateDevice(authDeviceRequest));

        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(jwtService, never()).generateDeviceAccessToken(any(), any());
    }

    @Test
    void shouldThrowRuntimeExceptionOnCriticalError() {
        // Given
        when(deviceService.getByUuid("device-uuid-123")).thenReturn(device);
        when(userService.getUserOrThrow("test@example.com")).thenReturn(user);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new RuntimeException("Some critical error"));

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class,
            () -> authDeviceService.authenticateDevice(authDeviceRequest));
        assertEquals("Critical error during authentication", exception.getMessage());
    }

    @Test
    void shouldAuthenticateWithCorrectCredentials() {
        // Given
        when(deviceService.getByUuid("device-uuid-123")).thenReturn(device);
        when(userService.getUserOrThrow("test@example.com")).thenReturn(user);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(new UsernamePasswordAuthenticationToken(user, null));
        when(jwtService.generateDeviceAccessToken(user, device)).thenReturn("token");
        when(jwtService.generateDeviceRefreshToken(user)).thenReturn("refresh");

        // When
        authDeviceService.authenticateDevice(authDeviceRequest);

        // Then
        verify(authenticationManager).authenticate(
                argThat(auth ->
                    auth.getPrincipal().equals("test@example.com") &&
                    auth.getCredentials().equals("password123")
                )
        );
    }

    // ===================== REFRESH DEVICE TOKEN TESTS =====================

    @Test
    void shouldRefreshDeviceTokenSuccessfully() {
        // Given
        String authHeader = "Bearer old-device-token";
        AuthDeviceRefreshRequest refreshRequest = AuthDeviceRefreshRequest.builder()
                .refreshToken("valid-refresh-token")
                .build();

        when(jwtService.extractDeviceUUID("old-device-token")).thenReturn("device-uuid-123");
        when(deviceService.getByUuid("device-uuid-123")).thenReturn(device);
        when(jwtService.isRefreshTokenValid("valid-refresh-token")).thenReturn(true);
        when(jwtService.generateDeviceAccessToken(user, device)).thenReturn("new-device-token");

        // When
        AuthDeviceAccessTokenResponse response = authDeviceService.refreshDeviceToken(authHeader, refreshRequest);

        // Then
        assertNotNull(response);
        assertEquals("new-device-token", response.refreshedAccessToken());

        verify(jwtService).extractDeviceUUID("old-device-token");
        verify(deviceService).getByUuid("device-uuid-123");
        verify(jwtService).isRefreshTokenValid("valid-refresh-token");
        verify(jwtService).generateDeviceAccessToken(user, device);
    }

    @Test
    void shouldThrowExceptionWhenRefreshTokenInvalid() {
        // Given
        String authHeader = "Bearer old-device-token";
        AuthDeviceRefreshRequest refreshRequest = AuthDeviceRefreshRequest.builder()
                .refreshToken("invalid-refresh-token")
                .build();

        when(jwtService.extractDeviceUUID("old-device-token")).thenReturn("device-uuid-123");
        when(deviceService.getByUuid("device-uuid-123")).thenReturn(device);
        when(jwtService.isRefreshTokenValid("invalid-refresh-token")).thenReturn(false);

        // When & Then
        assertThrows(InvalidTokenException.class,
            () -> authDeviceService.refreshDeviceToken(authHeader, refreshRequest));

        verify(jwtService, never()).generateDeviceAccessToken(any(), any());
    }

    @Test
    void shouldExtractDeviceUUIDFromAuthHeader() {
        // Given
        String authHeader = "Bearer some-token-value";
        AuthDeviceRefreshRequest refreshRequest = AuthDeviceRefreshRequest.builder()
                .refreshToken("refresh-token")
                .build();

        when(jwtService.extractDeviceUUID("some-token-value")).thenReturn("device-uuid-123");
        when(deviceService.getByUuid("device-uuid-123")).thenReturn(device);
        when(jwtService.isRefreshTokenValid("refresh-token")).thenReturn(true);
        when(jwtService.generateDeviceAccessToken(user, device)).thenReturn("new-token");

        // When
        authDeviceService.refreshDeviceToken(authHeader, refreshRequest);

        // Then
        verify(jwtService).extractDeviceUUID("some-token-value");
    }

    @Test
    void shouldGetDeviceOwnerFromDevice() {
        // Given
        String authHeader = "Bearer token";
        AuthDeviceRefreshRequest refreshRequest = AuthDeviceRefreshRequest.builder()
                .refreshToken("refresh-token")
                .build();

        when(jwtService.extractDeviceUUID("token")).thenReturn("device-uuid-123");
        when(deviceService.getByUuid("device-uuid-123")).thenReturn(device);
        when(jwtService.isRefreshTokenValid("refresh-token")).thenReturn(true);
        when(jwtService.generateDeviceAccessToken(user, device)).thenReturn("new-token");

        // When
        authDeviceService.refreshDeviceToken(authHeader, refreshRequest);

        // Then
        verify(jwtService).generateDeviceAccessToken(user, device);
    }

    // ===================== EDGE CASE TESTS =====================

    @Test
    void shouldHandleEmptyDeviceUuid() {
        // Given
        AuthDeviceRequest requestWithEmptyUuid = new AuthDeviceRequest(
                "",
                "test@example.com",
                "password123"
        );

        when(deviceService.getByUuid("")).thenThrow(new RuntimeException("Device not found"));

        // When & Then
        assertThrows(RuntimeException.class,
            () -> authDeviceService.authenticateDevice(requestWithEmptyUuid));
    }

    @Test
    void shouldHandleNullDeviceUuid() {
        // Given
        AuthDeviceRequest requestWithNullUuid = new AuthDeviceRequest(
                null,
                "test@example.com",
                "password123"
        );

        when(deviceService.getByUuid(null)).thenThrow(new RuntimeException("Device not found"));

        // When & Then
        assertThrows(RuntimeException.class,
            () -> authDeviceService.authenticateDevice(requestWithNullUuid));
    }

    @Test
    void shouldHandleDeviceWithDifferentOwner() {
        // Given
        User otherOwner = User.builder()
                .id(99)
                .name("Other")
                .last_name("Owner")
                .email("other@example.com")
                .password("password")
                .role("USER")
                .enabled(true)
                .created_at(new Date())
                .build();

        Device deviceWithOtherOwner = Device.builder()
                .id(1)
                .uuid("device-uuid-123")
                .deviceName("Test Device")
                .address("Test Address")
                .user(otherOwner)
                .build();

        when(deviceService.getByUuid("device-uuid-123")).thenReturn(deviceWithOtherOwner);
        when(userService.getUserOrThrow("test@example.com")).thenReturn(user);

        // When & Then
        assertThrows(DeviceOwnerMismatchException.class,
            () -> authDeviceService.authenticateDevice(authDeviceRequest));
    }

    @Test
    void shouldGenerateUniqueTokensForDifferentDevices() {
        // Given
        Device device2 = Device.builder()
                .id(2)
                .uuid("device-uuid-456")
                .deviceName("Second Device")
                .address("Second Address")
                .user(user)
                .build();

        AuthDeviceRequest request1 = new AuthDeviceRequest("device-uuid-123", "test@example.com", "password123");
        AuthDeviceRequest request2 = new AuthDeviceRequest("device-uuid-456", "test@example.com", "password123");

        when(deviceService.getByUuid("device-uuid-123")).thenReturn(device);
        when(deviceService.getByUuid("device-uuid-456")).thenReturn(device2);
        when(userService.getUserOrThrow("test@example.com")).thenReturn(user);
        when(authenticationManager.authenticate(any())).thenReturn(new UsernamePasswordAuthenticationToken(user, null));
        when(jwtService.generateDeviceAccessToken(user, device)).thenReturn("token-for-device-1");
        when(jwtService.generateDeviceAccessToken(user, device2)).thenReturn("token-for-device-2");
        when(jwtService.generateDeviceRefreshToken(user)).thenReturn("refresh-token");

        // When
        AuthDeviceResponse response1 = authDeviceService.authenticateDevice(request1);
        AuthDeviceResponse response2 = authDeviceService.authenticateDevice(request2);

        // Then
        assertNotEquals(response1.token(), response2.token());
    }

    // ===================== AUTHORIZATION HEADER TESTS =====================

    @Test
    void shouldHandleAuthorizationHeaderWithBearer() {
        // Given
        String authHeader = "Bearer valid-token-here";
        AuthDeviceRefreshRequest refreshRequest = AuthDeviceRefreshRequest.builder()
                .refreshToken("refresh")
                .build();

        when(jwtService.extractDeviceUUID("valid-token-here")).thenReturn("device-uuid-123");
        when(deviceService.getByUuid("device-uuid-123")).thenReturn(device);
        when(jwtService.isRefreshTokenValid("refresh")).thenReturn(true);
        when(jwtService.generateDeviceAccessToken(user, device)).thenReturn("new-token");

        // When
        AuthDeviceAccessTokenResponse response = authDeviceService.refreshDeviceToken(authHeader, refreshRequest);

        // Then
        assertNotNull(response);
        verify(jwtService).extractDeviceUUID("valid-token-here");
    }

    @Test
    void shouldExtractTokenAfterBearerPrefix() {
        // Given
        String authHeader = "Bearer abc123xyz";
        AuthDeviceRefreshRequest refreshRequest = AuthDeviceRefreshRequest.builder()
                .refreshToken("refresh")
                .build();

        when(jwtService.extractDeviceUUID("abc123xyz")).thenReturn("device-uuid-123");
        when(deviceService.getByUuid("device-uuid-123")).thenReturn(device);
        when(jwtService.isRefreshTokenValid("refresh")).thenReturn(true);
        when(jwtService.generateDeviceAccessToken(user, device)).thenReturn("new-token");

        // When
        authDeviceService.refreshDeviceToken(authHeader, refreshRequest);

        // Then
        verify(jwtService).extractDeviceUUID("abc123xyz");
    }

    // ===================== VERIFICATION TESTS =====================

    @Test
    void shouldVerifyAllDependenciesAreCalled() {
        // Given
        when(deviceService.getByUuid("device-uuid-123")).thenReturn(device);
        when(userService.getUserOrThrow("test@example.com")).thenReturn(user);
        when(authenticationManager.authenticate(any())).thenReturn(new UsernamePasswordAuthenticationToken(user, null));
        when(jwtService.generateDeviceAccessToken(user, device)).thenReturn("token");
        when(jwtService.generateDeviceRefreshToken(user)).thenReturn("refresh");

        // When
        authDeviceService.authenticateDevice(authDeviceRequest);

        // Then
        verify(deviceService, times(1)).getByUuid(anyString());
        verify(userService, times(1)).getUserOrThrow(anyString());
        verify(authenticationManager, times(1)).authenticate(any());
        verify(jwtService, times(1)).generateDeviceAccessToken(any(), any());
        verify(jwtService, times(1)).generateDeviceRefreshToken(any());
    }

    @Test
    void shouldNotCallTokenGenerationOnAuthenticationFailure() {
        // Given
        when(deviceService.getByUuid("device-uuid-123")).thenReturn(device);
        when(userService.getUserOrThrow("test@example.com")).thenReturn(user);
        when(authenticationManager.authenticate(any())).thenThrow(new BadCredentialsException("Bad credentials"));

        // When & Then
        assertThrows(WrongLoginCredentialsException.class,
            () -> authDeviceService.authenticateDevice(authDeviceRequest));

        verify(jwtService, never()).generateDeviceAccessToken(any(), any());
        verify(jwtService, never()).generateDeviceRefreshToken(any());
    }
}

