//
//  PasswordSecureFieltView.swift
//  IoT_App
//
//  Created by Kacper Karabinowski on 20/01/2025.
//

import SwiftUI

struct PasswordSecureFieltView: View {
    
    @Binding var password: String
    
    var body: some View {
        SecureField(
            "Password",
            text: $password,
            prompt: Text(
                "Password"
            ).foregroundStyle(.white.opacity(0.5))
        )
        .foregroundStyle(Color.white)
            .padding()
            .background(Color.white.opacity(0.2))
            .cornerRadius(8)
            .textInputAutocapitalization(.never)
    }
}

#Preview {
    ZStack {
        Color(
            red: 0.1,
            green: 0.1,
            blue: 0.1
        )
        .ignoresSafeArea()
        PasswordSecureFieltView(password: .constant("Password.123"))
    }
}
