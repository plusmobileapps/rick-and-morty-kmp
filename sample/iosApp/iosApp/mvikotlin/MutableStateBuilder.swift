//
//  MutableStateBuilder.swift
//  iosApp
//
//  Created by Andrew Steinmetz on 9/11/22.
//

import rickandmortysdk

func valueOf<T: AnyObject>(_ value: T) -> Value<T> {
    return MutableValueBuilderKt.MutableValue(initialValue: value) as! MutableValue<T>
}
