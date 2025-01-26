import SwiftUI

struct AvailableNetworksView: View {
    @State private var networks: [WiFiNetwork] = []
    @State private var showPasswordPrompt = false
    @State private var selectedNetwork: String = ""
    @State private var wifiPassword: String = ""
    
    let device: Device
    
    init(device: Device) {
        UINavigationBar.appearance().largeTitleTextAttributes = [.foregroundColor: UIColor.white]
        
        self.device = device
    }

    var body: some View {
        NavigationView {
            ZStack {
                LinearGradient(
                    gradient: Gradient(colors: [Color.black, Color.gray.opacity(0.8)]),
                    startPoint: .topLeading,
                    endPoint: .bottomTrailing
                )
                .ignoresSafeArea()

                ScrollView {
                    VStack(spacing: 20) {
                        Spacer()
                        ForEach(networks) { network in
                            Button(action: {
                                selectedNetwork = network.ssid
                                showPasswordPrompt = true
                            }) {
                                glassEffectCard(
                                    title: network.ssid.isEmpty ? "(Hidden Network)" : network.ssid,
                                    subtitle: "Signal: \(network.signalStrength)%"
                                )
                            }
                        }
                    }
                    .padding(.horizontal, 20)
                }
            }
            .navigationTitle("Available Networks")
            .onAppear(perform: fetchNetworks)
            .alert("Enter Wi-Fi Password", isPresented: $showPasswordPrompt) {
                TextField("Password", text: $wifiPassword)
                    .textContentType(.password)

                Button("Connect") {
                    connectToNetwork(ssid: selectedNetwork, password: wifiPassword)
                }
                Button("Cancel", role: .cancel) {}
            }
        }
    }

    private func glassEffectCard(title: String, subtitle: String) -> some View {
        VStack(alignment: .leading, spacing: 5) {
            Text(title)
                .font(.headline)
                .foregroundColor(.white)

            Text(subtitle)
                .font(.subheadline)
                .foregroundColor(.gray)
        }
        .padding()
        .frame(maxWidth: .infinity, alignment: .leading)
        .background(BlurView(style: .systemUltraThinMaterialDark))
        .cornerRadius(15)
        .overlay(
            RoundedRectangle(cornerRadius: 15)
                .stroke(Color.white.opacity(0.2), lineWidth: 1)
        )
    }

    private func fetchNetworks() {
        let baseURL = "http://\(device.name).local:5000/api/v1/available-networks"
        guard let url = URL(string: baseURL) else {
            print("Invalid URL: \(baseURL)")
            return
        }

        URLSession.shared.dataTask(with: url) { data, response, error in
            if let error = error {
                print("Error fetching networks: \(error.localizedDescription)")
                return
            }

            guard let data = data else {
                print("No data received")
                return
            }

            do {
                if let json = try JSONSerialization.jsonObject(with: data) as? [String: Any],
                   let networksData = json["networks"] as? [[String: String]] {
                    DispatchQueue.main.async {
                        self.networks = networksData.compactMap { item in
                            if let ssid = item["ssid"], let signal = item["signal_strength"] {
                                return WiFiNetwork(ssid: ssid, signalStrength: signal)
                            }
                            return nil
                        }
                    }
                } else {
                    print("Invalid JSON format")
                }
            } catch {
                print("Error parsing JSON: \(error.localizedDescription)")
            }
        }.resume()
    }


    private func connectToNetwork(ssid: String, password: String) {
        guard let url = URL(string: "http://\(device.name).local:5000/api/v1/config") else {
            print("Invalid URL for network configuration")
            return
        }

        // Tworzenie żądania
        var request = URLRequest(url: url)
        request.httpMethod = "POST"
        request.setValue("application/json", forHTTPHeaderField: "Content-Type")

        // Tworzenie danych JSON
        let body: [String: String] = [
            "ssid": ssid,
            "password": password
        ]
        
        do {
            let jsonData = try JSONSerialization.data(withJSONObject: body, options: [])
            request.httpBody = jsonData
        } catch {
            print("Error serializing JSON: \(error.localizedDescription)")
            return
        }
        
        URLSession.shared.dataTask(with: request) { data, response, error in
            if let error = error {
                print("Error connecting to network: \(error.localizedDescription)")
                return
            }

            if let httpResponse = response as? HTTPURLResponse, httpResponse.statusCode == 200 {
                print("Successfully connected to network \(ssid)")
            } else {
                print("Failed to connect to network \(ssid)")
            }
        }.resume()
    }

}

#Preview {
    AvailableNetworksView(device: Device(name: "IoT_Device", type: "TEST", domain: "TEST"))
}
