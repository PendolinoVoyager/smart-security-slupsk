import SwiftUI

struct LoginView: View {
    
    @EnvironmentObject var appState: AppState
    @StateObject private var viewModel = LoginViewModel()
    
    var body: some View {
        ZStack {
            // Background with gradient and blurred circles
            LinearGradient(
                gradient: Gradient(colors: [Color.black, Color.gray.opacity(0.8)]),
                startPoint: .topLeading,
                endPoint: .bottomTrailing
            )
            .ignoresSafeArea()

            // Blurred circles for modern look
            Circle()
                .fill(Color.blue.opacity(0.3))
                .frame(width: 300, height: 300)
                .blur(radius: 50)
                .offset(x: -150, y: -200)

            Circle()
                .fill(Color.purple.opacity(0.3))
                .frame(width: 250, height: 250)
                .blur(radius: 50)
                .offset(x: 150, y: 250)

            VStack {
                // Title Section
                VStack {
                    Text("IoT App")
                        .font(.largeTitle)
                        .foregroundColor(.white)
                        .fontWeight(.bold)
                    Text("Connect your world")
                        .font(.subheadline)
                        .foregroundColor(.white.opacity(0.7))
                }
                .padding(.top, 50)

                Spacer()

                // Login Form with Glassmorphism Effect
                VStack(spacing: 16) {
                    Text("Login")
                        .font(.title2)
                        .foregroundColor(.white)
                        .fontWeight(.semibold)
                        .frame(maxWidth: .infinity, alignment: .leading)

                    EmailTextFieldView(email: $viewModel.email)
                    PasswordSecureFieltView(password: $viewModel.password)

                    if let errorMessage = viewModel.errorMessage {
                        Text(errorMessage)
                            .font(.caption)
                            .foregroundColor(.red)
                            .frame(maxWidth: .infinity, alignment: .leading)
                            .padding(.top, 4)
                    }
                }
                .padding(.horizontal, 16)
                .padding(.vertical, 20)
                .background(
                    Color.white.opacity(0.2)
                        .blur(radius: 10)
                        .background(VisualEffectBlurView(style: .systemUltraThinMaterial))
                        .cornerRadius(15)
                )
                .shadow(color: .black.opacity(0.2), radius: 10, x: 0, y: 5)

                Spacer()

                // Buttons Section
                VStack(spacing: 10) {
                    Button {
                        viewModel.login(appState: appState) { success, message in
                            if success {
                                print(message)
                            } else {
                                print(message)
                            }
                        }
                    } label: {
                        ButtonView(buttonText: "Login Now!")
                    }

                    Button(action: {
                        print("Forgot Password")
                    }) {
                        Text("Forgot Password?")
                            .font(.subheadline)
                            .foregroundColor(.white.opacity(0.8))
                    }
                }
                .padding(.horizontal, 16)

                Spacer()
            }
            .padding(.horizontal)
        }
    }
}

// Helper View for Visual Effect Blur
struct VisualEffectBlurView: UIViewRepresentable {
    var style: UIBlurEffect.Style

    func makeUIView(context: Context) -> UIVisualEffectView {
        UIVisualEffectView(effect: UIBlurEffect(style: style))
    }

    func updateUIView(_ uiView: UIVisualEffectView, context: Context) {}
}

#Preview {
    LoginView()
        .environmentObject(AppState())
}
