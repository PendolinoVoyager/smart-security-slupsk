package com.kacper.iot_backend.minio;

import com.kacper.iot_backend.ai_service_notification.AiServiceNotification;
import com.kacper.iot_backend.ai_service_notification.AiServiceNotificationRepository;
import com.kacper.iot_backend.notification_images.NotificationImage;
import com.kacper.iot_backend.notification_images.NotificationImageRepository;
import com.kacper.iot_backend.utils.DefaultResponse;
import io.minio.*;
import io.minio.messages.Item;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MinioServiceTest {

    @Mock
    private MinioClient minioClient;

    @Mock
    private AiServiceNotificationRepository aiServiceNotificationRepository;

    @Mock
    private NotificationImageRepository notificationImageRepository;

    @InjectMocks
    private MinioService minioService;

    private AiServiceNotification notification;
    private MockMultipartFile mockFile;

    @BeforeEach
    void setUp() {
        notification = AiServiceNotification.builder()
                .id(1)
                .notificationType("MOTION_DETECTED")
                .message("Motion detected")
                .hasSeen(false)
                .timestamp(OffsetDateTime.now(ZoneOffset.UTC))
                .images(new ArrayList<>())
                .build();

        mockFile = new MockMultipartFile(
                "file",
                "test-image.jpg",
                "image/jpeg",
                "test image content".getBytes()
        );
    }

    // ===================== UPLOAD IMAGE TESTS =====================

    @Test
    void shouldUploadImageSuccessfully() throws Exception {
        // Given
        UploadImageRequest request = new UploadImageRequest(mockFile);

        when(aiServiceNotificationRepository.findById(1)).thenReturn(Optional.of(notification));
        when(minioClient.bucketExists(any(BucketExistsArgs.class))).thenReturn(true);

        // When
        DefaultResponse response = minioService.uploadImageToMinio(request, 1);

        // Then
        assertNotNull(response);
        assertTrue(response.message().startsWith("File has been saved:"));

        verify(minioClient).putObject(any(PutObjectArgs.class));
        verify(notificationImageRepository).save(any(NotificationImage.class));
    }

    @Test
    void shouldCreateBucketIfNotExists() throws Exception {
        // Given
        UploadImageRequest request = new UploadImageRequest(mockFile);

        when(aiServiceNotificationRepository.findById(1)).thenReturn(Optional.of(notification));
        when(minioClient.bucketExists(any(BucketExistsArgs.class))).thenReturn(false);

        // When
        DefaultResponse response = minioService.uploadImageToMinio(request, 1);

        // Then
        verify(minioClient).makeBucket(any(MakeBucketArgs.class));
        verify(minioClient).putObject(any(PutObjectArgs.class));
    }

    @Test
    void shouldReturnErrorForEmptyFile() throws Exception {
        // Given
        MockMultipartFile emptyFile = new MockMultipartFile(
                "file",
                "empty.jpg",
                "image/jpeg",
                new byte[0]
        );
        UploadImageRequest request = new UploadImageRequest(emptyFile);

        // When
        DefaultResponse response = minioService.uploadImageToMinio(request, 1);

        // Then
        assertEquals("Empty file", response.message());
    }

    @Test
    void shouldReturnErrorForNullFile() {
        // Given
        UploadImageRequest request = new UploadImageRequest(null);

        // When
        DefaultResponse response = minioService.uploadImageToMinio(request, 1);

        // Then
        assertEquals("Empty file", response.message());
    }

    @Test
    void shouldThrowExceptionWhenNotificationNotFound() {
        // Given
        UploadImageRequest request = new UploadImageRequest(mockFile);
        when(aiServiceNotificationRepository.findById(999)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(RuntimeException.class,
            () -> minioService.uploadImageToMinio(request, 999));
    }

    @Test
    void shouldSaveNotificationImageWithCorrectUrl() throws Exception {
        // Given
        UploadImageRequest request = new UploadImageRequest(mockFile);

        when(aiServiceNotificationRepository.findById(1)).thenReturn(Optional.of(notification));
        when(minioClient.bucketExists(any(BucketExistsArgs.class))).thenReturn(true);

        // When
        minioService.uploadImageToMinio(request, 1);

        // Then
        ArgumentCaptor<NotificationImage> captor = ArgumentCaptor.forClass(NotificationImage.class);
        verify(notificationImageRepository).save(captor.capture());

        NotificationImage savedImage = captor.getValue();
        assertTrue(savedImage.getImageUrl().startsWith("images/"));
        assertTrue(savedImage.getImageUrl().contains("test-image.jpg"));
        assertEquals(notification, savedImage.getAiServiceNotification());
    }

    @Test
    void shouldHandleMinioException() throws Exception {
        // Given
        UploadImageRequest request = new UploadImageRequest(mockFile);

        when(aiServiceNotificationRepository.findById(1)).thenReturn(Optional.of(notification));
        when(minioClient.bucketExists(any(BucketExistsArgs.class))).thenThrow(new RuntimeException("Minio error"));

        // When
        DefaultResponse response = minioService.uploadImageToMinio(request, 1);

        // Then
        assertTrue(response.message().startsWith("Error? xD:"));
    }

    @Test
    void shouldGenerateUniqueObjectName() throws Exception {
        // Given
        UploadImageRequest request = new UploadImageRequest(mockFile);

        when(aiServiceNotificationRepository.findById(1)).thenReturn(Optional.of(notification));
        when(minioClient.bucketExists(any(BucketExistsArgs.class))).thenReturn(true);

        // When
        minioService.uploadImageToMinio(request, 1);

        // Then
        ArgumentCaptor<PutObjectArgs> captor = ArgumentCaptor.forClass(PutObjectArgs.class);
        verify(minioClient).putObject(captor.capture());

        String objectName = captor.getValue().object();
        assertTrue(objectName.contains("_test-image.jpg"));
        // Should contain UUID prefix
        assertTrue(objectName.length() > "test-image.jpg".length());
    }

    @Test
    void shouldSetCorrectContentType() throws Exception {
        // Given
        MockMultipartFile pngFile = new MockMultipartFile(
                "file",
                "test.png",
                "image/png",
                "png content".getBytes()
        );
        UploadImageRequest request = new UploadImageRequest(pngFile);

        when(aiServiceNotificationRepository.findById(1)).thenReturn(Optional.of(notification));
        when(minioClient.bucketExists(any(BucketExistsArgs.class))).thenReturn(true);

        // When
        minioService.uploadImageToMinio(request, 1);

        // Then
        ArgumentCaptor<PutObjectArgs> captor = ArgumentCaptor.forClass(PutObjectArgs.class);
        verify(minioClient).putObject(captor.capture());

        assertEquals("image/png", captor.getValue().contentType());
    }

    // ===================== GET ALL IMAGES TESTS =====================

    @Test
    void shouldReturnEmptyListWhenBucketNotExists() throws Exception {
        // Given
        when(minioClient.bucketExists(any(BucketExistsArgs.class))).thenReturn(false);

        // When
        List<String> images = minioService.getAllImages();

        // Then
        assertNotNull(images);
        assertTrue(images.isEmpty());
    }

    @Test
    void shouldReturnPresignedUrlsForAllImages() throws Exception {
        // Given
        when(minioClient.bucketExists(any(BucketExistsArgs.class))).thenReturn(true);

        Item item1 = mock(Item.class);
        Item item2 = mock(Item.class);
        when(item1.objectName()).thenReturn("image1.jpg");
        when(item2.objectName()).thenReturn("image2.jpg");

        Result<Item> result1 = mock(Result.class);
        Result<Item> result2 = mock(Result.class);
        when(result1.get()).thenReturn(item1);
        when(result2.get()).thenReturn(item2);

        List<Result<Item>> results = List.of(result1, result2);

        when(minioClient.listObjects(any(ListObjectsArgs.class))).thenReturn(results);
        when(minioClient.getPresignedObjectUrl(any(GetPresignedObjectUrlArgs.class)))
                .thenReturn("http://minio/images/image1.jpg?presigned")
                .thenReturn("http://minio/images/image2.jpg?presigned");

        // When
        List<String> images = minioService.getAllImages();

        // Then
        assertEquals(2, images.size());
        assertTrue(images.get(0).contains("presigned"));
        assertTrue(images.get(1).contains("presigned"));
    }

    @Test
    void shouldThrowExceptionOnListingError() throws Exception {
        // Given
        when(minioClient.bucketExists(any(BucketExistsArgs.class))).thenReturn(true);
        when(minioClient.listObjects(any(ListObjectsArgs.class))).thenThrow(new RuntimeException("List error"));

        // When & Then
        assertThrows(RuntimeException.class, () -> minioService.getAllImages());
    }

    // ===================== GET IMAGES BY NOTIFICATION ID TESTS =====================

    @Test
    void shouldReturnImagesForNotificationId() throws Exception {
        // Given
        NotificationImage image1 = NotificationImage.builder()
                .id(1)
                .imageUrl("images/uuid1_image1.jpg")
                .aiServiceNotification(notification)
                .build();

        NotificationImage image2 = NotificationImage.builder()
                .id(2)
                .imageUrl("images/uuid2_image2.jpg")
                .aiServiceNotification(notification)
                .build();

        when(notificationImageRepository.findByAiServiceNotificationId(1))
                .thenReturn(List.of(image1, image2));
        when(minioClient.getPresignedObjectUrl(any(GetPresignedObjectUrlArgs.class)))
                .thenReturn("http://minio/presigned/image1.jpg")
                .thenReturn("http://minio/presigned/image2.jpg");

        // When
        List<String> images = minioService.getImagesByNotificationId(1);

        // Then
        assertEquals(2, images.size());
        verify(notificationImageRepository).findByAiServiceNotificationId(1);
        verify(minioClient, times(2)).getPresignedObjectUrl(any(GetPresignedObjectUrlArgs.class));
    }

    @Test
    void shouldReturnEmptyListWhenNoImagesForNotification() {
        // Given
        when(notificationImageRepository.findByAiServiceNotificationId(999))
                .thenReturn(List.of());

        // When
        List<String> images = minioService.getImagesByNotificationId(999);

        // Then
        assertTrue(images.isEmpty());
    }

    @Test
    void shouldExtractObjectNameCorrectly() throws Exception {
        // Given
        NotificationImage image = NotificationImage.builder()
                .id(1)
                .imageUrl("images/uuid_test-image.jpg")
                .aiServiceNotification(notification)
                .build();

        when(notificationImageRepository.findByAiServiceNotificationId(1))
                .thenReturn(List.of(image));
        when(minioClient.getPresignedObjectUrl(any(GetPresignedObjectUrlArgs.class)))
                .thenReturn("http://presigned-url");

        // When
        minioService.getImagesByNotificationId(1);

        // Then
        ArgumentCaptor<GetPresignedObjectUrlArgs> captor = ArgumentCaptor.forClass(GetPresignedObjectUrlArgs.class);
        verify(minioClient).getPresignedObjectUrl(captor.capture());

        assertEquals("uuid_test-image.jpg", captor.getValue().object());
    }

    @Test
    void shouldThrowExceptionOnPresignedUrlError() throws Exception {
        // Given
        NotificationImage image = NotificationImage.builder()
                .id(1)
                .imageUrl("images/test.jpg")
                .aiServiceNotification(notification)
                .build();

        when(notificationImageRepository.findByAiServiceNotificationId(1))
                .thenReturn(List.of(image));
        when(minioClient.getPresignedObjectUrl(any(GetPresignedObjectUrlArgs.class)))
                .thenThrow(new RuntimeException("Presigned URL error"));

        // When & Then
        assertThrows(RuntimeException.class,
            () -> minioService.getImagesByNotificationId(1));
    }

    // ===================== FILE TYPE TESTS =====================

    @Test
    void shouldHandleJpegFile() throws Exception {
        // Given
        MockMultipartFile jpegFile = new MockMultipartFile(
                "file", "photo.jpeg", "image/jpeg", "jpeg content".getBytes()
        );
        UploadImageRequest request = new UploadImageRequest(jpegFile);

        when(aiServiceNotificationRepository.findById(1)).thenReturn(Optional.of(notification));
        when(minioClient.bucketExists(any(BucketExistsArgs.class))).thenReturn(true);

        // When
        DefaultResponse response = minioService.uploadImageToMinio(request, 1);

        // Then
        assertTrue(response.message().contains("photo.jpeg"));
    }

    @Test
    void shouldHandlePngFile() throws Exception {
        // Given
        MockMultipartFile pngFile = new MockMultipartFile(
                "file", "image.png", "image/png", "png content".getBytes()
        );
        UploadImageRequest request = new UploadImageRequest(pngFile);

        when(aiServiceNotificationRepository.findById(1)).thenReturn(Optional.of(notification));
        when(minioClient.bucketExists(any(BucketExistsArgs.class))).thenReturn(true);

        // When
        DefaultResponse response = minioService.uploadImageToMinio(request, 1);

        // Then
        assertTrue(response.message().contains("image.png"));
    }

    @Test
    void shouldHandleGifFile() throws Exception {
        // Given
        MockMultipartFile gifFile = new MockMultipartFile(
                "file", "animation.gif", "image/gif", "gif content".getBytes()
        );
        UploadImageRequest request = new UploadImageRequest(gifFile);

        when(aiServiceNotificationRepository.findById(1)).thenReturn(Optional.of(notification));
        when(minioClient.bucketExists(any(BucketExistsArgs.class))).thenReturn(true);

        // When
        DefaultResponse response = minioService.uploadImageToMinio(request, 1);

        // Then
        assertTrue(response.message().contains("animation.gif"));
    }

    // ===================== EDGE CASE TESTS =====================

    @Test
    void shouldHandleFileWithNoExtension() throws Exception {
        // Given
        MockMultipartFile noExtFile = new MockMultipartFile(
                "file", "filename", "application/octet-stream", "content".getBytes()
        );
        UploadImageRequest request = new UploadImageRequest(noExtFile);

        when(aiServiceNotificationRepository.findById(1)).thenReturn(Optional.of(notification));
        when(minioClient.bucketExists(any(BucketExistsArgs.class))).thenReturn(true);

        // When
        DefaultResponse response = minioService.uploadImageToMinio(request, 1);

        // Then
        assertTrue(response.message().contains("filename"));
    }

    @Test
    void shouldHandleFileWithSpecialCharactersInName() throws Exception {
        // Given
        MockMultipartFile specialFile = new MockMultipartFile(
                "file", "my image (1).jpg", "image/jpeg", "content".getBytes()
        );
        UploadImageRequest request = new UploadImageRequest(specialFile);

        when(aiServiceNotificationRepository.findById(1)).thenReturn(Optional.of(notification));
        when(minioClient.bucketExists(any(BucketExistsArgs.class))).thenReturn(true);

        // When
        DefaultResponse response = minioService.uploadImageToMinio(request, 1);

        // Then
        assertNotNull(response);
        assertTrue(response.message().startsWith("File has been saved:"));
    }

    @Test
    void shouldHandleLargeFile() throws Exception {
        // Given
        byte[] largeContent = new byte[10 * 1024 * 1024]; // 10MB
        MockMultipartFile largeFile = new MockMultipartFile(
                "file", "large-image.jpg", "image/jpeg", largeContent
        );
        UploadImageRequest request = new UploadImageRequest(largeFile);

        when(aiServiceNotificationRepository.findById(1)).thenReturn(Optional.of(notification));
        when(minioClient.bucketExists(any(BucketExistsArgs.class))).thenReturn(true);

        // When
        DefaultResponse response = minioService.uploadImageToMinio(request, 1);

        // Then
        assertTrue(response.message().contains("large-image.jpg"));
    }

    // ===================== VERIFICATION TESTS =====================

    @Test
    void shouldVerifyCorrectBucketIsUsed() throws Exception {
        // Given
        UploadImageRequest request = new UploadImageRequest(mockFile);

        when(aiServiceNotificationRepository.findById(1)).thenReturn(Optional.of(notification));
        when(minioClient.bucketExists(any(BucketExistsArgs.class))).thenReturn(true);

        // When
        minioService.uploadImageToMinio(request, 1);

        // Then
        ArgumentCaptor<BucketExistsArgs> bucketCaptor = ArgumentCaptor.forClass(BucketExistsArgs.class);
        verify(minioClient).bucketExists(bucketCaptor.capture());
        assertEquals("images", bucketCaptor.getValue().bucket());
    }

    @Test
    void shouldNotCreateBucketIfExists() throws Exception {
        // Given
        UploadImageRequest request = new UploadImageRequest(mockFile);

        when(aiServiceNotificationRepository.findById(1)).thenReturn(Optional.of(notification));
        when(minioClient.bucketExists(any(BucketExistsArgs.class))).thenReturn(true);

        // When
        minioService.uploadImageToMinio(request, 1);

        // Then
        verify(minioClient, never()).makeBucket(any(MakeBucketArgs.class));
    }

    @Test
    void shouldCallRepositorySaveOnce() throws Exception {
        // Given
        UploadImageRequest request = new UploadImageRequest(mockFile);

        when(aiServiceNotificationRepository.findById(1)).thenReturn(Optional.of(notification));
        when(minioClient.bucketExists(any(BucketExistsArgs.class))).thenReturn(true);

        // When
        minioService.uploadImageToMinio(request, 1);

        // Then
        verify(notificationImageRepository, times(1)).save(any(NotificationImage.class));
    }
}

