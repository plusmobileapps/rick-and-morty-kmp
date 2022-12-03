package com.plusmobileapps.rickandmorty.characters.search

import com.arkivanov.mvikotlin.core.store.Reducer
import com.arkivanov.mvikotlin.core.store.SimpleBootstrapper
import com.arkivanov.mvikotlin.core.store.Store
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.arkivanov.mvikotlin.extensions.coroutines.CoroutineExecutor
import com.plusmobileapps.konnectivity.Konnectivity
import com.plusmobileapps.rickandmorty.api.RickAndMortyApiClient
import com.plusmobileapps.paging.PagingDataSource
import com.plusmobileapps.rickandmorty.api.characters.CharacterGender
import com.plusmobileapps.rickandmorty.api.characters.CharacterStatus
import com.plusmobileapps.rickandmorty.characters.RickAndMortyCharacter
import com.plusmobileapps.rickandmorty.characters.search.CharacterSearchStore.Intent
import com.plusmobileapps.rickandmorty.characters.search.CharacterSearchStore.State
import com.plusmobileapps.rickandmorty.util.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

internal class CharacterSearchStoreProvider(
    private val storeFactory: StoreFactory,
    private val dispatchers: Dispatchers,
    private val useCase: CharacterSearchUseCase,
    private val konnectivity: Konnectivity,
) {
    sealed class Message {
        data class NetworkConnectionUpdated(val isConnected: Boolean) : Message()
        data class Error(val error: String) : Message()
        data class PageLoaderDataUpdated(val data: PagingDataSource.State<RickAndMortyCharacter>) : Message()
        data class UpdateQuery(val query: String) : Message()
        data class StatusUpdated(val status: CharacterStatus?) : Message()
        data class SpeciesUpdated(val species: String) : Message()
        data class GenderUpdated(val gender: CharacterGender?) : Message()
        data class FilterVisibilityUpdated(val show: Boolean) : Message()
        object ClearSearch : Message()
    }

    fun provide(): CharacterSearchStore =
        object : CharacterSearchStore, Store<Intent, State, Nothing> by storeFactory.create(
            name = "Store",
            initialState = State(isConnectedToNetwork = konnectivity.isConnected),
            bootstrapper = SimpleBootstrapper(Unit),
            executorFactory = ::Executor,
            reducer = ReducerImpl
        ) {}

    private inner class Executor :
        CoroutineExecutor<Intent, Unit, State, Message, Nothing>(dispatchers.main) {

        override fun executeAction(action: Unit, getState: () -> State) {
            scope.launch {
                useCase.pageLoaderState.collect {
                    dispatch(Message.PageLoaderDataUpdated(it))
                }
            }
            scope.launch {
                konnectivity.isConnectedState.collect {
                    dispatch(Message.NetworkConnectionUpdated(it))
                }
            }
        }

        override fun executeIntent(intent: Intent, getState: () -> State) {
            when (intent) {
                Intent.InitiateSearch -> search(getState())
                is Intent.UpdateGender -> dispatch(Message.GenderUpdated(intent.gender))
                is Intent.UpdateQuery -> dispatch(Message.UpdateQuery(intent.query))
                is Intent.UpdateSpecies -> dispatch(Message.SpeciesUpdated(intent.species))
                is Intent.UpdateStatus -> dispatch(Message.StatusUpdated(intent.status))
                is Intent.ToggleFilters -> dispatch(Message.FilterVisibilityUpdated(!getState().showFilters))
                Intent.ClearSearch -> dispatch(Message.ClearSearch)
                Intent.LoadNextPage -> useCase.loadNextPage()
            }
        }

        private fun search(state: State) {
            if (!state.isConnectedToNetwork) {
                dispatch(Message.Error("Not connected to internet."))
                return
            }
            useCase.loadFirstPage(
                query = state.query,
                status = state.status,
                species = state.species,
                gender = state.gender,
            )
        }
    }

    private object ReducerImpl : Reducer<State, Message> {
        override fun State.reduce(msg: Message): State = when (msg) {
            is Message.GenderUpdated -> copy(gender = msg.gender)
            is Message.SpeciesUpdated -> copy(species = msg.species)
            is Message.StatusUpdated -> copy(status = msg.status)
            is Message.UpdateQuery -> copy(query = msg.query)
            is Message.FilterVisibilityUpdated -> copy(showFilters = msg.show)
            Message.ClearSearch -> State(isConnectedToNetwork = isConnectedToNetwork)
            is Message.NetworkConnectionUpdated -> copy(isConnectedToNetwork = msg.isConnected)
            is Message.Error -> copy(error = msg.error)
            is Message.PageLoaderDataUpdated -> copy(pageLoaderState = msg.data, error = null)
        }
    }
}