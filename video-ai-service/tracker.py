from typing import Tuple
class TrackedObject:
    def __init__(self, confidence: int, pos: Tuple[int, int, int, int], name: str):
        self.confidence = confidence
        self.position = pos
        self.name = name

STATIC_OBJECT_MINIMUM_LIFETIME = 10
STATIC_OBJECT_BOX_PX_VARIATION = 10 # if it moves more than 10 pixels at a time it will be delisted from static objects

class Tracker:

    static_objects: list[TrackedObject]
    """ Objects that have been marked as background scenery
        To be classified as such their box can't change too much.
    """
    new_objects: list[TrackedObject] 
    """ New objects that have been detected in last detection.
    """
    current_objects: list[Tuple[TrackedObject, float]]
    """ All objects that aren't static_objects
    """


