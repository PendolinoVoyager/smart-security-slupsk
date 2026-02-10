import SwiftUI

struct DeviceDetailsView: View {
    let device: Device

    var body: some View {
        ZStack {
            PremiumBackground()

            VStack(spacing: 14) {
                header
                detailsCard
                actions
                Spacer()
            }
            .padding(.horizontal, 20)
            .padding(.top, 12)
            .padding(.bottom, 16)
        }
        .navigationTitle("Device")
        .navigationBarTitleDisplayMode(.large)
        .toolbarColorScheme(.dark, for: .navigationBar)
        .toolbarBackground(.hidden, for: .navigationBar)
    }
}

// MARK: - UI
private extension DeviceDetailsView {

    var header: some View {
        VStack(alignment: .leading, spacing: 8) {
            Text(device.name.isEmpty ? "Unknown device" : device.name)
                .font(.system(.title2, design: .rounded).weight(.bold))
                .foregroundStyle(.white)

            Text("Choose what you want to do next.")
                .font(.system(.callout, design: .rounded))
                .foregroundStyle(.white.opacity(0.75))
        }
        .frame(maxWidth: .infinity, alignment: .leading)
        .padding(.top, 4)
    }

    var detailsCard: some View {
        VStack(alignment: .leading, spacing: 12) {
            HStack {
                Text("Details")
                    .font(.system(.headline, design: .rounded).weight(.semibold))
                    .foregroundStyle(.white)

                Spacer()

                Image(systemName: "info.circle")
                    .foregroundStyle(.white.opacity(0.75))
            }

            Divider().overlay(.white.opacity(0.12))

            infoRow(title: "Service", value: device.type)
            infoRow(title: "Domain", value: device.domain)
            infoRow(title: "Hostname", value: "\(device.name).local")

            Text("Tip: On VPN, Bonjour (.local) discovery may not work. If actions fail, use the VPN IP or backend registry.")
                .font(.system(.footnote, design: .rounded))
                .foregroundStyle(.white.opacity(0.65))
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

    func infoRow(title: String, value: String) -> some View {
        HStack(alignment: .firstTextBaseline) {
            Text(title)
                .font(.system(.subheadline, design: .rounded))
                .foregroundStyle(.white.opacity(0.70))

            Spacer()

            Text(value)
                .font(.system(.subheadline, design: .rounded).weight(.semibold))
                .foregroundStyle(.white.opacity(0.92))
                .multilineTextAlignment(.trailing)
        }
    }

    var actions: some View {
        VStack(spacing: 12) {
            NavigationLink {
                AuthenticateDeviceView(device: device)
            } label: {
                HStack(spacing: 10) {
                    Image(systemName: "lock.open.fill")
                        .font(.system(size: 16, weight: .semibold))
                    Text("Authenticate")
                        .font(.system(.headline, design: .rounded).weight(.semibold))
                }
                .frame(maxWidth: .infinity)
                .padding(.vertical, 14)
            }
            .buttonStyle(PremiumPrimaryButtonStyle())

            NavigationLink {
                AvailableNetworksView(device: device)
            } label: {
                HStack(spacing: 10) {
                    Image(systemName: "wifi")
                        .font(.system(size: 16, weight: .semibold))
                    Text("Switch to your Wi-Fi")
                        .font(.system(.headline, design: .rounded).weight(.semibold))
                }
                .frame(maxWidth: .infinity)
                .padding(.vertical, 14)
            }
            .buttonStyle(PremiumSecondaryButtonStyle())
        }
        .padding(.top, 4)
    }
}

// MARK: - Shared styles (in this file)

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

#Preview {
    NavigationStack {
        DeviceDetailsView(device: Device(id: "1", name: "IoT_Device", type: "_http._tcp.", domain: "local"))
    }
}
