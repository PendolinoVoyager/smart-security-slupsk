
import cv2
from matplotlib.pylab import ndarray
import numpy as np
from pipelineElement import PipelineElement
import face_recognition
from backend_api import fetch_faces_for_device, send_notification, send_image, NotificationType
import time
import os
import tempfile
import requests

FACE_RECOGNIZER_NAME = "FR"
TIME_PER_RECOGNITION_ATTEMPT = 0.5
# after TIME_PER_ATTEMPT * ATTEMPTS secs it will freeze the facial recognition
STOP_AFTER_NUMEBR_OF_NO_FACES = 15 
TOLERANCE = 0.5
TARGET_HEIGHT_FOR_FACE_RECOGNITON = 480

class DeviceFaceRecognitionData:
    def __init__(self, device_id: int):
        self.device_id = device_id
        self.encodings = []
        self.names = []
        self.meta_last_recognition_time = time.time()
        self.meta_no_faces_strikes = 0
        self.meta_reported_faces = set()
        self.meta_refetch_data = True

        # private temp storage
        self._temp_dir = tempfile.TemporaryDirectory(prefix=f"fr_device_{device_id}_")

    # -------------------------
    # private helpers
    # -------------------------

    def _download_image_to_temp(self, url: str) -> str | None:
        """
        Downloads an image from URL into the device temp directory.
        Returns local file path or None on failure.
        """
        try:
            response = requests.get(url, timeout=5)
            response.raise_for_status()

            filename = os.path.basename(url.split("?")[0]) or "face.jpg"
            path = os.path.join(self._temp_dir.name, filename)

            with open(path, "wb") as f:
                f.write(response.content)

            return path

        except Exception as e:
            print(f"[FR][WARN] Failed to download image {url}: {e}")
            return None

    def _cleanup_temp_dir(self):
        """
        Explicit cleanup (optional, TemporaryDirectory also auto-cleans)
        """
        if self._temp_dir:
            self._temp_dir.cleanup()
            self._temp_dir = None

    # -------------------------
    # public methods
    # -------------------------

    def update_data(self):
        print(f"[FR] Updating face data for device {self.device_id}")

        images = fetch_faces_for_device(self.device_id)

        self.names.clear()
        self.encodings.clear()

        for image in images:
            url = image.get("imageUrl")
            name = image.get("name")

            if not url or not name:
                continue

            path = self._download_image_to_temp(url)
            if not path:
                continue

            try:
                img = face_recognition.load_image_file(path)
                encs = face_recognition.face_encodings(img)

                if not encs:
                    print(f"[FR][WARN] No face found in image for {name}")
                    continue

                self.encodings.append(encs[0])
                self.names.append(name)

            except Exception as e:
                print(f"[FR][ERROR] Failed processing face image for {name}: {e}")

        print(f"[FR] Loaded {len(self.encodings)} known faces")

    def should_make_recognition_attempt(self):
        now = time.time()
        if now - self.meta_last_recognition_time >= TIME_PER_RECOGNITION_ATTEMPT:
            self.meta_last_recognition_time = now
            return True
        return False


class FaceRecognizerElement(PipelineElement):

    NAME = FACE_RECOGNIZER_NAME
    _face_database: dict[int, str] = {}
    encoding_data: dict[int, DeviceFaceRecognitionData]
    def __init__(self, manager):
        self.encoding_data = {}
        super().__init__(manager)

    
    def _load_faces_for_device(self, device_id):
        # Placeholder for loading face database logic
        print(f"Loading face database for device {device_id}")
        return "Loaded Face Database"
    
    def _recognize(self, image):
        # Placeholder for face recognition logic
        print("Recognizing face in the provided image")
        return "Recognized Face"
    
    def on_stream_start(self, device_id):
        print("Starting face recognizer for stream", device_id)
        self.encoding_data[device_id] = DeviceFaceRecognitionData(device_id)
        self.freeze(device_id) # No need to process for now

    def on_stream_end(self, device_id):
        del self.encoding_data[device_id]
        self.unfreeze(device_id)
    
    def on_frame(self, device_id, frame):
        data = self.encoding_data[device_id]
        if data.meta_refetch_data:
            data.update_data()
            data.meta_refetch_data = False
        #process
        if not data.should_make_recognition_attempt():
            return
        resolution = frame.shape[:2]
        FRAME_RESIZE = TARGET_HEIGHT_FOR_FACE_RECOGNITON / resolution[0]
        small_frame = cv2.resize(frame, (0, 0), fx=FRAME_RESIZE, fy=FRAME_RESIZE)
        rgb_small_frame = cv2.cvtColor(small_frame, cv2.COLOR_BGR2RGB)
        
        # New faces data from frame
        face_locations = face_recognition.face_locations(rgb_small_frame)
        if len(face_locations) == 0:
            data.meta_no_faces_strikes += 1
            if data.meta_no_faces_strikes >= STOP_AFTER_NUMEBR_OF_NO_FACES:
                data.meta_no_faces_strikes = 0
                data.meta_reported_faces.clear()
                data.meta_refetch_data = True
                self.freeze(device_id)
                return
        
        face_encodings = face_recognition.face_encodings(rgb_small_frame, face_locations)

        for face_encoding in face_encodings:
            distances = face_recognition.face_distance(data.encodings, face_encoding)

            name = "Unknown person"
            if len(distances) > 0:
                best_match = np.argmin(distances)
                if distances[best_match] < TOLERANCE:
                    name = data.names[best_match]
            
            if name in data.meta_reported_faces:
                continue

            notif_id = send_notification(NotificationType.Visit, f"{name} has arrived!", device_id)
            data.meta_reported_faces.add(name)
            send_image(notif_id, frame)
            
        