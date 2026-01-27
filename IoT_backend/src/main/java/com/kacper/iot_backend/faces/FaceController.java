package com.kacper.iot_backend.faces;

import com.kacper.iot_backend.utils.DefaultResponse;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

import java.util.List;
import java.util.logging.Logger;

import org.springframework.data.crossstore.ChangeSetPersister.NotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/faces")
public class FaceController
{   
    private final static Logger logger = Logger.getLogger(FaceController.class.getName());
    private final FaceService faceService;
    public FaceController(FaceService faceService) {
        this.faceService = faceService;
    }

      /**
     * This endpoint returns 403 like other non-existing paths some accesses it so that potential attackers think it doesn't exist.
     * But in reality, they have a wrong Ip address and are blocked.
     * Smart. Genius.
     * @param request
     * @param deviceId
     * @return
     * @throws NotFoundException
     */
    @GetMapping(value = "/ai-service/{deviceId}")
    public ResponseEntity<List<FaceResponse>> getAllImagesService(
        HttpServletRequest request,
        @PathVariable Integer deviceId
    ) throws NotFoundException {
        try {
            return new ResponseEntity<>(faceService.getAllFacesByDeviceIdIpProtected(request, deviceId), HttpStatus.OK);
        }
        catch (Exception e) {
            logger.warning("AI service access failed: " + e);
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }
    }
    
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public DefaultResponse upload(
            @AuthenticationPrincipal UserDetails userDetails, 
            @ModelAttribute UploadFaceRequest request

    ) {
        return faceService.saveFace(request, userDetails);
    }

    @GetMapping("/{deviceId}")
    public List<FaceResponse> getAllImages(
        @AuthenticationPrincipal UserDetails userDetails,
        @PathVariable Integer deviceId) throws Exception {
        logger.info("Accessed getAllImages for user");

        return faceService.getAllFacesByDeviceIdForUser(userDetails, deviceId);
    }

    @DeleteMapping("/{faceId}")
    public FaceResponse deleteFace(
        @AuthenticationPrincipal UserDetails userDetails,
        @PathVariable Integer faceId) {
        return faceService.deleteFaceById(userDetails, faceId);
    }

    @PatchMapping("/{faceId}")
    public FaceResponse updateFaceName(
        @AuthenticationPrincipal UserDetails userDetails,
        @Valid @RequestBody  ChangeFaceNameRequest request,
        @PathVariable Integer faceId) {

        return faceService.changeFaceName(userDetails, request, faceId);
    }

  
}