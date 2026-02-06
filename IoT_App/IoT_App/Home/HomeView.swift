import SwiftUI

struct HomeView: View {

    @EnvironmentObject var appState: AppState

    var body: some View {
        NavigationStack {
            ZStack {
                PremiumBackground()

                VStack(spacing: 18) {
                    header

                    connectCard

                    Spacer()

                    footer
                }
                .padding(.horizontal, 20)
                .padding(.top, 18)
                .padding(.bottom, 20)
            }
            .navigationTitle("Home")
            .navigationBarTitleDisplayMode(.large)
            .toolbarColorScheme(.dark, for: .navigationBar)
            .toolbarBackground(.hidden, for: .navigationBar)
            .toolbar {
                ToolbarItem(placement: .topBarTrailing) {
                    Menu {
                        Button {
                            AuthService.shared.logout(appState: appState)
                        } label: {
                            Label("Log out", systemImage: "rectangle.portrait.and.arrow.right")
                        }

                        Button {
                            print("Profil")
                        } label: {
                            Label("Profile", systemImage: "person.circle")
                        }
                    } label: {
                        Image(systemName: "line.3.horizontal")
                            .font(.system(size: 18, weight: .semibold))
                            .foregroundStyle(.white.opacity(0.95))
                            .padding(10)
                            .background(.ultraThinMaterial, in: Circle())
                            .overlay(
                                Circle().strokeBorder(.white.opacity(0.12), lineWidth: 1)
                            )
                    }
                }
            }
        }
    }
}

// MARK: - Sections
private extension HomeView {

    var header: some View {
        VStack(alignment: .leading, spacing: 10) {
            Text("Device Hub")
                .font(.system(.title, design: .rounded).weight(.bold))
                .foregroundStyle(.white)

            Text("Connect and manage your IoT devices securely.")
                .font(.system(.callout, design: .rounded))
                .foregroundStyle(.white.opacity(0.75))
        }
        .frame(maxWidth: .infinity, alignment: .leading)
        .padding(.top, 6)
    }

    var connectCard: some View {
        NavigationLink {
            ConnectDeviceView()
        } label: {
            HStack(spacing: 14) {
                ZStack {
                    Circle()
                        .fill(.white.opacity(0.10))
                        .frame(width: 52, height: 52)
                        .overlay(Circle().strokeBorder(.white.opacity(0.12), lineWidth: 1))

                    Image(systemName: "antenna.radiowaves.left.and.right")
                        .font(.system(size: 20, weight: .semibold))
                        .foregroundStyle(.white.opacity(0.95))
                }

                VStack(alignment: .leading, spacing: 4) {
                    Text("Connect Device")
                        .font(.system(.headline, design: .rounded).weight(.semibold))
                        .foregroundStyle(.white)

                    Text("Pair a new device via Bluetooth / Wi-Fi")
                        .font(.system(.subheadline, design: .rounded))
                        .foregroundStyle(.white.opacity(0.75))
                        .lineLimit(1)
                }

                Spacer()

                Image(systemName: "chevron.right")
                    .font(.system(size: 14, weight: .semibold))
                    .foregroundStyle(.white.opacity(0.7))
            }
            .padding(16)
            .frame(maxWidth: .infinity)
            .background {
                RoundedRectangle(cornerRadius: 18, style: .continuous)
                    .fill(.ultraThinMaterial)
                    .overlay {
                        RoundedRectangle(cornerRadius: 18, style: .continuous)
                            .strokeBorder(.white.opacity(0.12), lineWidth: 1)
                    }
            }
            .shadow(color: .black.opacity(0.28), radius: 22, x: 0, y: 12)
        }
        .buttonStyle(.plain)
        .accessibilityLabel("Connect Device")
        .accessibilityHint("Opens device pairing flow")
    }

    var footer: some View {
        VStack(spacing: 6) {
            Text("Kacper Karabinowski | SSS Team")
                .font(.footnote)
                .foregroundStyle(.white.opacity(0.78))

            Text("Version 1.0.0")
                .font(.footnote)
                .foregroundStyle(.white.opacity(0.55))
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

#Preview {
    HomeView()
        .environmentObject(AppState())
}
