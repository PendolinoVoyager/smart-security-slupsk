import SwiftUI

struct LoginView: View {

    @EnvironmentObject var appState: AppState
    @StateObject private var viewModel = LoginViewModel()

    @FocusState private var focus: Field?
    @State private var isPasswordVisible = false

    private enum Field { case email, password }

    var body: some View {
        ZStack {
            PremiumBackground()

            ScrollView(showsIndicators: false) {
                VStack(spacing: 22) {
                    header

                    formCard

                    primaryActions
                }
                .padding(.horizontal, 20)
                .padding(.top, 28)
                .padding(.bottom, 24)
            }
        }
        .toolbar(.hidden, for: .navigationBar)
        .scrollDismissesKeyboard(.interactively)
        .onTapGesture { focus = nil }
    }
}

// MARK: - Sections
private extension LoginView {

    var header: some View {
        VStack(spacing: 10) {
            HStack(spacing: 10) {
                Image(systemName: "dot.radiowaves.left.and.right")
                    .font(.system(size: 22, weight: .semibold))
                    .foregroundStyle(.white.opacity(0.95))

                Text("IoT App")
                    .font(.system(.largeTitle, design: .rounded).weight(.bold))
                    .foregroundStyle(.white)
            }

            Text("Connect your world")
                .font(.system(.callout, design: .rounded))
                .foregroundStyle(.white.opacity(0.75))
        }
        .frame(maxWidth: .infinity, alignment: .leading)
        .padding(.top, 8)
        .accessibilityElement(children: .combine)
    }

    var formCard: some View {
        VStack(spacing: 14) {
            HStack {
                Text("Sign in")
                    .font(.system(.title3, design: .rounded).weight(.semibold))
                    .foregroundStyle(.white)

                Spacer()
            }

            VStack(spacing: 12) {
                PremiumTextField(
                    title: "Email",
                    systemImage: "envelope",
                    text: $viewModel.email,
                    keyboard: .emailAddress,
                    textContentType: .username,
                    submitLabel: .next,
                    isSecure: false,
                    isPasswordVisible: .constant(true)
                )
                .focused($focus, equals: .email)
                .onSubmit { focus = .password }

                PremiumTextField(
                    title: "Password",
                    systemImage: "lock",
                    text: $viewModel.password,
                    keyboard: .default,
                    textContentType: .password,
                    submitLabel: .go,
                    isSecure: true,
                    isPasswordVisible: $isPasswordVisible
                )
                .focused($focus, equals: .password)
                .onSubmit { login() }
            }

            if let error = viewModel.errorMessage, !error.isEmpty {
                Text(error)
                    .font(.footnote)
                    .foregroundStyle(.red.opacity(0.95))
                    .frame(maxWidth: .infinity, alignment: .leading)
                    .padding(.top, 4)
                    .transition(.opacity.combined(with: .move(edge: .top)))
                    .accessibilityLabel("Error: \(error)")
            }
        }
        .padding(18)
        .background {
            RoundedRectangle(cornerRadius: 18, style: .continuous)
                .fill(.ultraThinMaterial)
                .overlay {
                    RoundedRectangle(cornerRadius: 18, style: .continuous)
                        .strokeBorder(.white.opacity(0.12), lineWidth: 1)
                }
        }
        .shadow(color: .black.opacity(0.28), radius: 22, x: 0, y: 12)
        .animation(.easeInOut(duration: 0.2), value: viewModel.errorMessage)
    }

    var primaryActions: some View {
        VStack(spacing: 12) {
            Button(action: login) {
                HStack(spacing: 10) {
                    if viewModel.isLoading {
                        ProgressView()
                            .tint(.white)
                            .scaleEffect(0.9)
                    } else {
                        Image(systemName: "arrow.right.circle.fill")
                            .font(.system(size: 18, weight: .semibold))
                    }

                    Text(viewModel.isLoading ? "Signing in..." : "Continue")
                        .font(.system(.headline, design: .rounded).weight(.semibold))
                }
                .frame(maxWidth: .infinity)
                .padding(.vertical, 14)
            }
            .buttonStyle(PremiumPrimaryButtonStyle())
            .disabled(viewModel.isLoading || !viewModel.isValid)
            .opacity((viewModel.isLoading || !viewModel.isValid) ? 0.7 : 1.0)
            .accessibilityHint("Logs into your account")
        }
        .padding(.top, 2)
    }

    func login() {
        focus = nil
        viewModel.login(appState: appState) { success, message in
            print(message)
        }
    }
}

// MARK: - Background
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

            // “Aurora” blobs – subtelniejsze i bardziej warstwowe
            Circle()
                .fill(.blue.opacity(0.25))
                .frame(width: 420, height: 420)
                .blur(radius: 60)
                .offset(x: -210, y: -260)

            Circle()
                .fill(.purple.opacity(0.22))
                .frame(width: 360, height: 360)
                .blur(radius: 60)
                .offset(x: 210, y: 240)

            Circle()
                .fill(.cyan.opacity(0.12))
                .frame(width: 340, height: 340)
                .blur(radius: 70)
                .offset(x: -120, y: 320)
        }
        .accessibilityHidden(true)
    }
}

// MARK: - Premium TextField
private struct PremiumTextField: View {
    let title: String
    let systemImage: String

    @Binding var text: String
    let keyboard: UIKeyboardType
    let textContentType: UITextContentType?
    let submitLabel: SubmitLabel

    let isSecure: Bool
    @Binding var isPasswordVisible: Bool

    var body: some View {
        HStack(spacing: 12) {
            Image(systemName: systemImage)
                .foregroundStyle(.white.opacity(0.75))
                .frame(width: 22)

            if isSecure && !isPasswordVisible {
                SecureField(title, text: $text)
                    .textContentType(textContentType)
                    .submitLabel(submitLabel)
                    .foregroundStyle(.white)
                    .tint(.white)
            } else {
                TextField(title, text: $text)
                    .keyboardType(keyboard)
                    .textContentType(textContentType)
                    .autocorrectionDisabled(true)
                    .textInputAutocapitalization(.never)
                    .submitLabel(submitLabel)
                    .foregroundStyle(.white)
                    .tint(.white)
            }

            if isSecure {
                Button {
                    isPasswordVisible.toggle()
                } label: {
                    Image(systemName: isPasswordVisible ? "eye.slash" : "eye")
                        .foregroundStyle(.white.opacity(0.75))
                        .font(.system(size: 16, weight: .semibold))
                }
                .buttonStyle(.plain)
                .accessibilityLabel(isPasswordVisible ? "Hide password" : "Show password")
            }
        }
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
    }
}

// MARK: - Button Style
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
    LoginView()
        .environmentObject(AppState())
}
