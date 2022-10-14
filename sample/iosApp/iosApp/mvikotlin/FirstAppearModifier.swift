//
//  FirstAppearModifier.swift
//  iosApp
//
//  Created by Andrew Steinmetz on 10/10/22.
//

import SwiftUI

extension View {
    func onFirstAppear(perform action: @escaping () -> Void) -> some View {
        modifier(FirstAppearModifier(action: action))
    }
}

private struct FirstAppearModifier: ViewModifier {
    let action: () -> Void

    @State
    private var appeared = false

    func body(content: Content) -> some View {
        content.onAppear {
            if !appeared {
                appeared = true
                action()
            }
        }
    }
}
