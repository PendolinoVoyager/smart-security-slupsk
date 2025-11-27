package com.kacper.iot_backend.measurements;

import com.kacper.iot_backend.device.Device;
import com.kacper.iot_backend.exception.ResourceNotFoundException;
import com.kacper.iot_backend.jwt.JWTService;
import com.kacper.iot_backend.user.User;
import com.kacper.iot_backend.user.UserRepository;
import com.kacper.iot_backend.user.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;
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
        var device = getDeviceFromUser(addMeasurementRequest.deviceId());

        var measurement = Measurement.builder()
                .measurementType(addMeasurementRequest.measurementType())
                .value(addMeasurementRequest.value())
                .device(device)
                .timestamp(addMeasurementRequest.timestamp())
                .build();

        measurementRepository.save(measurement);
    }

    public Page<GetMeasurementResponse> getMeasurementsForDevice(int deviceId, Pageable pageable) {
        var device = getDeviceFromUser(deviceId);

        var measurementsPage = measurementRepository.findByDeviceId(device.getId(), pageable);
        return measurementsPage.map(measurement -> new GetMeasurementResponse(
                measurement.getId(),
                measurement.getMeasurementType(),
                measurement.getValue(),
                measurement.getTimestamp().toString()
        ));
    }

    private Device getDeviceFromUser(int deviceId) {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        var userFromRequest = authentication.getName();

        logger.info("\n\nuserFromRequest: " + userFromRequest + "\n\n");

        var user = userService.getUserOrThrow(userFromRequest);

        var devices = user.getDevices();
        return devices.stream()
                .filter(d -> d.getId().equals(deviceId))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Device not found for user"));
    }

}
