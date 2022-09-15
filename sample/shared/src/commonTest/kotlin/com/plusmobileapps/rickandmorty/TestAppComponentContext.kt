@file:OptIn(ExperimentalCoroutinesApi::class)

package com.plusmobileapps.rickandmorty

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.DefaultComponentContext
import com.arkivanov.essenty.lifecycle.Lifecycle
import com.arkivanov.essenty.lifecycle.LifecycleRegistry
import com.arkivanov.essenty.lifecycle.resume
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.arkivanov.mvikotlin.main.store.DefaultStoreFactory
import com.plusmobileapps.rickandmorty.util.Dispatchers
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*

class TestAppComponentContext(
    scheduler: TestCoroutineScheduler,
    lifecycle: Lifecycle = LifecycleRegistry().also { it.resume() },
    componentContext: ComponentContext = DefaultComponentContext(lifecycle),
): AppComponentContext, ComponentContext by componentContext {
    val testDispatcher = StandardTestDispatcher(scheduler)
    val mainDispatcher = UnconfinedTestDispatcher(scheduler)
    override val dispatchers: Dispatchers = object : Dispatchers {
        override val main: CoroutineDispatcher = mainDispatcher
        override val unconfined: CoroutineDispatcher = testDispatcher
        override val default: CoroutineDispatcher = testDispatcher
    }
    override val storeFactory: StoreFactory = DefaultStoreFactory()
}

fun runBlocTest(testBody: suspend TestScope.(AppComponentContext) -> Unit) = runTest {
    val appComponentContext = TestAppComponentContext(testScheduler)
    try {
        kotlinx.coroutines.Dispatchers.setMain(appComponentContext.mainDispatcher)
        testBody(appComponentContext)
    } finally {
        kotlinx.coroutines.Dispatchers.resetMain()
    }
}
