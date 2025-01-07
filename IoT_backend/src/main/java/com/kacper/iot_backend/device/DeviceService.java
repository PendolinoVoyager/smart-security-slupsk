package com.kacper.iot_backend.device;

import com.kacper.iot_backend.exception.ResourceNotFoundException;
import com.kacper.iot_backend.user.User;
import com.kacper.iot_backend.user.UserService;
import com.kacper.iot_backend.utils.DefaultResponse;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DeviceService
{
    private final DeviceRepository deviceRepository;
    private final UserService userService;
    private final DeviceListResponseMapper deviceListResponseMapper;

    public DeviceService(
            DeviceRepository deviceRepository,
            UserService userService,
            DeviceListResponseMapper deviceListResponseMapper
    ) {
        this.deviceRepository = deviceRepository;
        this.userService = userService;
        this.deviceListResponseMapper = deviceListResponseMapper;
    }

    public List<DevicesListResponse> getUserDevices(UserDetails userDetails) {
        User user = userService.getUserOrThrow(userDetails.getUsername());

        List<Device> userDevices = deviceRepository.findByUser(user);

        if (userDevices.isEmpty()) {
            return List.of();
        }

        return userDevices.stream()
                .map(deviceListResponseMapper)
                .toList();

    }

    public User getUserByDevice(Integer deviceId) {
        Device device = deviceRepository.findById(deviceId)
                .orElseThrow(() -> new ResourceNotFoundException("Device not found"));

        return device.getUser();
    }
}
