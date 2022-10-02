//
//  BlocHolder.swift
//  iosApp
//
//  Created by Andrew Steinmetz on 9/11/22.
//

import rickandmortysdk

class BlocHolder<T> {
    let lifecycle: LifecycleRegistry
    let bloc: T
    
    init(factory: (ComponentContext) -> T) {
        let lifecycle = LifecycleRegistryKt.LifecycleRegistry()
        let bloc = factory(DefaultComponentContext(lifecycle: lifecycle))
        self.lifecycle = lifecycle
        self.bloc = bloc
        
        lifecycle.onCreate()
    }
    
    deinit {
        lifecycle.onDestroy()
    }
}