import socket
import cv2
import requests
from faceRecognizer import FaceRecognizerElement
from pipelineElement import PipelineElement
from streams_api import fetch_all_streams, make_udp_request
from tracker import TrackingPipeline
from config import STREAMING_SERVER_URL

"""This class is responsible for fetching and updating available video streams from the streaming server"""
class StreamManager:


    streams: dict[int, cv2.VideoCapture] = {}
    pipeline: list[PipelineElement] = []
    __pipeline_by_name: dict[str, PipelineElement] = {}

    def __init__(self):
        self._init_pipeline()
        for id in self.check_missing_streams():
            self.add_stream(id)

    def _init_pipeline(self):
        self.pipeline.append(TrackingPipeline(self))
        self.pipeline.append(FaceRecognizerElement(self))
        self.__pipeline_by_name = {el.NAME: el for el in self.pipeline}
        print("Initialized pipeline with elements:", [el.NAME for el in self.pipeline])
    """Delete or add streams based on their availability"""
    def sync_streams(self):
        try:
            current_ids = set(fetch_all_streams())
        except Exception as e:
            print(f"[sync] failed: {e}")
            return

        existing_ids = set(self.streams.keys())

        for device_id in current_ids - existing_ids:
            try:
                print(f"[sync] adding stream {device_id}")
                self.add_stream(device_id)
            except Exception as e:
                print(f"[sync] failed to add {device_id}: {e}")

        for device_id in existing_ids - current_ids:
            print(f"[sync] removing stream {device_id}")
            self.delete_stream(device_id)

    """Check and return missing device IDs which are not being processed"""
    def check_missing_streams(self):
        streams = fetch_all_streams()
        missing = []
        for stream in streams:
            if self.streams.get(stream) is None:
                missing.append(stream)
        return missing
    
    def add_stream(self, device_id):
        if self.streams.get(device_id) is not None:
            print(f"Failed to add stream {device_id}, stream exists already.")
            return

        self.streams[device_id] = self.__open_udp_capture(device_id)
        
        # init pipeline for this stream
        for element in self.pipeline:
            element.on_stream_start(device_id)

    
    def delete_stream(self, device_id):
        cap = self.streams[device_id]
        if cap is None:
            print(f"Attempted to delete {device_id} even though it was empty")
            return
        cap.release()

        # clean up pipelines
        for element in self.pipeline:
            element.on_stream_end(device_id)

        del self.streams[device_id]


    def __open_udp_capture(self, device_id):
        port = StreamManager.__get_free_udp_port()
        if port is None:
            with open("CRITICAL_ERROR.txt", "w") as f:
                f.write("YOU RUN OUT OF PORTS SOMEHOW!!!")
            raise Exception(f"Failed to fetch stream for {device_id}: no free ports!")
        
        make_udp_request(device_id, port)

        cap = cv2.VideoCapture(f"udp://0.0.0.0:{port}?fifo_size=5000000&overrun_nonfatal=1", cv2.CAP_FFMPEG)
        cap.set(cv2.CAP_PROP_READ_TIMEOUT_MSEC, 200)
        cap.set(cv2.CAP_PROP_OPEN_TIMEOUT_MSEC, 1000)
        if not cap.isOpened():
            raise Exception("capture failed")
        
        return cap
    
    def pipe_stream(self, device_id):
        ret, frame = self.streams[device_id].read()
            
        if not ret:
            if not self.streams[device_id].read().isOpened():
                self.delete_stream(device_id)
            else:
                return
        for element in self.pipeline:
            if element.is_frozen(device_id):
                continue

            element.on_frame(device_id, frame)
    
    def get_pipe_element_by_name(self, name: str) -> PipelineElement | None:
        return self.__pipeline_by_name.get(name)

    @staticmethod
    def __get_free_udp_port():
        try:
            s = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
            s.bind(("0.0.0.0", 0))
            port = s.getsockname()[1]
            s.close()
            return port
        except socket.error as msg:
            print(f"Failed to acquire free port!\n{msg}")
            return None
            