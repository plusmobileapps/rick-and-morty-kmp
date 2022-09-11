package com.plusmobileapps.rickandmorty.bottomnav

import com.arkivanov.mvikotlin.core.store.Reducer
import com.arkivanov.mvikotlin.core.store.Store
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.arkivanov.mvikotlin.extensions.coroutines.CoroutineExecutor
import com.plusmobileapps.rickandmorty.bottomnav.BottomNavBloc.NavItem
import com.plusmobileapps.rickandmorty.bottomnav.BottomNavigationStore.Intent
import com.plusmobileapps.rickandmorty.bottomnav.BottomNavigationStore.State
import com.plusmobileapps.rickandmorty.util.Dispatchers

internal class BottomNavStoreProvider(private val storeFactory: StoreFactory, private val dispatchers: Dispatchers) {
    private sealed class Result {
        data class NavListUpdated(val navItems: List<NavItem>) : Result()
    }

    fun create(): BottomNavigationStore =
        object : BottomNavigationStore, Store<Intent, State, Nothing> by storeFactory.create(
            name = "NavigationComponentStore",
            initialState = State(),
            executorFactory = ::ExecutorImpl,
            reducer = ReducerImpl
        ) {}

    private object ReducerImpl : Reducer<State, Result> {

        override fun State.reduce(msg: Result): State =
            when (msg) {
                is Result.NavListUpdated -> copy(navItems = msg.navItems)
            }
    }

    private inner class ExecutorImpl :
        CoroutineExecutor<Intent, Nothing, State, Result, Nothing>(dispatchers.main) {

        override fun executeIntent(intent: Intent, getState: () -> State) =
            when (intent) {
                is Intent.SelectNavItem -> selectNavItem(intent.type, getState())
            }

        private fun selectNavItem(type: NavItem.Type, state: State) {
            val newList = state.navItems.map { it.copy(selected = type == it.type) }
            dispatch(Result.NavListUpdated(newList))
        }
    }
}