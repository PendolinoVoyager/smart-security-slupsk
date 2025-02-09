from zeroconf import ServiceListener


class MDNSListener(ServiceListener):
    def __init__(self):
        self.devices = []

    def add_service(self, zeroconf, type_, name):
        info = zeroconf.get_service_info(type_, name)
        if info:
            addresses = [addr for addr in info.parsed_addresses()]
            device = {
                "name": name,
                "type": type_,
                "domain": info.server,
                "addresses": addresses,
                "port": info.port,
                "properties": {k.decode(): v.decode() for k, v in info.properties.items()}
            }
            self.devices.append(device)

    def get_devices(self):
        return self.devices
