from typing import Any, Dict, List, Optional, Set, Tuple
from backend_api import NotificationType, send_image, send_notification
from pipelineElement import PipelineElement
from ultralytics import YOLO
import time 
import math
from faceRecognizer import FACE_RECOGNIZER_NAME

from typing import TYPE_CHECKING
if TYPE_CHECKING:
    from streamManager import StreamManager

class TrackedObject:
    def __init__(self, confidence: int, pos: Tuple[int, int, int, int], id: str, name: str):
        self.confidence = confidence
        self.position = pos #x1, y1, x2, y2
        self.name = name
        self.id = id

        # Tracking metadata
        self.lifetime = 1
        self.last_position = pos

        # Notification metadata
        self.reported_types: Set[NotificationType] = set()
        self.last_reported_type: Optional[NotificationType] = None

    def center(self) -> Tuple[int, int]:
        x1, y1, x2, y2 = self.position
        return ((x1 + x2) // 2, (y1 + y2) // 2)

    def movement(self) -> float:
        cx1, cy1 = self.center()
        x1, y1, x2, y2 = self.last_position
        cx2 = (x1 + x2) // 2
        cy2 = (y1 + y2) // 2
        return math.hypot(cx1 - cx2, cy1 - cy2)

STATIC_OBJECT_MINIMUM_LIFETIME = 10 # if an object is on screen for more than 10 seconds and doesn't move by X pixels then its background
STATIC_OBJECT_BOX_PX_VARIATION = 20 # if it moves more than X pixels at a time it will be delisted from background objects

class Tracker:

    static_objects: list[TrackedObject]
    """ Objects that have been marked as background scenery
        To be classified as such their box can't change too much.
    """
    new_objects: list[TrackedObject] 
    """ New objects that have been detected in last update.
    """
    current_objects: list[TrackedObject]
    """ All objects that aren't static_objects
    """
    def __init__(self):
        self.static_objects = []
        self.new_objects = []
        self.current_objects = []
   
    def update(self, new_objects: List[TrackedObject]):
        """
        Update tracker with objects detected in the current frame.
        Deduplicate by ID, update lifetime, detect static objects.
        """
        self.new_objects = []

        # Index existing objects by ID
        tracked_by_id: Dict[str, TrackedObject] = {
            obj.id: obj for obj in self.current_objects + self.static_objects
        }

        updated_current: List[TrackedObject] = []
        updated_static: List[TrackedObject] = []

        for obj in new_objects:
            if obj.id in tracked_by_id:
                tracked = tracked_by_id[obj.id]

                # Update position and lifetime
                tracked.last_position = tracked.position
                tracked.position = obj.position
                tracked.confidence = obj.confidence
                tracked.lifetime += 1

                movement = tracked.movement()

                # Static → moving again
                if tracked in self.static_objects and movement > STATIC_OBJECT_BOX_PX_VARIATION:
                    tracked.lifetime = 1
                    updated_current.append(tracked)
                # Current → static
                elif (
                    tracked.lifetime >= STATIC_OBJECT_MINIMUM_LIFETIME
                    and movement <= STATIC_OBJECT_BOX_PX_VARIATION
                ):
                    updated_static.append(tracked)
                else:
                    updated_current.append(tracked)

            else:
                # Brand new object
                self.new_objects.append(obj)
                updated_current.append(obj)

        self.current_objects = updated_current
        self.static_objects = updated_static
       
TRACKING_ELEMENT_NAME = "TE"
class TrackingPipeline(PipelineElement):
    NAME = TRACKING_ELEMENT_NAME
    TIME_BETWEEN_TRACKS = 1.0
    MIN_CONFIDENCE = 0.5  # Minimum confidence to consider an object
    MODEL_NAME="yolo26n.pt"

    trackers: dict[int, dict["last_track": float, "model": Any, "tracker": Tracker]] = {}

    def __init__(self, manager: "StreamManager"):
        print("Tracking pipeline created")
        super().__init__(manager)

    def _init_tracker_for_stream(self, device_id):
        model = YOLO(self.MODEL_NAME)
        print("model created")
        self.trackers[device_id] = {"last_track": time.time(), "model": model, "tracker": Tracker()}
        
    def _extract_new_tracked_objects(self, device_id, frame) -> list[TrackedObject]:
        results = self.trackers[device_id]["model"].track(frame, conf=self.MIN_CONFIDENCE, iou=0.7, verbose=False)  # random ass magic parameters
        detected_objects = []
        # Process results list
        for result in results:
            for box in result.boxes:
                x1, y1, x2, y2 = map(int, box.xyxy[0])  # Get box coordinates
                obj_id = int(box.id[0]) if box.id is not None else None  # Track ID
                cls = int(box.cls[0])  # Class index
                conf = box.conf[0]  # Confidence score
                detected_objects.append(TrackedObject(conf, (x1, y1, x2, y2), obj_id,
                                                     name=self.trackers[device_id]["model"].names[cls]) )
        return detected_objects

    def on_frame(self, device_id, frame):
        last_tracking_time = self.trackers[device_id]["last_track"]
        if last_tracking_time + self.TIME_BETWEEN_TRACKS > time.time():
            return
        
        self.trackers[device_id]["last_track"] = time.time()
        objects = self._extract_new_tracked_objects(device_id, frame)
        tracker: Tracker = self.trackers[device_id]["tracker"]
        tracker.update(objects)
        notifs = self._get_notifications(tracker, len(frame[0]), len(frame))
        for notif in notifs:

            if notif[2] == "person":
                face_recognizer = self.manager.get_pipe_element_by_name(FACE_RECOGNIZER_NAME)
                if face_recognizer is not None:
                    face_recognizer.unfreeze(device_id) # enable face recognition for this stream

            try:
                notif_id = send_notification(notif[0], notif[1], device_id)
                if notif_id is None:
                    raise Exception("Cannot send notification, no response")
                send_image(notif_id, frame)
            except Exception as e:
                print(f"ERROR: Cannot send notification {str(e)}")
            
            

    def _get_notifications(self, tracker: Tracker, width, height):
        notifications: List[Tuple[NotificationType, str, str]] = []

        for object in tracker.new_objects:
            notifications.append(classify_notification(object, frame_width=width,
                                        frame_height=height,
                                        is_static=False,
                                        is_new=True))
        for object in tracker.static_objects:
            notifications.append(classify_notification(object, frame_width=width,
                                        frame_height=height,
                                        is_static=True,
                                        is_new=False))
        for object in tracker.current_objects:
            notifications.append(classify_notification(object, frame_width=width,
                                        frame_height=height,
                                        is_static=False,
                                        is_new=False))
            
        return list(filter(lambda x: x[0] is not None, notifications))
    
    def on_stream_end(self, device_id):
        del self.trackers[device_id]
        self.unfreeze(device_id)
        print(f"Tracking ended {device_id}")

    def on_stream_start(self, device_id):
        self._init_tracker_for_stream(device_id)
        print(f"Tracking started {device_id}")


#############################################

PERSON_CRITICAL_AREA = 0.35     # Person very close to camera
PERSON_VISIT_AREA = 0.15       # Normal door visit
RANDOM_LOG_AREA = 0.05         # Random objects = log only
CAR_WARNING_AREA = 0.20        # Large vehicle
STATIC_REPORT_ONCE = True

def classify_notification(
    obj: TrackedObject,
    frame_width: int,
    frame_height: int,
    is_static: bool,
    is_new: bool,
) -> Tuple[NotificationType | None, str | None, str]:
    """
    Returns (NotificationType, message, objectName) or (None, None) if already reported.
    """

    x1, y1, x2, y2 = obj.position
    box_area = max(0, x2 - x1) * max(0, y2 - y1)
    frame_area = frame_width * frame_height
    area_ratio = box_area / frame_area if frame_area else 0

    name = obj.name.lower()

    def report(ntype: NotificationType, message: str):
        if ntype in obj.reported_types:
            return None, None, None
        obj.reported_types.add(ntype)
        obj.last_reported_type = ntype
        return ntype, message, name

    # --- STATIC OBJECTS (reported once on appearance) ---
    if is_static and is_new:
        if name in {"package", "box"}:
            return report(
                NotificationType.Info,
                "A package was left near the door."
            )

        if name in {"car", "vehicle"}:
            return report(
                NotificationType.Warning,
                "A vehicle has parked nearby."
            )

        return report(
            NotificationType.Info,
            f"Static object detected: {obj.name}"
        )

    # --- PERSON ---
    if name == "person":
        if area_ratio >= 0.25:
            return report(
                NotificationType.Visit,
                "Someone is at the door."
            )

        return report(
            NotificationType.Info,
            "Person detected at a distance."
        )

    # --- VEHICLES ---
    if name in {"car", "vehicle", "truck"}:
        if area_ratio >= 0.20:
            return report(
                NotificationType.Warning,
                "A vehicle is approaching the property."
            )

        return report(
            NotificationType.Log,
            "Vehicle detected in the area."
        )

    # --- ANIMALS ---
    if name in {"dog", "cat"}:
        if area_ratio < 0.05:
            return report(
                NotificationType.Log,
                f"Small {name} detected briefly."
            )

        return report(
            NotificationType.Info,
            f"{name.capitalize()} detected near the door."
        )

    # --- FALLBACK ---
    if is_new:
        return report(
            NotificationType.Info,
            f"Detected: {obj.name}"
        )

    return None, None, None
