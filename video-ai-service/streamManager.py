import socket
import cv2
import requests
from pipelineElement import PipelineElement
from tracker import TrackingPipeline
"""This class is responsible for fetching and updating available video streams from the streaming server"""
class StreamManager:

    _STREAMING_SERVER_URL: str = "http://127.0.0.1:9002"
    _STREAMING_SERVER_UDP_ENDPOINT: str = "/udp_stream_start"
    _STREAMING_SERVER_STREAMS_ENDPOINT: str = "/streams/all"

    streams: dict[int, cv2.VideoCapture] = {}
    pipeline: list[PipelineElement] = [TrackingPipeline()]

    def __init__(self):
        for id in self.check_missing_streams():
            self.add_stream(id)
    
    """Delete or add streams based on their availability"""
    def sync_streams(self):
        try:
            current_ids = set(self._StreamManager__fetch_all_streams())
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
        streams = self.__fetch_all_streams()
        missing = []
        for stream in streams:
            if self.streams.get(stream) is None:
                missing.append(stream)
        return missing
    
    def add_stream(self, device_id):
        if self.streams.get(device_id) is not None:
            print(f"Failed to add stream {device_id}, stream exists already.")
            return

        self.streams[device_id] = self.__make_udp_request(device_id)
        
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

    def __fetch_all_streams(self):
        response = requests.get(f"{self._STREAMING_SERVER_URL}{self._STREAMING_SERVER_STREAMS_ENDPOINT}")
        if response.status_code != 200:
            raise Exception(f"Failed to get streams: {response.text}")
        res_json = response.json()
        if res_json["status"] != "success":
            raise Exception(res_json["message"] if res_json["message"] is not None else "Unknown error on streaming server: " + response.status_code)
        return res_json["payload"]["devices"]    


    """Make a request to start streaming. Return capture bound to the udp address"""
    def __make_udp_request(self, device_id):
        port = StreamManager.__get_free_udp_port()
        if port is None:
            with open("CRITICAL_ERROR.txt", "w") as f:
                f.write("YOU RUN OUT OF PORTS SOMEHOW!!!")
            raise Exception(f"Failed to fetch stream for {device_id}: no free ports!")
        
        payload = {
            "device_id": device_id,
            "address": f"0.0.0.0:{port}"
        }

        headers = {"Content-Type": "application/json"}

        response = requests.get(f"{self._STREAMING_SERVER_URL}{self._STREAMING_SERVER_UDP_ENDPOINT}", json=payload, headers=headers)
        if response.status_code != 200:
            raise Exception(f"Failed to start stream: {response.text}")    
        
        
        cap = cv2.VideoCapture(f"udp://0.0.0.0:{port}?fifo_size=5000000&overrun_nonfatal=1", cv2.CAP_FFMPEG)

        if not cap.isOpened():
            raise Exception("capture failed")
        
        return cap
    
    def pipe_stream(self, device_id):
        for element in self.pipeline:
            ret, frame = self.streams[device_id].read()
            
            # if bad return then ignore, it will sync eventually
            if not ret:
                continue

            element.on_frame(device_id, frame)
    
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