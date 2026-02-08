import Foundation
import Network

@MainActor
final class ConnectDeviceViewModel: ObservableObject {
    @Published var devices: [Device] = []
    @Published var isScanning: Bool = false
    @Published var errorMessage: String? = nil

    private var browser: NWBrowser?
    private var discovered: [String: Device] = [:]   // dedupe po stabilnym kluczu
    private var timeoutTask: Task<Void, Never>?

    // Jeśli Twoje RPI publikuje coś innego niż http – zmień tu.
    private let bonjourType = "_http._tcp."
    private let bonjourDomain: String? = nil

    deinit {
        Task { @MainActor in
            stopBrowsing()
        }
    }


    func startBrowsing() {
        guard !isScanning else { return }

        errorMessage = nil
        isScanning = true
        devices.removeAll()
        discovered.removeAll()

        requestLocalNetworkPermission()

        let parameters = NWParameters()
        parameters.includePeerToPeer = true

        let browser = NWBrowser(for: .bonjour(type: bonjourType, domain: bonjourDomain), using: parameters)
        self.browser = browser

        browser.browseResultsChangedHandler = { [weak self] results, _ in
            guard let self else { return }
            Task { @MainActor in
                self.handle(results: results)
            }
        }

        browser.stateUpdateHandler = { [weak self] state in
            guard let self else { return }
            Task { @MainActor in
                self.handle(state: state)
            }
        }

        browser.start(queue: .main)
        startTimeout(seconds: 8)
    }

    func stopBrowsing() {
        timeoutTask?.cancel()
        timeoutTask = nil

        browser?.cancel()
        browser = nil

        isScanning = false
    }

    // MARK: - Private

    private func handle(results: Set<NWBrowser.Result>) {
        for result in results {
            guard case let NWEndpoint.service(name, type, domain, _) = result.endpoint else { continue }

            let key = "\(name)|\(type)|\(domain)".lowercased()
            if discovered[key] == nil {
                discovered[key] = Device(id: key, name: name, type: type, domain: domain)
            }
        }

        // stabilna lista (sort po nazwie)
        devices = discovered.values.sorted { $0.name.localizedCaseInsensitiveCompare($1.name) == .orderedAscending }

        // jeśli coś znaleźliśmy — możemy uznać, że skan “ma sens”, ale nie musimy go zatrzymywać
        if !devices.isEmpty {
            // możesz zatrzymać timeout, żeby nie pokazało błędu
            // (timeout i tak tylko przy pustej liście daje error)
        }
    }

    private func handle(state: NWBrowser.State) {
        switch state {
        case .ready:
            // browser działa, nic nie rób
            break

        case .failed(let error):
            // bardzo często: brak uprawnień do Local Network
            errorMessage = mapBrowserError(error)
            stopBrowsing()

        case .waiting(let error):
            // np. network down
            errorMessage = mapBrowserError(error)

        case .cancelled:
            isScanning = false

        case .setup:
            break

        @unknown default:
            break
        }
    }

    private func startTimeout(seconds: UInt64) {
        timeoutTask?.cancel()
        timeoutTask = Task { [weak self] in
            try? await Task.sleep(nanoseconds: seconds * 1_000_000_000)
            guard let self else { return }
            await MainActor.run {
                // timeout ma sens tylko jeśli dalej skanujemy i nic nie znaleźliśmy
                if self.isScanning && self.devices.isEmpty && self.errorMessage == nil {
                    self.errorMessage = "No devices found. If you’re on VPN, mDNS/Bonjour discovery may not work."
                    self.isScanning = false
                }
            }
        }
    }

    private func mapBrowserError(_ error: NWError) -> String {
        // Nie bawimy się w pełną mapę – ma działać i być czytelne.
        // Najczęściej tu wpadnie permission / network.
        return "Scanning failed: \(error.localizedDescription). Check Local Network permission and network connectivity."
    }

    func requestLocalNetworkPermission() {
        // Hack do wywołania prompta Local Network
        let temp = NWBrowser(for: .bonjour(type: bonjourType, domain: bonjourDomain), using: .udp)
        temp.stateUpdateHandler = { _ in
            temp.cancel()
        }
        temp.start(queue: .main)
    }
}

// MARK: - Device
struct Device: Identifiable, Hashable {
    let id: String          // STABILNE dla SwiftUI
    let name: String
    let type: String
    let domain: String
}
