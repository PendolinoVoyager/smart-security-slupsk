import os
import time
import threading
import RPi.GPIO as GPIO
import subprocess
from subprocess import Popen

from watchdog.observers import Observer
from watchdog.events import FileSystemEventHandler

CONFIG_FILE_PATH = "/etc/sss_firmware/config.cfg"
VENV_PYTHON = "/home/kacper/venv/bin/python3"
SERVER_SCRIPT = "/home/kacper/Desktop/start_up_app/app_v0.0.1.py"
LOG_FILE = "/home/kacper/Desktop/start_up_app/log.txt"

server_process = None


def is_configured():
    try:
        with open(CONFIG_FILE_PATH, "r", encoding="utf-8") as f:
            lines = f.readlines()
            for i, line in enumerate(lines):
                if line.strip() == "[Is Configured]":
                    if i + 1 < len(lines):
                        return lines[i + 1].strip() == "1"
    except Exception as e:
        log(f"Error reading config file: {e}")
    return False


def setup_configuration_file():
    with open(CONFIG_FILE_PATH, "w", encoding="utf-8") as f:
        f.write("[Is Configured]\n0\n")


# Don't touch this func. On start dnsmasq always fails on startup for some reason.
# Single restart always helps.
def restart_dnsmasq_till_works():
    while True:
        try:
            subprocess.run(["systemctl", "restart", "dnsmasq"], check=True)
            log("dnsmasq restarted successfully.")
            break
        except subprocess.CalledProcessError as e:
            log(f"Failed to restart dnsmasq: {e}. Retrying in 5 seconds...")
            time.sleep(5)


def log(message):
    with open(LOG_FILE, "a+") as f:
        f.write("\n\n\n")
        f.write(message)


def stop_dnsmasq_on_configured():
    if is_configured():
        try:
            subprocess.run(["systemctl", "stop", "dnsmasq"], check=True)
            log("dnsmasq stopped as device is configured.")
        except subprocess.CalledProcessError as e:
            log(f"Failed to stop dnsmasq: {e}.")


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


def stop_mdns_service():
    try:
        print("Disabling mDNS (avahi-daemon)...")
        subprocess.run(["systemctl", "stop", "avahi-daemon"], check=True)
        subprocess.run(["systemctl", "disable", "avahi-daemon"], check=True)
        print("mDNS disabled.")
    except subprocess.CalledProcessError as e:
        print(f"Error disabling mDNS: {e}")


def apply_config_state():
    if is_configured():
        print("Device configured - Server stopped.")
        stop_http_server()
        stop_dnsmasq_on_configured()
    else:
        print("Device NOT configured - Starting server and network.")
        start_http_server()
        connect_to_config_network()


class ConfigFileHandler(FileSystemEventHandler):
    def on_modified(self, event):
        if getattr(event, "is_directory", False):
            return

        try:
            src = os.path.realpath(event.src_path)
            target = os.path.realpath(CONFIG_FILE_PATH)
        except Exception:
            src = event.src_path
            target = CONFIG_FILE_PATH

        if src == target:
            apply_config_state()

    def on_created(self, event):
        self.on_modified(event)

    def on_moved(self, event):
        if getattr(event, "is_directory", False):
            return
        try:
            dest = os.path.realpath(getattr(event, "dest_path", ""))
            target = os.path.realpath(CONFIG_FILE_PATH)
        except Exception:
            dest = getattr(event, "dest_path", "")
            target = CONFIG_FILE_PATH

        if dest == target:
            apply_config_state()


def monitor_config_with_watchdog():
    handler = ConfigFileHandler()
    observer = Observer()
    observer.schedule(handler, path=os.path.dirname(CONFIG_FILE_PATH) or ".", recursive=False)
    observer.start()

    apply_config_state()

    try:
        while True:
            time.sleep(1)
    finally:
        observer.stop()
        observer.join()


def main():
    setup_configuration_file()
    restart_dnsmasq_till_works()
    monitor_config_with_watchdog()


if __name__ == "__main__":
    main()