package com.kacper.iot_backend.measurements;

import com.kacper.iot_backend.exception.ResourceNotFoundException;
import com.kacper.iot_backend.jwt.JWTService;
import com.kacper.iot_backend.user.UserRepository;
import com.kacper.iot_backend.user.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.logging.Logger;

@Slf4j
@Service
public class MeasurementService
{
    private final MeasurementRepository measurementRepository;
    private final UserService userService;
    private final static Logger logger = Logger.getLogger(MeasurementService.class.getName());

    public MeasurementService(MeasurementRepository measurementRepository, UserService userService) {
        this.measurementRepository = measurementRepository;
        this.userService = userService;
    }

    public void addMeasurement(AddMeasurementRequest addMeasurementRequest) {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        var userFromRequest = authentication.getName();

        logger.info("\n\nuserFromRequest: " + userFromRequest + "\n\n");

        var user = userService.getUserOrThrow(userFromRequest);

        var devices = user.getDevices();
        var device = devices.stream()
                .filter(d -> d.getId().equals(addMeasurementRequest.deviceId()))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Device not found for user"));

        var measurement = Measurement.builder()
                .measurementType(addMeasurementRequest.measurementType())
                .value(addMeasurementRequest.value())
                .device(device)
                .build();

        measurementRepository.save(measurement);
    }
}
