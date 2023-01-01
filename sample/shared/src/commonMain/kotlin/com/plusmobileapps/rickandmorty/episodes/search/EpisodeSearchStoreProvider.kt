package com.plusmobileapps.rickandmorty.episodes.search

import com.arkivanov.mvikotlin.core.store.Reducer
import com.arkivanov.mvikotlin.core.store.SimpleBootstrapper
import com.arkivanov.mvikotlin.core.store.Store
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.arkivanov.mvikotlin.extensions.coroutines.CoroutineExecutor
import com.plusmobileapps.paging.PagingDataSourceState
import com.plusmobileapps.rickandmorty.api.episodes.Episode
import com.plusmobileapps.rickandmorty.episodes.search.EpisodeSearchStore.Intent
import com.plusmobileapps.rickandmorty.episodes.search.EpisodeSearchStore.State
import com.plusmobileapps.rickandmorty.util.Dispatchers
import kotlinx.coroutines.launch

internal class EpisodeSearchStoreProvider(
    private val dispatchers: Dispatchers,
    private val storeFactory: StoreFactory,
    private val useCase: EpisodeSearchUseCase,
) {

    sealed interface Message {
        data class UpdateNameQuery(val name: String) : Message
        data class UpdateEpisodeCode(val code: String) : Message
        object ClearSearch : Message
        data class FilterVisibilityUpdate(val visible: Boolean) : Message
        data class PagingStateUpdated(val state: PagingDataSourceState<Episode>) : Message
    }

    fun provide(): EpisodeSearchStore =
        object : EpisodeSearchStore, Store<Intent, State, Nothing> by storeFactory.create(
            name = "EpisodeSearchStore",
            initialState = State(),
            bootstrapper = SimpleBootstrapper(Unit),
            executorFactory = ::ExecutorImpl,
            reducer = ReducerImpl
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
                Intent.InitiateSearch -> initiateSearch(getState())
                Intent.ToggleFilters -> dispatch(Message.FilterVisibilityUpdate(!getState().showFilters))
                is Intent.UpdateEpisodeCode -> dispatch(Message.UpdateEpisodeCode(intent.episodeCode))
                is Intent.UpdateNameQuery -> dispatch(Message.UpdateNameQuery(intent.query))
                Intent.LoadMoreResults -> scope.launch { useCase.loadNextPage() }
            }
        }

        private fun initiateSearch(state: State) {
            scope.launch {
                useCase.loadFirstPage(
                    name = state.nameQuery,
                    episodeCode = state.episodeCode.takeIf { it.isNotBlank() }
                )
            }
        }
    }

    private object ReducerImpl : Reducer<State, Message> {
        override fun State.reduce(msg: Message): State = when (msg) {
            Message.ClearSearch -> State()
            is Message.FilterVisibilityUpdate -> copy(showFilters = msg.visible)
            is Message.UpdateEpisodeCode -> copy(episodeCode = msg.code)
            is Message.UpdateNameQuery -> copy(nameQuery = msg.name)
            is Message.PagingStateUpdated -> copy(pageLoaderState = msg.state)
        }
    }

}