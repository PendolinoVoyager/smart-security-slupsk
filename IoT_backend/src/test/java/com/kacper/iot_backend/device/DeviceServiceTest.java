package com.kacper.iot_backend.device;

import com.kacper.iot_backend.user.User;
import com.kacper.iot_backend.user.UserService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.List;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class DeviceServiceTest {

    private final DeviceRepository deviceRepository = mock(DeviceRepository.class);
    private final UserService userService = mock(UserService.class);
    private final DeviceListResponseMapper deviceListResponseMapper = mock(DeviceListResponseMapper.class);
    private final DeviceService deviceService = new DeviceService(deviceRepository, userService, deviceListResponseMapper);

    @Test
    void shouldReturnDevicesForUser() {
        UserDetails userDetails = mock(UserDetails.class);
        when(userDetails.getUsername()).thenReturn("test@example.com");

        User user = new User();
        user.setEmail("test@example.com");

        Device device1 = new Device(1, "Grove street 1", "Device1", "uuid1", user);
        Device device2 = new Device(2, "Grove street 2", "Device2", "uuid2", user);

        when(userService.getUserOrThrow("test@example.com")).thenReturn(user);
        when(deviceRepository.findByUser(user)).thenReturn(List.of(device1, device2));

        DevicesListResponse response1 = new DevicesListResponse(1, "Grove street 1", "Device1", "uuid1");
        DevicesListResponse response2 = new DevicesListResponse(2, "Grove street 2", "Device2", "uuid2");

        when(deviceListResponseMapper.apply(device1)).thenReturn(response1);
        when(deviceListResponseMapper.apply(device2)).thenReturn(response2);

        List<DevicesListResponse> result = deviceService.getUserDevices(userDetails);

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("Device1", result.get(0).deviceName());
        assertEquals("uuid2", result.get(1).uuid());

        verify(userService, times(1)).getUserOrThrow("test@example.com");
        verify(deviceRepository, times(1)).findByUser(user);
    }

    @Test
    void shouldReturnEmptyListWhenNoDevicesForUser() {
        UserDetails userDetails = mock(UserDetails.class);
        when(userDetails.getUsername()).thenReturn("test@example.com");

        User user = new User();
        user.setEmail("test@example.com");

        when(userService.getUserOrThrow("test@example.com")).thenReturn(user);
        when(deviceRepository.findByUser(user)).thenReturn(Collections.emptyList());

        List<DevicesListResponse> result = deviceService.getUserDevices(userDetails);

        assertNotNull(result);
        assertTrue(result.isEmpty());

        verify(userService, times(1)).getUserOrThrow("test@example.com");
        verify(deviceRepository, times(1)).findByUser(user);
    }

    @Test
    void shouldThrowExceptionWhenUserNotFound() {
        UserDetails userDetails = mock(UserDetails.class);
        when(userDetails.getUsername()).thenReturn("nonexistent@example.com");

        when(userService.getUserOrThrow("nonexistent@example.com"))
                .thenThrow(new RuntimeException("User not found"));

        Exception exception = assertThrows(RuntimeException.class, () -> {
            deviceService.getUserDevices(userDetails);
        });

        assertEquals("User not found", exception.getMessage());

        verify(userService, times(1)).getUserOrThrow("nonexistent@example.com");
        verifyNoInteractions(deviceRepository);
    }
}
