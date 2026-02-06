package com.kacper.iot_backend.notification_images;

import com.kacper.iot_backend.ai_service_notification.AiServiceNotification;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class NotificationImagesTests {

    private NotificationImage notificationImage;
    private AiServiceNotification aiServiceNotification;
    private OffsetDateTime testTimestamp;

    @BeforeEach
    void setUp() {
        testTimestamp = OffsetDateTime.of(2026, 1, 25, 12, 0, 0, 0, ZoneOffset.UTC);

        aiServiceNotification = AiServiceNotification.builder()
                .id(1)
                .notificationType("MOTION_DETECTED")
                .message("Motion detected in front yard")
                .hasSeen(false)
                .timestamp(testTimestamp)
                .images(new ArrayList<>())
                .build();

        notificationImage = NotificationImage.builder()
                .id(1)
                .imageUrl("http://example.com/image.jpg")
                .aiServiceNotification(aiServiceNotification)
                .build();
    }

    // ===================== BUILDER TESTS =====================

    @Test
    void shouldCreateNotificationImageUsingBuilder() {
        // Given & When
        NotificationImage builtImage = NotificationImage.builder()
                .id(1)
                .imageUrl("http://example.com/test.jpg")
                .aiServiceNotification(aiServiceNotification)
                .build();

        // Then
        assertNotNull(builtImage);
        assertEquals(1, builtImage.getId());
        assertEquals("http://example.com/test.jpg", builtImage.getImageUrl());
        assertEquals(aiServiceNotification, builtImage.getAiServiceNotification());
    }

    @Test
    void shouldCreateNotificationImageWithNoArgsConstructor() {
        // Given & When
        NotificationImage emptyImage = new NotificationImage();

        // Then
        assertNotNull(emptyImage);
        assertNull(emptyImage.getId());
        assertNull(emptyImage.getImageUrl());
        assertNull(emptyImage.getAiServiceNotification());
    }

    @Test
    void shouldCreateNotificationImageWithAllArgsConstructor() {
        // When
        NotificationImage fullImage = new NotificationImage(
                1,
                "http://example.com/full.jpg",
                aiServiceNotification
        );

        // Then
        assertNotNull(fullImage);
        assertEquals(1, fullImage.getId());
        assertEquals("http://example.com/full.jpg", fullImage.getImageUrl());
        assertEquals(aiServiceNotification, fullImage.getAiServiceNotification());
    }

    // ===================== GETTER TESTS =====================

    @Test
    void shouldGetId() {
        assertEquals(1, notificationImage.getId());
    }

    @Test
    void shouldGetImageUrl() {
        assertEquals("http://example.com/image.jpg", notificationImage.getImageUrl());
    }

    @Test
    void shouldGetAiServiceNotification() {
        assertEquals(aiServiceNotification, notificationImage.getAiServiceNotification());
    }

    // ===================== SETTER TESTS =====================

    @Test
    void shouldSetId() {
        // When
        notificationImage.setId(999);

        // Then
        assertEquals(999, notificationImage.getId());
    }

    @Test
    void shouldSetImageUrl() {
        // When
        notificationImage.setImageUrl("http://example.com/new-image.jpg");

        // Then
        assertEquals("http://example.com/new-image.jpg", notificationImage.getImageUrl());
    }

    @Test
    void shouldSetAiServiceNotification() {
        // Given
        AiServiceNotification newNotification = AiServiceNotification.builder()
                .id(2)
                .notificationType("INTRUDER_ALERT")
                .message("Intruder detected")
                .hasSeen(false)
                .timestamp(testTimestamp)
                .images(new ArrayList<>())
                .build();

        // When
        notificationImage.setAiServiceNotification(newNotification);

        // Then
        assertEquals(newNotification, notificationImage.getAiServiceNotification());
        assertEquals(2, notificationImage.getAiServiceNotification().getId());
    }

    // ===================== IMAGE URL FORMAT TESTS =====================

    @Test
    void shouldHandleHttpUrl() {
        // When
        notificationImage.setImageUrl("http://example.com/image.jpg");

        // Then
        assertTrue(notificationImage.getImageUrl().startsWith("http://"));
    }

    @Test
    void shouldHandleHttpsUrl() {
        // When
        notificationImage.setImageUrl("https://secure.example.com/image.jpg");

        // Then
        assertTrue(notificationImage.getImageUrl().startsWith("https://"));
    }

    @Test
    void shouldHandleUrlWithQueryParameters() {
        // Given
        String urlWithParams = "https://example.com/image.jpg?size=large&format=png&quality=80";

        // When
        notificationImage.setImageUrl(urlWithParams);

        // Then
        assertEquals(urlWithParams, notificationImage.getImageUrl());
        assertTrue(notificationImage.getImageUrl().contains("?"));
    }

    @Test
    void shouldHandleUrlWithSpecialCharacters() {
        // Given
        String urlWithSpecialChars = "https://example.com/images/2026%2F01%2F25/image%20name.jpg";

        // When
        notificationImage.setImageUrl(urlWithSpecialChars);

        // Then
        assertEquals(urlWithSpecialChars, notificationImage.getImageUrl());
    }

    @Test
    void shouldHandleS3Url() {
        // Given
        String s3Url = "https://my-bucket.s3.eu-central-1.amazonaws.com/notifications/image-123.jpg";

        // When
        notificationImage.setImageUrl(s3Url);

        // Then
        assertEquals(s3Url, notificationImage.getImageUrl());
        assertTrue(notificationImage.getImageUrl().contains("s3"));
    }

    @Test
    void shouldHandleCloudinaryUrl() {
        // Given
        String cloudinaryUrl = "https://res.cloudinary.com/demo/image/upload/v1234567890/sample.jpg";

        // When
        notificationImage.setImageUrl(cloudinaryUrl);

        // Then
        assertEquals(cloudinaryUrl, notificationImage.getImageUrl());
    }

    @Test
    void shouldHandleBase64DataUrl() {
        // Given
        String base64Url = "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mNk+M9QDwADhgGAWjR9awAAAABJRU5ErkJggg==";

        // When
        notificationImage.setImageUrl(base64Url);

        // Then
        assertEquals(base64Url, notificationImage.getImageUrl());
        assertTrue(notificationImage.getImageUrl().startsWith("data:image"));
    }

    @Test
    void shouldHandleLocalFilePath() {
        // Given
        String localPath = "/var/www/images/notification-123.jpg";

        // When
        notificationImage.setImageUrl(localPath);

        // Then
        assertEquals(localPath, notificationImage.getImageUrl());
    }

    @Test
    void shouldHandleRelativePath() {
        // Given
        String relativePath = "../images/notification.jpg";

        // When
        notificationImage.setImageUrl(relativePath);

        // Then
        assertEquals(relativePath, notificationImage.getImageUrl());
    }

    // ===================== IMAGE FORMAT TESTS =====================

    @Test
    void shouldHandleJpgExtension() {
        // When
        notificationImage.setImageUrl("http://example.com/image.jpg");

        // Then
        assertTrue(notificationImage.getImageUrl().endsWith(".jpg"));
    }

    @Test
    void shouldHandleJpegExtension() {
        // When
        notificationImage.setImageUrl("http://example.com/image.jpeg");

        // Then
        assertTrue(notificationImage.getImageUrl().endsWith(".jpeg"));
    }

    @Test
    void shouldHandlePngExtension() {
        // When
        notificationImage.setImageUrl("http://example.com/image.png");

        // Then
        assertTrue(notificationImage.getImageUrl().endsWith(".png"));
    }

    @Test
    void shouldHandleGifExtension() {
        // When
        notificationImage.setImageUrl("http://example.com/image.gif");

        // Then
        assertTrue(notificationImage.getImageUrl().endsWith(".gif"));
    }

    @Test
    void shouldHandleWebpExtension() {
        // When
        notificationImage.setImageUrl("http://example.com/image.webp");

        // Then
        assertTrue(notificationImage.getImageUrl().endsWith(".webp"));
    }

    @Test
    void shouldHandleBmpExtension() {
        // When
        notificationImage.setImageUrl("http://example.com/image.bmp");

        // Then
        assertTrue(notificationImage.getImageUrl().endsWith(".bmp"));
    }

    @Test
    void shouldHandleSvgExtension() {
        // When
        notificationImage.setImageUrl("http://example.com/image.svg");

        // Then
        assertTrue(notificationImage.getImageUrl().endsWith(".svg"));
    }

    // ===================== URL LENGTH TESTS =====================

    @Test
    void shouldHandleShortUrl() {
        // Given
        String shortUrl = "http://a.co/1";

        // When
        notificationImage.setImageUrl(shortUrl);

        // Then
        assertEquals(shortUrl, notificationImage.getImageUrl());
    }

    @Test
    void shouldHandleLongUrl() {
        // Given
        String longUrl = "https://example.com/" + "a".repeat(450) + ".jpg";

        // When
        notificationImage.setImageUrl(longUrl);

        // Then
        assertEquals(longUrl, notificationImage.getImageUrl());
        assertTrue(notificationImage.getImageUrl().length() <= 500);
    }

    @Test
    void shouldHandleMaxLengthUrl() {
        // Given - imageUrl column is length 500
        String maxLengthUrl = "https://example.com/" + "x".repeat(476) + ".jpg";

        // When
        notificationImage.setImageUrl(maxLengthUrl);

        // Then
        assertEquals(500, notificationImage.getImageUrl().length());
    }

    // ===================== NULL HANDLING TESTS =====================

    @Test
    void shouldHandleNullId() {
        // When
        notificationImage.setId(null);

        // Then
        assertNull(notificationImage.getId());
    }

    @Test
    void shouldHandleNullImageUrl() {
        // When
        notificationImage.setImageUrl(null);

        // Then
        assertNull(notificationImage.getImageUrl());
    }

    @Test
    void shouldHandleNullAiServiceNotification() {
        // When
        notificationImage.setAiServiceNotification(null);

        // Then
        assertNull(notificationImage.getAiServiceNotification());
    }

    // ===================== EDGE CASE TESTS =====================

    @Test
    void shouldHandleZeroId() {
        // When
        notificationImage.setId(0);

        // Then
        assertEquals(0, notificationImage.getId());
    }

    @Test
    void shouldHandleNegativeId() {
        // When
        notificationImage.setId(-1);

        // Then
        assertEquals(-1, notificationImage.getId());
    }

    @Test
    void shouldHandleMaxIntegerId() {
        // When
        notificationImage.setId(Integer.MAX_VALUE);

        // Then
        assertEquals(Integer.MAX_VALUE, notificationImage.getId());
    }

    @Test
    void shouldHandleEmptyImageUrl() {
        // When
        notificationImage.setImageUrl("");

        // Then
        assertEquals("", notificationImage.getImageUrl());
    }

    @Test
    void shouldHandleWhitespaceOnlyImageUrl() {
        // Given
        String whitespaceUrl = "   \t\n   ";

        // When
        notificationImage.setImageUrl(whitespaceUrl);

        // Then
        assertEquals(whitespaceUrl, notificationImage.getImageUrl());
    }

    // ===================== RELATIONSHIP TESTS =====================

    @Test
    void shouldMaintainBidirectionalRelationship() {
        // Given
        AiServiceNotification notification = AiServiceNotification.builder()
                .id(5)
                .notificationType("ALERT")
                .message("Test alert")
                .hasSeen(false)
                .timestamp(testTimestamp)
                .images(new ArrayList<>())
                .build();

        NotificationImage image = NotificationImage.builder()
                .id(10)
                .imageUrl("http://example.com/bidirectional.jpg")
                .aiServiceNotification(notification)
                .build();

        notification.getImages().add(image);

        // Then
        assertEquals(notification, image.getAiServiceNotification());
        assertTrue(notification.getImages().contains(image));
    }

    @Test
    void shouldChangeNotificationAssociation() {
        // Given
        AiServiceNotification originalNotification = aiServiceNotification;
        AiServiceNotification newNotification = AiServiceNotification.builder()
                .id(99)
                .notificationType("NEW_ALERT")
                .message("New alert message")
                .hasSeen(true)
                .timestamp(testTimestamp)
                .images(new ArrayList<>())
                .build();

        // When
        assertEquals(originalNotification, notificationImage.getAiServiceNotification());
        notificationImage.setAiServiceNotification(newNotification);

        // Then
        assertEquals(newNotification, notificationImage.getAiServiceNotification());
        assertEquals(99, notificationImage.getAiServiceNotification().getId());
    }

    @Test
    void shouldAccessNotificationProperties() {
        // Then
        assertNotNull(notificationImage.getAiServiceNotification());
        assertEquals(1, notificationImage.getAiServiceNotification().getId());
        assertEquals("MOTION_DETECTED", notificationImage.getAiServiceNotification().getNotificationType());
        assertEquals("Motion detected in front yard", notificationImage.getAiServiceNotification().getMessage());
        assertFalse(notificationImage.getAiServiceNotification().isHasSeen());
    }

    // ===================== BUILDER PARTIAL TESTS =====================

    @Test
    void shouldBuildWithOnlyIdAndUrl() {
        // When
        NotificationImage minimalImage = NotificationImage.builder()
                .id(1)
                .imageUrl("http://example.com/minimal.jpg")
                .build();

        // Then
        assertNotNull(minimalImage);
        assertEquals(1, minimalImage.getId());
        assertEquals("http://example.com/minimal.jpg", minimalImage.getImageUrl());
        assertNull(minimalImage.getAiServiceNotification());
    }

    @Test
    void shouldBuildWithOnlyUrl() {
        // When
        NotificationImage urlOnlyImage = NotificationImage.builder()
                .imageUrl("http://example.com/url-only.jpg")
                .build();

        // Then
        assertNotNull(urlOnlyImage);
        assertNull(urlOnlyImage.getId());
        assertEquals("http://example.com/url-only.jpg", urlOnlyImage.getImageUrl());
    }

    // ===================== URL PATTERN TESTS =====================

    @Test
    void shouldHandleUrlWithPort() {
        // Given
        String urlWithPort = "http://localhost:8080/images/test.jpg";

        // When
        notificationImage.setImageUrl(urlWithPort);

        // Then
        assertEquals(urlWithPort, notificationImage.getImageUrl());
    }

    @Test
    void shouldHandleUrlWithAuthentication() {
        // Given
        String authUrl = "https://user:password@example.com/secure/image.jpg";

        // When
        notificationImage.setImageUrl(authUrl);

        // Then
        assertEquals(authUrl, notificationImage.getImageUrl());
    }

    @Test
    void shouldHandleUrlWithFragment() {
        // Given
        String urlWithFragment = "https://example.com/image.jpg#section1";

        // When
        notificationImage.setImageUrl(urlWithFragment);

        // Then
        assertEquals(urlWithFragment, notificationImage.getImageUrl());
        assertTrue(notificationImage.getImageUrl().contains("#"));
    }

    @Test
    void shouldHandleUrlWithUnicodeCharacters() {
        // Given
        String unicodeUrl = "https://example.com/obrazy/zdjęcie-äöü.jpg";

        // When
        notificationImage.setImageUrl(unicodeUrl);

        // Then
        assertEquals(unicodeUrl, notificationImage.getImageUrl());
    }

    @Test
    void shouldHandleIpAddressUrl() {
        // Given
        String ipUrl = "http://192.168.1.100/cameras/snapshot.jpg";

        // When
        notificationImage.setImageUrl(ipUrl);

        // Then
        assertEquals(ipUrl, notificationImage.getImageUrl());
    }

    @Test
    void shouldHandleIpv6Url() {
        // Given
        String ipv6Url = "http://[2001:db8::1]/images/capture.jpg";

        // When
        notificationImage.setImageUrl(ipv6Url);

        // Then
        assertEquals(ipv6Url, notificationImage.getImageUrl());
    }

    // ===================== MULTIPLE IMAGES TESTS =====================

    @Test
    void shouldCreateMultipleImagesForSameNotification() {
        // Given
        NotificationImage image1 = NotificationImage.builder()
                .id(1)
                .imageUrl("http://example.com/image1.jpg")
                .aiServiceNotification(aiServiceNotification)
                .build();

        NotificationImage image2 = NotificationImage.builder()
                .id(2)
                .imageUrl("http://example.com/image2.jpg")
                .aiServiceNotification(aiServiceNotification)
                .build();

        NotificationImage image3 = NotificationImage.builder()
                .id(3)
                .imageUrl("http://example.com/image3.jpg")
                .aiServiceNotification(aiServiceNotification)
                .build();

        aiServiceNotification.getImages().add(image1);
        aiServiceNotification.getImages().add(image2);
        aiServiceNotification.getImages().add(image3);

        // Then
        assertEquals(3, aiServiceNotification.getImages().size());
        assertEquals(aiServiceNotification, image1.getAiServiceNotification());
        assertEquals(aiServiceNotification, image2.getAiServiceNotification());
        assertEquals(aiServiceNotification, image3.getAiServiceNotification());
    }

    @Test
    void shouldHaveDifferentUrlsForDifferentImages() {
        // Given
        NotificationImage image1 = NotificationImage.builder()
                .id(1)
                .imageUrl("http://example.com/unique1.jpg")
                .aiServiceNotification(aiServiceNotification)
                .build();

        NotificationImage image2 = NotificationImage.builder()
                .id(2)
                .imageUrl("http://example.com/unique2.jpg")
                .aiServiceNotification(aiServiceNotification)
                .build();

        // Then
        assertNotEquals(image1.getImageUrl(), image2.getImageUrl());
    }

    // ===================== TIMESTAMP IN URL TESTS =====================

    @Test
    void shouldHandleTimestampedUrl() {
        // Given
        String timestampedUrl = "https://example.com/images/2026/01/25/12-00-00-capture.jpg";

        // When
        notificationImage.setImageUrl(timestampedUrl);

        // Then
        assertEquals(timestampedUrl, notificationImage.getImageUrl());
    }

    @Test
    void shouldHandleUuidInUrl() {
        // Given
        String uuidUrl = "https://storage.example.com/550e8400-e29b-41d4-a716-446655440000.jpg";

        // When
        notificationImage.setImageUrl(uuidUrl);

        // Then
        assertEquals(uuidUrl, notificationImage.getImageUrl());
    }

    // ===================== SECURITY CAMERA URL TESTS =====================

    @Test
    void shouldHandleRtspUrl() {
        // Given
        String rtspUrl = "rtsp://admin:password@192.168.1.64:554/stream1";

        // When
        notificationImage.setImageUrl(rtspUrl);

        // Then
        assertEquals(rtspUrl, notificationImage.getImageUrl());
        assertTrue(notificationImage.getImageUrl().startsWith("rtsp://"));
    }

    @Test
    void shouldHandleCameraSnapshotUrl() {
        // Given
        String snapshotUrl = "http://camera.local/cgi-bin/snapshot.cgi?channel=1";

        // When
        notificationImage.setImageUrl(snapshotUrl);

        // Then
        assertEquals(snapshotUrl, notificationImage.getImageUrl());
    }
}
