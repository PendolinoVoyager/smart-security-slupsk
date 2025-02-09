import SwiftUI

struct ConnectDeviceView: View {
    @StateObject private var viewModel = ConnectDeviceViewModel()
    
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
                
                VStack(spacing: 20) {
                    if viewModel.devices.isEmpty {
                        Text("Looking for devices...")
                            .font(.headline)
                            .foregroundColor(.gray)
                    } else {
                        ForEach(viewModel.devices) { device in
                            NavigationLink(destination: DeviceDetailsView(device: device)) {
                                ListDeviceItemView(device: device)
                            }
                            .buttonStyle(PlainButtonStyle())
                        }
                    }
                }
                .padding()
                .onAppear {
                    viewModel.startBrowsing()
                }
                .onDisappear {
                    viewModel.stopBrowsing()
                }
            }
            .navigationTitle("Connect Device")
        }
    }
}

#Preview {
    ConnectDeviceView()
}
