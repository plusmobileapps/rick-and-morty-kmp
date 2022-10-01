package com.plusmobileapps.rickandmorty.locations.detail

import com.arkivanov.mvikotlin.core.store.Store
import com.plusmobileapps.rickandmorty.api.locations.Location
import com.plusmobileapps.rickandmorty.characters.RickAndMortyCharacter
import com.plusmobileapps.rickandmorty.locations.detail.LocationDetailStore.Intent
import com.plusmobileapps.rickandmorty.locations.detail.LocationDetailStore.State

internal interface LocationDetailStore : Store<Intent, State, Nothing> {

    data class State(
        val isLoading: Boolean = true,
        val location: Location = Location(),
        val residentsCount: Int = 0,
        val isCharactersLoading: Boolean = true,
        val characters: List<RickAndMortyCharacter> = emptyList(),
    )

    sealed interface Intent {

    }
}