import cv2
import time
from on_movement import on_movement
from config import get_video_source

#check if we can stream: /etc/sss_firmware/token.txt should have something written
with open("/etc/sss_firmware/token.txt", "r") as f:
    token = f.read().strip()
    if token == "":
        print("No token found in /etc/sss_firmware/token.txt\ndevice not configured")
        exit()

videosrc = get_video_source()

# Time tracking
last_detected_movement = 0
start_time = time.time()

COOLDOWN_PERIOD = 5
INITIAL_IGNORE_PERIOD = 5  # Ignore first few seconds
FRAME_SKIP = 6  # Process every 6th frame (~5 FPS if source is 30 FPS)
TARGET_WIDTH, TARGET_HEIGHT = 320, 240  # Reduce resolution

THRESHOLD_ALERT = 20000
THRESHOLD_TOO_SMALL = 300

cap = cv2.VideoCapture(videosrc)
cap.set(cv2.CAP_PROP_BUFFERSIZE, 1)

back_sub = cv2.createBackgroundSubtractorMOG2(detectShadows=False, history=5)

if not cap.isOpened():
    print("Cannot open camera")
    exit()

frame_count = 0

while True:
    ret, frame = cap.read()
    if not ret:
        break

    frame_count += 1

    # Skip frames to achieve ~5 FPS
    if frame_count % FRAME_SKIP != 0:
        continue

    # Reduce resolution
    frame = cv2.resize(frame, (TARGET_WIDTH, TARGET_HEIGHT))

    fg_mask = back_sub.apply(frame)
    
    # Clean up the mask
    kernel = cv2.getStructuringElement(cv2.MORPH_ELLIPSE, (5, 5))
    fg_mask = cv2.morphologyEx(fg_mask, cv2.MORPH_CLOSE, kernel)
    fg_mask = cv2.morphologyEx(fg_mask, cv2.MORPH_OPEN, kernel)
    
    # Find contours of detected movement
    contours, _ = cv2.findContours(fg_mask, cv2.RETR_EXTERNAL, cv2.CHAIN_APPROX_SIMPLE)
    changed_area = sum(cv2.contourArea(contour) for contour in contours)

    if time.time() - start_time < INITIAL_IGNORE_PERIOD:
        continue

    if changed_area > THRESHOLD_ALERT and time.time() - last_detected_movement > COOLDOWN_PERIOD:
        last_detected_movement = time.time()
        if on_movement() is None:
            break

    if cv2.waitKey(1) & 0xFF == ord('q'):  # Reduce delay in waitKey
        break

cap.release()
cv2.destroyAllWindows()
