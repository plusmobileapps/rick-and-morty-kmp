//
//  LazyView.swift
//  iosApp
//
//  Created by Andrew Steinmetz on 10/10/22.
//

import SwiftUI

struct LazyView<Content: View>: View {
    var build: () -> Content

    var body: Content {
        build()
    }
}
