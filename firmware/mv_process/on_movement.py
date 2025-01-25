import time

last_detected_movement = 0

COOLDOWN_PERIOD = 15  

def update_last_detected_movement():
    global last_detected_movement
    last_detected_movement = time.time()

def on_movement():
    global last_detected_movement
    current_time = time.time()

    if current_time - last_detected_movement < COOLDOWN_PERIOD:
        update_last_detected_movement()
        print("Movement detected and processed.")
        return

    update_last_detected_movement()
    print("Sending some request")
