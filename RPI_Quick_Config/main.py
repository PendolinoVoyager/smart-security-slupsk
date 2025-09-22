import ipaddress
import time
import requests
from zeroconf import Zeroconf, ServiceBrowser
from mDNS import MDNSListener

RESET = "\033[0m"
GREEN = "\033[92m"
RED = "\033[91m"
WHITE = "\033[97m"
YELLOW = "\033[93m"


def colored_print(color, message: str) ->None:
    print(f"{color}{message}{RESET}")


def get_backend_ip() -> str:
    while True:
        backend_ip = input(f"{WHITE}Enter Backend Server IP (e.g., 192.168.0.10): {RESET}")
        try:
            ipaddress.ip_address(backend_ip)
            colored_print(GREEN, f"[OK] Backend IP: {backend_ip}")
            return backend_ip
        except ValueError:
            colored_print(RED, f"[ERROR] Invalid IP address: {backend_ip}")


def discover_devices(service_type="_http._tcp.local.", timeout=5):
    zeroconf = Zeroconf()
    listener = MDNSListener()
    ServiceBrowser(zeroconf, service_type, listener)
    colored_print(WHITE, "\n[INFO] Searching for mDNS devices...")
    time.sleep(timeout)
    zeroconf.close()
    return listener.get_devices()


def print_menu() -> None:
    menu_options = [
        "\nMain Menu:",
        "[1] Search for devices",
        "[2] Devices list",
        "[3] Select device to configure",
        "[4] Switch WIFI",
        "[5] Authenticate device",
        "[6] Security testing",
        "[99] Exit"
    ]
    colored_print(RESET, "\n".join(menu_options))


def search_for_devices() -> list:
    devices = discover_devices()
    if devices:
        colored_print(GREEN, "[OK!] mDNS devices found!")
    else:
        colored_print(RED, "[ERROR] No mDNS devices found.")
    return devices


def print_devices_list(devices) -> None:
    if not devices:
        colored_print(RED, "[ERROR] No devices found.")
        return

    for idx, device in enumerate(devices):
        print(f"{idx}. {device['name']} - {device['addresses'][0]}")


def select_device(devices) -> int | None:
    if not devices:
        colored_print(RED, "[ERROR] No devices found.")
        return None

    print_devices_list(devices)
    try:
        selected_index = int(input("Enter device index: "))
        if 0 <= selected_index < len(devices):
            colored_print(GREEN, f"[OK] Selected device: {devices[selected_index]['name']}")
            return selected_index
        else:
            colored_print(RED, "[ERROR] Invalid device index.")
    except ValueError:
        colored_print(RED, "[ERROR] Invalid input. Please enter a number.")
    return None


def switch_wifi(devices, selected_device) -> None:
    if selected_device is None:
        colored_print(RED, "[ERROR] No device selected. Please select a device first.")
        return

    device_ip = devices[selected_device]['addresses'][0]
    url = f"http://{device_ip}:5000/api/v1/available-networks"

    try:
        response = requests.get(url, timeout=5)
        response.raise_for_status()
        networks = response.json().get("networks", [])

        if not networks:
            colored_print(RED, "[ERROR] No networks found.")
            return

        colored_print(GREEN, "[OK] Available networks:")
        for idx, network in enumerate(networks):
            print(f"[{idx}] {network.get('ssid', 'Unknown')} (Signal: {network.get('signal_strength', 'N/A')})")

        try:
            selected_network = int(input("\nSelect network by index: "))
            if 0 <= selected_network < len(networks):
                chosen_ssid = networks[selected_network]["ssid"]
                colored_print(GREEN, f"[OK] You selected: {chosen_ssid}")
                wifi_pass = input("Enter WIFI password: ")

                config_url = f"http://{device_ip}:5000/api/v1/config"
                data = {"ssid": chosen_ssid, "password": wifi_pass}
                colored_print(WHITE, f"[INFO] Switching WIFI to {chosen_ssid}")

                try:
                    requests.post(config_url, json=data, timeout=5)
                except requests.exceptions.ConnectionError:
                    colored_print(RED, f"[ERROR] Could not connect to {device_ip}. Is the device online?")
            else:
                colored_print(RED, "[ERROR] Invalid selection. Please try again.")
        except ValueError:
            colored_print(RED, "[ERROR] Invalid input. Please enter a number.")
    except requests.exceptions.RequestException as e:
        colored_print(RED, f"[ERROR] Network error: {e}")


