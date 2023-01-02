package com.plusmobileapps.rickandmorty.locations.search

import com.arkivanov.mvikotlin.core.store.Reducer
import com.arkivanov.mvikotlin.core.store.SimpleBootstrapper
import com.arkivanov.mvikotlin.core.store.Store
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.arkivanov.mvikotlin.extensions.coroutines.CoroutineExecutor
import com.plusmobileapps.paging.PagingDataSourceState
import com.plusmobileapps.rickandmorty.api.locations.Location
import com.plusmobileapps.rickandmorty.locations.search.LocationSearchStore.Intent
import com.plusmobileapps.rickandmorty.locations.search.LocationSearchStore.State
import com.plusmobileapps.rickandmorty.util.Dispatchers
import kotlinx.coroutines.launch

internal class LocationSearchStoreProvider(
    private val storeFactory: StoreFactory,
    private val dispatchers: Dispatchers,
    private val useCase: LocationSearchUseCase,
) {

    private sealed interface Message {
        data class PagingStateUpdated(val state: PagingDataSourceState<Location>) : Message
        data class LocationUpdated(val location: String) : Message
        data class DimensionUpdated(val dimension: String) : Message
        data class TypeUpdated(val type: String) : Message
        object ClearSearch : Message
    }

    fun provide(): LocationSearchStore =
        object : LocationSearchStore, Store<Intent, State, Nothing> by storeFactory.create(
            name = "LocationSearchStore",
            initialState = State(),
            bootstrapper = SimpleBootstrapper(Unit),
            executorFactory = ::ExecutorImpl,
            reducer = ReducerImpl,
        ) {}

    private inner class ExecutorImpl :
        CoroutineExecutor<Intent, Unit, State, Message, Nothing>(dispatchers.main) {
        override fun executeAction(action: Unit, getState: () -> State) {
            scope.launch {
                useCase.pageLoaderState.collect {
                    dispatch(Message.PagingStateUpdated(it))
                }
            }
        }

        override fun executeIntent(intent: Intent, getState: () -> State) {
            when (intent) {
                Intent.ClearSearch -> dispatch(Message.ClearSearch)
                Intent.ExecuteSearch -> executeSearch(getState())
                Intent.LoadMore -> loadNextPage()
                is Intent.UpdateDimension -> dispatch(Message.DimensionUpdated(intent.dimension))
                is Intent.UpdateLocation -> dispatch(Message.LocationUpdated(intent.location))
                is Intent.UpdateType -> dispatch(Message.TypeUpdated(intent.type))
            }
        }

        private fun executeSearch(state: State) {
            scope.launch {
                useCase.clearAndLoadFirstPage(
                    location = state.location,
                    type = state.type,
                    dimension = state.dimension,
                )
            }
        }

        private fun loadNextPage() {
            scope.launch { useCase.loadNextPage() }
        }
    }

    private object ReducerImpl : Reducer<State, Message> {
        override fun State.reduce(msg: Message): State = when (msg) {
            is Message.PagingStateUpdated -> copy(pageLoaderState = msg.state)
            is Message.DimensionUpdated -> copy(dimension = msg.dimension)
            is Message.LocationUpdated -> copy(location = msg.location)
            is Message.TypeUpdated -> copy(type = msg.type)
            Message.ClearSearch -> copy(
                dimension = "",
                location = "",
                type = "",
            )
        }
    }
}