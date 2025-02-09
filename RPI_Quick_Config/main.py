import ipaddress
import time

import requests
from zeroconf import Zeroconf, ServiceBrowser

from mDNS import MDNSListener

RESET = "\033[0m"
GREEN = "\033[92m"
RED = "\033[91m"
WHITE = "\033[97m"

def backend_ip_set_up() -> str:
    backend_ip: str = input(f"{WHITE}Enter Backend Server IP (f.e 192.168.0.10): {RESET}")
    try:
        ipaddress.ip_address(backend_ip)
        print(f"{GREEN}[OK]{RESET} Backend IP: {backend_ip}")
    except ValueError:
        print(f"{RED}[ERROR]{RESET} Invalid IP address: {backend_ip}")
        backend_ip = backend_ip_set_up()

    return backend_ip


def discover_devices(service_type="_http._tcp.local.", timeout=5):
    zeroconf = Zeroconf()
    listener = MDNSListener()
    ServiceBrowser(zeroconf, service_type, listener)

    print(f"{WHITE}\n[INFO] Searching for mDNS devices...{RESET}")
    time.sleep(timeout)
    zeroconf.close()

    return listener.get_devices()




def main() -> None:
    print(f"{WHITE}\nRPI QUICK CONFIGURATION{RESET}")

    backend_ip: str = backend_ip_set_up()
    devices: list = []
    selected_device: str = ""

    while True:
        print(f"{WHITE}\nMain Menu:{RESET}")
        print("[1] Search for devices")
        print("[2] Devices list")
        print("[3] Select device to configure")
        print("[4] Switch WIFI")
        print("[99] Exit")

        choice: str = input("Choose an option: ")

        match choice:
            case "1":
                devices = discover_devices()
                if not devices:
                    print(f"{RED}[ERROR]{RESET} No mDNS devices found.")
                    continue
                print(f"{GREEN}[OK!]{RESET} mDNS device found!")

            case "2":
                print(f"{WHITE}\n[INFO] Devices list:{RESET}")
                if not devices:
                    print(f"{WHITE}[INFO]{RESET} No devices found.")
                    continue
                for idx, device in enumerate(devices):
                    print(f"{idx}. {device['name']} - {device['addresses'][0]}")

            case "3":
                print(f"{WHITE}\n[INFO] Configure device:{RESET}")
                if not devices:
                    print(f"{WHITE}[INFO]{RESET} No devices found.")
                    continue
                print(f"{WHITE}[INFO]{RESET} Choose device to configure:")
                for idx, device in enumerate(devices):
                    print(f"{idx}. {device['name']} - {device['addresses'][0]}")
                selected_device = input("Enter device index: ")
                if int(selected_device) < 0 or int(selected_device) >= len(devices):
                    print(f"{RED}[ERROR]{RESET} Invalid device index.")
                    continue
                print(f"{GREEN}[OK]{RESET} Selected device: {devices[int(selected_device)]['name']}")
            case "4":
                print(f"{WHITE}\n[INFO] Switch WIFI:{RESET}")

                if not selected_device:
                    print(f"{RED}[ERROR]{RESET} No device selected. Please select a device first.")
                    continue

                try:
                    device_ip = devices[int(selected_device)]['addresses'][0]
                    url = f"http://{device_ip}:5000/api/v1/available-networks"
                    response = requests.get(url, timeout=5)
                    response.raise_for_status()

                    networks = response.json().get("networks", [])

                    if not networks:
                        print(f"{RED}[ERROR]{RESET} No networks found.")
                        continue

                    print(f"{GREEN}[OK]{RESET} Available networks:\n")

                    for idx, network in enumerate(networks):
                        ssid = network.get("ssid", "Unknown")
                        signal = network.get("signal_strength", "N/A")
                        print(f"{WHITE}[{idx}] {ssid} (Signal: {signal}){RESET}")

                    selected_network: str = input("\nSelect network by index: ")

                    if not selected_network.isdigit() or int(selected_network) < 0 or int(selected_network) >= len(
                            networks):
                        print(f"{RED}[ERROR]{RESET} Invalid selection. Please try again.")
                        continue

                    chosen_ssid = networks[int(selected_network)]["ssid"]
                    print(f"{GREEN}[OK]{RESET} You selected: {chosen_ssid}")
                    wifi_pass: str = input("Enter WIFI password: ")
                    url: str = f"http://{device_ip}:5000/api/v1/config"
                    data: dict[str, str] = {"ssid": chosen_ssid, "password": wifi_pass}
                    print(f"{WHITE}[INFO]{RESET} Switch your WIFI to {chosen_ssid}")
                    try:
                        requests.post(url, json=data, timeout=5)
                    except requests.exceptions.ConnectionError:
                        print(f"{RED}[ERROR]{RESET} Could not connect to {device_ip}. Is the device online?")
                        continue

                except requests.exceptions.ConnectionError:
                    print(f"{RED}[ERROR]{RESET} Could not connect to {devices[int(selected_device)]['addresses'][0]}. Is the device online?")
                except requests.exceptions.Timeout:
                    print(f"{RED}[ERROR]{RESET} Connection timed out.")
                except requests.exceptions.HTTPError as e:
                    print(f"{RED}[ERROR]{RESET} HTTP Error: {e}")

            case "99":
                print(f"{GREEN}[INFO]{RESET} Exiting...")
                break

            case _:
                print(f"{RED}[ERROR]{RESET} Invalid choice. Try again.\n")


if __name__ == '__main__':
    main()
