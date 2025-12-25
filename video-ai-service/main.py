import cv2
from streamManager import StreamManager
import time
from backend_api import send_notification, NotificationType, send_image


HAS_SEEN_FRAME = False
def process_frame(device_id: int, frame):
    global HAS_SEEN_FRAME
    if HAS_SEEN_FRAME is False:
        HAS_SEEN_FRAME = True
        notification_id = send_notification(NotificationType.Visit, "You have a frame!")
        if notification_id is not None:
            send_image(notification_id, frame)

STREAM_REFRESH_SEC = 5.0
FRAME_SLEEP_SEC = 0.001  

def main_loop():
    manager = StreamManager()

    last_sync = 0.0

    while True:
        now = time.monotonic()

        if now - last_sync >= STREAM_REFRESH_SEC:
            manager.sync_streams()
            last_sync = now

        # Read frames from all active streams
        for device_id, cap in list(manager.streams.items()):
            if not cap.isOpened():
                print(f"[stream {device_id}] capture closed, removing")
                manager.delete_stream(device_id)
                continue

            ret, frame = cap.read()
            if not ret:
                # UDP hiccup – don't kill stream immediately
                continue

            try:
                process_frame(device_id, frame)
            except Exception as e:
                print(f"[stream {device_id}] processing error: {e}")

        time.sleep(FRAME_SLEEP_SEC)

if __name__ == "__main__":
    main_loop()