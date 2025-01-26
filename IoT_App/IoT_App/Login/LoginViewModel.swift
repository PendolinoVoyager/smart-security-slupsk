import Foundation

final class LoginViewModel: ObservableObject {
    @Published var email: String = ""
    @Published var password: String = ""
    @Published var errorMessage: String?

    func login(appState: AppState, completion: @escaping (Bool, String) -> Void) {
        errorMessage = nil

        guard !email.isEmpty else {
            errorMessage = "Email cannot be empty."
            completion(false, "Email cannot be empty.")
            return
        }

        guard email.contains("@") else {
            errorMessage = "Please enter a valid email address."
            completion(false, "Invalid email address.")
            return
        }

        guard !password.isEmpty else {
            errorMessage = "Password cannot be empty."
            completion(false, "Password cannot be empty.")
            return
        }

        
        AuthService.shared.login(email: email, password: password, appState: appState) { success, message in
            if success {
                completion(true, message)
            } else {
                self.errorMessage = message
                completion(false, message)
            }
        }
    }
}
