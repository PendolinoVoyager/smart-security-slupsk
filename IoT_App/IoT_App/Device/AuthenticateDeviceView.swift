import SwiftUI

struct AuthenticateDeviceView: View {
    let device: Device

    @Environment(\.dismiss) private var dismiss

    @State private var uuid: String?
    @State private var password: String = ""

    @State private var isLoading: Bool = true
    @State private var isSubmitting: Bool = false
    @State private var showPasswordField: Bool = false

    @State private var statusMessage: String = "Starting authentication..."
    @State private var errorMessage: String?

    @FocusState private var isPasswordFocused: Bool

    var body: some View {
        ZStack {
            PremiumBackground()

            VStack(spacing: 14) {
                header

                statusCard

                if showPasswordField {
                    passwordCard
                }

                Spacer()
            }
            .padding(.horizontal, 20)
            .padding(.top, 12)
            .padding(.bottom, 16)
        }
        .navigationTitle("Authenticate")
        .navigationBarTitleDisplayMode(.large)
        .toolbarColorScheme(.dark, for: .navigationBar)
        .toolbarBackground(.hidden, for: .navigationBar)
        .toolbar {
            ToolbarItem(placement: .topBarTrailing) {
                Button {
                    dismiss()
                } label: {
                    Image(systemName: "xmark")
                        .font(.system(size: 14, weight: .semibold))
                        .foregroundStyle(.white.opacity(0.95))
                        .padding(10)
                        .background(.ultraThinMaterial, in: Circle())
                        .overlay(Circle().strokeBorder(.white.opacity(0.12), lineWidth: 1))
                }
                .accessibilityLabel("Close")
            }
        }
        .onAppear {
            startAuthentication(device: device)
        }
        .onTapGesture {
            isPasswordFocused = false
        }
    }
}

// MARK: - UI
private extension AuthenticateDeviceView {

    var header: some View {
        VStack(alignment: .leading, spacing: 8) {
            Text("Secure pairing")
                .font(.system(.title2, design: .rounded).weight(.bold))
                .foregroundStyle(.white)

            Text("Device: \(device.name)")
                .font(.system(.callout, design: .rounded))
                .foregroundStyle(.white.opacity(0.75))
        }
        .frame(maxWidth: .infinity, alignment: .leading)
        .padding(.top, 4)
    }

