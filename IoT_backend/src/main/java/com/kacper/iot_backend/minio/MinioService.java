package com.kacper.iot_backend.minio;

import com.kacper.iot_backend.notification_images.NotificationImage;
import com.kacper.iot_backend.notification_images.NotificationImageRepository;
import com.kacper.iot_backend.utils.DefaultResponse;

import io.minio.*;
import io.minio.http.Method;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import com.kacper.iot_backend.exception.DeviceOwnerMismatchException;
import com.kacper.iot_backend.exception.ResourceNotFoundException;
import com.kacper.iot_backend.notification.Notification;
import com.kacper.iot_backend.notification.NotificationRepository;


@Service
public class MinioService {
    private final MinioClient minioClient;
    private final NotificationRepository notificationRepository;
    private final NotificationImageRepository notificationImageRepository;

    private static final String BUCKET = "images";

    @Value("${minio.faceBucket}")
    private String FACE_BUCKET;

    @Value("${security.allowed-ips}")
    private List<String> allowedIps;


    @Value("${minio.proxy-url}")
    private String proxyUrl;

    private final static Logger logger = Logger.getLogger(MinioService.class.getName());

    public MinioService(
            MinioClient minioClient,
            NotificationRepository notificationRepository,
            NotificationImageRepository notificationImageRepository
    ) {
        this.minioClient = minioClient;
        this.notificationRepository = notificationRepository;
        this.notificationImageRepository = notificationImageRepository;
    }

    public boolean checkIpAllowed(String ipAddress) {
        return allowedIps.contains(ipAddress);
    }
    
    private static String generateObjectName(String originalFilename) {
        return UUID.randomUUID().toString() + "_" + originalFilename;
    }


    public DefaultResponse uploadNotificationImageToMinio(UploadImageRequest request, Integer notificationId) {
        try {
            var file = request.file();
            if (file == null || file.isEmpty()) {
                return DefaultResponse.builder()
                                    .message("Empty file not allowed")
                                    .build();
            }

            String objectName = uploadImageToMinio(file, BUCKET);
            linkImageToNotification(objectName, notificationId);
            return DefaultResponse.builder()
                    .message("Image uploaded and linked to notification successfully")
                    .build();
        }
        catch (Exception e) {
            return DefaultResponse.builder()
                    .message("Error uploading image: " + e.getMessage())
                    .build();
        }
    }
    /**
     * Uploads an image to MinIO bucket
     * @param request
     * @param bucket
     * @return object name inside the bucket
     * @throws Exception 
     */
    public String uploadImageToMinio(MultipartFile file, String bucket) throws Exception {
        if (file == null || file.isEmpty()) {
            throw new RuntimeException("File is missing");
        }

        String objectName = generateObjectName(file.getOriginalFilename());
        InputStream is = file.getInputStream();
        PutObjectArgs putArgs = PutObjectArgs.builder()
                .bucket(bucket)
                .object(objectName)
                .stream(is, file.getSize(), -1)
                .contentType(file.getContentType())
                .build();
        minioClient.putObject(putArgs);
        return objectName;
    }

    private void linkImageToNotification(String objectName, Integer notificationId) {
        Notification notification = notificationRepository.findById(notificationId).orElseThrow(() ->
                new RuntimeException("Notification not found with id: " + notificationId)
        );
        String imageUrl = BUCKET + "/" + objectName;
        NotificationImage notificationImage = NotificationImage.builder()
                .imageUrl(imageUrl)
                .notification(notification)
                .build();
        logger.info("Saving notification image" + notificationImage);
        notificationImageRepository.save(notificationImage);
    }


    public List<String> getImagesByNotificationIdForUser(UserDetails userDetails, Integer notificationId) throws Exception {
        
        Notification notif = notificationRepository.findById(notificationId)
                            .orElseThrow(() -> new ResourceNotFoundException("Device does not exist"));
        if (!notif.getDevice().getUser().getEmail().equals(userDetails.getUsername())) {
            throw new DeviceOwnerMismatchException("This user does not own this device.");
        }
        return getImagesByNotificationId(notificationId);

    }

    private List<String> getImagesByNotificationId(Integer notificationId) {
        List<NotificationImage> notificationImages = notificationImageRepository.findByNotificationId(notificationId);

        List<String> imageUrls = new ArrayList<>();
        for (NotificationImage image : notificationImages) {
            try {
                String objectName = image.getImageUrl().replace(BUCKET + "/", "");
                String url = generateUrlByBucketAndName(BUCKET, objectName);
                imageUrls.add(url);
            } catch (Exception e) {
                throw new RuntimeException("Error generating presigned URL", e);
            }
        }
        return imageUrls;
    }
    public String generateUrlByBucketAndName(String bucket, String objectName) throws Exception {
        String baseUrl = minioClient.getPresignedObjectUrl(
                GetPresignedObjectUrlArgs.builder()
                        .method(Method.GET)
                        .bucket(bucket)
                        .object(objectName)
                        .expiry(30, TimeUnit.MINUTES)
                        .build()
        );

        if (proxyUrl == null || proxyUrl.isBlank()) {
            return baseUrl;
        }

        URI original = URI.create(baseUrl);
        URI proxy = URI.create(proxyUrl);

        // Replace scheme + host + port, keep path & query
        URI rewritten = new URI(
                proxy.getScheme(),
                null,
                proxy.getHost(),
                proxy.getPort(),
                original.getPath(),
                original.getQuery(),
                null
        );

        return rewritten.toString();
    }
    public void deleteImageFromMinio(String bucket, String objectName) throws Exception {
        minioClient.removeObject(
                RemoveObjectArgs.builder()
                        .bucket(bucket)
                        .object(objectName)
                        .build()
        );
    }
}
