//
//  ButtonView.swift
//  IoT_App
//
//  Created by Kacper Karabinowski on 20/01/2025.
//

import SwiftUI

struct ButtonView: View {
    
    var buttonText: String
    
    var body: some View {
        Text(buttonText)
            .font(.headline)
            .foregroundColor(.white)
            .padding()
            .frame(maxWidth: .infinity)
            .background(
                LinearGradient(
                    colors: [Color.blue, Color.purple],
                    startPoint: .leading,
                    endPoint: .trailing
                )
            )
            .cornerRadius(10)
            .shadow(color: .black.opacity(0.3), radius: 5, x: 0, y: 5)
    }
}

#Preview {
    ButtonView(buttonText: "Login Now!")
}
