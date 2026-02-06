import Foundation

final class LoginViewModel: ObservableObject {
    @Published var email: String = ""
    @Published var password: String = ""
    @Published var errorMessage: String?
    @Published var isLoading: Bool = false

    // Prosta flaga do UI (np. disable przycisku)
    var isValid: Bool {
        !email.isEmpty && email.contains("@") && !password.isEmpty
    }

    func login(appState: AppState, completion: @escaping (Bool, String) -> Void) {
        // blokada przed spam klikaniem
        guard !isLoading else { return }

        DispatchQueue.main.async {
            self.errorMessage = nil
            self.isLoading = true
        }

        // --- Twoja walidacja (bez zmian logicznych) ---
        let email = self.email
        let password = self.password

        guard !email.isEmpty else {
            finish(success: false, message: "Email cannot be empty.", completion: completion)
            return
        }

        guard email.contains("@") else {
            finish(success: false, message: "Please enter a valid email address.", completion: completion)
            return
        }

        guard !password.isEmpty else {
            finish(success: false, message: "Password cannot be empty.", completion: completion)
            return
        }
        // --------------------------------------------

        AuthService.shared.login(email: email, password: password, appState: appState) { [weak self] success, message in
            guard let self else { return }

            if success {
                self.finish(success: true, message: message, completion: completion)
            } else {
                DispatchQueue.main.async {
                    self.errorMessage = message
                }
                self.finish(success: false, message: message, completion: completion)
            }
        }
    }

    private func finish(success: Bool, message: String, completion: @escaping (Bool, String) -> Void) {
        DispatchQueue.main.async {
            self.isLoading = false
            if !success {
                // ustawiamy errorMessage tylko jeśli jeszcze nie został ustawiony wyżej
                self.errorMessage = self.errorMessage ?? message
            }
            completion(success, message)
        }
    }
}
