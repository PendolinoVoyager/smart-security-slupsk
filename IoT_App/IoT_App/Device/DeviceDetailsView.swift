import SwiftUI

struct WiFiNetwork: Identifiable {
    let id = UUID()
    let ssid: String
    let signalStrength: String
}

struct DeviceDetailsView: View {
    let device: Device
    @State private var showNetworksView = false

    var body: some View {
        NavigationView {
            ZStack {
                LinearGradient(
                    gradient: Gradient(colors: [Color.black, Color.gray.opacity(0.8)]),
                    startPoint: .topLeading,
                    endPoint: .bottomTrailing
                )
                .ignoresSafeArea()

                VStack(spacing: 20) {
                    Spacer()

                    VStack(spacing: 20) {
                        NavigationLink(destination: AvailableNetworksView(device: device)) {
                            Text("Switch to Your Wi-Fi")
                                .font(.headline)
                                .padding()
                                .frame(maxWidth: .infinity)
                                .background(Color.blue)
                                .foregroundColor(.white)
                                .cornerRadius(10)
                        }
                        
                        NavigationLink(destination: AuthenticateDeviceView(device: device)) {
                            Text("Authenticate")
                                .font(.headline)
                                .padding()
                                .frame(maxWidth: .infinity)
                                .background(Color.green)
                                .foregroundColor(.white)
                                .cornerRadius(10)
                        }
                    }
                    .padding(.horizontal, 20)

                    Spacer()
                }
                .padding(.vertical, 40)
            }
            .navigationTitle(device.name)
        }
    }
}


struct BlurView: UIViewRepresentable {
    var style: UIBlurEffect.Style

    func makeUIView(context: Context) -> UIVisualEffectView {
        return UIVisualEffectView(effect: UIBlurEffect(style: style))
    }

    func updateUIView(_ uiView: UIVisualEffectView, context: Context) {}
}

#Preview {
    DeviceDetailsView(device: Device(name: "IoT_Device", type: "_http._tcp.", domain: "local"))
}
