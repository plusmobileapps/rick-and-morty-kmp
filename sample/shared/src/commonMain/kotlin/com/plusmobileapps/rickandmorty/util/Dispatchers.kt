package com.plusmobileapps.rickandmorty.util

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.Default
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.Dispatchers.Unconfined
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

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