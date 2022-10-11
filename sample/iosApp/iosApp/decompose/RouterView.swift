//
//  RouterView.swift
//  iosApp
//
//  Created by Andrew Steinmetz on 10/1/22.
//

import SwiftUI
import rickandmortysdk

struct RouterView<Content : View> : View {
    @ObservedObject
    private var state: ObservableValue<NSArray>
    private let render: (RootBlocChild, _ isHidden: Bool) -> Content

    init(_ routerState: Value<NSArray>, @ViewBuilder render: @escaping (RootBlocChild, _ isHidden: Bool) -> Content) {
        self.state = ObservableValue(routerState)
        self.render = render
    }
    
    var body: some View {
        let routerState = self.state.value
        
        let children =
            routerState
                .compactMap { $0 as? AnimatedChildBloc }
                .map { $0 }
        
        return ZStack {
            
            let active = children.last
        
            
                ForEach(children.indices, id: \.hashValue) { i in
                    let item = children[i]
                    
                    
                                    
                    if item.type == AnimatedChildBloc.Type_.firstBackStack {
                        self.render(item.child, false)
                            .onAppear {
                                
                            }.onChange(of: children, perform: { value in
                                <#code#>
                            })
                                .transition(AnyTransition.asymmetric(insertion: .slideToLeftTransition, removal: AnyTransition.identity))
                    }
                    
                    if item.type == AnimatedChildBloc.Type_.active {
                        if item.entrance == AnimatedDirection.left {
                            self.render(item.child, true)
                                .transition(AnyTransition.asymmetric(insertion: .move(edge: .leading), removal: AnyTransition.identity))
                        } else if item.entrance == AnimatedDirection.right{
                            self.render(item.child, true)
                                .transition(AnyTransition.asymmetric(insertion: .move(edge: .trailing), removal: AnyTransition.identity))
                        } else {
                            self.render(item.child, true)
                        }
                    }
                    
                    if item.type == AnimatedChildBloc.Type_.popped {
                        self.render(item.child, false)
                            .transition(AnyTransition.asymmetric(insertion: .slideToRightTransition, removal: AnyTransition.identity))
                    }
                
                }
            }
    }
}


extension AnyTransition {
    static var slideToLeftTransition: AnyTransition {
        .modifier(active: MySlideViewModifier(x: 0), identity: MySlideViewModifier(x: -(UIScreen.main.bounds.width)))
    }
    
    static var slideToRightTransition: AnyTransition {
        .modifier(active: MySlideViewModifier(x: 0), identity: MySlideViewModifier(x: UIScreen.main.bounds.width))
    }
}

struct MySlideViewModifier : ViewModifier {
    let x: CGFloat
    
    func body(content: Content) -> some View {
        content.offset(x: x, y: 0)
    }
}


class Router: ObservableObject {
    
    let router: ChildAnimationHelper

    @Published
    var stack: [RootBlocChild] = []
    
    init(_ router: ChildAnimationHelper) {
        self.router = router
        router.subscribe(observer_: observeRouter(state:))
    }

    private func observeRouter(state: [AnimatedChildBloc]) {
        stack = state.map { child in child.child }
    }

    deinit {
        self.router.unsubscribe(observer: observeRouter(state:))
    }
}
