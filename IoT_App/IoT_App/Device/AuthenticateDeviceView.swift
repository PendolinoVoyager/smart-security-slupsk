import SwiftUI

struct AuthenticateDeviceView: View {
    let device: Device

    @State private var uuid: String?
    @State private var password: String = ""
    @State private var isLoading: Bool = true
    @State private var showPasswordField: Bool = false
    @State private var statusMessage: String = "Starting authentication..."
    @State private var errorMessage: String?
    @State private var navigationPath = NavigationPath()
    
    
    var body: some View {
        NavigationStack(path: $navigationPath) {
            ZStack {
                LinearGradient(
                    gradient: Gradient(colors: [Color.black, Color.gray.opacity(0.8)]),
                    startPoint: .topLeading,
                    endPoint: .bottomTrailing
                )
                .ignoresSafeArea()

                VStack(spacing: 20) {
                    if let errorMessage = errorMessage {
                        Text(errorMessage)
                            .foregroundColor(.red)
                            .multilineTextAlignment(.center)
                    } else {
                        Text(statusMessage)
                            .foregroundColor(.green)
                            .multilineTextAlignment(.center)
                    }

                    if isLoading {
                        ProgressView()
                    }

                    if showPasswordField {
                        SecureField("Enter Password", text: $password)
                            .textFieldStyle(RoundedBorderTextFieldStyle())
                            .padding()
                        
                        Button(action: {
                            authenticateWithPassword(device: device)
                        }) {
                            Text("Authenticate Device")
                                .font(.headline)
                                .padding()
                                .frame(maxWidth: .infinity)
                                .background(Color.green)
                                .foregroundColor(.white)
                                .cornerRadius(10)
                        }

                    }
                }
                .padding()
            }
            .navigationTitle("Authenticate Device")
            .onAppear{
                startAuthentication(device: device)
            }
            .navigationDestination(for: String.self) { destination in
                if destination == "HomeView" {
                    HomeView()
                }
            }
        }
    }

    private func startAuthentication(device: Device) {
        Task {
            do {
                try await fetchUUID(device: device)
                statusMessage = "RPI OK"
                showPasswordField = true
                isLoading = false
            } catch {
                errorMessage = error.localizedDescription
                isLoading = false
            }
        }
    }

    private func fetchUUID(device: Device) async throws {
        guard let url = URL(string: "http://\(device.name).local:5000/api/v1/uuid") else {
            throw NSError(domain: "Invalid Raspberry Pi URL", code: 0, userInfo: nil)
        }

        let (data, _) = try await URLSession.shared.data(from: url)

        guard let decodedResponse = try? JSONDecoder().decode(UUIDResponse.self, from: data) else {
            throw NSError(domain: "Failed to decode UUID from JSON", code: 0, userInfo: nil)
        }

        uuid = decodedResponse.uuid
    }

    private func authenticateWithPassword(device: Device) {
        Task {
            do {
                let token = try await authenticateWithBackend()
                statusMessage = "Backend OK"

                try await sendTokenToRaspberryPi(token: token, device: device)
                statusMessage = "RPI OK"

                navigationPath.append("HomeView")
            } catch {
                errorMessage = error.localizedDescription
                isLoading = false
            }
        }
    }

    private func authenticateWithBackend() async throws -> String {
        guard let uuid = uuid, !password.isEmpty else {
            throw NSError(domain: "UUID or password missing", code: 0, userInfo: nil)
        }

        guard let email = UserDefaults.standard.string(forKey: "username") else {
            throw NSError(domain: "User email not found in UserDefaults", code: 0, userInfo: nil)
        }

        guard let backendURL = URL(string: "http://192.168.0.7:8080/api/v1/auth/device") else {
            throw NSError(domain: "Invalid backend URL", code: 0, userInfo: nil)
        }

        let payload: [String: Any] = [
            "deviceUuid": uuid,
            "email": email,
            "password": password
        ]

        let requestData = try JSONSerialization.data(withJSONObject: payload)

        var request = URLRequest(url: backendURL)
        request.httpMethod = "POST"
        request.setValue("application/json", forHTTPHeaderField: "Content-Type")
        request.httpBody = requestData

        let (data, response) = try await URLSession.shared.data(for: request)

        guard let httpResponse = response as? HTTPURLResponse, httpResponse.statusCode == 200 else {
            throw NSError(domain: "Authentication failed with backend", code: 0, userInfo: nil)
        }

        guard let decodedResponse = try? JSONDecoder().decode(AuthDeviceResponse.self, from: data) else {
            throw NSError(domain: "Failed to decode token from backend response", code: 0, userInfo: nil)
        }

        return decodedResponse.token
    }

    private func sendTokenToRaspberryPi(token: String, device: Device) async throws {
        guard let url = URL(string: "http://\(device.name).local:5000/api/v1/token") else {
            throw NSError(domain: "Invalid Raspberry Pi URL", code: 0, userInfo: nil)
        }

        let payload: [String: String] = [
            "token": token
        ]

        let requestData = try JSONSerialization.data(withJSONObject: payload)

        var request = URLRequest(url: url)
        request.httpMethod = "POST"
        request.setValue("application/json", forHTTPHeaderField: "Content-Type")
        request.httpBody = requestData

        let (_, response) = try await URLSession.shared.data(for: request)

        guard let httpResponse = response as? HTTPURLResponse, httpResponse.statusCode == 200 else {
            throw NSError(domain: "Failed to send token to Raspberry Pi", code: 0, userInfo: nil)
        }
    }

    struct UUIDResponse: Decodable {
        let uuid: String
    }

    struct AuthDeviceResponse: Decodable {
        let token: String
    }
}

#Preview {
    AuthenticateDeviceView(device: Device(name: "IoT_Device", type: "TEST", domain: "TEST"))
}
