//package com.kacper.iot_backend.device;
//
//import org.junit.jupiter.api.Test;
//
//import java.util.List;
//
//import static org.junit.jupiter.api.Assertions.*;
//
//class DeviceListResponseMapperTest
//{
//    @Test
//    void shouldMapDeviceToResponse() {
//        Device device = new Device(1, "Grove street 1", "Device1", "uuid1", null);
//
//        DeviceListResponseMapper deviceListResponseMapper = new DeviceListResponseMapper();
//
//        DevicesListResponse response = deviceListResponseMapper.apply(device);
//
//        assertNotNull(response);
//        assertEquals(1, response.id());
//        assertEquals("Grove street 1", response.address());
//        assertEquals("Device1", response.deviceName());
//        assertEquals("uuid1", response.uuid());
//    }
//
//    @Test
//    void shouldMapDevicesListToResponseList() {
//        List<Device> devices = List.of(
//                new Device(1, "Grove street 1", "Device1", "uuid1", null),
//                new Device(2, "Grove street 2", "Device2", "uuid2", null)
//        );
//
//        DeviceListResponseMapper deviceListResponseMapper = new DeviceListResponseMapper();
//
//        List<DevicesListResponse> response = devices.stream()
//                .map(deviceListResponseMapper)
//                .toList();
//
//        assertNotNull(response);
//        assertEquals(2, response.size());
//        assertEquals(1, response.getFirst().id());
//        assertEquals(2, response.getLast().id());
//    }
//}