def authenticate_device(devices, selected_device, backend_ip) -> None:
    if selected_device is None:
        colored_print(RED, "[ERROR] No device selected. Please select a device first.")
        return

    device_ip = devices[selected_device]['addresses'][0]
    url = f"http://{device_ip}:5000/api/v1/uuid"
    response = requests.get(url, timeout=5)
    response.raise_for_status()
    device_uuid = response.json().get("uuid")

    if not device_uuid:
        colored_print(RED, "[ERROR] Could not get device UUID.")
        return

    colored_print(GREEN, f"[OK] Device UUID: {device_uuid}")

    colored_print(YELLOW, "\n[WARNING] Device must belong to an account.")
    email = input("Enter account email: ")
    password = input("Enter account password: ")

    auth_url = f"http://{backend_ip}:8080/api/v1/auth/device"
    data = {
        "deviceUuid": device_uuid,
        "email": email,
        "password": password
    }

    try:
        colored_print(WHITE, f"[INFO] Request has been sent to backend.")
        response = requests.post(auth_url, json=data, timeout=5)
        response.raise_for_status()
    except requests.exceptions.RequestException as e:
        colored_print(RED, f"[ERROR] Network error: {e}")

    is_ok = response.status_code == 200 and response.json().get("token") is not None and response.json().get(
        "refreshToken") is not None

    if is_ok:
        colored_print(GREEN, "[OK] Fetched tokens.")
    else:
        colored_print(RED, "[ERROR] Device authentication failed.")
        return

    rpi_token_url = f"http://{device_ip}:5000/api/v1/token"
    rpi_data = {
        "token": response.json().get("token"),
        "refreshToken": response.json().get("refreshToken")
        }
    try:
        response = requests.post(rpi_token_url, json=rpi_data, timeout=5)
        colored_print(GREEN, "[OK] Tokens sent to device.")

        if response.status_code == 200:
            colored_print(GREEN, "[OK] Device authenticated successfully!!!!!!.")


    except requests.exceptions.RequestException as e:
        colored_print(RED, f"[ERROR] Network error: {e}")

def test_security() -> None:
    colored_print(YELLOW, "\n[SECURITY TEST 1] Discovering already configured mDNS devices")

    devices = discover_devices()
    if not devices:
        colored_print(RED, "[ERROR] No mDNS devices found.")
        return

    for device in devices:
        device_ip = device['addresses'][0]
        try:
            response = requests.get(f"http://{device_ip}:5000/api/v1/uuid", timeout=3)
            if response.status_code == 200:
                colored_print(GREEN, f"[OK] Device {device['name']} is responsive to /uuid – might be configured.")
            else:
                colored_print(YELLOW, f"[INFO] Device {device['name']} returned status {response.status_code}.")
        except Exception as e:
            colored_print(RED, f"[ERROR] No response from {device['name']} ({device_ip}): {e}")

    colored_print(YELLOW, "\n[SECURITY TEST 2] Attempting to re-authenticate an already configured device")
    colored_print(WHITE, "[INFO] Skipped – requires valid token and backend in test mode.")

    colored_print(YELLOW, "\n[SECURITY TEST 3] Sending malformed Wi-Fi configuration data")
    for device in devices:
        device_ip = device['addresses'][0]
        try:
            url = f"http://{device_ip}:5000/api/v1/config"
            bad_payloads = [
                {"ssid": "", "password": "12345678"},
                {"ssid": "ValidSSID", "password": ""},
                {"ssid": "ValidSSID", "password": "p" * 1000}
            ]
            for payload in bad_payloads:
                response = requests.post(url, json=payload, timeout=3)
                if response.status_code >= 400:
                    colored_print(GREEN, f"[OK] Device {device['name']} correctly rejected bad input.")
                else:
                    colored_print(RED, f"[ALERT] Device {device['name']} accepted malformed input!")
        except Exception as e:
            colored_print(RED, f"[ERROR] Error while sending malformed config: {e}")

    colored_print(YELLOW, "\n[SECURITY TEST 4] DoS test – sending multiple requests in rapid succession")
    for device in devices:
        device_ip = device['addresses'][0]
        url = f"http://{device_ip}:5000/api/v1/uuid"
        try:
            for _ in range(10):  # small burst
                requests.get(url, timeout=1)
            colored_print(GREEN, f"[OK] Device {device['name']} handled the request burst.")
        except Exception as e:
            colored_print(RED, f"[ERROR] Device {device['name']} did not respond during DoS test: {e}")

    colored_print(YELLOW, "\n[SECURITY TEST 5] Checking if /token allows overwriting existing tokens")
    colored_print(WHITE, "[INFO] Skipped – requires access to valid tokens and backend support.")



def main() -> None:
    colored_print(WHITE, "\nRPI QUICK CONFIGURATION")
    backend_ip = get_backend_ip()
    devices = []
    selected_device = None

    while True:
        print_menu()
        choice = input("Choose an option: ")

        match choice:
            case "1":
                devices = search_for_devices()
            case "2":
                colored_print(WHITE, "\n[INFO] Devices list:")
                print_devices_list(devices)
            case "3":
                colored_print(WHITE, "\n[INFO] Configure device:")
                selected_device = select_device(devices)
            case "4":
                colored_print(WHITE, "\n[INFO] Switch WIFI:")
                switch_wifi(devices, selected_device)
            case "5":
                colored_print(WHITE, "\n[INFO] Authenticate Device:")
                authenticate_device(devices, selected_device, backend_ip)
            case "6":
                colored_print(WHITE, "\n[INFO] Security testing:")
                test_security()
            case "99":
                colored_print(GREEN, "[INFO] Exiting...")
                break
            case _:
                colored_print(RED, "[ERROR] Invalid choice. Try again.\n")


if __name__ == '__main__':
    main()
