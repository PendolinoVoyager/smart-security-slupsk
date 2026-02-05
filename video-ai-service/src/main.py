
# These lines need to be called first in this order to init the service properly
from config import parse_args, update_globals
args = parse_args()
update_globals(args)

import torch

device = "cuda" if torch.cuda.is_available() else "cpu"

import cv2
from streamManager import StreamManager
import time

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

        handle_streams(manager)
        time.sleep(FRAME_SLEEP_SEC)

def _main_loop_debug():
    manager = StreamManager.__new__(StreamManager)
    StreamManager._init_pipeline(manager)
    manager.streams[100] = cv2.VideoCapture(0)
    for pipeline_element in manager.pipeline:
        pipeline_element.on_stream_start(100)
    while True:
        handle_streams(manager)
        time.sleep(FRAME_SLEEP_SEC)

def handle_streams(manager: StreamManager):
    # Read frames from all active streams
    for device_id, cap in list(manager.streams.items()):
        if not cap.isOpened():
            print(f"[stream {device_id}] capture closed, removing")
            manager.delete_stream(device_id)
            continue

        try:
            manager.pipe_stream(device_id)    
        except Exception as e:
            print(f"[stream {device_id}] processing error: {e}")


if __name__ == "__main__":
    from config import DEBUG

    if DEBUG:
        _main_loop_debug()

    main_loop()
