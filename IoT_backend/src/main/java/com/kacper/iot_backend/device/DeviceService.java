package com.kacper.iot_backend.device;

import com.kacper.iot_backend.exception.ResourceNotFoundException;
import com.kacper.iot_backend.user.User;
import com.kacper.iot_backend.user.UserService;
import com.kacper.iot_backend.utils.DefaultResponse;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.logging.Logger;

@Service
public class DeviceService
{
    private final DeviceRepository deviceRepository;
    private final UserService userService;
    private final DeviceListResponseMapper deviceListResponseMapper;
    private final static Logger logger = Logger.getLogger(DeviceService.class.getName());

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

    public User getUserByDeviceUuIdOrThrow(String deviceUuid) {
        logger.info("\n\ndeviceUuid: " + deviceUuid + "\n\n");
        Device device = deviceRepository.findByUuid(deviceUuid)
                .orElseThrow(() -> new ResourceNotFoundException("Device not found"));

        logger.info("\n\ndevice: " + device + "\n\n");
        return device.getUser();
    }
    /**
     * Przepraszam cie za to Kacper, ale duplikuje kod, bo mam go zamiar użyć w jednym miejscu.
     * getUserByDeviceUuidOrThrow wg. mnie powinno zwracać całe device, bo i tak już je mamy.
     * Ta metoda poniżej robi to co tamto, ale zamiast return device.getUser() ma return device.
     * @param deviceUuid
     * @return
     */
    public Device getByUuid(String deviceUuid) {
        logger.info("\n\ndeviceUuid: " + deviceUuid + "\n\n");
        Device device = deviceRepository.findByUuid(deviceUuid)
                .orElseThrow(() -> new ResourceNotFoundException("Device not found"));

        logger.info("\n\ndevice: " + device + "\n\n");
        return device;
    }
}
