package com.plusmobileapps.rickandmorty.locations.list

import com.arkivanov.mvikotlin.core.store.Reducer
import com.arkivanov.mvikotlin.core.store.SimpleBootstrapper
import com.arkivanov.mvikotlin.core.store.Store
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.arkivanov.mvikotlin.extensions.coroutines.CoroutineExecutor
import com.plusmobileapps.rickandmorty.locations.LocationListItem
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
        data class LocationsUpdated(val items: List<LocationListItem>) : Message
        data class LoadingNextPage(val hasMore: Boolean) : Message
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
            scope.launch {
                repository.locations.collect {
                    dispatch(
                        Message.LocationsUpdated(it.map { location ->
                            LocationListItem.LocationItem(location)
                        })
                    )
                }
            }
        }

        override fun executeIntent(intent: Intent, getState: () -> State) {
            when (intent) {
                Intent.LoadMoreCharacters -> {
                    dispatch(Message.LoadingNextPage(true))
                    scope.launch { repository.loadMore() }
                }
            }
        }
    }

    private object ReducerImpl : Reducer<State, Message> {
        override fun State.reduce(msg: Message): State = when (msg) {
            is Message.LoadingNextPage -> copy(
                locations = locations + LocationListItem.NextPageLoading,
                isLoading = true
            )
            is Message.LocationsUpdated -> copy(locations = msg.items, isLoading = false)
        }
    }
}