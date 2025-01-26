//
//  DeviceListItem.swift
//  IoT_App
//
//  Created by Kacper Karabinowski on 21/01/2025.
//

import SwiftUI

struct ListDeviceItemView: View {
    let device: Device

    var body: some View {
        HStack {
            Image(systemName: "wifi")
                .font(.largeTitle)
                .foregroundColor(.blue)
                .padding()
            Text(device.name)
                .font(.title3)
                .fontWeight(.medium)
                .foregroundColor(.white)
            Spacer()
        }
        .padding()
        .background(Color.gray.opacity(0.2))
        .cornerRadius(10)
    }
}

#Preview {
    ListDeviceItemView(device: Device(name: "Device1", type: "Device", domain: "local"))
}


