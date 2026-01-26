package com.kacper.iot_backend.minio;

import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import com.kacper.iot_backend.notification.NotificationService;

@Component
public class MinioStartupCheck implements CommandLineRunner
{
    private final MinioClient minioClient;

    @Value("${minio.bucket}")
    private String bucket;

    @Value("${minio.faceBucket}")
    private String faceBucket;

    private final static Logger logger = Logger.getLogger(NotificationService.class.getName());

    public MinioStartupCheck(MinioClient minioClient) {
        this.minioClient = minioClient;
    }

    @Override
    public void run(String... args) throws Exception {
        checkBucketExistsAndCreateIfNot(bucket);
        checkBucketExistsAndCreateIfNot(faceBucket);
    
        System.out.println("MinIO OK: connected, bucket exists = " + bucket);
    }
    private boolean checkBucketExistsAndCreateIfNot(String bucketName) throws Exception {
        boolean exists = minioClient.bucketExists(
                BucketExistsArgs.builder().bucket(bucketName).build()
        );

        if (!exists) {
            logger.log(Level.INFO, "MinIO bucket '{0}' does not exist. Creating...\"", bucketName);
            minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucketName).build());
            logger.log(Level.INFO, "MinIO bucket '{0}' created.", bucketName);
        }
        return exists;
    }
}
