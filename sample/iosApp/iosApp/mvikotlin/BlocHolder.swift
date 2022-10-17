//
//  BlocHolder.swift
//  iosApp
//
//  Created by Andrew Steinmetz on 9/11/22.
//

import rickandmortysdk

class BlocHolder<T> : ObservableObject {
    let lifecycle: LifecycleRegistry
    let bloc: T
    
    init(factory: (Lifecycle) -> T) {
        let lifecycle = LifecycleRegistryKt.LifecycleRegistry()
        let bloc = factory(lifecycle)
        self.lifecycle = lifecycle
        self.bloc = bloc
        
        lifecycle.onCreate()
    }
    
    deinit {
        lifecycle.onDestroy()
    }
}