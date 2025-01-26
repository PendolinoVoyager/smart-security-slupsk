import SwiftUI

struct HomeView: View {
    
    @EnvironmentObject var appState: AppState
    
    init() {
        UINavigationBar.appearance().largeTitleTextAttributes = [.foregroundColor: UIColor.white]
    }
    
    var body: some View {
        NavigationStack {
            ZStack {
                LinearGradient(
                    gradient: Gradient(colors: [Color.black, Color.gray.opacity(0.8)]),
                    startPoint: .topLeading,
                    endPoint: .bottomTrailing
                )
                .ignoresSafeArea()

                Circle()
                    .fill(Color.blue.opacity(0.3))
                    .frame(width: 300, height: 300)
                    .blur(radius: 50)
                    .offset(x: -150, y: -200)

                Circle()
                    .fill(Color.purple.opacity(0.3))
                    .frame(width: 250, height: 250)
                    .blur(radius: 50)
                    .offset(x: 150, y: 200)

                VStack(spacing: 30) {
                    // Title and button section
                    VStack(spacing: 20) {
                        // First row of buttons
                        HStack(spacing: 16) {
                            NavigationLink(destination: AlertsView()) {
                                GridButtonView(title: "Alerts", color: Color.red.opacity(0.8))
                            }
                            NavigationLink(destination: MyDevicesView()) {
                                GridButtonView(title: "My Devices", color: Color.green.opacity(0.8))
                            }
                        }
                        .padding(.horizontal, 16)

                        // Second row of buttons
                        HStack(spacing: 16) {
                            NavigationLink(destination: ConnectDeviceView()) {
                                GridButtonView(title: "Connect Device", color: Color.blue.opacity(0.8))
                            }
                            NavigationLink(destination: MainControlView()) {
                                GridButtonView(title: "Main Control", color: Color.orange.opacity(0.8))
                            }
                        }
                        .padding(.horizontal, 16)
                    }

                    Spacer()

                    // Footer with optional description or other elements
                    VStack(spacing: 8) {
                        Text("Kacper Karabinowski | SSS Team")
                            .font(.footnote)
                            .foregroundColor(.white.opacity(0.8))
                        Text("Version 1.0.0")
                            .font(.footnote)
                            .foregroundColor(.white.opacity(0.6))
                    }
                    .padding(.bottom, 20)
                }
            }
            .navigationTitle("Home")
            .toolbar {
                ToolbarItem(placement: .topBarTrailing) {
                    Menu {
                        Button(action: {
                            print("Wyloguj")
                            appState.isLoggedIn = false
                        }) {
                            Label("Log out", systemImage: "rectangle.portrait.and.arrow.right")
                        }
                        
                        Button(action: {
                            // Navigate to profile logic
                            print("Profil")
                        }) {
                            Label("Profil", systemImage: "person.circle")
                        }
                    } label: {
                        Image(systemName: "line.3.horizontal")
                            .font(.title2)
                            .foregroundColor(.white)
                    }
                }
            }
        }
    }
}

struct GridButtonView: View {
    let title: String
    let color: Color

    var body: some View {
        Text(title)
            .font(.headline)
            .fontWeight(.semibold)
            .foregroundColor(.white)
            .frame(maxWidth: .infinity, maxHeight: .infinity)
            .padding()
            .background(color)
            .cornerRadius(15)
            .frame(height: 100) // Button height
            .shadow(color: .black.opacity(0.3), radius: 10, x: 0, y: 5)
    }
}

struct MyDevicesView: View {
    var body: some View {
        Text("My Devices View")
            .font(.largeTitle)
            .foregroundColor(.primary)
    }
}

struct ConfigureDeviceView: View {
    var body: some View {
        Text("Configure Device View")
            .font(.largeTitle)
            .foregroundColor(.primary)
    }
}

struct MainControlView: View {
    var body: some View {
        Text("Main Control View")
            .font(.largeTitle)
            .foregroundColor(.primary)
    }
}

#Preview {
    HomeView()
}
