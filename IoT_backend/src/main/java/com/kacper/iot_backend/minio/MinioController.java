package com.kacper.iot_backend.minio;

import com.kacper.iot_backend.exception.DeviceOwnerMismatchException;
import com.kacper.iot_backend.exception.ResourceNotFoundException;
import com.kacper.iot_backend.utils.DefaultResponse;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
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


    @GetMapping("/images/{notificationId}")
    public ResponseEntity<?> getImagesByNotificationId(@AuthenticationPrincipal UserDetails userDetails,
                                                  @PathVariable Integer notificationId) {
        try {
            var notifs = minioService.getImagesByNotificationIdForUser(userDetails, notificationId);
            return new ResponseEntity<List<String>>(notifs, HttpStatus.OK);
        }
        catch (ResourceNotFoundException e) {
            return new ResponseEntity<String>(e.getMessage(), HttpStatus.NOT_FOUND);
        }
        catch (DeviceOwnerMismatchException e) {
            return new ResponseEntity<String>(e.getMessage(), HttpStatus.FORBIDDEN);
        }
        catch (Exception e) {
            return new ResponseEntity<String>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
