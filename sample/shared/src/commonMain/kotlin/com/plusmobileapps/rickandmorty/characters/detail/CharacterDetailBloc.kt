package com.plusmobileapps.rickandmorty.characters.detail

import com.arkivanov.decompose.value.Value
import com.plusmobileapps.rickandmorty.characters.RickAndMortyCharacter
import com.plusmobileapps.rickandmorty.util.BackClickBloc

interface CharacterDetailBloc : BackClickBloc {

    val models: Value<Model>

    data class Model(
        val character: RickAndMortyCharacter,
        val isLoading: Boolean,
        val error: String?,
    )

    sealed interface Output {
        object Done : Output
    }
}