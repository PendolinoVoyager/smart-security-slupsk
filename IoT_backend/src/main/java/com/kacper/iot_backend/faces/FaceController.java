package com.kacper.iot_backend.faces;

import com.kacper.iot_backend.utils.DefaultResponse;

import jakarta.validation.Valid;

import java.util.List;

import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/faces")
public class FaceController
{   
    private final FaceService faceService;
    public FaceController(FaceService faceService) {
        this.faceService = faceService;
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public DefaultResponse upload(
            @AuthenticationPrincipal UserDetails userDetails, 
            @ModelAttribute UploadFaceRequest request

    ) {
        return faceService.saveFace(request, userDetails);
    }

    @GetMapping(value = "/{deviceId}")
    public List<FaceResponse> getAllImages(
        @AuthenticationPrincipal UserDetails userDetails,
        @PathVariable Integer deviceId) throws Exception {
        return faceService.getAllFacesByDeviceId(userDetails, deviceId);
    }

    @DeleteMapping(value = "/{faceId}")
    public FaceResponse deleteFace(
        @AuthenticationPrincipal UserDetails userDetails,
        @PathVariable Integer faceId) {
        return faceService.deleteFaceById(userDetails, faceId);
    }

    @PatchMapping(value = "/{faceId}")
    public FaceResponse updateFaceName(
        @AuthenticationPrincipal UserDetails userDetails,
        @Valid @RequestBody  ChangeFaceNameRequest request,
        @PathVariable Integer faceId) {

        return faceService.changeFaceName(userDetails, request, faceId);
    }
}