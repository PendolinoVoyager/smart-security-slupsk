
from abc import ABC, abstractmethod

from typing import TYPE_CHECKING
if TYPE_CHECKING:
    from streamManager import StreamManager

class PipelineElement(ABC):

    def is_frozen(self, device_id):
        return device_id in self.__frozen_ids
    def freeze(self, device_id):
        self.__frozen_ids.add(device_id)
    def unfreeze(self, device_id):
        self.__frozen_ids.discard(device_id)

    manager: "StreamManager"

    def __init__(self, manager: "StreamManager"):
        self.__frozen_ids = set()
        self.manager = manager
        super().__init__()

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
