package com.kacper.iot_backend.minio;

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
import java.util.logging.Logger;

import com.kacper.iot_backend.notification.Notification;
import com.kacper.iot_backend.notification.NotificationRepository;
import com.kacper.iot_backend.notification.NotificationService;


@Service
public class MinioService {
    private final MinioClient minioClient;
    private final NotificationRepository notificationRepository;
    private final NotificationImageRepository notificationImageRepository;
    private static final String BUCKET = "images";
    private final static Logger logger = Logger.getLogger(NotificationService.class.getName());

    public MinioService(
            MinioClient minioClient,
            NotificationRepository notificationRepository,
            NotificationImageRepository notificationImageRepository
    ) {
        this.minioClient = minioClient;
        this.notificationRepository = notificationRepository;
        this.notificationImageRepository = notificationImageRepository;
    }

    public DefaultResponse uploadImageToMinio(UploadImageRequest request, Integer notificationId) {
        MultipartFile file = request.file();
        if (file == null || file.isEmpty()) {
            return DefaultResponse.builder()
                    .message("Empty file")
                    .build();
        }

        Notification notification = notificationRepository.findById(notificationId).orElseThrow(() ->
                new RuntimeException("Notification not found with id: " + notificationId)
        );

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
                    .notification(notification)
                    .build();
            logger.info("Saving notification image" + notificationImage);
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
        List<NotificationImage> notificationImages = notificationImageRepository.findByNotificationId(notificationId);

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
