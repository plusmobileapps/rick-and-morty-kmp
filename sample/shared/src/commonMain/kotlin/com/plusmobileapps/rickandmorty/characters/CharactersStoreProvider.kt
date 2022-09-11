package com.plusmobileapps.rickandmorty.characters

import com.arkivanov.mvikotlin.core.store.Reducer
import com.arkivanov.mvikotlin.core.store.SimpleBootstrapper
import com.arkivanov.mvikotlin.core.store.Store
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.arkivanov.mvikotlin.extensions.coroutines.CoroutineExecutor
import com.plusmobileapps.rickandmorty.characters.CharactersStore.State
import com.plusmobileapps.rickandmorty.util.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

internal class CharactersStoreProvider(
    private val storeFactory: StoreFactory,
    private val dispatchers: Dispatchers,
    private val repository: CharactersRepository
) {

    private sealed class Message {
        data class CharactersUpdated(val characters: List<RickAndMortyCharacter>) : Message()
    }

    fun provide(): CharactersStore = object : CharactersStore,
        Store<Nothing, State, Nothing> by storeFactory.create(
            name = "CharactersStore",
            initialState = State(),
            bootstrapper = SimpleBootstrapper(Unit),
            executorFactory = ::ExecutorImpl,
            reducer = ReducerImpl
        ) {}

    private inner class ExecutorImpl :
        CoroutineExecutor<Nothing, Unit, State, Message, Nothing>(dispatchers.main) {

        override fun executeAction(action: Unit, getState: () -> State) {
            scope.launch {
                repository.getCharacters().collect {
                    dispatch(Message.CharactersUpdated(it))
                }
            }
        }
    }

    private object ReducerImpl : Reducer<State, Message> {
        override fun State.reduce(msg: Message): State =
            when (msg) {
                is Message.CharactersUpdated -> copy(
                    characters = msg.characters,
                    isLoading = false
                )
            }
    }
}