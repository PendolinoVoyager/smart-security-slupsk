import SwiftUI

// MARK: - Model (must match your JSON mapping)
struct WiFiNetwork: Identifiable, Hashable {
    // Stabilny ID - nie UUID (unikasz mrugania listy po refreshu)
    let id: String
    let ssid: String
    let signalStrength: String

    init(ssid: String, signalStrength: String) {
        self.ssid = ssid
        self.signalStrength = signalStrength
        self.id = "\(ssid)|\(signalStrength)".lowercased()
    }
}

struct AvailableNetworksView: View {
    let device: Device

    @State private var networks: [WiFiNetwork] = []
    @State private var isLoading: Bool = false
    @State private var errorMessage: String? = nil
    @State private var successMessage: String? = nil

    @State private var selectedNetwork: WiFiNetwork?
    @State private var wifiPassword: String = ""
    @State private var isSubmitting: Bool = false
    @State private var showPasswordSheet: Bool = false

    var body: some View {
        ZStack {
            PremiumBackground()

            VStack(spacing: 14) {
                headerCard
                content
            }
            .padding(.horizontal, 20)
            .padding(.top, 12)
            .padding(.bottom, 16)
        }
        .navigationTitle("Wi-Fi Networks")
        .navigationBarTitleDisplayMode(.large)
        .toolbarColorScheme(.dark, for: .navigationBar)
        .toolbarBackground(.hidden, for: .navigationBar)
        .toolbar {
            ToolbarItem(placement: .topBarTrailing) {
                Button {
                    fetchNetworks()
                } label: {
                    Image(systemName: "arrow.clockwise")
                        .font(.system(size: 16, weight: .semibold))
                        .foregroundStyle(.white.opacity(0.95))
                        .padding(10)
                        .background(.ultraThinMaterial, in: Circle())
                        .overlay(Circle().strokeBorder(.white.opacity(0.12), lineWidth: 1))
                }
                .disabled(isLoading)
                .accessibilityLabel("Refresh")
            }
        }
        .onAppear(perform: fetchNetworks)
        .sheet(isPresented: $showPasswordSheet) {
            passwordSheet
                .presentationDetents([.medium])
                .presentationDragIndicator(.visible)
        }
    }
}

// MARK: - UI
private extension AvailableNetworksView {

    var headerCard: some View {
        HStack(spacing: 12) {
            ZStack {
                Circle()
                    .fill(.white.opacity(0.10))
                    .frame(width: 44, height: 44)
                    .overlay(Circle().strokeBorder(.white.opacity(0.12), lineWidth: 1))

                if isLoading {
                    ProgressView().tint(.white.opacity(0.9)).scaleEffect(0.9)
                } else if errorMessage != nil {
                    Image(systemName: "xmark.octagon.fill")
                        .font(.system(size: 18, weight: .semibold))
                        .foregroundStyle(.red.opacity(0.92))
                } else if successMessage != nil {
                    Image(systemName: "checkmark.seal.fill")
                        .font(.system(size: 18, weight: .semibold))
                        .foregroundStyle(.green.opacity(0.9))
                } else {
                    Image(systemName: "wifi")
                        .font(.system(size: 18, weight: .semibold))
                        .foregroundStyle(.white.opacity(0.9))
                }
            }

            VStack(alignment: .leading, spacing: 2) {
                Text("Choose a network")
                    .font(.system(.headline, design: .rounded).weight(.semibold))
                    .foregroundStyle(.white)

                Text(errorMessage ?? successMessage ?? "Tap a network to send credentials to the device.")
                    .font(.system(.subheadline, design: .rounded))
                    .foregroundStyle(.white.opacity(0.75))
                    .lineLimit(2)
            }

            Spacer()
        }
        .padding(14)
        .background {
            RoundedRectangle(cornerRadius: 18, style: .continuous)
                .fill(.ultraThinMaterial)
                .overlay {
                    RoundedRectangle(cornerRadius: 18, style: .continuous)
                        .strokeBorder(.white.opacity(0.12), lineWidth: 1)
                }
        }
        .shadow(color: .black.opacity(0.22), radius: 18, x: 0, y: 10)
        .animation(.easeInOut(duration: 0.2), value: isLoading)
        .animation(.easeInOut(duration: 0.2), value: errorMessage)
        .animation(.easeInOut(duration: 0.2), value: successMessage)
    }

    var content: some View {
        Group {
            if isLoading && networks.isEmpty {
                loadingState
            } else if let error = errorMessage, networks.isEmpty {
                errorState(error)
            } else if networks.isEmpty {
                emptyState
            } else {
                networkList
            }
        }
        .animation(.easeInOut(duration: 0.2), value: networks.count)
        .animation(.easeInOut(duration: 0.2), value: isLoading)
        .animation(.easeInOut(duration: 0.2), value: errorMessage)
    }

