import SwiftUI
import Foundation

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

    @State private var backendVerified: Bool = false
    @State private var tokenSent: Bool = false

    @FocusState private var isPasswordFocused: Bool

    // ✅ Success popup
    @State private var showSuccessAlert: Bool = false

    // ✅ keyboard handling
    @State private var keyboardInset: CGFloat = 0

    var body: some View {
        ZStack {
            PremiumBackground()

            // ✅ Scroll + safe layout when keyboard appears
            ScrollView(showsIndicators: false) {
                VStack(spacing: 14) {
                    header
                    statusCard

                    if showPasswordField {
                        passwordCard
                    }

                    // tiny spacer to keep bottom safe
                    Spacer(minLength: 8)
                }
                .padding(.horizontal, 20)
                .padding(.top, 12)
                .padding(.bottom, 16)
                // ✅ push content above keyboard
                .padding(.bottom, keyboardInset)
                .animation(.easeOut(duration: 0.22), value: keyboardInset)
            }
        }
        .navigationTitle("Authenticate")
        .navigationBarTitleDisplayMode(.large)
        .toolbarColorScheme(.dark, for: .navigationBar)
        .toolbarBackground(.hidden, for: .navigationBar)
        .toolbar {
            ToolbarItem(placement: .topBarTrailing) {
                Button { dismiss() } label: {
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
            startKeyboardObserver()
        }
        .onDisappear {
            stopKeyboardObserver()
        }
        .onTapGesture { isPasswordFocused = false }
        .alert("Device paired", isPresented: $showSuccessAlert) {
            Button("OK") { dismiss() }
        } message: {
            Text("Authentication finished successfully. You can now use the device.")
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

            Divider().overlay(.white.opacity(0.12))

            VStack(spacing: 8) {
                StepRow(title: "Reach device", isDone: uuid != nil, isActive: isLoading)
                StepRow(title: "Verify backend", isDone: backendVerified, isActive: isSubmitting && uuid != nil && !backendVerified)
                StepRow(title: "Send token", isDone: tokenSent, isActive: isSubmitting && backendVerified && !tokenSent)
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

            Button { authenticateWithPassword(device: device) } label: {
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

// MARK: - Logic + Debug
private extension AuthenticateDeviceView {

    func startAuthentication(device: Device) {
        errorMessage = nil
        statusMessage = "Starting authentication..."
        isLoading = true
        isSubmitting = false
        showPasswordField = false
        uuid = nil
        backendVerified = false
        tokenSent = false

        let urlString = "http://\(device.name).local:5000/api/v1/uuid"
        print("=== AUTH FLOW START ===")
        print("Device: name=\(device.name), type=\(device.type), domain=\(device.domain)")
        print("UUID URL: \(urlString)")

        Task {
            do {
                try await fetchUUID(device: device)
                statusMessage = "Device reachable. Enter password to continue."
                showPasswordField = true
                isLoading = false
                isPasswordFocused = true

                print("✅ UUID OK -> uuid=\(uuid ?? "nil")")
                print("=== AUTH FLOW WAITING FOR PASSWORD ===")
            } catch {
                logError(stage: "FETCH_UUID", error: error)
                errorMessage = prettyError(stage: "Device", error: error)
                isLoading = false
            }
        }
    }

    func authenticateWithPassword(device: Device) {
        guard !isSubmitting else { return }
        guard uuid != nil else {
            errorMessage = "Device UUID missing. Restart authentication."
            return
        }

        errorMessage = nil
        isPasswordFocused = false

        isSubmitting = true
        backendVerified = false
        tokenSent = false
        statusMessage = "Authenticating with backend..."

        print("=== AUTH SUBMIT START ===")
        print("User password: \(password.isEmpty ? "<empty>" : "<provided>")")

        Task {
            do {
                let (token, refreshToken) = try await authenticateWithBackend()
                backendVerified = true
                statusMessage = "Sending token to device..."

                print("✅ [BACKEND] token len=\(token.count), refresh len=\(refreshToken.count)")

                try await sendTokenToRaspberryPi(token: token, refreshToken: refreshToken, device: device)
                tokenSent = true

                statusMessage = "Paired successfully."
                isSubmitting = false

                print("✅ [SEND_TOKEN] done")
                print("=== AUTH FLOW SUCCESS ===")

                showSuccessAlert = true
            } catch {
                logError(stage: "AUTH_SUBMIT", error: error)
                errorMessage = prettyError(stage: "Authentication", error: error)
                isSubmitting = false
            }
        }
    }

    func fetchUUID(device: Device) async throws {
        let urlString = "http://\(device.name).local:5000/api/v1/uuid"
        guard let url = URL(string: urlString) else {
            throw NSError(domain: "Invalid Raspberry Pi URL", code: 0, userInfo: [
                NSLocalizedDescriptionKey: "Invalid URL: \(urlString)"
            ])
        }

        print("→ [FETCH_UUID] GET \(urlString)")

        let (data, response) = try await URLSession.shared.data(from: url)
        logHTTP(stage: "FETCH_UUID", response: response, data: data)

        do {
            let decoded = try JSONDecoder().decode(UUIDResponse.self, from: data)
            uuid = decoded.uuid
        } catch {
            print("✗ [FETCH_UUID] decode failed, raw body=\(String(data: data, encoding: .utf8) ?? "<binary>")")
            throw error
        }
    }

    func authenticateWithBackend() async throws -> (String, String) {
        guard let uuid = uuid, !password.isEmpty else {
            throw NSError(domain: "UUID or password missing", code: 0, userInfo: nil)
        }

        guard let email = UserDefaults.standard.string(forKey: "username") else {
            throw NSError(domain: "User email not found in UserDefaults", code: 0, userInfo: nil)
        }

        let urlString = "https://smart-intercom.duckdns.org/api/v1/auth/device"
        guard let backendURL = URL(string: urlString) else {
            throw NSError(domain: "Invalid backend URL", code: 0, userInfo: [
                NSLocalizedDescriptionKey: "Invalid URL: \(urlString)"
            ])
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

        print("→ [BACKEND] POST \(urlString)")
        print("  payload: deviceUuid=\(uuid), email=\(email), password=<redacted>")

        let (data, response) = try await URLSession.shared.data(for: request)
        logHTTP(stage: "BACKEND", response: response, data: data)

        guard let http = response as? HTTPURLResponse else {
            throw NSError(domain: "Backend invalid response", code: 0, userInfo: nil)
        }

        guard http.statusCode == 200 else {
            throw NSError(domain: "Authentication failed with backend", code: http.statusCode, userInfo: nil)
        }

        do {
            let decoded = try JSONDecoder().decode(AuthDeviceResponse.self, from: data)
            return (decoded.token, decoded.refreshToken)
        } catch {
            print("✗ [BACKEND] decode failed, raw body=\(String(data: data, encoding: .utf8) ?? "<binary>")")
            throw error
        }
    }

    func sendTokenToRaspberryPi(token: String, refreshToken: String, device: Device) async throws {
        let urlString = "http://\(device.name).local:5000/api/v1/token"
        guard let url = URL(string: urlString) else {
            throw NSError(domain: "Invalid Raspberry Pi URL", code: 0, userInfo: [
                NSLocalizedDescriptionKey: "Invalid URL: \(urlString)"
            ])
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

        print("→ [SEND_TOKEN] POST \(urlString)")
        print("  payload: token=<len \(token.count)>, refresh=<len \(refreshToken.count)>")

        let (data, response) = try await URLSession.shared.data(for: request)
        logHTTP(stage: "SEND_TOKEN", response: response, data: data)

        guard let http = response as? HTTPURLResponse else {
            throw NSError(domain: "Device invalid response", code: 0, userInfo: nil)
        }

        guard http.statusCode == 200 else {
            throw NSError(domain: "Failed to send token to Raspberry Pi", code: http.statusCode, userInfo: nil)
        }
    }

    // MARK: - Debug helpers

    func logHTTP(stage: String, response: URLResponse, data: Data?) {
        if let http = response as? HTTPURLResponse {
            print("← [\(stage)] HTTP \(http.statusCode)")
            if let data, !data.isEmpty {
                let body = String(data: data, encoding: .utf8) ?? "<binary>"
                let trimmed = body.count > 300 ? String(body.prefix(300)) + "..." : body
                print("  body: \(trimmed)")
            } else {
                print("  body: <empty>")
            }
        } else {
            print("← [\(stage)] non-HTTP response: \(response)")
        }
    }

    func logError(stage: String, error: Error) {
        print("✗ [\(stage)] ERROR: \(error)")
        if let urlError = error as? URLError {
            print("  URLError.code: \(urlError.code) (\(urlError.code.rawValue))")
        }
        let ns = error as NSError
        print("  NSError.domain: \(ns.domain), code: \(ns.code)")
    }

    func prettyError(stage: String, error: Error) -> String {
        if let urlError = error as? URLError {
            switch urlError.code {
            case .cannotFindHost, .dnsLookupFailed:
                return "\(stage) error: Host not found. Bonjour/.local may not resolve (VPN?)."
            case .cannotConnectToHost:
                return "\(stage) error: Can't connect to host. Device/service may be offline or blocked."
            case .timedOut:
                return "\(stage) error: Connection timed out."
            case .notConnectedToInternet:
                return "\(stage) error: No network connection."
            default:
                return "\(stage) error: \(urlError.localizedDescription)"
            }
        }
        return "\(stage) error: \(error.localizedDescription)"
    }

    // MARK: - Keyboard observer (keeps header + button from overlapping)
    func startKeyboardObserver() {
        NotificationCenter.default.addObserver(
            forName: UIResponder.keyboardWillChangeFrameNotification,
            object: nil,
            queue: .main
        ) { note in
            guard
                let userInfo = note.userInfo,
                let endFrame = userInfo[UIResponder.keyboardFrameEndUserInfoKey] as? CGRect,
                let duration = userInfo[UIResponder.keyboardAnimationDurationUserInfoKey] as? Double
            else { return }

            // Convert keyboard frame to inset.
            let screenHeight = UIScreen.main.bounds.height
            let heightFromBottom = max(0, screenHeight - endFrame.origin.y)

            // A bit of extra breathing room so fields/buttons don’t touch the keyboard.
            withAnimation(.easeOut(duration: duration)) {
                keyboardInset = max(0, heightFromBottom - 10)
            }
        }

        NotificationCenter.default.addObserver(
            forName: UIResponder.keyboardWillHideNotification,
            object: nil,
            queue: .main
        ) { note in
            let duration = (note.userInfo?[UIResponder.keyboardAnimationDurationUserInfoKey] as? Double) ?? 0.2
            withAnimation(.easeOut(duration: duration)) {
                keyboardInset = 0
            }
        }
    }

    func stopKeyboardObserver() {
        NotificationCenter.default.removeObserver(self, name: UIResponder.keyboardWillChangeFrameNotification, object: nil)
        NotificationCenter.default.removeObserver(self, name: UIResponder.keyboardWillHideNotification, object: nil)
    }
}

// MARK: - DTOs (IN THIS FILE)
private struct UUIDResponse: Decodable { let uuid: String }
private struct AuthDeviceResponse: Decodable { let token: String; let refreshToken: String }

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

// MARK: - Premium UI (IN THIS FILE)
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

// MARK: - Preview
#Preview {
    NavigationStack {
        AuthenticateDeviceView(device: Device(id: "1", name: "IoT_Device", type: "TEST", domain: "TEST"))
    }
}
