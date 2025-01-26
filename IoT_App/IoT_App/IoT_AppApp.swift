//
//  IoT_AppApp.swift
//  IoT_App
//
//  Created by Kacper Karabinowski on 20/01/2025.
//

import SwiftUI

@main
struct IoT_AppApp: App {
    
    @StateObject private var appState = AppState()
    
    var body: some Scene {
        WindowGroup {
            ContentView()
                .environmentObject(appState)
        }
    }
}
