package com.plusmobileapps.rickandmorty.util

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers.Default
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.Dispatchers.Unconfined

interface Dispatchers {
    val main: CoroutineDispatcher
    val unconfined: CoroutineDispatcher
    val default: CoroutineDispatcher
}

internal object DispatchersImpl : Dispatchers {
    override val main: CoroutineDispatcher = Main
    override val unconfined: CoroutineDispatcher = Unconfined
    override val default: CoroutineDispatcher = Default
}