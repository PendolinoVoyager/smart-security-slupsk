import os
import time
import threading
import RPi.GPIO as GPIO
import subprocess
from subprocess import Popen

LED_PIN = 4
CONFIG_FILE_PATH = "/etc/sss_firmware/config.cfg"
VENV_PYTHON = "/home/kacper/venv/bin/python3"
SERVER_SCRIPT = "/home/kacper/Desktop/start_up_app/app_v0.0.1.py"
LOG_FILE = "/home/kacper/Desktop/start_up_app/log.txt"

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
        print(f"Error reading config file: {e}")
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
        print(f"Starting Flask server in venv: {VENV_PYTHON} (logging to {LOG_FILE})")
        try:
            # Ensure log directory exists
            os.makedirs(os.path.dirname(LOG_FILE), exist_ok=True)
            # Open log file for appending
            log_fd = open(LOG_FILE, 'a+')
            # Start server process, redirecting stdout and stderr to log
            server_process = Popen(
                [VENV_PYTHON, SERVER_SCRIPT],
                stdout=log_fd,
                stderr=log_fd
            )
        except Exception as e:
            print(f"Failed to start HTTP server: {e}")


def connect_to_config_network():
    try:
        result = subprocess.run(
            ["nmcli", "-t", "-f", "NAME,DEVICE", "connection", "show", "--active"],
            capture_output=True,
            text=True,
            check=True
        )
        active_connections = result.stdout.strip().split("\n")

        for line in active_connections:
            parts = line.strip().split(":")
            if len(parts) == 2:
                conn_name, device = parts
                if conn_name == "IoT-Config":
                    print("Network 'IoT-Config' already active – skipping.")
                    return
                elif conn_name != "IoT-Config" and device.startswith("wl"):
                    print(f"Connected to another Wi-Fi network ({conn_name}) – not starting IoT-Config.")
                    return

        print("Attempting to connect to 'IoT-Config'...")
        subprocess.run(["nmcli", "connection", "up", "IoT-Config"], check=True)
        print("Connected to 'IoT-Config'")
    except subprocess.CalledProcessError as e:
        print(f"Failed to connect to 'IoT-Config': {e}")


def stop_http_server():
    global server_process
    if server_process:
        print("Stopping Flask server...")
        try:
            server_process.terminate()
            server_process.wait()
        except Exception as e:
            print(f"Error stopping HTTP server: {e}")
        finally:
            server_process = None


def monitor_config():
    global blinking
    while True:
        if is_configured():
            print("Device configured - LED solid, server stopped.")
            blinking = False
            stop_http_server()
        else:
            print("Device NOT configured - LED blinking, starting server and network.")
            blinking = True
            start_http_server()
            connect_to_config_network()

        time.sleep(5)


def stop_mdns_service():
    try:
        print("Disabling mDNS (avahi-daemon)...")
        subprocess.run(["sudo", "systemctl", "stop", "avahi-daemon"], check=True)
        subprocess.run(["sudo", "systemctl", "disable", "avahi-daemon"], check=True)
        print("mDNS disabled.")
    except subprocess.CalledProcessError as e:
        print(f"Error disabling mDNS: {e}")


def main():
    setup()
    threading.Thread(target=blink_led, daemon=True).start()
    monitor_config()
    stop_http_server()
    stop_mdns_service()


if __name__ == "__main__":
    main()