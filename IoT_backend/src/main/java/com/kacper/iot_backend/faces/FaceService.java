package com.kacper.iot_backend.faces;

import com.kacper.iot_backend.utils.DefaultResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.logging.Logger;

import com.kacper.iot_backend.device.Device;
import com.kacper.iot_backend.device.DeviceRepository;
import com.kacper.iot_backend.minio.MinioService;
import com.kacper.iot_backend.notification.NotificationService;


@Service
public class FaceService {
    private final FaceRepository faceRepository;
    private final DeviceRepository deviceRepository;
    private final MinioService minioService;
    @Value("${minio.faceBucket}")
    private String FACE_BUCKET;

    private final static Logger logger = Logger.getLogger(NotificationService.class.getName());

    public FaceService(
            FaceRepository faceRepository,
            MinioService minioService,
            DeviceRepository deviceRepository
    ) {
        this.faceRepository = faceRepository;
        this.minioService = minioService;
        this.deviceRepository = deviceRepository;
    }

    private boolean isUserOwnerOfDevice(UserDetails userDetails, Device device) {
        return device.getUser().getEmail().equals(userDetails.getUsername());
    }

    public DefaultResponse saveFace(UploadFaceRequest request,
                                    UserDetails userDetails) 
                                    {
        logger.log(java.util.logging.Level.INFO, "Saving face for user: " + userDetails.getUsername() + " with face name: " + request.faceName());
        
        try {
            MultipartFile file = request.file();
            if (file == null || file.isEmpty()) {
                return new DefaultResponse("Empty file not allowed.");
            }
            String fileName = minioService.uploadImageToMinio(file, FACE_BUCKET); 
            Device device = deviceRepository.findById(request.deviceId())
                .orElseThrow(() -> new Exception("No device found with id: " + request.deviceId()));

            if (!isUserOwnerOfDevice(userDetails, device)) {
                throw new SecurityException("User does not own the device");
            } 
            Face face = Face.builder()
                    .faceName(request.faceName())
                    .imageUrl(FACE_BUCKET + "/" + fileName)
                    .device(device)
                    .build();
            faceRepository.save(face);
            return new DefaultResponse("Face saved as " + fileName);
        }
        catch (Exception e) {
            logger.log(java.util.logging.Level.SEVERE, "Error uploading face image: " + e.getMessage());
            return new DefaultResponse("Error uploading face image: " + e.getMessage());
        }

    }

    public List<FaceResponse> getAllFacesByDeviceId(UserDetails userDetails, int deviceId) throws Exception {
        var device = deviceRepository.findById(deviceId)
                .orElseThrow(() -> new Exception("No device found with id: " + deviceId));

        if (!isUserOwnerOfDevice(userDetails, device)) {
            throw new SecurityException("User does not own the device");
        }

        try {
            var faces = faceRepository.findByDeviceId(deviceId);
        List<FaceResponse> facesWithCorrectUrl = new java.util.ArrayList<>();
        for (Face face : faces) {
            String faceName = face.getImageUrl().replace(FACE_BUCKET+"/", "");
            facesWithCorrectUrl.add(new FaceResponse(
                face.getId(),
                face.getFaceName(),
                minioService.generateUrlByBucketAndName(FACE_BUCKET, faceName)
            ));
        }
        return facesWithCorrectUrl;
    
        } catch (Exception e) {
            throw e;
        }
    }

    public FaceResponse changeFaceName(UserDetails userDetails,
        ChangeFaceNameRequest request,
        Integer faceId) {
        try {
            Face face = faceRepository.findById(faceId)
                    .orElseThrow(() -> new Exception("No face found with id: " + faceId));

            if (!isUserOwnerOfDevice(userDetails, face.getDevice())) {
                throw new SecurityException("User does not own the face's device");
            }
            face.setFaceName(request.newFaceName());
            faceRepository.save(face);
            return new FaceResponse(
                face.getId(),
                face.getFaceName(),
                face.getImageUrl()
            );
        } catch (Exception e) {
            logger.log(java.util.logging.Level.SEVERE, "Error changing face name: " + e.getMessage());
            throw new RuntimeException("Error changing face name: " + e.getMessage());
        }
    }

    public FaceResponse deleteFaceById(UserDetails userDetails, Integer faceId) {
        try {
            Face face = faceRepository.findById(faceId)
                    .orElseThrow(() -> new Exception("No face found with id: " + faceId));

            if (!isUserOwnerOfDevice(userDetails, face.getDevice())) {
                throw new SecurityException("User does not own the face's device");
            }
            minioService.deleteImageFromMinio(FACE_BUCKET, face.getImageUrl().replace(FACE_BUCKET + "/", ""));
            faceRepository.delete(face);
            return new FaceResponse(
                face.getId(),
                face.getFaceName(),
                face.getImageUrl()
            );
        } catch (Exception e) {
            logger.log(java.util.logging.Level.SEVERE, "Error deleting face: " + e.getMessage());
            throw new RuntimeException("Error deleting face: " + e.getMessage());
        }
    }
}