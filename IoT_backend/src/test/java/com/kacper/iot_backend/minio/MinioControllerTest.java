package com.kacper.iot_backend.minio;

import com.kacper.iot_backend.utils.DefaultResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class MinioControllerTest {

    @Mock
    private MinioService minioService;

    @InjectMocks
    private MinioController minioController;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(minioController).build();
    }

    // ===================== UPLOAD ENDPOINT TESTS =====================

    @Test
    void shouldUploadFileSuccessfully() throws Exception {
        // Given
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test-image.jpg",
                "image/jpeg",
                "test content".getBytes()
        );

        DefaultResponse response = DefaultResponse.builder()
                .message("File has been saved: uuid_test-image.jpg")
                .build();

        when(minioService.uploadImageToMinio(any(UploadImageRequest.class), eq(1))).thenReturn(response);

        // When & Then
        mockMvc.perform(multipart("/api/v1/minio/upload")
                        .file(file)
                        .param("ai-service-notification-id", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("File has been saved: uuid_test-image.jpg"));

        verify(minioService).uploadImageToMinio(any(UploadImageRequest.class), eq(1));
    }

    @Test
    void shouldPassCorrectNotificationIdToService() throws Exception {
        // Given
        MockMultipartFile file = new MockMultipartFile(
                "file", "image.jpg", "image/jpeg", "content".getBytes()
        );

        DefaultResponse response = DefaultResponse.builder().message("Success").build();
        when(minioService.uploadImageToMinio(any(), eq(42))).thenReturn(response);

        // When
        mockMvc.perform(multipart("/api/v1/minio/upload")
                        .file(file)
                        .param("ai-service-notification-id", "42"))
                .andExpect(status().isOk());

        // Then
        verify(minioService).uploadImageToMinio(any(), eq(42));
    }

    @Test
    void shouldHandleEmptyFileUpload() throws Exception {
        // Given
        MockMultipartFile emptyFile = new MockMultipartFile(
                "file", "empty.jpg", "image/jpeg", new byte[0]
        );

        DefaultResponse response = DefaultResponse.builder()
                .message("Empty file")
                .build();

        when(minioService.uploadImageToMinio(any(), eq(1))).thenReturn(response);

        // When & Then
        mockMvc.perform(multipart("/api/v1/minio/upload")
                        .file(emptyFile)
                        .param("ai-service-notification-id", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Empty file"));
    }

    @Test
    void shouldReturnJsonContentType() throws Exception {
        // Given
        MockMultipartFile file = new MockMultipartFile(
                "file", "test.jpg", "image/jpeg", "content".getBytes()
        );

        DefaultResponse response = DefaultResponse.builder().message("Success").build();
        when(minioService.uploadImageToMinio(any(), any())).thenReturn(response);

        // When & Then
        mockMvc.perform(multipart("/api/v1/minio/upload")
                        .file(file)
                        .param("ai-service-notification-id", "1"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    // ===================== GET ALL IMAGES ENDPOINT TESTS =====================

    @Test
    void shouldGetAllImagesSuccessfully() throws Exception {
        // Given
        List<String> images = Arrays.asList(
                "http://minio/images/image1.jpg?presigned=abc",
                "http://minio/images/image2.jpg?presigned=xyz"
        );

        when(minioService.getAllImages()).thenReturn(images);

        // When & Then
        mockMvc.perform(get("/api/v1/minio/images"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0]").value("http://minio/images/image1.jpg?presigned=abc"))
                .andExpect(jsonPath("$[1]").value("http://minio/images/image2.jpg?presigned=xyz"));

        verify(minioService).getAllImages();
    }

    @Test
    void shouldReturnEmptyListWhenNoImages() throws Exception {
        // Given
        when(minioService.getAllImages()).thenReturn(Collections.emptyList());

        // When & Then
        mockMvc.perform(get("/api/v1/minio/images"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void shouldReturnSingleImage() throws Exception {
        // Given
        List<String> images = List.of("http://minio/images/single.jpg?presigned");

        when(minioService.getAllImages()).thenReturn(images);

        // When & Then
        mockMvc.perform(get("/api/v1/minio/images"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void shouldCallGetAllImagesExactlyOnce() throws Exception {
        // Given
        when(minioService.getAllImages()).thenReturn(Collections.emptyList());

        // When
        mockMvc.perform(get("/api/v1/minio/images"))
                .andExpect(status().isOk());

        // Then
        verify(minioService, times(1)).getAllImages();
    }

    // ===================== GET IMAGES BY NOTIFICATION ID ENDPOINT TESTS =====================

    @Test
    void shouldGetImagesByNotificationIdSuccessfully() throws Exception {
        // Given
        List<String> images = Arrays.asList(
                "http://minio/presigned/notification1-image1.jpg",
                "http://minio/presigned/notification1-image2.jpg"
        );

        when(minioService.getImagesByNotificationId(1)).thenReturn(images);

        // When & Then
        mockMvc.perform(get("/api/v1/minio/images/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2));

        verify(minioService).getImagesByNotificationId(1);
    }

    @Test
    void shouldPassCorrectNotificationIdToGetImages() throws Exception {
        // Given
        when(minioService.getImagesByNotificationId(123)).thenReturn(Collections.emptyList());

        // When
        mockMvc.perform(get("/api/v1/minio/images/123"))
                .andExpect(status().isOk());

        // Then
        verify(minioService).getImagesByNotificationId(123);
    }

    @Test
    void shouldReturnEmptyListForNotificationWithNoImages() throws Exception {
        // Given
        when(minioService.getImagesByNotificationId(999)).thenReturn(Collections.emptyList());

        // When & Then
        mockMvc.perform(get("/api/v1/minio/images/999"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void shouldReturnMultipleImagesForNotification() throws Exception {
        // Given
        List<String> images = Arrays.asList(
                "url1", "url2", "url3", "url4", "url5"
        );

        when(minioService.getImagesByNotificationId(5)).thenReturn(images);

        // When & Then
        mockMvc.perform(get("/api/v1/minio/images/5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(5));
    }

    // ===================== PATH VARIABLE TESTS =====================

    @Test
    void shouldHandleZeroNotificationId() throws Exception {
        // Given
        when(minioService.getImagesByNotificationId(0)).thenReturn(Collections.emptyList());

        // When & Then
        mockMvc.perform(get("/api/v1/minio/images/0"))
                .andExpect(status().isOk());

        verify(minioService).getImagesByNotificationId(0);
    }

    @Test
    void shouldHandleLargeNotificationId() throws Exception {
        // Given
        when(minioService.getImagesByNotificationId(Integer.MAX_VALUE)).thenReturn(Collections.emptyList());

        // When & Then
        mockMvc.perform(get("/api/v1/minio/images/" + Integer.MAX_VALUE))
                .andExpect(status().isOk());

        verify(minioService).getImagesByNotificationId(Integer.MAX_VALUE);
    }

    // ===================== CONTENT TYPE TESTS =====================

    @Test
    void shouldAcceptMultipartFormData() throws Exception {
        // Given
        MockMultipartFile file = new MockMultipartFile(
                "file", "test.jpg", "image/jpeg", "content".getBytes()
        );

        DefaultResponse response = DefaultResponse.builder().message("Success").build();
        when(minioService.uploadImageToMinio(any(), any())).thenReturn(response);

        // When & Then
        mockMvc.perform(multipart("/api/v1/minio/upload")
                        .file(file)
                        .param("ai-service-notification-id", "1")
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isOk());
    }

    @Test
    void shouldReturnJsonFromGetAllImages() throws Exception {
        // Given
        when(minioService.getAllImages()).thenReturn(List.of("url1", "url2"));

        // When & Then
        mockMvc.perform(get("/api/v1/minio/images"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    void shouldReturnJsonFromGetImagesByNotificationId() throws Exception {
        // Given
        when(minioService.getImagesByNotificationId(1)).thenReturn(List.of("url1"));

        // When & Then
        mockMvc.perform(get("/api/v1/minio/images/1"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    // ===================== VERIFICATION TESTS =====================

    @Test
    void shouldVerifyUploadServiceIsCalledOnce() throws Exception {
        // Given
        MockMultipartFile file = new MockMultipartFile(
                "file", "test.jpg", "image/jpeg", "content".getBytes()
        );

        DefaultResponse response = DefaultResponse.builder().message("Success").build();
        when(minioService.uploadImageToMinio(any(), any())).thenReturn(response);

        // When
        mockMvc.perform(multipart("/api/v1/minio/upload")
                        .file(file)
                        .param("ai-service-notification-id", "1"))
                .andExpect(status().isOk());

        // Then
        verify(minioService, times(1)).uploadImageToMinio(any(), any());
    }

    @Test
    void shouldVerifyGetImagesByNotificationIdIsCalledOnce() throws Exception {
        // Given
        when(minioService.getImagesByNotificationId(anyInt())).thenReturn(Collections.emptyList());

        // When
        mockMvc.perform(get("/api/v1/minio/images/1"))
                .andExpect(status().isOk());

        // Then
        verify(minioService, times(1)).getImagesByNotificationId(anyInt());
    }

    // ===================== DIFFERENT FILE TYPES TESTS =====================

    @Test
    void shouldHandleJpegUpload() throws Exception {
        // Given
        MockMultipartFile jpegFile = new MockMultipartFile(
                "file", "photo.jpeg", "image/jpeg", "jpeg content".getBytes()
        );

        DefaultResponse response = DefaultResponse.builder().message("Success").build();
        when(minioService.uploadImageToMinio(any(), any())).thenReturn(response);

        // When & Then
        mockMvc.perform(multipart("/api/v1/minio/upload")
                        .file(jpegFile)
                        .param("ai-service-notification-id", "1"))
                .andExpect(status().isOk());
    }

    @Test
    void shouldHandlePngUpload() throws Exception {
        // Given
        MockMultipartFile pngFile = new MockMultipartFile(
                "file", "image.png", "image/png", "png content".getBytes()
        );

        DefaultResponse response = DefaultResponse.builder().message("Success").build();
        when(minioService.uploadImageToMinio(any(), any())).thenReturn(response);

        // When & Then
        mockMvc.perform(multipart("/api/v1/minio/upload")
                        .file(pngFile)
                        .param("ai-service-notification-id", "1"))
                .andExpect(status().isOk());
    }

    @Test
    void shouldHandleGifUpload() throws Exception {
        // Given
        MockMultipartFile gifFile = new MockMultipartFile(
                "file", "animation.gif", "image/gif", "gif content".getBytes()
        );

        DefaultResponse response = DefaultResponse.builder().message("Success").build();
        when(minioService.uploadImageToMinio(any(), any())).thenReturn(response);

        // When & Then
        mockMvc.perform(multipart("/api/v1/minio/upload")
                        .file(gifFile)
                        .param("ai-service-notification-id", "1"))
                .andExpect(status().isOk());
    }

    @Test
    void shouldHandleWebpUpload() throws Exception {
        // Given
        MockMultipartFile webpFile = new MockMultipartFile(
                "file", "image.webp", "image/webp", "webp content".getBytes()
        );

        DefaultResponse response = DefaultResponse.builder().message("Success").build();
        when(minioService.uploadImageToMinio(any(), any())).thenReturn(response);

        // When & Then
        mockMvc.perform(multipart("/api/v1/minio/upload")
                        .file(webpFile)
                        .param("ai-service-notification-id", "1"))
                .andExpect(status().isOk());
    }
}

