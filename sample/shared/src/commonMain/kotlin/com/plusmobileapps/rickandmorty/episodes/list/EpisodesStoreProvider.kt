package com.plusmobileapps.rickandmorty.episodes.list

import com.arkivanov.mvikotlin.core.store.Reducer
import com.arkivanov.mvikotlin.core.store.SimpleBootstrapper
import com.arkivanov.mvikotlin.core.store.Store
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.arkivanov.mvikotlin.extensions.coroutines.CoroutineExecutor
import com.plusmobileapps.paging.PagingDataSourceState
import com.plusmobileapps.rickandmorty.api.episodes.Episode
import com.plusmobileapps.rickandmorty.episodes.EpisodesRepository
import com.plusmobileapps.rickandmorty.episodes.list.EpisodesStore.Intent
import com.plusmobileapps.rickandmorty.episodes.list.EpisodesStore.State
import com.plusmobileapps.rickandmorty.util.Dispatchers
import kotlinx.coroutines.launch

internal class EpisodesStoreProvider(
    private val storeFactory: StoreFactory,
    private val dispatchers: Dispatchers,
    private val repository: EpisodesRepository,
) {

    sealed interface Message {
        data class PagingStateUpdated(val state: PagingDataSourceState<Episode>) : Message
    }

    fun provide(): EpisodesStore =
        object : EpisodesStore, Store<Intent, State, Nothing> by storeFactory.create(
            name = "EpisodesStore",
            initialState = State(),
            bootstrapper = SimpleBootstrapper(Unit),
            executorFactory = ::ExecutorImpl,
            reducer = ReducerImpl,
        ) {}

    private inner class ExecutorImpl :
        CoroutineExecutor<Intent, Unit, State, Message, Nothing>(dispatchers.main) {
        override fun executeAction(action: Unit, getState: () -> State) {
            scope.launch { repository.loadFirstPage() }
            scope.launch {
                repository.pagingState.collect {
                    dispatch(Message.PagingStateUpdated(it))
                }
            }
        }

        override fun executeIntent(intent: Intent, getState: () -> State) {
            when (intent) {
                Intent.LoadMoreCharacters -> scope.launch { repository.loadNextPage() }
            }
        }
    }

    private object ReducerImpl : Reducer<State, Message> {
        override fun State.reduce(msg: Message): State =
            when (msg) {
                is Message.PagingStateUpdated -> copy(
                    firstPageIsLoading = msg.state.isFirstPageLoading,
                    nextPageIsLoading = msg.state.isNextPageLoading,
                    pageLoadedError = msg.state.pageLoaderError,
                    hasMoreToLoad = msg.state.hasMoreToLoad,
                    items = msg.state.data,
                )
            }
    }
}