    var loadingState: some View {
        VStack(spacing: 14) {
            Spacer()

            ProgressView()
                .tint(.white.opacity(0.9))
                .scaleEffect(1.1)

            Text("Fetching networks…")
                .font(.system(.title3, design: .rounded).weight(.semibold))
                .foregroundStyle(.white)

            Text("Make sure the device is reachable on the same network.")
                .font(.system(.callout, design: .rounded))
                .foregroundStyle(.white.opacity(0.70))
                .multilineTextAlignment(.center)

            Spacer()
        }
        .padding(.horizontal, 8)
    }

    func errorState(_ error: String) -> some View {
        VStack(spacing: 14) {
            Spacer()

            Image(systemName: "exclamationmark.triangle.fill")
                .font(.system(size: 34, weight: .semibold))
                .foregroundStyle(.yellow.opacity(0.9))

            Text("Can’t load networks")
                .font(.system(.title3, design: .rounded).weight(.semibold))
                .foregroundStyle(.white)

            Text(error)
                .font(.system(.callout, design: .rounded))
                .foregroundStyle(.white.opacity(0.70))
                .multilineTextAlignment(.center)
                .padding(.horizontal, 6)

            Button {
                fetchNetworks()
            } label: {
                Text("Try again")
                    .font(.system(.headline, design: .rounded).weight(.semibold))
                    .frame(maxWidth: .infinity)
                    .padding(.vertical, 14)
            }
            .buttonStyle(PremiumPrimaryButtonStyle())

            Spacer()
        }
        .padding(.horizontal, 8)
    }

    var emptyState: some View {
        VStack(spacing: 14) {
            Spacer()

            Image(systemName: "wifi.slash")
                .font(.system(size: 34, weight: .semibold))
                .foregroundStyle(.white.opacity(0.85))

            Text("No networks found")
                .font(.system(.title3, design: .rounded).weight(.semibold))
                .foregroundStyle(.white)

            Text("Try refreshing, or ensure the device has Wi-Fi enabled.")
                .font(.system(.callout, design: .rounded))
                .foregroundStyle(.white.opacity(0.70))
                .multilineTextAlignment(.center)
                .padding(.horizontal, 6)

            Button {
                fetchNetworks()
            } label: {
                Text("Refresh")
                    .font(.system(.headline, design: .rounded).weight(.semibold))
                    .frame(maxWidth: .infinity)
                    .padding(.vertical, 14)
            }
            .buttonStyle(PremiumPrimaryButtonStyle())

            Spacer()
        }
        .padding(.horizontal, 8)
    }

    var networkList: some View {
        ScrollView(showsIndicators: false) {
            VStack(spacing: 12) {
                ForEach(networks) { network in
                    Button {
                        selectedNetwork = network
                        wifiPassword = ""
                        showPasswordSheet = true
                    } label: {
                        NetworkRowCard(network: network)
                    }
                    .buttonStyle(.plain)
                }
            }
            .padding(.top, 4)
            .padding(.bottom, 6)
        }
    }

    var passwordSheet: some View {
        ZStack {
            LinearGradient(
                colors: [
                    Color.black,
                    Color(red: 0.09, green: 0.11, blue: 0.18),
                    Color(red: 0.06, green: 0.06, blue: 0.08)
                ],
                startPoint: .topLeading,
                endPoint: .bottomTrailing
            )
            .ignoresSafeArea()

            VStack(alignment: .leading, spacing: 14) {
                Text("Enter password")
                    .font(.system(.title3, design: .rounded).weight(.bold))
                    .foregroundStyle(.white)

                Text((selectedNetwork?.ssid.isEmpty == false) ? (selectedNetwork?.ssid ?? "") : "Hidden network")
                    .font(.system(.callout, design: .rounded))
                    .foregroundStyle(.white.opacity(0.75))

                SecureField("Wi-Fi password", text: $wifiPassword)
                    .textContentType(.password)
                    .submitLabel(.go)
                    .padding(.horizontal, 14)
                    .padding(.vertical, 12)
                    .background {
                        RoundedRectangle(cornerRadius: 14, style: .continuous)
                            .fill(.white.opacity(0.08))
                            .overlay {
                                RoundedRectangle(cornerRadius: 14, style: .continuous)
                                    .strokeBorder(.white.opacity(0.10), lineWidth: 1)
                            }
                    }
                    .foregroundStyle(.white)
                    .tint(.white)
                    .disabled(isSubmitting)

                Button {
                    guard let selectedNetwork else { return }
                    connectToNetwork(network: selectedNetwork, password: wifiPassword)
                } label: {
                    HStack(spacing: 10) {
                        Image(systemName: "paperplane.fill")
                            .font(.system(size: 16, weight: .semibold))

                        Text("Connect")
                            .font(.system(.headline, design: .rounded).weight(.semibold))
                    }
                    .frame(maxWidth: .infinity)
                    .padding(.vertical, 14)
                }
                .buttonStyle(PremiumPrimaryButtonStyle())
                .disabled(isSubmitting || wifiPassword.isEmpty || selectedNetwork == nil)
                .opacity((isSubmitting || wifiPassword.isEmpty || selectedNetwork == nil) ? 0.7 : 1.0)

                Button {
                    showPasswordSheet = false
                } label: {
                    Text("Cancel")
                        .font(.system(.callout, design: .rounded).weight(.semibold))
                        .frame(maxWidth: .infinity)
                        .padding(.vertical, 12)
                }
                .buttonStyle(PremiumSecondaryButtonStyle())
                .disabled(isSubmitting)

                Spacer()
            }
            .padding(20)
        }
    }
}

