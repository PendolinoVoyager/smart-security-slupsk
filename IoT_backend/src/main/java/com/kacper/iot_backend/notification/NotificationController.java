package com.kacper.iot_backend.notification;

import com.kacper.iot_backend.utils.DefaultResponse;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/notification")
public class NotificationController
{
    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @GetMapping("")
    public NotificationPageResponse getNotifications(@AuthenticationPrincipal UserDetails userDetails, Pageable pageable) {
        return notificationService.getNotifications(userDetails, pageable);
    }

    @PostMapping("/")
    public DefaultResponse addNotification(@RequestHeader("Authorization") String authorizationHeader, @RequestBody NotificationRequest notificationRequest) {
        return notificationService.addNotification(authorizationHeader, notificationRequest);
    }
}
