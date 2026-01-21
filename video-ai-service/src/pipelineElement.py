from abc import ABC, abstractmethod


class PipelineElement(ABC):
    @abstractmethod
    def on_frame(self, device_id, frame):
        """
        Called for each frame coming from a device stream.
        """
        pass

    @abstractmethod
    def on_stream_start(self, device_id):
        """
        Called when a device stream starts.
        """
        pass

    @abstractmethod
    def on_stream_end(self, device_id):
        """
        Called when a device stream ends.
        """
        pass
