//
//  BottomNavView.swift
//  iosApp
//
//  Created by Andrew Steinmetz on 9/11/22.
//

import SwiftUI
import rickandmortysdk

struct BottomNavigationView: View {
    
    private let bloc: BottomNavBloc
    
    @ObservedObject
    private var routerStates: ObservableValue<ChildStack<AnyObject, BottomNavBlocChild>>
    
    @ObservedObject
    private var models: ObservableValue<BottomNavBlocModel>
    
    init(_ bloc: BottomNavBloc) {
        self.bloc = bloc
        self.routerStates = ObservableValue(bloc.routerState)
        self.models = ObservableValue(bloc.models)
    }
    
    var body: some View {
        let model = models.value
        let child = self.routerStates.value.active.instance

        return NavigationView {
            ZStack {
                VStack {
                    switch child {
                    case let characters as BottomNavBlocChild.Characters:
                        CharactersView(characters.bloc)
                    default:
                        EmptyView()
                        
                    }
                }
                VStack {
                    Spacer()
                    BottomNavComponentView(navItems: model.navItems, onItemClicked: { item in bloc.onNavItemClicked(item: item)})
                }
            }
        }.navigationViewStyle(.stack)
    }
    
}
