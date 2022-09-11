//
//  ObservableValue.swift
//  iosApp
//
//  Created by Andrew Steinmetz on 9/11/22.
//
import rickandmortysdk

public class ObservableValue<T : AnyObject> : ObservableObject {
    private let observableValue: Value<T>

    @Published
    var value: T
    
    private var observer: ((T) -> Void)?
    
    init(_ value: Value<T>) {
        self.observableValue = value
        self.value = observableValue.value
        self.observer = { [weak self] value in self?.value = value }

        observableValue.subscribe(observer_: observer!)
    }
    
    deinit {
        self.observableValue.unsubscribe(observer: self.observer!)
    }
}
