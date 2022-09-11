//
//  RootView.swift
//  iosApp
//
//  Created by Andrew Steinmetz on 9/11/22.
//


import SwiftUI
import rickandmortysdk

struct RootView: View {
    private let component: RootBloc
    private let routerState: Value<ChildStack<AnyObject, RootBlocChild>>

    init(_ bloc: RootBloc) {
        self.component = bloc
        self.routerState = bloc.routerState
    }

    var body: some View {

        let child = self.routerState.value.active.instance
        
        switch child {
        case let bottomNav as RootBlocChild.BottomNav:
            BottomNavigationView(bottomNav.bloc)
        default: EmptyView()
        }

    }
}
