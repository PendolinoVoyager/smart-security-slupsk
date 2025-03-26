import cv2
import requests
STREAM_SERVER_URL = "http://127.0.0.1:9000/udp_stream_start"  
DEVICE_ID = 100 
HOST = "127.0.0.1"
PORT = 5000  

def get_capture():
    payload = {
        "device_id": DEVICE_ID,
        "address": f"{HOST}:{PORT}"
    }

    headers = {"Content-Type": "application/json"}

    response = requests.get(STREAM_SERVER_URL, json=payload, headers=headers)
    if response.status_code != 200:
        print(f"Failed to start stream: {response.text}")
        exit(1)

    print(f"Streaming from udp://{HOST}:{PORT}")

    # OpenCV capture from UDP stream
    cap = cv2.VideoCapture(f"udp://{HOST}:{PORT}?fifo_size=5000000&overrun_nonfatal=1", cv2.CAP_FFMPEG)

    if not cap.isOpened():
        raise Exception("capture failed: ")
    return cap
