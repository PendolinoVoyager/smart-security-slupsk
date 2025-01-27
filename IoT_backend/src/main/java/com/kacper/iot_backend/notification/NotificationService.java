package com.kacper.iot_backend.notification;

import com.kacper.iot_backend.device.Device;
import com.kacper.iot_backend.device.DeviceRepository;
import com.kacper.iot_backend.exception.ResourceNotFoundException;
import com.kacper.iot_backend.jwt.JWTService;
import com.kacper.iot_backend.user.User;
import com.kacper.iot_backend.user.UserService;
import com.kacper.iot_backend.utils.DefaultResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.logging.Logger;

@Service
public class NotificationService
{
    private final NotificationRepository notificationRepository;
    private final UserService userService;
    private final JWTService jwtService;
    private final static Logger logger = Logger.getLogger(NotificationService.class.getName());
    private final DeviceRepository deviceRepository;

    public NotificationService(
            NotificationRepository notificationRepository,
            UserService userService,
            JWTService jwtService,
            DeviceRepository deviceRepository) {
        this.notificationRepository = notificationRepository;
        this.userService = userService;
        this.jwtService = jwtService;
        this.deviceRepository = deviceRepository;
    }

    public NotificationPageResponse getNotifications(UserDetails userDetails, Pageable pageable) {
        User user = userService.getUserOrThrow(userDetails.getUsername());
        Page<Notification> notificationsPage = notificationRepository.findByUser(user, pageable);

        List<NotificationResponse> notifications = notificationsPage.getContent().stream()
                .map(notification -> new NotificationResponse(
                        notification.getId(),
                        notification.getType(),
                        notification.getMessage(),
                        notification.getHas_seen(),
                        notification.getTimestamp()
                ))
                .toList();



        return new NotificationPageResponse(
                notificationsPage.getNumber(),
                notificationsPage.getTotalPages(),
                notifications
        );
    }

    public DefaultResponse addNotification(String authorizationHeader, NotificationRequest notificationRequest) {
        String deviceUUID = jwtService.extractDeviceUUID(authorizationHeader.substring(7));

        Device device = deviceRepository.findByUuid(deviceUUID)
                .orElseThrow(() -> new ResourceNotFoundException("Device not found"));

        Notification notification = Notification.builder()
                .type(notificationRequest.type())
                .message(notificationRequest.message())
                .timestamp(OffsetDateTime.now(ZoneOffset.UTC))
                .device(device)
                .has_seen(false)
                .build();

        notificationRepository.save(notification);

        return new DefaultResponse("Notification added successfully");
    }
}
