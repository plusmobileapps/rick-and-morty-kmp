package com.plusmobileapps.rickandmorty.characters.list

import com.arkivanov.mvikotlin.core.store.Reducer
import com.arkivanov.mvikotlin.core.store.SimpleBootstrapper
import com.arkivanov.mvikotlin.core.store.Store
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.arkivanov.mvikotlin.extensions.coroutines.CoroutineExecutor
import com.plusmobileapps.paging.PagingDataSourceState
import com.plusmobileapps.rickandmorty.characters.CharactersRepository
import com.plusmobileapps.rickandmorty.characters.RickAndMortyCharacter
import com.plusmobileapps.rickandmorty.characters.list.CharactersStore.Intent
import com.plusmobileapps.rickandmorty.characters.list.CharactersStore.State
import com.plusmobileapps.rickandmorty.util.Dispatchers
import kotlinx.coroutines.launch

internal class CharactersStoreProvider(
    private val storeFactory: StoreFactory,
    private val dispatchers: Dispatchers,
    private val repository: CharactersRepository
) {

    private sealed class Message {
        data class PageLoaderStateUpdated(val state: PagingDataSourceState<RickAndMortyCharacter>) :
            Message()
    }

    fun provide(): CharactersStore = object : CharactersStore,
        Store<Intent, State, Nothing> by storeFactory.create(
            name = "CharactersStore",
            initialState = State(),
            bootstrapper = SimpleBootstrapper(Unit),
            executorFactory = ::ExecutorImpl,
            reducer = ReducerImpl
        ) {}

    private inner class ExecutorImpl :
        CoroutineExecutor<Intent, Unit, State, Message, Nothing>(dispatchers.main) {

        override fun executeIntent(intent: Intent, getState: () -> State) {
            when (intent) {
                Intent.LoadMoreCharacters -> loadMoreCharacters()
            }
        }

        override fun executeAction(action: Unit, getState: () -> State) {
            scope.launch { repository.loadFirstPage() }
            observeCharacters()
        }

        private fun observeCharacters() {
            scope.launch {
                repository.pageLoaderState.collect { state ->
                    dispatch(Message.PageLoaderStateUpdated(state))
                }
            }
        }

        private fun loadMoreCharacters() {
            scope.launch {
                repository.loadNextPage()
            }
        }
    }

    private object ReducerImpl : Reducer<State, Message> {
        override fun State.reduce(msg: Message): State =
            when (msg) {
                is Message.PageLoaderStateUpdated -> copy(
                    firstPageIsLoading = msg.state.isFirstPageLoading,
                    nextPageIsLoading = msg.state.isNextPageLoading,
                    pageLoadedError = msg.state.pageLoaderError,
                    hasMoreToLoad = msg.state.hasMoreToLoad,
                    items = msg.state.data,
                )
            }
    }
}