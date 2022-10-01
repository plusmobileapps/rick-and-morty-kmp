package com.plusmobileapps.rickandmorty.locations.detail

import com.arkivanov.decompose.value.Value
import com.plusmobileapps.rickandmorty.api.locations.Location
import com.plusmobileapps.rickandmorty.characters.RickAndMortyCharacter
import com.plusmobileapps.rickandmorty.util.BackClickBloc

interface LocationDetailBloc : BackClickBloc {

    val models: Value<Model>

    fun onCharacterClicked(character: RickAndMortyCharacter)

    data class Model(
        val isLoadingLocation: Boolean,
        val location: Location,
        val isLoadingCharacters: Boolean,
        val characters: List<RickAndMortyCharacter>,
    )

    sealed interface Output {
        object Done : Output
        data class OpenCharacter(val id: Int) : Output
    }
}