// MARK: - Row UI
private struct NetworkRowCard: View {
    let network: WiFiNetwork

    var body: some View {
        HStack(spacing: 12) {
            ZStack {
                RoundedRectangle(cornerRadius: 14, style: .continuous)
                    .fill(.white.opacity(0.10))
                    .frame(width: 48, height: 48)
                    .overlay(
                        RoundedRectangle(cornerRadius: 14, style: .continuous)
                            .strokeBorder(.white.opacity(0.12), lineWidth: 1)
                    )

                Image(systemName: "wifi")
                    .font(.system(size: 18, weight: .semibold))
                    .foregroundStyle(.white.opacity(0.9))
            }

            VStack(alignment: .leading, spacing: 3) {
                Text(network.ssid.isEmpty ? "Hidden network" : network.ssid)
                    .font(.system(.headline, design: .rounded).weight(.semibold))
                    .foregroundStyle(.white)
                    .lineLimit(1)

                Text("Signal: \(network.signalStrength)%")
                    .font(.system(.subheadline, design: .rounded))
                    .foregroundStyle(.white.opacity(0.70))
            }

            Spacer()

            Image(systemName: "chevron.right")
                .font(.system(size: 14, weight: .semibold))
                .foregroundStyle(.white.opacity(0.6))
        }
        .padding(14)
        .background {
            RoundedRectangle(cornerRadius: 18, style: .continuous)
                .fill(.ultraThinMaterial)
                .overlay {
                    RoundedRectangle(cornerRadius: 18, style: .continuous)
                        .strokeBorder(.white.opacity(0.12), lineWidth: 1)
                }
        }
        .shadow(color: .black.opacity(0.18), radius: 14, x: 0, y: 8)
    }
}

// MARK: - Networking
private extension AvailableNetworksView {

    func fetchNetworks() {
        errorMessage = nil
        successMessage = nil
        isLoading = true

        let baseURL = "http://\(device.name).local:5000/api/v1/available-networks"
        guard let url = URL(string: baseURL) else {
            isLoading = false
            errorMessage = "Invalid URL."
            return
        }

        URLSession.shared.dataTask(with: url) { data, response, error in
            DispatchQueue.main.async { isLoading = false }

            if let error = error {
                DispatchQueue.main.async { errorMessage = "Network error: \(error.localizedDescription)" }
                return
            }

            guard let http = response as? HTTPURLResponse else {
                DispatchQueue.main.async { errorMessage = "Invalid response." }
                return
            }

            guard (200...299).contains(http.statusCode) else {
                DispatchQueue.main.async { errorMessage = "Device responded with status \(http.statusCode)." }
                return
            }

            guard let data = data else {
                DispatchQueue.main.async { errorMessage = "No data received." }
                return
            }

            do {
                if let json = try JSONSerialization.jsonObject(with: data) as? [String: Any],
                   let networksData = json["networks"] as? [[String: String]] {

                    let parsed: [WiFiNetwork] = networksData.compactMap { item in
                        guard let ssid = item["ssid"], let signal = item["signal_strength"] else { return nil }
                        return WiFiNetwork(ssid: ssid, signalStrength: signal)
                    }

                    DispatchQueue.main.async { networks = parsed }
                } else {
                    DispatchQueue.main.async { errorMessage = "Invalid JSON format." }
                }
            } catch {
                DispatchQueue.main.async { errorMessage = "Error parsing JSON: \(error.localizedDescription)" }
            }
        }.resume()
    }

