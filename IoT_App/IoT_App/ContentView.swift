//
//  ContentView.swift
//  IoT_App
//
//  Created by Kacper Karabinowski on 20/01/2025.
//

import SwiftUI

struct ContentView: View {
    
    @EnvironmentObject var appState: AppState
    
    var body: some View {
            if appState.isLoggedIn {
                HomeView()
                    .environmentObject(appState)
            } else {
                LoginView()
            }
    }
}

#Preview {
    ContentView()
        .environmentObject(AppState())
}
