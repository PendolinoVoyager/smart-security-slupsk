import requests
import enum
BACKEND_URL = "http://127.0.0.1:8000/api/v1/notification/debug_add"  
class NotificationType(enum.Enum):
    Info = "Info"
    Warning = "Warning"
    Critical = "Critical"


def send_notification(type: NotificationType, message: str):
    payload = {
        "type": type.value,
        "message": message
    }

    headers = {"Content-Type": "application/json"}

    response = requests.post(BACKEND_URL, json=payload, headers=headers)
    if response.status_code != 200:
        print(f"Failed to send notification: {response.text}")
        exit(1)

    print(response.json())
