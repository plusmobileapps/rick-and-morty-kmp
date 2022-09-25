package com.plusmobileapps.rickandmorty.characters.detail

import com.arkivanov.mvikotlin.core.store.Reducer
import com.arkivanov.mvikotlin.core.store.SimpleBootstrapper
import com.arkivanov.mvikotlin.core.store.Store
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.arkivanov.mvikotlin.extensions.coroutines.CoroutineExecutor
import com.plusmobileapps.rickandmorty.characters.CharactersRepository
import com.plusmobileapps.rickandmorty.characters.RickAndMortyCharacter
import com.plusmobileapps.rickandmorty.characters.detail.CharacterDetailStore.State
import com.plusmobileapps.rickandmorty.util.Dispatchers
import kotlinx.coroutines.launch

internal class CharacterDetailStoreProvider(
    private val id: Int,
    private val repository: CharactersRepository,
    private val dispatchers: Dispatchers,
    private val storeFactory: StoreFactory,
) {

    private sealed interface Message {
        data class CharacterUpdated(val character: RickAndMortyCharacter) : Message
        object Loading : Message
        data class Error(val error: String) : Message
    }

    fun provide(): CharacterDetailStore =
        object : CharacterDetailStore, Store<Nothing, State, Nothing> by storeFactory.create(
            name = "CharacterDetailStore",
            initialState = State(),
            bootstrapper = SimpleBootstrapper(Unit),
            executorFactory = ::ExecutorImpl,
            reducer = ReducerImpl
        ){}

    private inner class ExecutorImpl :
        CoroutineExecutor<Nothing, Unit, State, Message, Nothing>(dispatchers.main) {
        override fun executeAction(action: Unit, getState: () -> State) {
            scope.launch {
                val character = repository.getCharacter(id)
                dispatch(Message.CharacterUpdated(character))
            }
        }
    }

    private object ReducerImpl : Reducer<State, Message> {
        override fun State.reduce(msg: Message): State = when (msg) {
            is Message.CharacterUpdated -> State(isLoading = false, character = msg.character)
            is Message.Error -> State(isLoading = false, error = msg.error)
            Message.Loading -> State(isLoading = true)
        }
    }
}