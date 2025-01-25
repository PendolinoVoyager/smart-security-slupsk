import cv2
import time
from on_movement import on_movement

# time when the last movement was processed 
last_detected_movement = 0

COOLDOWN_PERIOD = 15  

def update_last_detected_movement():
    global last_detected_movement
    last_detected_movement = time.time()

# Sum-of-contours threshold above which something suspicious might happen 
THRESHOLD_ALERT = 10000
THRESHOLD_TOO_SMALL = 300
# Initialize the video capture and background subtractor
cap = cv2.VideoCapture(0)  # Use 0 for the first webcam, or provide the video path

frame_width = int(cap.get(3)) 
frame_height = int(cap.get(4)) 

back_sub = cv2.createBackgroundSubtractorMOG2(detectShadows=False, history=5)


while True:
    ret, frame = cap.read()
    if not ret:
        break

    # Apply background subtraction
    fg_mask = back_sub.apply(frame)

    # Optional: Clean up the mask with morphological operations
    kernel = cv2.getStructuringElement(cv2.MORPH_ELLIPSE, (5, 5))
    fg_mask = cv2.morphologyEx(fg_mask, cv2.MORPH_CLOSE, kernel)
    fg_mask = cv2.morphologyEx(fg_mask, cv2.MORPH_OPEN, kernel)

    # Find contours of the detected foreground regions
    contours, _ = cv2.findContours(fg_mask, cv2.RETR_EXTERNAL, cv2.CHAIN_APPROX_SIMPLE)

    changed_area = sum([cv2.contourArea(contour) for contour in contours])
    if changed_area > THRESHOLD_ALERT:
        if time.time() - last_detected_movement < COOLDOWN_PERIOD:
            update_last_detected_movement()
            on_movement()
        else:        
            update_last_detected_movement()


    # Break on 'q' key
    if cv2.waitKey(30) & 0xFF == ord('q'):
        break

cap.release()
cv2.destroyAllWindows()
