import ipaddress
import time
import requests
from dataclasses import dataclass
from typing import Dict, List, Optional, Tuple

from zeroconf import Zeroconf, ServiceBrowser, ServiceListener

RESET = "\033[0m"
GREEN = "\033[92m"
RED = "\033[91m"
WHITE = "\033[97m"
YELLOW = "\033[93m"


def colored_print(color, message: str) -> None:
    print(f"{color}{message}{RESET}")


@dataclass
class Device:
    name: str
    type: str
    domain: str
    addresses: List[str]
    port: int
    properties: Dict[bytes, bytes]


class MDNSListener(ServiceListener):
    """
    Minimalny listener: zbiera usługi _http._tcp.local. i trzyma je w mapie,
    żeby nie dublować wpisów podczas update'ów.
    """

    def __init__(self) -> None:
        self._devices: Dict[str, Device] = {}

    def add_service(self, zeroconf: Zeroconf, service_type: str, name: str) -> None:
        info = zeroconf.get_service_info(service_type, name, timeout=2000)
        if not info:
            return

        addresses = list(info.parsed_addresses())  # list[str]
        device = Device(
            name=name,
            type=service_type,
            domain=info.server or "",
            addresses=addresses,
            port=int(info.port or 0),
            properties=dict(info.properties or {}),
        )
        self._devices[name] = device

    def update_service(self, zeroconf: Zeroconf, service_type: str, name: str) -> None:
        # traktujemy update jak ponowne add (odświeża IP/port)
        self.add_service(zeroconf, service_type, name)

    def remove_service(self, zeroconf: Zeroconf, service_type: str, name: str) -> None:
        self._devices.pop(name, None)

    def get_devices(self) -> List[Device]:
        return list(self._devices.values())


def get_backend_ip() -> str:
    while True:
        backend_ip = input(f"{WHITE}Enter Backend Server IP (e.g., 192.168.0.10): {RESET}").strip()
        try:
            ipaddress.ip_address(backend_ip)
            colored_print(GREEN, f"[OK] Backend IP: {backend_ip}")
            return backend_ip
        except ValueError:
            colored_print(RED, f"[ERROR] Invalid IP address: {backend_ip}")


def discover_devices(service_type: str = "_http._tcp.local.", timeout: int = 5) -> List[Device]:
    """
    Kluczowa różnica vs Twój kod:
    - TRZYMAMY referencję do `browser` w zmiennej, żeby GC nie zabił nasłuchu.
    """
    zeroconf = Zeroconf()
    listener = MDNSListener()
    browser = ServiceBrowser(zeroconf, service_type, listener)  # noqa: F841 (celowo trzymamy)

    colored_print(WHITE, f"\n[INFO] Searching for mDNS devices ({service_type}) for {timeout}s...")
    try:
        time.sleep(timeout)
    finally:
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
        "[99] Exit",
    ]
    colored_print(RESET, "\n".join(menu_options))


def search_for_devices() -> List[Device]:
    devices = discover_devices()
    if devices:
        colored_print(GREEN, f"[OK!] mDNS devices found: {len(devices)}")
    else:
        colored_print(RED, "[ERROR] No mDNS devices found.")
    return devices


def device_best_ip(device: Device) -> Optional[str]:
    # Preferuj IPv4 (jeśli jest), a jak nie ma to pierwsze co jest
    if not device.addresses:
        return None
    for a in device.addresses:
        try:
            if ipaddress.ip_address(a).version == 4:
                return a
        except ValueError:
            continue
    return device.addresses[0]


def print_devices_list(devices: List[Device]) -> None:
    if not devices:
        colored_print(RED, "[ERROR] No devices found.")
        return

    for idx, device in enumerate(devices):
        ip = device_best_ip(device) or "NO_IP"
        print(f"{idx}. {device.name} - {ip}:{device.port} ({device.domain})")


def select_device(devices: List[Device]) -> Optional[int]:
    if not devices:
        colored_print(RED, "[ERROR] No devices found.")
        return None

    print_devices_list(devices)
    try:
        selected_index = int(input("Enter device index: ").strip())
        if 0 <= selected_index < len(devices):
            colored_print(GREEN, f"[OK] Selected device: {devices[selected_index].name}")
            return selected_index
        colored_print(RED, "[ERROR] Invalid device index.")
    except ValueError:
        colored_print(RED, "[ERROR] Invalid input. Please enter a number.")
    return None


def switch_wifi(devices: List[Device], selected_device: Optional[int]) -> None:
    if selected_device is None:
        colored_print(RED, "[ERROR] No device selected. Please select a device first.")
        return

    device = devices[selected_device]
    device_ip = device_best_ip(device)
    if not device_ip:
        colored_print(RED, "[ERROR] Selected device has no IP address.")
        return

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
            selected_network = int(input("\nSelect network by index: ").strip())
            if not (0 <= selected_network < len(networks)):
                colored_print(RED, "[ERROR] Invalid selection. Please try again.")
                return

            chosen_ssid = networks[selected_network].get("ssid") or ""
            if not chosen_ssid:
                colored_print(RED, "[ERROR] Selected network has empty SSID.")
                return

            colored_print(GREEN, f"[OK] You selected: {chosen_ssid}")
            wifi_pass = input("Enter WIFI password: ")

            config_url = f"http://{device_ip}:5000/api/v1/config"
            data = {"ssid": chosen_ssid, "password": wifi_pass}
            colored_print(WHITE, f"[INFO] Switching WIFI to {chosen_ssid} ...")

            # To często rozłączy połączenie zanim dostaniesz odpowiedź — i to OK.
            try:
                r = requests.post(config_url, json=data, timeout=5)
                colored_print(WHITE, f"[INFO] Device response: HTTP {r.status_code}")
            except requests.exceptions.ConnectionError:
                colored_print(YELLOW, "[INFO] Connection dropped (expected if device switched Wi-Fi).")

        except ValueError:
            colored_print(RED, "[ERROR] Invalid input. Please enter a number.")
    except requests.exceptions.RequestException as e:
        colored_print(RED, f"[ERROR] Network error: {e}")