    var statusCard: some View {
        VStack(alignment: .leading, spacing: 10) {
            HStack(spacing: 12) {
                ZStack {
                    Circle()
                        .fill(.white.opacity(0.10))
                        .frame(width: 44, height: 44)
                        .overlay(Circle().strokeBorder(.white.opacity(0.12), lineWidth: 1))

                    if isLoading || isSubmitting {
                        ProgressView()
                            .tint(.white.opacity(0.9))
                            .scaleEffect(0.9)
                    } else if errorMessage != nil {
                        Image(systemName: "xmark.octagon.fill")
                            .font(.system(size: 18, weight: .semibold))
                            .foregroundStyle(.red.opacity(0.9))
                    } else {
                        Image(systemName: "checkmark.seal.fill")
                            .font(.system(size: 18, weight: .semibold))
                            .foregroundStyle(.green.opacity(0.9))
                    }
                }

                VStack(alignment: .leading, spacing: 2) {
                    Text(errorMessage == nil ? "Status" : "Error")
                        .font(.system(.headline, design: .rounded).weight(.semibold))
                        .foregroundStyle(.white)

                    Text(errorMessage ?? statusMessage)
                        .font(.system(.subheadline, design: .rounded))
                        .foregroundStyle(errorMessage == nil ? .white.opacity(0.75) : .red.opacity(0.92))
                        .fixedSize(horizontal: false, vertical: true)
                }

                Spacer()
            }

            Divider()
                .overlay(.white.opacity(0.12))

            // proste “kroki” — wygląd premium bez kombinowania
            VStack(spacing: 8) {
                StepRow(title: "Reach device", isDone: uuid != nil, isActive: isLoading)
                StepRow(title: "Verify backend", isDone: !isLoading && !isSubmitting && errorMessage == nil && uuid != nil && showPasswordField == false, isActive: false)
                StepRow(title: "Send token", isDone: false, isActive: isSubmitting)
            }
            .padding(.top, 2)
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
    }

    var passwordCard: some View {
        VStack(alignment: .leading, spacing: 12) {
            Text("Device password")
                .font(.system(.headline, design: .rounded).weight(.semibold))
                .foregroundStyle(.white)

            SecureField("Enter password", text: $password)
                .focused($isPasswordFocused)
                .textContentType(.password)
                .submitLabel(.go)
                .onSubmit { authenticateWithPassword(device: device) }
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

            Button {
                authenticateWithPassword(device: device)
            } label: {
                HStack(spacing: 10) {
                    if isSubmitting {
                        ProgressView().tint(.white).scaleEffect(0.9)
                    } else {
                        Image(systemName: "lock.open.fill")
                            .font(.system(size: 16, weight: .semibold))
                    }
                    Text(isSubmitting ? "Authenticating..." : "Authenticate")
                        .font(.system(.headline, design: .rounded).weight(.semibold))
                }
                .frame(maxWidth: .infinity)
                .padding(.vertical, 14)
            }
            .buttonStyle(PremiumPrimaryButtonStyle())
            .disabled(isSubmitting || password.isEmpty)
            .opacity((isSubmitting || password.isEmpty) ? 0.7 : 1.0)
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
        .shadow(color: .black.opacity(0.20), radius: 16, x: 0, y: 9)
    }
}

// MARK: - Logic (Twoje requesty, tylko lepsze sterowanie stanem)
private extension AuthenticateDeviceView {

    func startAuthentication(device: Device) {
        // reset stanu
        errorMessage = nil
        statusMessage = "Starting authentication..."
        isLoading = true
        isSubmitting = false
        showPasswordField = false
        uuid = nil

        Task {
            do {
                try await fetchUUID(device: device)
                statusMessage = "Device reachable. Enter password to continue."
                showPasswordField = true
                isLoading = false
                isPasswordFocused = true
            } catch {
                errorMessage = error.localizedDescription
                isLoading = false
            }
        }
    }

    func fetchUUID(device: Device) async throws {
        guard let url = URL(string: "http://\(device.name).local:5000/api/v1/uuid") else {
            throw NSError(domain: "Invalid Raspberry Pi URL", code: 0, userInfo: nil)
        }

        let (data, _) = try await URLSession.shared.data(from: url)

        guard let decodedResponse = try? JSONDecoder().decode(UUIDResponse.self, from: data) else {
            throw NSError(domain: "Failed to decode UUID from JSON", code: 0, userInfo: nil)
        }

        uuid = decodedResponse.uuid
    }

    func authenticateWithPassword(device: Device) {
        guard !isSubmitting else { return }
        errorMessage = nil
        isPasswordFocused = false

        isSubmitting = true
        statusMessage = "Authenticating with backend..."

        Task {
            do {
                let (token, refreshToken) = try await authenticateWithBackend()
                statusMessage = "Sending token to device..."

                try await sendTokenToRaspberryPi(token: token, refreshToken: refreshToken, device: device)

                statusMessage = "Paired successfully."
                isSubmitting = false

                // Profesjonalnie: zamykamy ekran (wracasz do poprzedniego, czyli Home)
                dismiss()
            } catch {
                errorMessage = error.localizedDescription
                isSubmitting = false
            }
        }
    }

    func authenticateWithBackend() async throws -> (String, String) {
        guard let uuid = uuid, !password.isEmpty else {
            throw NSError(domain: "UUID or password missing", code: 0, userInfo: nil)
        }

        guard let email = UserDefaults.standard.string(forKey: "username") else {
            throw NSError(domain: "User email not found in UserDefaults", code: 0, userInfo: nil)
        }

        guard let backendURL = URL(string: "https://smart-intercom.duckdns.org/api/v1/auth/device") else {
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

        return (decodedResponse.token, decodedResponse.refreshToken)
    }

    func sendTokenToRaspberryPi(token: String, refreshToken: String, device: Device) async throws {
        guard let url = URL(string: "http://\(device.name).local:5000/api/v1/token") else {
            throw NSError(domain: "Invalid Raspberry Pi URL", code: 0, userInfo: nil)
        }

        let payload: [String: String] = [
            "token": token,
            "refreshToken": refreshToken
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

    struct UUIDResponse: Decodable { let uuid: String }
    struct AuthDeviceResponse: Decodable { let token: String; let refreshToken: String }
}

// MARK: - Small UI helpers
private struct StepRow: View {
    let title: String
    let isDone: Bool
    let isActive: Bool

    var body: some View {
        HStack(spacing: 10) {
            Image(systemName: isDone ? "checkmark.circle.fill" : (isActive ? "circle.dotted" : "circle"))
                .font(.system(size: 14, weight: .semibold))
                .foregroundStyle(isDone ? .green.opacity(0.9) : .white.opacity(isActive ? 0.85 : 0.35))

            Text(title)
                .font(.system(.subheadline, design: .rounded))
                .foregroundStyle(.white.opacity(isDone ? 0.9 : 0.7))

            Spacer()
        }
    }
}

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
            .shadow(color: .black.opacity(configuration.isPressed ? 0.18 : 0.28),
                    radius: configuration.isPressed ? 10 : 18,
                    x: 0, y: configuration.isPressed ? 6 : 10)
            .scaleEffect(configuration.isPressed ? 0.98 : 1.0)
            .animation(.easeOut(duration: 0.12), value: configuration.isPressed)
    }
}

#Preview {
    NavigationStack {
        AuthenticateDeviceView(device: Device(name: "IoT_Device", type: "TEST", domain: "TEST"))
    }
}
