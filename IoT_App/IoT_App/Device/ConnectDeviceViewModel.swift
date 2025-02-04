import Foundation
import Network

class ConnectDeviceViewModel: ObservableObject {
    @Published var devices: [Device] = []

    private var browser: NWBrowser?

    init() {
        startBrowsing()
    }

    func startBrowsing() {
        requestLocalNetworkPermission()

        let parameters = NWParameters()
        parameters.includePeerToPeer = true

        browser = NWBrowser(for: .bonjour(type: "_http._tcp.", domain: nil), using: parameters)

        browser?.browseResultsChangedHandler = { [weak self] results, _ in
            DispatchQueue.main.async {
                print("Znaleziono wyniki przeglądarki mDNS: \(results)")
                self?.devices = results.compactMap { result in
                    if case let NWEndpoint.service(name, type, domain, _) = result.endpoint {
                        print("Dodano urządzenie: \(name), Typ: \(type), Domena: \(domain)")
                        return Device(name: name, type: type, domain: domain)
                    }
                    return nil
                }
            }
        }

        browser?.stateUpdateHandler = { state in
            switch state {
            case .ready:
                print("Przeglądarka mDNS gotowa")
            case .failed(let error):
                print("Błąd przeglądarki mDNS: \(error.localizedDescription)")
            case .setup:
                print("Przeglądarka mDNS: setup")
            case .cancelled:
                print("Przeglądarka mDNS anulowana")
            case .waiting(let error):
                print("Przeglądarka mDNS oczekuje: \(error.localizedDescription)")
            @unknown default:
                print("Nieznany stan przeglądarki mDNS")
            }
        }

        browser?.start(queue: .main)
    }

    func stopBrowsing() {
        browser?.cancel()
    }

    func requestLocalNetworkPermission() {
        let tempBrowser = NWBrowser(for: .bonjour(type: "_http._tcp.", domain: nil), using: .udp)
        tempBrowser.stateUpdateHandler = { state in
            switch state {
            case .ready:
                print("Przyznano dostęp do sieci lokalnej")
            case .failed(let error):
                print("Błąd dostępu do sieci lokalnej: \(error.localizedDescription)")
            default:
                break
            }
            tempBrowser.cancel()
        }
        tempBrowser.start(queue: .main)
    }
}

struct Device: Identifiable, Hashable {
    let id = UUID()
    let name: String
    let type: String
    let domain: String
}
