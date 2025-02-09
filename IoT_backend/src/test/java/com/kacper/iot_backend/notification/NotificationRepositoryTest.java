package com.kacper.iot_backend.notification;

import com.kacper.iot_backend.device.Device;
import com.kacper.iot_backend.device.DeviceRepository;
import com.kacper.iot_backend.user.User;
import com.kacper.iot_backend.user.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.OffsetDateTime;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@DataJpaTest
public class NotificationRepositoryTest {

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private DeviceRepository deviceRepository;

    @Autowired
    private UserRepository userRepository;

    private User user;
    private Device device;
    private Notification notification;

    @AfterEach
    public void cleanup() {
        if (notification != null) {
            notificationRepository.delete(notification);
        }
        if (device != null) {
            deviceRepository.delete(device);
        }
        if (user != null) {
            userRepository.delete(user);
        }
    }

    @Test
    public void shouldSaveNotification() {
        user = new User();
        user.setName("Test");
        user.setLast_name("Test");
        user.setEmail("test@example.com");
        user.setPassword("password");
        user.setRole("USER");
        user.setCreated_at(new Date());
        user.setEnabled(true);

        userRepository.save(user);

        device = Device.builder()
                .address("Grove street 1")
                .deviceName("Device 1")
                .uuid("uuid-1")
                .user(user)
                .build();

        device = deviceRepository.save(device);

        notification = Notification.builder()
                .type("INFO")
                .message("New notification")
                .timestamp(OffsetDateTime.now())
                .device(device)
                .has_seen(false)
                .build();

        notification = notificationRepository.save(notification);

        assertThat(notification.getMessage()).isEqualTo("New notification");
    }

    @Test
    public void shouldFindNotificationsByDeviceId() {
        user = new User();
        user.setName("Test");
        user.setLast_name("Test");
        user.setEmail("test@example.com");
        user.setPassword("password");
        user.setRole("USER");
        user.setCreated_at(new Date());
        user.setEnabled(true);

        userRepository.save(user);

        device = Device.builder()
                .address("Grove street 1")
                .deviceName("Device 1")
                .uuid("uuid-1")
                .user(user)
                .build();

        device = deviceRepository.save(device);

        notification = Notification.builder()
                .type("ALERT")
                .message("Alert notification")
                .timestamp(OffsetDateTime.now())
                .device(device)
                .has_seen(false)
                .build();

        notificationRepository.save(notification);

        List<Notification> foundNotifications = notificationRepository.findByDevice_Id(device.getId());

        assertThat(foundNotifications).hasSize(1);
        assertThat(foundNotifications.get(0).getMessage()).isEqualTo("Alert notification");
    }

    @Test
    public void shouldFindNotificationsByUser() {
        user = new User();
        user.setName("Test");
        user.setLast_name("Test");
        user.setEmail("test@example.com");
        user.setPassword("password");
        user.setRole("USER");
        user.setCreated_at(new Date());
        user.setEnabled(true);

        userRepository.save(user);

        device = Device.builder()
                .address("Grove street 1")
                .deviceName("Device 1")
                .uuid("uuid-1")
                .user(user)
                .build();

        device = deviceRepository.save(device);

        notification = Notification.builder()
                .type("WARNING")
                .message("Warning notification")
                .timestamp(OffsetDateTime.now())
                .device(device)
                .has_seen(false)
                .build();

        notificationRepository.save(notification);

        List<Notification> foundNotifications = notificationRepository.findByDevice_Id(device.getId());

        assertThat(foundNotifications).hasSize(1);
        assertThat(foundNotifications.get(0).getMessage()).isEqualTo("Warning notification");
    }
}
