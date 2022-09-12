@file:OptIn(ExperimentalCoroutinesApi::class)

package com.plusmobileapps.rickandmorty.root

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.DefaultComponentContext
import com.arkivanov.essenty.lifecycle.Lifecycle
import com.arkivanov.essenty.lifecycle.LifecycleRegistry
import com.arkivanov.essenty.lifecycle.resume
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.arkivanov.mvikotlin.main.store.DefaultStoreFactory
import com.plusmobileapps.rickandmorty.AppComponentContext
import com.plusmobileapps.rickandmorty.DefaultAppComponentContext
import com.plusmobileapps.rickandmorty.util.Dispatchers
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestDispatcher
import kotlin.test.Test

class TestAppComponentContext(
    lifecycle: Lifecycle = LifecycleRegistry().also { it.onResume() },
    componentContext: ComponentContext = DefaultComponentContext(lifecycle),
): AppComponentContext, ComponentContext by componentContext {
    val testDispatcher = StandardTestDispatcher()
    override val dispatchers: Dispatchers = object : Dispatchers {
        override val main: CoroutineDispatcher = testDispatcher
        override val unconfined: CoroutineDispatcher = testDispatcher
        override val default: CoroutineDispatcher = testDispatcher
    }
    override val storeFactory: StoreFactory = DefaultStoreFactory()
}

class RootBlocTest {


    @Test
    fun rootInitialState() {
        val testComponentContext = TestAppComponentContext()
        val bloc = RootBlocImpl(
            componentContext = testComponentContext,
            bottomNav = { _, _ -> TODO() },
            characterSearch = { _, _ -> TODO() }
        )
    }
}