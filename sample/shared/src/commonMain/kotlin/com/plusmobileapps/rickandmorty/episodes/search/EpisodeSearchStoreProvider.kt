package com.plusmobileapps.rickandmorty.episodes.search

import com.arkivanov.mvikotlin.core.store.Reducer
import com.arkivanov.mvikotlin.core.store.SimpleBootstrapper
import com.arkivanov.mvikotlin.core.store.Store
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.arkivanov.mvikotlin.extensions.coroutines.CoroutineExecutor
import com.plusmobileapps.rickandmorty.api.RickAndMortyApiClient
import com.plusmobileapps.rickandmorty.episodes.EpisodeListItem
import com.plusmobileapps.rickandmorty.episodes.search.EpisodeSearchStore.Intent
import com.plusmobileapps.rickandmorty.episodes.search.EpisodeSearchStore.State
import com.plusmobileapps.rickandmorty.util.Dispatchers
import kotlinx.coroutines.launch

internal class EpisodeSearchStoreProvider(
    private val dispatchers: Dispatchers,
    private val storeFactory: StoreFactory,
    private val api: RickAndMortyApiClient,
) {

    sealed interface Message {
        data class UpdateNameQuery(val name: String) : Message
        data class UpdateEpisodeCode(val code: String) : Message
        object LoadingSearch : Message
        object ClearSearch : Message
        data class FilterVisibilityUpdate(val visible: Boolean) : Message
        data class ResultsUpdated(val results: List<EpisodeListItem>) : Message
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
            super.executeAction(action, getState)
        }

        override fun executeIntent(intent: Intent, getState: () -> State) {
            when (intent) {
                Intent.ClearSearch -> dispatch(Message.ClearSearch)
                Intent.InitiateSearch -> initiateSearch(getState())
                Intent.ToggleFilters -> dispatch(Message.FilterVisibilityUpdate(!getState().showFilters))
                is Intent.UpdateEpisodeCode -> dispatch(Message.UpdateEpisodeCode(intent.episodeCode))
                is Intent.UpdateNameQuery -> dispatch(Message.UpdateNameQuery(intent.query))
            }
        }

        private fun initiateSearch(state: State) {
            if (state.isLoading) return
            dispatch(Message.LoadingSearch)
            scope.launch {
                try {
                    val episodes = api.getEpisodes(
                        page = 0,
                        name = state.nameQuery,
                        episodeCode = state.episodeCode,
                    ).results.map { EpisodeListItem.EpisodeItem(it) }

                    dispatch(Message.ResultsUpdated(episodes))
                } catch (e: Exception) {
                    // TODO
                }
            }
        }
    }

    private object ReducerImpl : Reducer<State, Message> {
        override fun State.reduce(msg: Message): State = when (msg) {
            Message.ClearSearch -> State()
            is Message.FilterVisibilityUpdate -> copy(showFilters = msg.visible)
            Message.LoadingSearch -> copy(isLoading = true)
            is Message.ResultsUpdated -> copy(isLoading = false, results = msg.results)
            is Message.UpdateEpisodeCode -> copy(episodeCode = msg.code)
            is Message.UpdateNameQuery -> copy(nameQuery = msg.name)
        }
    }

}