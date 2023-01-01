package com.plusmobileapps.rickandmorty.locations.list

import com.arkivanov.mvikotlin.core.store.Reducer
import com.arkivanov.mvikotlin.core.store.SimpleBootstrapper
import com.arkivanov.mvikotlin.core.store.Store
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.arkivanov.mvikotlin.extensions.coroutines.CoroutineExecutor
import com.plusmobileapps.paging.PagingDataSourceState
import com.plusmobileapps.rickandmorty.api.locations.Location
import com.plusmobileapps.rickandmorty.locations.LocationRepository
import com.plusmobileapps.rickandmorty.locations.list.LocationStore.Intent
import com.plusmobileapps.rickandmorty.locations.list.LocationStore.State
import com.plusmobileapps.rickandmorty.util.Dispatchers
import kotlinx.coroutines.launch

internal class LocationStoreProvider(
    private val dispatchers: Dispatchers,
    private val storeFactory: StoreFactory,
    private val repository: LocationRepository
) {

    private sealed interface Message {
        data class PageLoaderStateUpdated(
            val state: PagingDataSourceState<Location>,
        ) : Message
    }

    fun provide(): LocationStore =
        object : LocationStore, Store<Intent, State, Nothing> by storeFactory.create(
            name = "LocationStore",
            initialState = State(),
            bootstrapper = SimpleBootstrapper(Unit),
            executorFactory = ::ExecutorImpl,
            reducer = ReducerImpl
        ) {}

    private inner class ExecutorImpl :
        CoroutineExecutor<Intent, Unit, State, Message, Nothing>(dispatchers.main) {
        override fun executeAction(action: Unit, getState: () -> State) {
            scope.launch { repository.loadFirstPage() }
            scope.launch {
                repository.pageLoaderState.collect {
                    dispatch(Message.PageLoaderStateUpdated(it))
                }
            }
        }

        override fun executeIntent(intent: Intent, getState: () -> State) {
            when (intent) {
                Intent.LoadMoreLocations -> {
                    scope.launch { repository.loadNextPage() }
                }
            }
        }
    }

    private object ReducerImpl : Reducer<State, Message> {
        override fun State.reduce(msg: Message): State = when (msg) {
            is Message.PageLoaderStateUpdated -> copy(
                items = msg.state.data,
                firstPageIsLoading = msg.state.isFirstPageLoading,
                nextPageIsLoading = msg.state.isNextPageLoading,
                pageLoadedError = msg.state.pageLoaderError,
                hasMoreToLoad = msg.state.hasMoreToLoad,
            )
        }
    }
}