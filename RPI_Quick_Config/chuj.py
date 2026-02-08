import time
from zeroconf import Zeroconf, ServiceBrowser, ServiceListener


class SimpleListener(ServiceListener):
    def add_service(self, zeroconf, service_type, name):
        info = zeroconf.get_service_info(service_type, name)
        if not info:
            return

        addresses = info.parsed_addresses()
        print("\n=== SERVICE FOUND ===")
        print(f"Name: {name}")
        print(f"Type: {service_type}")
        print(f"Server: {info.server}")
        print(f"Port: {info.port}")
        print(f"Addresses: {addresses}")
        print("=====================\n")

    def remove_service(self, zeroconf, service_type, name):
        print(f"Service removed: {name}")

    def update_service(self, zeroconf, service_type, name):
        print(f"Service updated: {name}")


if __name__ == "__main__":
    SERVICE_TYPE = "_http._tcp.local."
    TIMEOUT = 10  # seconds

    print(f"Listening for mDNS services ({SERVICE_TYPE}) for {TIMEOUT}s...\n")

    zeroconf = Zeroconf()
    listener = SimpleListener()
    browser = ServiceBrowser(zeroconf, SERVICE_TYPE, listener)

    try:
        time.sleep(TIMEOUT)
    finally:
        zeroconf.close()

    print("\nDone.")
