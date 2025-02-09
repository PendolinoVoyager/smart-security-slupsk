package com.kacper.iot_backend.notification;

import com.kacper.iot_backend.device.Device;
import com.kacper.iot_backend.device.DeviceRepository;
import com.kacper.iot_backend.exception.ResourceNotFoundException;
import com.kacper.iot_backend.jwt.JWTService;
import com.kacper.iot_backend.user.User;
import com.kacper.iot_backend.user.UserService;
import com.kacper.iot_backend.utils.DefaultResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private UserService userService;

    @Mock
    private JWTService jwtService;

    @Mock
    private DeviceRepository deviceRepository;

    @InjectMocks
    private NotificationService notificationService;

    private User user;
    private Device device;
    private Notification notification;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setEmail("test@example.com");

        device = new Device();
        device.setUuid("device-uuid");
        device.setUser(user);

        notification = Notification.builder()
                .id(1)
                .type("INFO")
                .message("Test message")
                .timestamp(OffsetDateTime.now())
                .device(device)
                .has_seen(false)
                .build();
    }

    @Test
    void shouldReturnNotificationsForUser() {
        UserDetails userDetails = mock(UserDetails.class);
        when(userDetails.getUsername()).thenReturn("test@example.com");
        when(userService.getUserOrThrow("test@example.com")).thenReturn(user);

        Page<Notification> notificationPage = new PageImpl<>(List.of(notification));
        when(notificationRepository.findByUser(eq(user), any(Pageable.class))).thenReturn(notificationPage);

        NotificationPageResponse response = notificationService.getNotifications(userDetails, Pageable.unpaged());

        assertNotNull(response);
        assertEquals(1, response.notifications().size());
        assertEquals("Test message", response.notifications().get(0).message());

        verify(userService, times(1)).getUserOrThrow("test@example.com");
        verify(notificationRepository, times(1)).findByUser(eq(user), any(Pageable.class));
    }

    @Test
    void shouldAddNotificationSuccessfully() {
        NotificationRequest request = new NotificationRequest("WARNING", "New Warning");
        String authHeader = "Bearer validToken";

        when(jwtService.extractDeviceUUID("validToken")).thenReturn("device-uuid");
        when(deviceRepository.findByUuid("device-uuid")).thenReturn(Optional.of(device));

        DefaultResponse response = notificationService.addNotification(authHeader, request);

        assertNotNull(response);
        assertEquals("Notification added successfully", response.message());

        verify(deviceRepository, times(1)).findByUuid("device-uuid");
        verify(notificationRepository, times(1)).save(any(Notification.class));
    }

    @Test
    void shouldThrowExceptionWhenDeviceNotFound() {
        NotificationRequest request = new NotificationRequest("ERROR", "Device missing");
        String authHeader = "Bearer invalidToken";

        when(jwtService.extractDeviceUUID("invalidToken")).thenReturn("invalid-uuid");
        when(deviceRepository.findByUuid("invalid-uuid")).thenReturn(Optional.empty());

        Exception exception = assertThrows(ResourceNotFoundException.class, () ->
                notificationService.addNotification(authHeader, request));

        assertEquals("Device not found", exception.getMessage());
        verify(deviceRepository, times(1)).findByUuid("invalid-uuid");
        verifyNoInteractions(notificationRepository);
    }
}
