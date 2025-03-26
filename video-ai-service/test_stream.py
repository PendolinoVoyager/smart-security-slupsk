import cv2
import time
from ultralytics import YOLO
import get_capture

model = YOLO("yolov8n.pt")  # Download and load pre-trained model
# cap = get_capture() 
cap = cv2.VideoCapture(0)

last_detection_time = 0  # Track last detection timestamp
detection_interval = 1   # Detect once per second
boxes = None
tracked_objects = {}  # Store object positions

while cap.isOpened():
    ret, frame = cap.read()
    if not ret:
        break

    current_time = time.time()

    # Run detection & tracking every second
    if current_time - last_detection_time > detection_interval:
        results = model.track(frame, persist=True)  # Use tracking mode
        last_detection_time = current_time  # Update last detection time

        # Update tracked objects
        tracked_objects.clear()
        for r in results:
            for box in r.boxes:
                x1, y1, x2, y2 = map(int, box.xyxy[0])  # Get box coordinates
                obj_id = int(box.id[0]) if box.id is not None else None  # Track ID
                cls = int(box.cls[0])  # Class index
                conf = box.conf[0]  # Confidence score
                label = f"{model.names[cls]} {conf:.2f}"

                tracked_objects[obj_id] = (x1, y1, x2, y2, label)

    # Draw stored boxes (even if YOLO is not detecting)
    for obj_id, (x1, y1, x2, y2, label) in tracked_objects.items():
        cv2.rectangle(frame, (x1, y1), (x2, y2), (0, 255, 0), 2)
        cv2.putText(frame, label, (x1, y1 - 10), cv2.FONT_HERSHEY_SIMPLEX, 0.5, (0, 255, 0), 2)

    # Show the frame
    cv2.imshow("YOLOv8 Tracking", frame)

    # Exit on 'q' key
    if cv2.waitKey(1) & 0xFF == ord('q'):
        break

cap.release()
cv2.destroyAllWindows()