import SwiftUI

struct ConnectDeviceView: View {
    @StateObject private var viewModel = ConnectDeviceViewModel()

    var body: some View {
        ZStack {
            PremiumBackground()

            VStack(spacing: 14) {
                statusHeader

                content
            }
            .padding(.horizontal, 20)
            .padding(.top, 12)
            .padding(.bottom, 16)
        }
        .navigationTitle("Connect Device")
        .navigationBarTitleDisplayMode(.large)
        .toolbarColorScheme(.dark, for: .navigationBar)
        .toolbarBackground(.hidden, for: .navigationBar)
        .toolbar {
            ToolbarItem(placement: .topBarTrailing) {
                HStack(spacing: 10) {
                    Button {
                        viewModel.startBrowsing()
                    } label: {
                        Image(systemName: "arrow.clockwise")
                    }
                    .accessibilityLabel("Refresh")

                    Button {
                        // jeśli nie masz isBrowsing, to zostaw tylko stopBrowsing() zawsze
                        viewModel.stopBrowsing()
                    } label: {
                        Image(systemName: "stop.circle")
                    }
                    .accessibilityLabel("Stop scanning")
                }
                .font(.system(size: 16, weight: .semibold))
                .foregroundStyle(.white.opacity(0.95))
                .padding(10)
                .background(.ultraThinMaterial, in: Capsule())
                .overlay(
                    Capsule().strokeBorder(.white.opacity(0.12), lineWidth: 1)
                )
            }
        }
        .onAppear { viewModel.startBrowsing() }
        .onDisappear { viewModel.stopBrowsing() }
    }
}

// MARK: - UI Sections
private extension ConnectDeviceView {

    var statusHeader: some View {
        HStack(alignment: .center, spacing: 12) {
            ZStack {
                Circle()
                    .fill(.white.opacity(0.10))
                    .frame(width: 44, height: 44)
                    .overlay(Circle().strokeBorder(.white.opacity(0.12), lineWidth: 1))

                ProgressView()
                    .tint(.white.opacity(0.9))
                    .scaleEffect(0.9)
            }

            VStack(alignment: .leading, spacing: 2) {
                Text(viewModel.devices.isEmpty ? "Scanning for nearby devices" : "Devices found")
                    .font(.system(.headline, design: .rounded).weight(.semibold))
                    .foregroundStyle(.white)

                Text(viewModel.devices.isEmpty ? "Make sure your device is powered on and discoverable." : "Tap a device to continue pairing.")
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
    }

    var content: some View {
        Group {
            if viewModel.devices.isEmpty {
                emptyState
            } else {
                deviceList
            }
        }
        .animation(.easeInOut(duration: 0.2), value: viewModel.devices.count)
    }

    var emptyState: some View {
        VStack(spacing: 14) {
            Spacer()

            Image(systemName: "dot.radiowaves.left.and.right")
                .font(.system(size: 34, weight: .semibold))
                .foregroundStyle(.white.opacity(0.85))

            Text("Looking for devices…")
                .font(.system(.title3, design: .rounded).weight(.semibold))
                .foregroundStyle(.white)

            Text("If nothing shows up, try turning Bluetooth/Wi-Fi off and on, or move closer to the device.")
                .font(.system(.callout, design: .rounded))
                .foregroundStyle(.white.opacity(0.70))
                .multilineTextAlignment(.center)
                .padding(.horizontal, 6)

            Button {
                viewModel.startBrowsing()
            } label: {
                Text("Try again")
                    .font(.system(.headline, design: .rounded).weight(.semibold))
                    .frame(maxWidth: .infinity)
                    .padding(.vertical, 14)
            }
            .buttonStyle(PremiumPrimaryButtonStyle())
            .padding(.top, 6)

            Spacer()
        }
        .padding(.horizontal, 8)
    }

    var deviceList: some View {
        ScrollView(showsIndicators: false) {
            VStack(spacing: 12) {
                ForEach(viewModel.devices) { device in
                    NavigationLink {
                        DeviceDetailsView(device: device)
                    } label: {
                        DeviceRowCard(device: device)
                    }
                    .buttonStyle(.plain)
                }
            }
            .padding(.top, 4)
            .padding(.bottom, 6)
        }
    }
}

// MARK: - Row
private struct DeviceRowCard: View {
    let device: Device // <- zakładam, że masz typ Device identyczny jak wcześniej

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

                Image(systemName: "sensor.tag.radiowaves.forward")
                    .font(.system(size: 18, weight: .semibold))
                    .foregroundStyle(.white.opacity(0.9))
            }

            VStack(alignment: .leading, spacing: 3) {
                Text(device.name ?? "Unknown device")
                    .font(.system(.headline, design: .rounded).weight(.semibold))
                    .foregroundStyle(.white)
                    .lineLimit(1)

                Text("Tap to configure")
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

// MARK: - Shared styles
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
        ConnectDeviceView()
    }
}
