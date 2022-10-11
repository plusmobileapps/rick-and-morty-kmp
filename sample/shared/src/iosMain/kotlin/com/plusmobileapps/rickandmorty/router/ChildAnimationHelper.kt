package com.plusmobileapps.rickandmorty.router

import com.arkivanov.decompose.Child
import com.arkivanov.decompose.router.stack.ChildStack
import com.arkivanov.decompose.value.Value
import com.arkivanov.essenty.lifecycle.Lifecycle
import com.arkivanov.essenty.lifecycle.subscribe
import com.arkivanov.mvikotlin.rx.Disposable
import com.plusmobileapps.rickandmorty.root.RootBloc
import com.plusmobileapps.rickandmorty.router.AnimatedChildBloc.Type.*
import com.plusmobileapps.rickandmorty.router.AnimatedDirection.*
import io.github.aakira.napier.Napier
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.debounce
import kotlin.time.Duration.Companion.milliseconds

enum class AnimatedDirection {
    LEFT, RIGHT, NONE
}

data class AnimatedChildBloc(
    val child: RootBloc.Child,
    val entrance: AnimatedDirection,
    val type: Type
) {
    enum class Type {
        BACK_STACK, // something that doesn't need to be rendered at all
        FIRST_BACK_STACK, // used for rendering it off screen
        ACTIVE, // animated on screen, could be from right or left
        POPPED // removed from backstack and animated to the right
    }
}

class ChildAnimationHelper(
    private val routerState: Value<ChildStack<*, RootBloc.Child>>,
    lifecycle: Lifecycle,
) : Value<List<AnimatedChildBloc>>() {
    private var lastActiveChild: Child.Created<*, RootBloc.Child>? = null
    private var lastBackStack: List<Child<Any, RootBloc.Child>> = listOf()
    private val _value = MutableStateFlow(emptyList<AnimatedChildBloc>())
    override val value: List<AnimatedChildBloc>
        get() = _value.value
    private val routerObserver: (ChildStack<*, RootBloc.Child>) -> Unit = { state ->
        when (lastActiveChild) {
            null -> {
                //initializing
                lastActiveChild = state.active
                _value.value = getAnimatedBackstackChildren(state, BACK_STACK) +
                        AnimatedChildBloc(
                            child = state.active.instance,
                            entrance = NONE,
                            type = ACTIVE
                        )

            }
            else -> {
                val backStack = getAnimatedBackstackChildren(state)
                val isPushed = state.backStack.size > lastBackStack.size
                val newList = buildList<AnimatedChildBloc> {
                    addAll(backStack)
                    add(
                        AnimatedChildBloc(
                            child = state.active.instance,
                            entrance = if (isPushed) RIGHT else LEFT,
                            type = ACTIVE,
                        )
                    )
                    if (!isPushed) {
                        add(
                            AnimatedChildBloc(
                                child = lastActiveChild!!.instance,
                                entrance = LEFT,
                                type = POPPED
                            )
                        )
                    }
                }
                _value.value = newList
            }
        }

        lastActiveChild = state.active
        lastBackStack = state.backStack
    }

    private fun getAnimatedBackstackChildren(
        state: ChildStack<*, RootBloc.Child>,
        overideType: AnimatedChildBloc.Type? = null
    ): List<AnimatedChildBloc> =
        state.backStack.filterIsInstance<Child.Created<*, RootBloc.Child>>()
            .mapIndexed { index: Int, child: Child<Any, RootBloc.Child> ->
                child as Child.Created<*, RootBloc.Child>
                AnimatedChildBloc(
                    child = child.instance,
                    entrance = LEFT,
                    type = when {
                        overideType != null -> overideType
                        index == state.backStack.lastIndex -> FIRST_BACK_STACK
                        else -> BACK_STACK
                    }
                )
            }


    private var disposables = emptyMap<(List<AnimatedChildBloc>) -> Unit, Disposable>()

    init {
        lifecycle.subscribe(
            onCreate = {
                routerState.subscribe(routerObserver)
            },
            onDestroy = {
                routerState.unsubscribe(routerObserver)
            }
        )
    }

    override fun subscribe(observer: (List<AnimatedChildBloc>) -> Unit) {
        val scope = CoroutineScope(Dispatchers.Default)
        scope.launch {
            _value
                .debounce(250.milliseconds)
                .collect {
                Napier.d { it.toString() }
                withContext(Dispatchers.Main) { observer(it) }
            }
        }
        val disposable: Disposable = object : Disposable {
            override val isDisposed: Boolean get() = !scope.isActive
            override fun dispose() {
                scope.cancel()
            }
        }
        this.disposables += observer to disposable
    }

    override fun unsubscribe(observer: (List<AnimatedChildBloc>) -> Unit) {
        val disposable = disposables[observer] ?: return
        this.disposables -= observer
        disposable.dispose()
    }
}