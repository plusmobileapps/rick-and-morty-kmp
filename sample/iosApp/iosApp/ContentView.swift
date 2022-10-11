//
//  ContentView.swift
//  iosApp
//
//  Created by Andrew Steinmetz on 9/11/22.
//

import SwiftUI
import rickandmortysdk

struct ContentView: View {
    @State
    private var blocHolder = BlocHolder {
        RootBlocImplKt.buildRootBloc(context: $0, driverFactory: DriverFactory())
    }
    
    var body: some View {
        RootView(blocHolder.bloc, blocHolder.lifecycle)
    }
}

struct ContentView_Previews: PreviewProvider {
    static var previews: some View {
        ContentView()
    }
}