def authenticate_device(devices: List[Device], selected_device: Optional[int], backend_ip: str) -> None:
    if selected_device is None:
        colored_print(RED, "[ERROR] No device selected. Please select a device first.")
        return

    device = devices[selected_device]
    device_ip = device_best_ip(device)
    if not device_ip:
        colored_print(RED, "[ERROR] Selected device has no IP address.")
        return

    # 1) Get uuid
    try:
        response = requests.get(f"http://{device_ip}:5000/api/v1/uuid", timeout=5)
        response.raise_for_status()
        device_uuid = response.json().get("uuid")
    except requests.exceptions.RequestException as e:
        colored_print(RED, f"[ERROR] Failed to get /uuid: {e}")
        return

    if not device_uuid:
        colored_print(RED, "[ERROR] Could not get device UUID.")
        return

    colored_print(GREEN, f"[OK] Device UUID: {device_uuid}")

    # 2) Login to backend
    colored_print(YELLOW, "\n[WARNING] Device must belong to an account.")
    email = input("Enter account email: ").strip()
    password = input("Enter account password: ").strip()

    auth_url = f"http://18.184.29.75:8080/api/v1/auth/device"
    data = {"deviceUuid": device_uuid, "email": email, "password": password}

    try:
        colored_print(WHITE, "[INFO] Request has been sent to backend.")
        response = requests.post(auth_url, json=data, timeout=8)
        response.raise_for_status()
        body = response.json()
    except requests.exceptions.RequestException as e:
        colored_print(RED, f"[ERROR] Backend auth request failed: {e}")
        return
    except ValueError:
        colored_print(RED, "[ERROR] Backend returned non-JSON response.")
        return

    token = body.get("token")
    refresh_token = body.get("refreshToken")

    if not token or not refresh_token:
        colored_print(RED, "[ERROR] Device authentication failed (missing token/refreshToken).")
        return

    colored_print(GREEN, "[OK] Fetched tokens.")

    # 3) Send tokens to device
    rpi_token_url = f"http://{device_ip}:5000/api/v1/token"
    rpi_data = {"token": token, "refreshToken": refresh_token}

    try:
        resp = requests.post(rpi_token_url, json=rpi_data, timeout=8)
        if resp.status_code == 200:
            colored_print(GREEN, "[OK] Device authenticated successfully.")
        else:
            colored_print(RED, f"[ERROR] Device rejected tokens. HTTP {resp.status_code}: {resp.text}")
    except requests.exceptions.RequestException as e:
        colored_print(RED, f"[ERROR] Network error sending tokens to device: {e}")


def test_security() -> None:
    colored_print(YELLOW, "\n[SECURITY TEST 1] Discovering already configured mDNS devices")

    devices = discover_devices()
    if not devices:
        colored_print(RED, "[ERROR] No mDNS devices found.")
        return

    for device in devices:
        device_ip = device_best_ip(device)
        if not device_ip:
            colored_print(RED, f"[ERROR] {device.name} has no IP.")
            continue

        try:
            response = requests.get(f"http://{device_ip}:5000/api/v1/uuid", timeout=3)
            if response.status_code == 200:
                colored_print(GREEN, f"[OK] {device.name} responds to /uuid – might be configured.")
            else:
                colored_print(YELLOW, f"[INFO] {device.name} returned {response.status_code}.")
        except Exception as e:
            colored_print(RED, f"[ERROR] No response from {device.name} ({device_ip}): {e}")

    colored_print(YELLOW, "\n[SECURITY TEST 3] Sending malformed Wi-Fi configuration data")
    for device in devices:
        device_ip = device_best_ip(device)
        if not device_ip:
            continue

        url = f"http://{device_ip}:5000/api/v1/config"
        bad_payloads = [
            {"ssid": "", "password": "12345678"},
            {"ssid": "ValidSSID", "password": ""},
            {"ssid": "ValidSSID", "password": "p" * 1000},
        ]
        for payload in bad_payloads:
            try:
                response = requests.post(url, json=payload, timeout=3)
                if response.status_code >= 400:
                    colored_print(GREEN, f"[OK] {device.name} rejected bad input.")
                else:
                    colored_print(RED, f"[ALERT] {device.name} accepted malformed input!")
            except Exception as e:
                colored_print(RED, f"[ERROR] {device.name} error while sending malformed config: {e}")

    colored_print(YELLOW, "\n[SECURITY TEST 4] DoS test – sending multiple requests in rapid succession")
    for device in devices:
        device_ip = device_best_ip(device)
        if not device_ip:
            continue

        url = f"http://{device_ip}:5000/api/v1/uuid"
        try:
            for _ in range(10):
                requests.get(url, timeout=1)
            colored_print(GREEN, f"[OK] {device.name} handled the request burst.")
        except Exception as e:
            colored_print(RED, f"[ERROR] {device.name} did not respond during burst: {e}")


def main() -> None:
    colored_print(WHITE, "\nRPI QUICK CONFIGURATION")
    backend_ip = get_backend_ip()

    devices: List[Device] = []
    selected_device: Optional[int] = None

    while True:
        print_menu()
        choice = input("Choose an option: ").strip()

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


if __name__ == "__main__":
    main()