    // IMPORTANT: treat disconnect as success (device switches Wi-Fi)
    func connectToNetwork(network: WiFiNetwork, password: String) {
        guard !network.ssid.isEmpty else {
            errorMessage = "Hidden networks are not supported yet."
            return
        }

        guard let url = URL(string: "http://\(device.name).local:5000/api/v1/config") else {
            errorMessage = "Invalid URL for network configuration."
            return
        }

        // UX: zamykamy sheet od razu i uznajemy powodzenie
        isSubmitting = true
        errorMessage = nil
        successMessage = "Sending Wi-Fi credentials…"
        showPasswordSheet = false

        var request = URLRequest(url: url)
        request.httpMethod = "POST"
        request.setValue("application/json", forHTTPHeaderField: "Content-Type")
        request.timeoutInterval = 2.0

        let body: [String: String] = [
            "ssid": network.ssid,
            "password": password
        ]

        do {
            request.httpBody = try JSONSerialization.data(withJSONObject: body, options: [])
        } catch {
            isSubmitting = false
            errorMessage = "Failed to build request."
            successMessage = nil
            return
        }

        URLSession.shared.dataTask(with: request) { _, response, error in
            DispatchQueue.main.async {
                isSubmitting = false
                // ALWAYS OK message – device may drop off immediately
                successMessage = "Wi-Fi updated successfully. Device will reconnect shortly."
            }

            if let http = response as? HTTPURLResponse {
                print("WiFi config response: \(http.statusCode)")
                return
            }

            if let urlError = error as? URLError {
                print("WiFi config URLError: \(urlError.code)")
                return
            }

            if let error {
                print("WiFi config error: \(error.localizedDescription)")
            }
        }.resume()
    }
}

// MARK: - Premium shared UI (in this file)
private struct PremiumBackground: View {
    var body: some View {
        ZStack {
            LinearGradient(
                colors: [
                    Color.black,
                    Color(red: 0.09, green: 0.11, blue: 0.18),
                    Color(red: 0.06, green: 0.06, blue: 0.08)
                ],
                startPoint: .topLeading,
                endPoint: .bottomTrailing
            )
            .ignoresSafeArea()

            Circle()
                .fill(.blue.opacity(0.22))
                .frame(width: 420, height: 420)
                .blur(radius: 60)
                .offset(x: -210, y: -260)

            Circle()
                .fill(.purple.opacity(0.20))
                .frame(width: 360, height: 360)
                .blur(radius: 60)
                .offset(x: 210, y: 240)

            Circle()
                .fill(.cyan.opacity(0.12))
                .frame(width: 340, height: 340)
                .blur(radius: 70)
                .offset(x: -110, y: 330)
        }
        .accessibilityHidden(true)
    }
}

private struct PremiumPrimaryButtonStyle: ButtonStyle {
    func makeBody(configuration: Configuration) -> some View {
        configuration.label
            .foregroundStyle(.white)
            .background {
                RoundedRectangle(cornerRadius: 16, style: .continuous)
                    .fill(
                        LinearGradient(
                            colors: [
                                Color(red: 0.35, green: 0.45, blue: 1.0),
                                Color(red: 0.62, green: 0.30, blue: 0.95)
                            ],
                            startPoint: .leading,
                            endPoint: .trailing
                        )
                    )
                    .overlay {
                        RoundedRectangle(cornerRadius: 16, style: .continuous)
                            .strokeBorder(.white.opacity(0.18), lineWidth: 1)
                    }
            }
            .shadow(
                color: .black.opacity(configuration.isPressed ? 0.18 : 0.28),
                radius: configuration.isPressed ? 10 : 18,
                x: 0, y: configuration.isPressed ? 6 : 10
            )
            .scaleEffect(configuration.isPressed ? 0.98 : 1.0)
            .animation(.easeOut(duration: 0.12), value: configuration.isPressed)
    }
}

private struct PremiumSecondaryButtonStyle: ButtonStyle {
    func makeBody(configuration: Configuration) -> some View {
        configuration.label
            .foregroundStyle(.white.opacity(0.92))
            .background {
                RoundedRectangle(cornerRadius: 16, style: .continuous)
                    .fill(.ultraThinMaterial)
                    .overlay {
                        RoundedRectangle(cornerRadius: 16, style: .continuous)
                            .strokeBorder(.white.opacity(0.14), lineWidth: 1)
                    }
            }
            .shadow(
                color: .black.opacity(configuration.isPressed ? 0.12 : 0.18),
                radius: configuration.isPressed ? 10 : 14,
                x: 0, y: configuration.isPressed ? 6 : 8
            )
            .scaleEffect(configuration.isPressed ? 0.99 : 1.0)
            .animation(.easeOut(duration: 0.12), value: configuration.isPressed)
    }
}

// MARK: - Preview
#Preview {
    NavigationStack {
        AvailableNetworksView(device: Device(id: "1", name: "IoT_Device", type: "TEST", domain: "TEST"))
    }
}
