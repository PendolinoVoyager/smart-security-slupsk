package com.kacper.iot_backend.minio;

import org.springframework.web.multipart.MultipartFile;

public record UploadImageRequest(
    MultipartFile file
)
{};
