package com.kacper.iot_backend.measurements;

import com.kacper.iot_backend.device.Device;
import com.kacper.iot_backend.exception.ResourceNotFoundException;
import com.kacper.iot_backend.user.User;
import com.kacper.iot_backend.user.UserService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MeasurementsServiceTest {

    @Mock
    private MeasurementRepository measurementRepository;

    @Mock
    private UserService userService;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private MeasurementService measurementService;

    private User user;
    private Device device;
    private Measurement measurement;
    private LocalDateTime testTimestamp;

    @BeforeEach
    void setUp() {
        testTimestamp = LocalDateTime.of(2026, 1, 25, 12, 0, 0);

        device = Device.builder()
                .id(1)
                .address("Test Address")
                .deviceName("Test Device")
                .uuid("device-uuid-123")
                .build();

        List<Device> devices = new ArrayList<>();
        devices.add(device);

        user = User.builder()
                .id(1)
                .name("Test")
                .last_name("User")
                .email("test@example.com")
                .password("password")
                .role("USER")
                .enabled(true)
                .devices(devices)
                .build();

        measurement = Measurement.builder()
                .id(1)
                .measurementType("TEMPERATURE")
                .value(25.5)
                .timestamp(testTimestamp)
                .device(device)
                .build();

        SecurityContextHolder.setContext(securityContext);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void shouldAddMeasurementSuccessfully() {
        // Given
        AddMeasurementRequest request = new AddMeasurementRequest(1, "TEMPERATURE", 25.5, testTimestamp);

        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn("test@example.com");
        when(userService.getUserOrThrow("test@example.com")).thenReturn(user);

        // When
        measurementService.addMeasurement(request);

        // Then
        ArgumentCaptor<Measurement> measurementCaptor = ArgumentCaptor.forClass(Measurement.class);
        verify(measurementRepository, times(1)).save(measurementCaptor.capture());

        Measurement savedMeasurement = measurementCaptor.getValue();
        assertEquals("TEMPERATURE", savedMeasurement.getMeasurementType());
        assertEquals(25.5, savedMeasurement.getValue());
        assertEquals(testTimestamp, savedMeasurement.getTimestamp());
        assertEquals(device, savedMeasurement.getDevice());
    }

    @Test
    void shouldThrowExceptionWhenDeviceNotFoundForAddMeasurement() {
        // Given
        AddMeasurementRequest request = new AddMeasurementRequest(999, "TEMPERATURE", 25.5, testTimestamp);

        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn("test@example.com");
        when(userService.getUserOrThrow("test@example.com")).thenReturn(user);

        // When & Then
        assertThrows(ResourceNotFoundException.class, () -> measurementService.addMeasurement(request));
        verify(measurementRepository, never()).save(any(Measurement.class));
    }

    @Test
    void shouldGetMeasurementsForDeviceSuccessfully() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        List<Measurement> measurements = List.of(measurement);
        Page<Measurement> measurementPage = new PageImpl<>(measurements, pageable, 1);

        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn("test@example.com");
        when(userService.getUserOrThrow("test@example.com")).thenReturn(user);
        when(measurementRepository.findByDeviceId(1, pageable)).thenReturn(measurementPage);

        // When
        Page<GetMeasurementResponse> result = measurementService.getMeasurementsForDevice(1, pageable);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(1, result.getContent().size());

        GetMeasurementResponse response = result.getContent().getFirst();
        assertEquals(1, response.id());
        assertEquals("TEMPERATURE", response.measurementType());
        assertEquals(25.5, response.value());
        assertEquals(testTimestamp.toString(), response.timestamp());

        verify(measurementRepository, times(1)).findByDeviceId(1, pageable);
    }

    @Test
    void shouldThrowExceptionWhenDeviceNotFoundForGetMeasurements() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);

        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn("test@example.com");
        when(userService.getUserOrThrow("test@example.com")).thenReturn(user);

        // When & Then
        assertThrows(ResourceNotFoundException.class, () -> measurementService.getMeasurementsForDevice(999, pageable));
        verify(measurementRepository, never()).findByDeviceId(anyInt(), any(Pageable.class));
    }

    @Test
    void shouldReturnEmptyPageWhenNoMeasurementsExist() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        Page<Measurement> emptyPage = new PageImpl<>(List.of(), pageable, 0);

        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn("test@example.com");
        when(userService.getUserOrThrow("test@example.com")).thenReturn(user);
        when(measurementRepository.findByDeviceId(1, pageable)).thenReturn(emptyPage);

        // When
        Page<GetMeasurementResponse> result = measurementService.getMeasurementsForDevice(1, pageable);

        // Then
        assertNotNull(result);
        assertEquals(0, result.getTotalElements());
        assertTrue(result.getContent().isEmpty());
    }

    @Test
    void shouldAddMeasurementWithDifferentMeasurementTypes() {
        // Given
        AddMeasurementRequest humidityRequest = new AddMeasurementRequest(1, "HUMIDITY", 65.0, testTimestamp);

        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn("test@example.com");
        when(userService.getUserOrThrow("test@example.com")).thenReturn(user);

        // When
        measurementService.addMeasurement(humidityRequest);

        // Then
        ArgumentCaptor<Measurement> measurementCaptor = ArgumentCaptor.forClass(Measurement.class);
        verify(measurementRepository).save(measurementCaptor.capture());

        Measurement savedMeasurement = measurementCaptor.getValue();
        assertEquals("HUMIDITY", savedMeasurement.getMeasurementType());
        assertEquals(65.0, savedMeasurement.getValue());
    }

    @Test
    void shouldAddMeasurementWithNegativeValue() {
        // Given
        AddMeasurementRequest request = new AddMeasurementRequest(1, "TEMPERATURE", -15.5, testTimestamp);

        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn("test@example.com");
        when(userService.getUserOrThrow("test@example.com")).thenReturn(user);

        // When
        measurementService.addMeasurement(request);

        // Then
        ArgumentCaptor<Measurement> measurementCaptor = ArgumentCaptor.forClass(Measurement.class);
        verify(measurementRepository).save(measurementCaptor.capture());

        Measurement savedMeasurement = measurementCaptor.getValue();
        assertEquals(-15.5, savedMeasurement.getValue());
    }

    @Test
    void shouldAddMeasurementWithZeroValue() {
        // Given
        AddMeasurementRequest request = new AddMeasurementRequest(1, "TEMPERATURE", 0.0, testTimestamp);

        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn("test@example.com");
        when(userService.getUserOrThrow("test@example.com")).thenReturn(user);

        // When
        measurementService.addMeasurement(request);

        // Then
        ArgumentCaptor<Measurement> measurementCaptor = ArgumentCaptor.forClass(Measurement.class);
        verify(measurementRepository).save(measurementCaptor.capture());

        Measurement savedMeasurement = measurementCaptor.getValue();
        assertEquals(0.0, savedMeasurement.getValue());
    }

    @Test
    void shouldHandleMultipleMeasurementsInPage() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);

        Measurement measurement2 = Measurement.builder()
                .id(2)
                .measurementType("HUMIDITY")
                .value(60.0)
                .timestamp(testTimestamp.plusHours(1))
                .device(device)
                .build();

        Measurement measurement3 = Measurement.builder()
                .id(3)
                .measurementType("PRESSURE")
                .value(1013.25)
                .timestamp(testTimestamp.plusHours(2))
                .device(device)
                .build();

        List<Measurement> measurements = List.of(measurement, measurement2, measurement3);
        Page<Measurement> measurementPage = new PageImpl<>(measurements, pageable, 3);

        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn("test@example.com");
        when(userService.getUserOrThrow("test@example.com")).thenReturn(user);
        when(measurementRepository.findByDeviceId(1, pageable)).thenReturn(measurementPage);

        // When
        Page<GetMeasurementResponse> result = measurementService.getMeasurementsForDevice(1, pageable);

        // Then
        assertNotNull(result);
        assertEquals(3, result.getTotalElements());
        assertEquals(3, result.getContent().size());

        assertEquals("TEMPERATURE", result.getContent().get(0).measurementType());
        assertEquals("HUMIDITY", result.getContent().get(1).measurementType());
        assertEquals("PRESSURE", result.getContent().get(2).measurementType());
    }

    @Test
    void shouldHandlePaginationCorrectly() {
        // Given
        Pageable pageable = PageRequest.of(1, 5);
        Page<Measurement> measurementPage = new PageImpl<>(List.of(measurement), pageable, 10);

        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn("test@example.com");
        when(userService.getUserOrThrow("test@example.com")).thenReturn(user);
        when(measurementRepository.findByDeviceId(1, pageable)).thenReturn(measurementPage);

        // When
        Page<GetMeasurementResponse> result = measurementService.getMeasurementsForDevice(1, pageable);

        // Then
        assertNotNull(result);
        assertEquals(10, result.getTotalElements());
        assertEquals(1, result.getNumber());
        assertEquals(2, result.getTotalPages());
    }

    @Test
    void shouldWorkWithUserHavingMultipleDevices() {
        // Given
        Device device2 = Device.builder()
                .id(2)
                .address("Second Address")
                .deviceName("Second Device")
                .uuid("device-uuid-456")
                .build();

        List<Device> multipleDevices = new ArrayList<>();
        multipleDevices.add(device);
        multipleDevices.add(device2);

        User userWithMultipleDevices = User.builder()
                .id(1)
                .name("Test")
                .last_name("User")
                .email("test@example.com")
                .password("password")
                .role("USER")
                .enabled(true)
                .devices(multipleDevices)
                .build();

        AddMeasurementRequest request = new AddMeasurementRequest(2, "TEMPERATURE", 20.0, testTimestamp);

        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn("test@example.com");
        when(userService.getUserOrThrow("test@example.com")).thenReturn(userWithMultipleDevices);

        // When
        measurementService.addMeasurement(request);

        // Then
        ArgumentCaptor<Measurement> measurementCaptor = ArgumentCaptor.forClass(Measurement.class);
        verify(measurementRepository).save(measurementCaptor.capture());

        Measurement savedMeasurement = measurementCaptor.getValue();
        assertEquals(device2, savedMeasurement.getDevice());
    }

    @Test
    void shouldThrowExceptionWhenUserHasNoDevices() {
        // Given
        User userWithNoDevices = User.builder()
                .id(1)
                .name("Test")
                .last_name("User")
                .email("test@example.com")
                .password("password")
                .role("USER")
                .enabled(true)
                .devices(new ArrayList<>())
                .build();

        AddMeasurementRequest request = new AddMeasurementRequest(1, "TEMPERATURE", 25.5, testTimestamp);

        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn("test@example.com");
        when(userService.getUserOrThrow("test@example.com")).thenReturn(userWithNoDevices);

        // When & Then
        assertThrows(ResourceNotFoundException.class, () -> measurementService.addMeasurement(request));
    }

    @Test
    void shouldAddMeasurementWithVeryLargeValue() {
        // Given
        AddMeasurementRequest request = new AddMeasurementRequest(1, "TEMPERATURE", Double.MAX_VALUE, testTimestamp);

        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn("test@example.com");
        when(userService.getUserOrThrow("test@example.com")).thenReturn(user);

        // When
        measurementService.addMeasurement(request);

        // Then
        ArgumentCaptor<Measurement> measurementCaptor = ArgumentCaptor.forClass(Measurement.class);
        verify(measurementRepository).save(measurementCaptor.capture());

        Measurement savedMeasurement = measurementCaptor.getValue();
        assertEquals(Double.MAX_VALUE, savedMeasurement.getValue());
    }

    @Test
    void shouldAddMeasurementWithVerySmallValue() {
        // Given
        AddMeasurementRequest request = new AddMeasurementRequest(1, "TEMPERATURE", Double.MIN_VALUE, testTimestamp);

        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn("test@example.com");
        when(userService.getUserOrThrow("test@example.com")).thenReturn(user);

        // When
        measurementService.addMeasurement(request);

        // Then
        ArgumentCaptor<Measurement> measurementCaptor = ArgumentCaptor.forClass(Measurement.class);
        verify(measurementRepository).save(measurementCaptor.capture());

        Measurement savedMeasurement = measurementCaptor.getValue();
        assertEquals(Double.MIN_VALUE, savedMeasurement.getValue());
    }
}
