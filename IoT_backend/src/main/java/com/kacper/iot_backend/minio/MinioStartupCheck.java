package com.kacper.iot_backend.minio;

import io.minio.BucketExistsArgs;
import io.minio.MinioClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class MinioStartupCheck implements CommandLineRunner
{
    private final MinioClient minioClient;

    @Value("${minio.bucket}")
    private String bucket;

    public MinioStartupCheck(MinioClient minioClient) {
        this.minioClient = minioClient;
    }

    @Override
    public void run(String... args) throws Exception {
        boolean exists = minioClient.bucketExists(
                BucketExistsArgs.builder().bucket(bucket).build()
        );

        if (!exists) {
            throw new IllegalStateException("MinIO: bucket '" + bucket + "' does not exist");
        }

        System.out.println("MinIO OK: connected, bucket exists = " + bucket);
    }
}
