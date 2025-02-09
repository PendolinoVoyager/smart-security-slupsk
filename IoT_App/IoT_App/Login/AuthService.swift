import Foundation

final class AuthService {
    static let shared = AuthService()
    
    private init() {}
    
    func login(email: String, password: String, appState: AppState, completion: @escaping (Bool, String) -> Void) {
        guard let url = URL(string: "http://192.168.0.4:8080/api/v1/auth/login") else {
            completion(false, "Invalid request URL.")
            return
        }
        
        var request = URLRequest(url: url)
        request.httpMethod = "POST"
        request.setValue("application/json", forHTTPHeaderField: "Content-Type")
        
        let body: [String: Any] = [
            "email": email,
            "password": password
        ]
        
        do {
            request.httpBody = try JSONSerialization.data(withJSONObject: body, options: [])
        } catch {
            completion(false, "Invalid request body.")
            return
        }
        
        let task = URLSession.shared.dataTask(with: request) { data, response, error in
            if let error = error {
                DispatchQueue.main.async {
                    completion(false, "Network error: \(error.localizedDescription)")
                }
                return
            }
            
            guard let data = data else {
                DispatchQueue.main.async {
                    completion(false, "Invalid response.")
                }
                return
            }
            
            do {
                if let json = try JSONSerialization.jsonObject(with: data, options: []) as? [String: Any],
                   let token = json["token"] as? String,
                   let username = json["email"] as? String {
                    
                    KeychainHelper.shared.save(key: "authToken", value: token)
                    
                    UserDefaults.standard.set(username, forKey: "username")
                    
                    DispatchQueue.main.async {
                        appState.isLoggedIn = true
                        appState.username = username
                        completion(true, "Success!")
                    }
                } else {
                    DispatchQueue.main.async {
                        completion(false, "Invalid response.")
                    }
                }
            } catch {
                DispatchQueue.main.async {
                    completion(false, "Invalid data.")
                }
            }
        }
        
        task.resume()
    }
    
    func logout(appState: AppState) {
           KeychainHelper.shared.delete(key: "authToken")
           
           UserDefaults.standard.removeObject(forKey: "username")
           
           DispatchQueue.main.async {
               appState.isLoggedIn = false
               appState.username = nil
           }
       }
}
