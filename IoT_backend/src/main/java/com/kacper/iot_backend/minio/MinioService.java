package com.kacper.iot_backend.minio;

import com.kacper.iot_backend.ai_service_notification.AiServiceNotification;
import com.kacper.iot_backend.ai_service_notification.AiServiceNotificationRepository;
import com.kacper.iot_backend.notification_images.NotificationImage;
import com.kacper.iot_backend.notification_images.NotificationImageRepository;
import com.kacper.iot_backend.utils.DefaultResponse;
import io.minio.*;
import io.minio.http.Method;
import io.minio.messages.Item;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
public class MinioService {
    private final MinioClient minioClient;
    private final AiServiceNotificationRepository aiServiceNotificationRepository;
    private final NotificationImageRepository notificationImageRepository;
    private static final String BUCKET = "images";

    public MinioService(
            MinioClient minioClient,
            AiServiceNotificationRepository aiServiceNotificationRepository,
            NotificationImageRepository notificationImageRepository
    ) {
        this.minioClient = minioClient;
        this.aiServiceNotificationRepository = aiServiceNotificationRepository;
        this.notificationImageRepository = notificationImageRepository;
    }

    public DefaultResponse uploadImageToMinio(UploadImageRequest request, Integer aiServiceNotificationId) {
        MultipartFile file = request.file();
        if (file == null || file.isEmpty()) {
            return DefaultResponse.builder()
                    .message("Empty file")
                    .build();
        }

        AiServiceNotification notification = aiServiceNotificationRepository.findById(aiServiceNotificationId)
                .orElseThrow(() -> new RuntimeException("Notification not found with id: " + aiServiceNotificationId));

        try {
            boolean exists = minioClient.bucketExists(BucketExistsArgs.builder().bucket(BUCKET).build());
            if (!exists) {
                minioClient.makeBucket(MakeBucketArgs.builder().bucket(BUCKET).build());
            }

            String objectName = UUID.randomUUID().toString() + "_" + file.getOriginalFilename();
            try (InputStream is = file.getInputStream()) {
                PutObjectArgs putArgs = PutObjectArgs.builder()
                        .bucket(BUCKET)
                        .object(objectName)
                        .stream(is, file.getSize(), -1)
                        .contentType(file.getContentType())
                        .build();
                minioClient.putObject(putArgs);
            }

            String imageUrl = BUCKET + "/" + objectName;
            NotificationImage notificationImage = NotificationImage.builder()
                    .imageUrl(imageUrl)
                    .aiServiceNotification(notification)
                    .build();
            notificationImageRepository.save(notificationImage);

            return DefaultResponse.builder()
                    .message("File has been saved: " + objectName)
                    .build();
        } catch (Exception e) {
            return DefaultResponse.builder()
                    .message("Error? xD: " + e.getMessage())
                    .build();
        }
    }

    public List<String> getAllImages() {
        List<String> images = new ArrayList<>();

        try {
            if (!minioClient.bucketExists(
                    BucketExistsArgs.builder().bucket(BUCKET).build())) {
                return images;
            }

            Iterable<Result<Item>> results = minioClient.listObjects(
                    ListObjectsArgs.builder()
                            .bucket(BUCKET)
                            .build()
            );

            for (Result<Item> result : results) {
                String objectName = result.get().objectName();

                String url = minioClient.getPresignedObjectUrl(
                        GetPresignedObjectUrlArgs.builder()
                                .method(Method.GET)
                                .bucket(BUCKET)
                                .object(objectName)
                                .expiry(30, TimeUnit.MINUTES)
                                .build()
                );

                images.add(url);
            }

        } catch (Exception e) {
            throw new RuntimeException("Error listing images", e);
        }

        return images;
    }

    public List<String> getImagesByNotificationId(Integer notificationId) {
        List<NotificationImage> notificationImages = notificationImageRepository.findByAiServiceNotificationId(notificationId);

        List<String> imageUrls = new ArrayList<>();
        for (NotificationImage image : notificationImages) {
            try {
                String objectName = image.getImageUrl().replace(BUCKET + "/", "");
                String url = minioClient.getPresignedObjectUrl(
                        GetPresignedObjectUrlArgs.builder()
                                .method(Method.GET)
                                .bucket(BUCKET)
                                .object(objectName)
                                .expiry(30, TimeUnit.MINUTES)
                                .build()
                );
                imageUrls.add(url);
            } catch (Exception e) {
                throw new RuntimeException("Error generating presigned URL", e);
            }
        }
        return imageUrls;
    }
}
