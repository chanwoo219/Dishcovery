//
//  New_DishcoveryApp.swift
//  New_Dishcovery
//
//  Created by 박진희 on 2025/11/27.
//

import SwiftUI

@available(iOS 16.0, *)
@main
struct New_DishcoveryApp: App {
    @StateObject var appState = AppState()
    var body: some Scene {
            WindowGroup {
                ContentView()
                    .environmentObject(appState)
        }
    }
}
