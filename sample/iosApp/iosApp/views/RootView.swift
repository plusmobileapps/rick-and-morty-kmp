//
//  RootView.swift
//  iosApp
//
//  Created by Andrew Steinmetz on 9/11/22.
//


import SwiftUI
import rickandmortysdk

struct RootView: View {
    private let rootBloc: RootBloc
    private let children: ChildAnimationHelper

    init(_ rootBloc: RootBloc, _ lifecycle: LifecycleRegistry) {
        self.rootBloc = rootBloc
        self.children = ChildAnimationHelper(routerState: rootBloc.routerState, lifecycle: lifecycle)
    }
    
    var body: some View {
        RouterView(children) { child, isHidden in
            if child is RootBlocChild.BottomNav {
                BottomNavigationView((child as! RootBlocChild.BottomNav).bloc)
            } else if child is RootBlocChild.CharacterDetail {
                CharacterDetailView((child as! RootBlocChild.CharacterDetail).bloc)
            }
        }

    }
}
