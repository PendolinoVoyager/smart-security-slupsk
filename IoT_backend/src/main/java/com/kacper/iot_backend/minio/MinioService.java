package com.kacper.iot_backend.minio;

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
    private static final String BUCKET = "images";

    public MinioService(MinioClient minioClient) {
        this.minioClient = minioClient;
    }

    public DefaultResponse uploadImageToMinio(UploadImageRequest request) {
        MultipartFile file = request.file();
        if (file == null || file.isEmpty()) {
            return DefaultResponse.builder()
                    .message("Empty file")
                    .build();
        }

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
}
