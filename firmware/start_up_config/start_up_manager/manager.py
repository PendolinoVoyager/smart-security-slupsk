import os
import time
import threading
import RPi.GPIO as GPIO
from subprocess import Popen

LED_PIN = 4
CONFIG_FILE_PATH = "/etc/sss_firmware/config.cfg"
VENV_PYTHON = "/home/kacper/venv/bin/python3"
SERVER_SCRIPT = "/home/kacper/Desktop/start_up_app/app_v0.0.1.py"

server_process = None
blinking = False

def setup():
    GPIO.setmode(GPIO.BCM)
    GPIO.setup(LED_PIN, GPIO.OUT)
    GPIO.output(LED_PIN, GPIO.HIGH)

def is_configured():
    try:
        with open(CONFIG_FILE_PATH, "r", encoding="utf-8") as f:
            lines = f.readlines()
            for i, line in enumerate(lines):
                if line.strip() == "[Is Configured]":
                    if i + 1 < len(lines):
                        return lines[i + 1].strip() == "1"
    except Exception as e:
        print(f"[DEBUG] If you can see it in logs, check if file exists pls: {e}")
    return False

def blink_led():
    global blinking
    while True:
        if blinking:
            GPIO.output(LED_PIN, GPIO.HIGH)
            time.sleep(0.5)
            GPIO.output(LED_PIN, GPIO.LOW)
            time.sleep(0.5)
        else:
            GPIO.output(LED_PIN, GPIO.HIGH)
            time.sleep(1)

def start_http_server():
    global server_process
    if server_process is None:
        server_process = Popen([VENV_PYTHON, SERVER_SCRIPT])

def stop_http_server():
    global server_process
    if server_process:
        print("[DEBUG] Stopping server http")
        server_process.terminate()
        server_process.wait()
        server_process = None

def monitor_config():
    global blinking
    while True:
        if is_configured():
            print("Device configured.")
            blinking = False
            stop_http_server()
        else:
            print("[DEBUG] Not configured. Starting http server.")
            blinking = True
            start_http_server()
        time.sleep(5)

def main():
    setup()
    threading.Thread(target=blink_led, daemon=True).start()
    monitor_config()

if __name__ == "__main__":
    main()

