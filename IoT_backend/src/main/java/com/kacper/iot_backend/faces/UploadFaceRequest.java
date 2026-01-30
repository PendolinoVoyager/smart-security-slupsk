package com.kacper.iot_backend.faces;

import org.springframework.web.multipart.MultipartFile;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record UploadFaceRequest(
    @NotNull(message = "Device ID is required")
    int deviceId,

    @NotBlank(message = "Face name is required")
    String faceName,

    MultipartFile file
)
{
    
};
