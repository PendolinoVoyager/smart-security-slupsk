from typing import Tuple
class TrackedObject:
    def __init__(self, confidence: int, pos: Tuple[int, int, int, int], id: str, name: str):
        self.confidence = confidence
        self.position = pos #x1, y1, x2, y2
        self.name = name
        self.id = id

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
    def __init__(self):
        self.static_objects = []
        self.new_objects = []
        self.current_objects = []
    
    def update(self, new_objects: list[TrackedObject]):
        """ Update the tracker with new objects.
            Check if the new objects are repeated by id.
            It will also add new objects to the current objects list.
        """

        # Check if the new objects are repeated by id
        for new_object in new_objects:
            if new_object.id in [obj.id for obj in self.static_objects]:
                continue
            if new_object.id in [obj.id for obj in self.current_objects]:
                continue
            if new_object.id in [obj.id for obj in self.new_objects]:
                continue
            
            self.new_objects.append(new_object)
        
        # Check if the new objects are static
        for new_object in self.new_objects:
            if new_object.id in [obj.id for obj in self.static_objects]:
                continue
            if new_object.id in [obj.id for obj in self.current_objects]:
                continue
            
            # If the object is not repeated, add it to the current objects list
            self.current_objects.append((new_object, 0))
        
