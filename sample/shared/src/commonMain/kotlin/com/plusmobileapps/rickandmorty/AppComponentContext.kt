package com.plusmobileapps.rickandmorty

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.router.stack.ChildStack
import com.arkivanov.decompose.router.stack.StackNavigationSource
import com.arkivanov.decompose.router.stack.childStack
import com.arkivanov.decompose.value.Value
import com.arkivanov.essenty.lifecycle.Lifecycle
import com.arkivanov.essenty.parcelable.Parcelable
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.arkivanov.mvikotlin.main.store.DefaultStoreFactory
import com.plusmobileapps.rickandmorty.util.Dispatchers
import com.plusmobileapps.rickandmorty.util.DispatchersImpl
import kotlin.reflect.KClass

interface AppComponentContext : ComponentContext {
    val dispatchers: Dispatchers
    val storeFactory: StoreFactory
}

class DefaultAppComponentContext(
    componentContext: ComponentContext,
    override val dispatchers: Dispatchers,
    override val storeFactory: StoreFactory,
) : AppComponentContext, ComponentContext by componentContext

fun <C : Parcelable, T : Any> AppComponentContext.appChildStack(
    source: StackNavigationSource<C>,
    initialStack: () -> List<C>,
    configurationClass: KClass<out C>,
    key: String = "DefaultStack",
    handleBackButton: Boolean = false,
    childFactory: (configuration: C, AppComponentContext) -> T
): Value<ChildStack<C, T>> =
    childStack(
        source = source,
        initialStack = initialStack,
        configurationClass = configurationClass,
        key = key,
        handleBackButton = handleBackButton
    ) { configuration, componentContext ->
        childFactory(
            configuration,
            DefaultAppComponentContext(
                componentContext = componentContext,
                dispatchers = dispatchers,
                storeFactory = storeFactory
            )
        )
    }

inline fun <reified C : Parcelable, T : Any> AppComponentContext.appChildStack(
    source: StackNavigationSource<C>,
    noinline initialStack: () -> List<C>,
    key: String = "DefaultStack",
    handleBackButton: Boolean = false,
    noinline childFactory: (configuration: C, AppComponentContext) -> T
): Value<ChildStack<C, T>> =
    appChildStack(
        source = source,
        initialStack = initialStack,
        configurationClass = C::class,
        key = key,
        handleBackButton = handleBackButton,
        childFactory = childFactory,
    )