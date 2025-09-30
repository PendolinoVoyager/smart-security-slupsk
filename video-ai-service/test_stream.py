import cv2
import time
from ultralytics import YOLO
from get_capture import get_capture
from add_notif import send_notification
model = YOLO("yolo11n.pt")  # Download and load pre-trained model
cap = get_capture() 
# cap = cv2.VideoCapture(0)

last_detection_time = 0  # Track last detection timestamp
detection_interval = 1   # Detect once per second
boxes = None
tracked_objects = {}  # Store object positions
previous_objects = []  # Store previous object ids
new_previous_objects = []
MIN_AREA = 1000  # Minimum area to consider an object
MIN_CONFIDENCE = 0.5  # Minimum confidence to consider an object
while cap.isOpened():
    ret, frame = cap.read()
    if not ret:
        break

    current_time = time.time()

    # Run detection & tracking every second
    if current_time - last_detection_time > detection_interval:
        results = model.track(frame, persist=True)  # Use tracking mode
        last_detection_time = current_time  # Update last detection time
        new_previous_objects = []  # Reset new previous objects
        # Update tracked objects
        tracked_objects.clear()
        for r in results:
            for box in r.boxes:
                x1, y1, x2, y2 = map(int, box.xyxy[0])  # Get box coordinates
                obj_id = int(box.id[0]) if box.id is not None else None  # Track ID
                cls = int(box.cls[0])  # Class index
                conf = box.conf[0]  # Confidence score
                label = f"{model.names[cls]} {conf:.2f}"
                tracked_objects[obj_id] = (x1, y1, x2, y2, label, conf)  # Store object position and label
                new_previous_objects.append(obj_id)

    # Draw stored boxes (even if YOLO is not detecting)
    for obj_id, (x1, y1, x2, y2, label, conf) in tracked_objects.items():
        if obj_id not in previous_objects:
            # Draw the box
            cv2.rectangle(frame, (x1, y1), (x2, y2), (0, 255, 0), 2)
            cv2.putText(frame, f"NEW:{label}", (x1, y1 - 10), cv2.FONT_HERSHEY_SIMPLEX, 0.5, (0, 255, 0), 2)
            print(f"New object detected: {label}")
            if conf > MIN_CONFIDENCE and ((x2 - x1) * (y2 - y1) > MIN_AREA):
                send_notification("There's " + label)
        else:
            # Draw the box with a different color for previously detected objects
            cv2.rectangle(frame, (x1, y1), (x2, y2), (255, 0, 0), 2)
            cv2.putText(frame, label, (x1, y1 - 10), cv2.FONT_HERSHEY_SIMPLEX, 0.5, (255, 0, 0), 2)
    # Update previous objects
    if len(new_previous_objects) > 0:
        previous_objects = new_previous_objects.copy()
        new_previous_objects.clear()

    # Show the frame
    cv2.imshow("YOLOv8 Tracking", frame)

    # Exit on 'q' key
    if cv2.waitKey(1) & 0xFF == ord('q'):
        break

cap.release()
cv2.destroyAllWindows()
