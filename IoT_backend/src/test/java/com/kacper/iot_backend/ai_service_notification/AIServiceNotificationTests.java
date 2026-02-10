package com.kacper.iot_backend.ai_service_notification;

import com.kacper.iot_backend.notification_images.NotificationImage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class AIServiceNotificationTests {

    private AiServiceNotification notification;
    private OffsetDateTime testTimestamp;

    @BeforeEach
    void setUp() {
        testTimestamp = OffsetDateTime.of(2026, 1, 25, 12, 0, 0, 0, ZoneOffset.UTC);

        notification = AiServiceNotification.builder()
                .id(1)
                .notificationType("MOTION_DETECTED")
                .message("Motion detected in front yard")
                .hasSeen(false)
                .timestamp(testTimestamp)
                .images(new ArrayList<>())
                .build();
    }

    // ===================== BUILDER TESTS =====================

    @Test
    void shouldCreateNotificationUsingBuilder() {
        // Given & When
        AiServiceNotification builtNotification = AiServiceNotification.builder()
                .id(1)
                .notificationType("INTRUDER_ALERT")
                .message("Intruder detected")
                .hasSeen(false)
                .timestamp(testTimestamp)
                .images(new ArrayList<>())
                .build();

        // Then
        assertNotNull(builtNotification);
        assertEquals(1, builtNotification.getId());
        assertEquals("INTRUDER_ALERT", builtNotification.getNotificationType());
        assertEquals("Intruder detected", builtNotification.getMessage());
        assertFalse(builtNotification.isHasSeen());
        assertEquals(testTimestamp, builtNotification.getTimestamp());
        assertNotNull(builtNotification.getImages());
        assertTrue(builtNotification.getImages().isEmpty());
    }

    @Test
    void shouldCreateNotificationWithNoArgsConstructor() {
        // Given & When
        AiServiceNotification emptyNotification = new AiServiceNotification();

        // Then
        assertNotNull(emptyNotification);
        assertNull(emptyNotification.getId());
        assertNull(emptyNotification.getNotificationType());
        assertNull(emptyNotification.getMessage());
    }

    @Test
    void shouldCreateNotificationWithAllArgsConstructor() {
        // Given
        List<NotificationImage> images = new ArrayList<>();

        // When
        AiServiceNotification fullNotification = new AiServiceNotification(
                1,
                "FACE_RECOGNIZED",
                "Known person detected",
                true,
                testTimestamp,
                images
        );

        // Then
        assertNotNull(fullNotification);
        assertEquals(1, fullNotification.getId());
        assertEquals("FACE_RECOGNIZED", fullNotification.getNotificationType());
        assertEquals("Known person detected", fullNotification.getMessage());
        assertTrue(fullNotification.isHasSeen());
        assertEquals(testTimestamp, fullNotification.getTimestamp());
        assertEquals(images, fullNotification.getImages());
    }

    // ===================== GETTER TESTS =====================

    @Test
    void shouldGetId() {
        assertEquals(1, notification.getId());
    }

    @Test
    void shouldGetNotificationType() {
        assertEquals("MOTION_DETECTED", notification.getNotificationType());
    }

    @Test
    void shouldGetMessage() {
        assertEquals("Motion detected in front yard", notification.getMessage());
    }

    @Test
    void shouldGetHasSeen() {
        assertFalse(notification.isHasSeen());
    }

    @Test
    void shouldGetTimestamp() {
        assertEquals(testTimestamp, notification.getTimestamp());
    }

    @Test
    void shouldGetImages() {
        assertNotNull(notification.getImages());
        assertTrue(notification.getImages().isEmpty());
    }

    // ===================== SETTER TESTS =====================

    @Test
    void shouldSetId() {
        // When
        notification.setId(999);

        // Then
        assertEquals(999, notification.getId());
    }

    @Test
    void shouldSetNotificationType() {
        // When
        notification.setNotificationType("VEHICLE_DETECTED");

        // Then
        assertEquals("VEHICLE_DETECTED", notification.getNotificationType());
    }

    @Test
    void shouldSetMessage() {
        // When
        notification.setMessage("New message content");

        // Then
        assertEquals("New message content", notification.getMessage());
    }

    @Test
    void shouldSetHasSeen() {
        // When
        notification.setHasSeen(true);

        // Then
        assertTrue(notification.isHasSeen());
    }

    @Test
    void shouldSetTimestamp() {
        // Given
        OffsetDateTime newTimestamp = OffsetDateTime.of(2026, 6, 15, 10, 30, 0, 0, ZoneOffset.UTC);

        // When
        notification.setTimestamp(newTimestamp);

        // Then
        assertEquals(newTimestamp, notification.getTimestamp());
    }

    @Test
    void shouldSetImages() {
        // Given
        List<NotificationImage> newImages = new ArrayList<>();
        NotificationImage image = NotificationImage.builder()
                .id(1)
                .imageUrl("http://example.com/image.jpg")
                .aiServiceNotification(notification)
                .build();
        newImages.add(image);

        // When
        notification.setImages(newImages);

        // Then
        assertEquals(1, notification.getImages().size());
        assertEquals("http://example.com/image.jpg", notification.getImages().get(0).getImageUrl());
    }

    // ===================== NOTIFICATION TYPE TESTS =====================

    @Test
    void shouldHandleMotionDetectedType() {
        // When
        notification.setNotificationType("MOTION_DETECTED");

        // Then
        assertEquals("MOTION_DETECTED", notification.getNotificationType());
    }

    @Test
    void shouldHandleIntruderAlertType() {
        // When
        notification.setNotificationType("INTRUDER_ALERT");

        // Then
        assertEquals("INTRUDER_ALERT", notification.getNotificationType());
    }

    @Test
    void shouldHandleFaceRecognizedType() {
        // When
        notification.setNotificationType("FACE_RECOGNIZED");

        // Then
        assertEquals("FACE_RECOGNIZED", notification.getNotificationType());
    }

    @Test
    void shouldHandleVehicleDetectedType() {
        // When
        notification.setNotificationType("VEHICLE_DETECTED");

        // Then
        assertEquals("VEHICLE_DETECTED", notification.getNotificationType());
    }

    @Test
    void shouldHandlePackageDeliveryType() {
        // When
        notification.setNotificationType("PACKAGE_DELIVERY");

        // Then
        assertEquals("PACKAGE_DELIVERY", notification.getNotificationType());
    }

    // ===================== IMAGES RELATIONSHIP TESTS =====================

    @Test
    void shouldAddImageToNotification() {
        // Given
        NotificationImage image = NotificationImage.builder()
                .id(1)
                .imageUrl("http://example.com/motion1.jpg")
                .aiServiceNotification(notification)
                .build();

        // When
        notification.getImages().add(image);

        // Then
        assertEquals(1, notification.getImages().size());
        assertEquals(notification, notification.getImages().get(0).getAiServiceNotification());
    }

    @Test
    void shouldAddMultipleImagesToNotification() {
        // Given
        NotificationImage image1 = NotificationImage.builder()
                .id(1)
                .imageUrl("http://example.com/image1.jpg")
                .aiServiceNotification(notification)
                .build();

        NotificationImage image2 = NotificationImage.builder()
                .id(2)
                .imageUrl("http://example.com/image2.jpg")
                .aiServiceNotification(notification)
                .build();

        NotificationImage image3 = NotificationImage.builder()
                .id(3)
                .imageUrl("http://example.com/image3.jpg")
                .aiServiceNotification(notification)
                .build();

        // When
        notification.getImages().add(image1);
        notification.getImages().add(image2);
        notification.getImages().add(image3);

        // Then
        assertEquals(3, notification.getImages().size());
    }

    @Test
    void shouldRemoveImageFromNotification() {
        // Given
        NotificationImage image = NotificationImage.builder()
                .id(1)
                .imageUrl("http://example.com/image.jpg")
                .aiServiceNotification(notification)
                .build();
        notification.getImages().add(image);

        // When
        notification.getImages().remove(image);

        // Then
        assertTrue(notification.getImages().isEmpty());
    }

    @Test
    void shouldClearAllImagesFromNotification() {
        // Given
        NotificationImage image1 = NotificationImage.builder()
                .id(1)
                .imageUrl("http://example.com/image1.jpg")
                .aiServiceNotification(notification)
                .build();
        NotificationImage image2 = NotificationImage.builder()
                .id(2)
                .imageUrl("http://example.com/image2.jpg")
                .aiServiceNotification(notification)
                .build();
        notification.getImages().add(image1);
        notification.getImages().add(image2);

        // When
        notification.getImages().clear();

        // Then
        assertTrue(notification.getImages().isEmpty());
    }

    // ===================== HAS_SEEN STATUS TESTS =====================

    @Test
    void shouldMarkNotificationAsSeen() {
        // Given
        assertFalse(notification.isHasSeen());

        // When
        notification.setHasSeen(true);

        // Then
        assertTrue(notification.isHasSeen());
    }

    @Test
    void shouldMarkNotificationAsUnseen() {
        // Given
        notification.setHasSeen(true);
        assertTrue(notification.isHasSeen());

        // When
        notification.setHasSeen(false);

        // Then
        assertFalse(notification.isHasSeen());
    }

    @Test
    void shouldToggleHasSeenStatus() {
        // Given
        boolean initialStatus = notification.isHasSeen();

        // When
        notification.setHasSeen(!initialStatus);

        // Then
        assertEquals(!initialStatus, notification.isHasSeen());
    }

    // ===================== TIMESTAMP TESTS =====================

    @Test
    void shouldHandleTimestampInDifferentTimezones() {
        // Given
        OffsetDateTime utcTime = OffsetDateTime.of(2026, 1, 25, 12, 0, 0, 0, ZoneOffset.UTC);
        OffsetDateTime cetTime = OffsetDateTime.of(2026, 1, 25, 13, 0, 0, 0, ZoneOffset.ofHours(1));

        // When
        notification.setTimestamp(utcTime);
        OffsetDateTime utcResult = notification.getTimestamp();

        notification.setTimestamp(cetTime);
        OffsetDateTime cetResult = notification.getTimestamp();

        // Then
        assertEquals(utcTime, utcResult);
        assertEquals(cetTime, cetResult);
        assertTrue(utcResult.isEqual(cetResult)); // Same instant in time
    }

    @Test
    void shouldHandleTimestampAtMidnight() {
        // Given
        OffsetDateTime midnight = OffsetDateTime.of(2026, 1, 25, 0, 0, 0, 0, ZoneOffset.UTC);

        // When
        notification.setTimestamp(midnight);

        // Then
        assertEquals(0, notification.getTimestamp().getHour());
        assertEquals(0, notification.getTimestamp().getMinute());
        assertEquals(0, notification.getTimestamp().getSecond());
    }

    @Test
    void shouldHandleTimestampAtEndOfDay() {
        // Given
        OffsetDateTime endOfDay = OffsetDateTime.of(2026, 1, 25, 23, 59, 59, 999999999, ZoneOffset.UTC);

        // When
        notification.setTimestamp(endOfDay);

        // Then
        assertEquals(23, notification.getTimestamp().getHour());
        assertEquals(59, notification.getTimestamp().getMinute());
        assertEquals(59, notification.getTimestamp().getSecond());
    }

    // ===================== MESSAGE TESTS =====================

    @Test
    void shouldHandleEmptyMessage() {
        // When
        notification.setMessage("");

        // Then
        assertEquals("", notification.getMessage());
    }

    @Test
    void shouldHandleLongMessage() {
        // Given
        String longMessage = "A".repeat(255);

        // When
        notification.setMessage(longMessage);

        // Then
        assertEquals(255, notification.getMessage().length());
    }

    @Test
    void shouldHandleMessageWithSpecialCharacters() {
        // Given
        String specialMessage = "Alert! Motion detected at <location> with 95% confidence. Time: 12:30:45 & temp: -5°C";

        // When
        notification.setMessage(specialMessage);

        // Then
        assertEquals(specialMessage, notification.getMessage());
    }

    @Test
    void shouldHandleMessageWithUnicodeCharacters() {
        // Given
        String unicodeMessage = "Wykryto ruch! 检测到运动 движение обнаружено 🚨";

        // When
        notification.setMessage(unicodeMessage);

        // Then
        assertEquals(unicodeMessage, notification.getMessage());
    }

    @Test
    void shouldHandleMessageWithNewlines() {
        // Given
        String multilineMessage = "Alert detected:\nLocation: Front door\nTime: 12:00\nConfidence: 98%";

        // When
        notification.setMessage(multilineMessage);

        // Then
        assertEquals(multilineMessage, notification.getMessage());
        assertTrue(notification.getMessage().contains("\n"));
    }

    // ===================== NULL HANDLING TESTS =====================

    @Test
    void shouldHandleNullImages() {
        // When
        notification.setImages(null);

        // Then
        assertNull(notification.getImages());
    }

    @Test
    void shouldHandleNullMessage() {
        // When
        notification.setMessage(null);

        // Then
        assertNull(notification.getMessage());
    }

    @Test
    void shouldHandleNullNotificationType() {
        // When
        notification.setNotificationType(null);

        // Then
        assertNull(notification.getNotificationType());
    }

    @Test
    void shouldHandleNullTimestamp() {
        // When
        notification.setTimestamp(null);

        // Then
        assertNull(notification.getTimestamp());
    }

    // ===================== BUILDER PARTIAL TESTS =====================

    @Test
    void shouldBuildWithOnlyRequiredFields() {
        // When
        AiServiceNotification minimalNotification = AiServiceNotification.builder()
                .notificationType("ALERT")
                .message("Test")
                .hasSeen(false)
                .build();

        // Then
        assertNotNull(minimalNotification);
        assertNull(minimalNotification.getId());
        assertEquals("ALERT", minimalNotification.getNotificationType());
        assertEquals("Test", minimalNotification.getMessage());
    }

    @Test
    void shouldBuildWithImagesInitialized() {
        // When
        AiServiceNotification notificationWithImages = AiServiceNotification.builder()
                .notificationType("MOTION")
                .message("Motion detected")
                .hasSeen(false)
                .images(new ArrayList<>())
                .build();

        // Then
        assertNotNull(notificationWithImages.getImages());
        assertTrue(notificationWithImages.getImages().isEmpty());
    }

    // ===================== EDGE CASE TESTS =====================

    @Test
    void shouldHandleZeroId() {
        // When
        notification.setId(0);

        // Then
        assertEquals(0, notification.getId());
    }

    @Test
    void shouldHandleNegativeId() {
        // When
        notification.setId(-1);

        // Then
        assertEquals(-1, notification.getId());
    }

    @Test
    void shouldHandleMaxIntegerId() {
        // When
        notification.setId(Integer.MAX_VALUE);

        // Then
        assertEquals(Integer.MAX_VALUE, notification.getId());
    }

    @Test
    void shouldHandleWhitespaceOnlyMessage() {
        // Given
        String whitespaceMessage = "   \t\n   ";

        // When
        notification.setMessage(whitespaceMessage);

        // Then
        assertEquals(whitespaceMessage, notification.getMessage());
    }

    @Test
    void shouldHandleWhitespaceOnlyNotificationType() {
        // Given
        String whitespaceType = "   ";

        // When
        notification.setNotificationType(whitespaceType);

        // Then
        assertEquals(whitespaceType, notification.getNotificationType());
    }

    // ===================== IMAGE COLLECTION BEHAVIOR TESTS =====================

    @Test
    void shouldMaintainImageOrderInList() {
        // Given
        NotificationImage image1 = NotificationImage.builder().id(1).imageUrl("url1").aiServiceNotification(notification).build();
        NotificationImage image2 = NotificationImage.builder().id(2).imageUrl("url2").aiServiceNotification(notification).build();
        NotificationImage image3 = NotificationImage.builder().id(3).imageUrl("url3").aiServiceNotification(notification).build();

        // When
        notification.getImages().add(image1);
        notification.getImages().add(image2);
        notification.getImages().add(image3);

        // Then
        assertEquals("url1", notification.getImages().get(0).getImageUrl());
        assertEquals("url2", notification.getImages().get(1).getImageUrl());
        assertEquals("url3", notification.getImages().get(2).getImageUrl());
    }

    @Test
    void shouldCheckIfImagesContainSpecificImage() {
        // Given
        NotificationImage image = NotificationImage.builder()
                .id(1)
                .imageUrl("http://example.com/test.jpg")
                .aiServiceNotification(notification)
                .build();
        notification.getImages().add(image);

        // Then
        assertTrue(notification.getImages().contains(image));
    }

    @Test
    void shouldGetImageByIndex() {
        // Given
        NotificationImage image = NotificationImage.builder()
                .id(5)
                .imageUrl("http://example.com/indexed.jpg")
                .aiServiceNotification(notification)
                .build();
        notification.getImages().add(image);

        // When
        NotificationImage retrievedImage = notification.getImages().get(0);

        // Then
        assertEquals(5, retrievedImage.getId());
        assertEquals("http://example.com/indexed.jpg", retrievedImage.getImageUrl());
    }
}
