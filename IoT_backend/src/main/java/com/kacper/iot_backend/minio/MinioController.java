package com.kacper.iot_backend.minio;

import com.kacper.iot_backend.utils.DefaultResponse;
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
        return minioService.uploadImageToMinio(request, notificationId);
    }

    @GetMapping("/images")
    public List<String> getAllImages() {
        return minioService.getAllImages();
    }

    @GetMapping("/images/{notificationId}")
    public List<String> getImagesByNotificationId(@PathVariable Integer notificationId) {
        return minioService.getImagesByNotificationId(notificationId);
    }
}
