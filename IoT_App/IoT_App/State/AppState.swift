import Foundation

final class AppState: ObservableObject {
    @Published var isLoggedIn: Bool = false
    @Published var username: String? = nil 
    
    init() {
        if let token = KeychainHelper.shared.read(key: "authToken") {
            isLoggedIn = true
            print("Token znaleziony: \(token)")
        } else {
            isLoggedIn = false
        }
        
        if let savedUsername = UserDefaults.standard.string(forKey: "username") {
            username = savedUsername
        }
    }
}
