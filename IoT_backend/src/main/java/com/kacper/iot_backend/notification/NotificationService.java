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
import org.springframework.messaging.simp.SimpMessagingTemplate;
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
    private final SimpMessagingTemplate messagingTemplate;


    public NotificationService(
            NotificationRepository notificationRepository,
            UserService userService,
            JWTService jwtService,
            DeviceRepository deviceRepository, SimpMessagingTemplate messagingTemplate) {
        this.notificationRepository = notificationRepository;
        this.userService = userService;
        this.jwtService = jwtService;
        this.deviceRepository = deviceRepository;
        this.messagingTemplate = messagingTemplate;
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

        var notificationResponse = new NotificationResponse(
                notification.getId(),
                notification.getType(),
                notification.getMessage(),
                notification.getHas_seen(),
                notification.getTimestamp()
        );

        // If you think hmm... how websocket knows which user to send to?
        // The answer: it is YOLO broadcast to all subscribed clients.
        messagingTemplate.convertAndSend(
                "/topic/notifications",
                notificationResponse
        );

        return new DefaultResponse("Notification added successfully");
    }

    public DefaultResponse addAiServiceNotification(NotificationRequest notificationRequest) {
        Notification notification = Notification.builder()
                .type(notificationRequest.type())
                .message(notificationRequest.message())
                .timestamp(OffsetDateTime.now(ZoneOffset.UTC))
                .has_seen(false)
                .build();

        notificationRepository.save(notification);

        var notificationResponse = new NotificationResponse(
                notification.getId(),
                notification.getType(),
                notification.getMessage(),
                notification.getHas_seen(),
                notification.getTimestamp()
        );

        messagingTemplate.convertAndSend(
                "/topic/notifications",
                notificationResponse
        );

        return new DefaultResponse("AI Service Notification added successfully");
    }
}
