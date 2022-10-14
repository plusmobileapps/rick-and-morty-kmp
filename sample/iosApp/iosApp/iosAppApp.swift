//
//  iosAppApp.swift
//  iosApp
//
//  Created by Andrew Steinmetz on 9/11/22.
//

import SwiftUI
import rickandmortysdk

@main
struct iosAppApp: App {
    
    @StateObject
    private var holder = Holder()
    
    var body: some Scene {
        WindowGroup {
            ContentView(di: holder.di)
        }
    }
}

private class Holder : ObservableObject {
    let di: ServiceLocator
    
    init() {
        di = ServiceLocatorKt.buildServiceLocator()
    }
}
