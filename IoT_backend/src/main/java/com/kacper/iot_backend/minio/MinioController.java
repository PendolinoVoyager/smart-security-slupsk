package com.kacper.iot_backend.minio;

import com.kacper.iot_backend.utils.DefaultResponse;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/minio")
public class MinioController
{
    private final MinioService minioService;

    public MinioController(MinioService minioService) {
        this.minioService = minioService;
    }

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public DefaultResponse upload(
            @ModelAttribute UploadImageRequest request,
            @RequestParam("notification-id") Integer notificationId
    ) {
        return minioService.uploadNotificationImageToMinio(request, notificationId);
    }



    @GetMapping("/images")
    public List<String> getAllImages() {
        return minioService.getAllImages("images");
    }

    @GetMapping("/images/{notificationId}")
    public List<String> getImagesByNotificationId(HttpServletRequest servletRequest, @PathVariable Integer notificationId) {
        String ipAddress = servletRequest.getRemoteAddr();
        if (!minioService.checkIpAllowed(ipAddress)) {
            throw new RuntimeException("IP address is not allowed");
        }
        return minioService.getImagesByNotificationId(notificationId);
    }
}
