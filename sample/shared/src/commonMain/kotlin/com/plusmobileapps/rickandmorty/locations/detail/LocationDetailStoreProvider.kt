package com.plusmobileapps.rickandmorty.locations.detail

import com.arkivanov.mvikotlin.core.store.Reducer
import com.arkivanov.mvikotlin.core.store.SimpleBootstrapper
import com.arkivanov.mvikotlin.core.store.Store
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.arkivanov.mvikotlin.extensions.coroutines.CoroutineExecutor
import com.plusmobileapps.rickandmorty.api.locations.Location
import com.plusmobileapps.rickandmorty.characters.CharactersRepository
import com.plusmobileapps.rickandmorty.characters.RickAndMortyCharacter
import com.plusmobileapps.rickandmorty.locations.LocationRepository
import com.plusmobileapps.rickandmorty.locations.detail.LocationDetailStore.Intent
import com.plusmobileapps.rickandmorty.locations.detail.LocationDetailStore.State
import com.plusmobileapps.rickandmorty.util.Dispatchers
import kotlinx.coroutines.launch

internal class LocationDetailStoreProvider(
    private val locationId: Int,
    private val storeFactory: StoreFactory,
    private val dispatchers: Dispatchers,
    private val charactersRepository: CharactersRepository,
    private val locationsRepository: LocationRepository,
) {

    sealed interface Message {
        data class LocationUpdated(val location: Location) : Message
        data class CharactersUpdated(val characters: List<RickAndMortyCharacter>) : Message
    }

    fun provide(): LocationDetailStore =
        object : LocationDetailStore, Store<Intent, State, Nothing> by storeFactory.create(
            name = "LocationDetailStore-$locationId",
            initialState = State(),
            bootstrapper = SimpleBootstrapper(Unit),
            executorFactory = ::ExecutorImpl,
            reducer = ReducerImpl
        ){}

    private inner class ExecutorImpl :
        CoroutineExecutor<Intent, Unit, State, Message, Nothing>(dispatchers.main) {
        override fun executeAction(action: Unit, getState: () -> State) {
            scope.launch {
                val location = locationsRepository.getLocation(locationId) ?: return@launch
                dispatch(Message.LocationUpdated(location))
                val residents = location.residents
                if (residents.isEmpty()) {
                    dispatch(Message.CharactersUpdated(emptyList()))
                    return@launch
                }

                val characters = charactersRepository.getCharacters(residents.mapNotNull {
                    it.removePrefix("https://rickandmortyapi.com/api/character/").toIntOrNull()
                })
                dispatch(Message.CharactersUpdated(characters))
            }
        }

        override fun executeIntent(intent: Intent, getState: () -> State) {
            super.executeIntent(intent, getState)
        }
    }

    private object ReducerImpl : Reducer<State, Message> {
        override fun State.reduce(msg: Message): State = when (msg) {
            is Message.CharactersUpdated -> copy(
                isCharactersLoading = false,
                characters = msg.characters
            )
            is Message.LocationUpdated -> copy(
                isLoading = false,
                location = msg.location
            )
        }
    }

}