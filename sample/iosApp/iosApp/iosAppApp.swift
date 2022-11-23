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
    
    var body: some Scene {
        WindowGroup {
            BlocHolderView()
        }
    }
}

struct BlocHolderView: View {
    
    @State
    private var blocHolder =
        ComponentHolder {
            RootBlocImplKt.buildRootBloc(context: $0, driverFactory: DriverFactory())
        }
    
    var body: some View {
        RootView(blocHolder.component)
    }
}

struct RootView : View {
    
    @State private var path: Array<RootBlocChild> = []
    
    private let bloc: RootBloc

    @ObservedObject
    private var routerState: ObservableValue<ChildStack<AnyObject, RootBlocChild>>


    init(_ bloc: RootBloc) {
        self.bloc = bloc
        self.routerState = ObservableValue(bloc.routerState)
    }
    
    var body: some View {
        let router = routerState.value
        
        let items = router.items
            .compactMap { $0 as? ChildCreated<AnyObject, RootBlocChild> }
            .map { $0.instance }
        
        NavigationStack(path: $path) {
            let firstBloc = items[0]
            
            switch firstBloc {
            case let bottomNav as RootBlocChild.BottomNav:
                Text("Bottom nav screen")
            case let characterDetail as RootBlocChild.CharacterDetail:
                Text("Character detail \(characterDetail.bloc)")
            default: EmptyView()
            }.navigationDestination(for: RootBlocChild.self) { route in
                
            }
            
        }
    }
}
