package com.kacper.iot_backend.device;

import com.kacper.iot_backend.user.User;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.List;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class DeviceControllerTest {

    private final DeviceService deviceService = mock(DeviceService.class);
    private final DeviceController deviceController = new DeviceController(deviceService);

    @Test
    void shouldReturnUserDevicesWhenDevicesExist() {
        UserDetails userDetails = mock(UserDetails.class);
        when(userDetails.getUsername()).thenReturn("test@example.com");

        DevicesListResponse response1 = new DevicesListResponse(1, "Grove street 1", "Device1", "uuid1");
        DevicesListResponse response2 = new DevicesListResponse(2, "Grove street 2", "Device2", "uuid2");

        when(deviceService.getUserDevices(userDetails)).thenReturn(List.of(response1, response2));

        List<DevicesListResponse> result = deviceController.getUserDevices(userDetails);

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("Device1", result.get(0).deviceName());
        assertEquals("uuid2", result.get(1).uuid());

        verify(deviceService, times(1)).getUserDevices(userDetails);
    }

    @Test
    void shouldReturnEmptyListWhenNoDevicesExist() {
        UserDetails userDetails = mock(UserDetails.class);
        when(userDetails.getUsername()).thenReturn("test@example.com");

        when(deviceService.getUserDevices(userDetails)).thenReturn(Collections.emptyList());

        List<DevicesListResponse> result = deviceController.getUserDevices(userDetails);

        assertNotNull(result);
        assertTrue(result.isEmpty());

        verify(deviceService, times(1)).getUserDevices(userDetails);
    }

    @Test
    void shouldHandleNullReturnedFromService() {
        UserDetails userDetails = mock(UserDetails.class);
        when(userDetails.getUsername()).thenReturn("test@example.com");

        when(deviceService.getUserDevices(userDetails)).thenReturn(null);

        List<DevicesListResponse> result = deviceController.getUserDevices(userDetails);

        assertNull(result);

        verify(deviceService, times(1)).getUserDevices(userDetails);
    }
}
