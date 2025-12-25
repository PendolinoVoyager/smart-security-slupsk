import requests
import enum
import cv2
BACKEND_URL = "http://127.0.0.1:8080/"
BACKEND_POST_NOTIF_URL = "api/v1/notification/ai-service"
BACKEND_POST_IMAGE_URL = "api/v1/minio/upload"
class NotificationType(enum.Enum):
    Info = "Info"
    Warning = "Warning"
    Critical = "Critical"
    Visit = "Visit"


def send_notification(type: NotificationType, message: str) -> int:
    payload = {
        "type": type.value,
        "message": message
    }

    headers = {"Content-Type": "application/json"}

    response = requests.post(BACKEND_URL + BACKEND_POST_NOTIF_URL, json=payload, headers=headers)
    if response.status_code != 200:
        print(f"Failed to send notification: {response.text}")
        exit(1)
    
    res = response.json()
    print(res)
    return res["id"]

def send_image(notification_id: int, frame):
    success, encoded = cv2.imencode(".jpg", frame)

    if not success:
        raise RuntimeError("Failed to encode frame")
    
    image_bytes = encoded.tobytes()

    files = {
    "file": (
        "frame.jpg",        # originalFilename
        image_bytes,        # content
        "image/jpeg"        # contentType
        )
    }

    response = requests.post(f"{BACKEND_URL}{BACKEND_POST_IMAGE_URL}?ai-service-notification-id={notification_id}", files=files)

    print(response.status_code)
    print(response.text)