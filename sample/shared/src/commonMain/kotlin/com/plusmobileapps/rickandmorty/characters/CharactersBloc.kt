package com.plusmobileapps.rickandmorty.characters

import com.arkivanov.decompose.value.Value

interface CharactersBloc {

    val models: Value<Model>

    fun onCharacterClicked(character: RickAndMortyCharacter)

    data class Model(
        val characters: List<RickAndMortyCharacter> = emptyList(),
        val error: String? = null,
        val isLoading: Boolean = false
    )

    sealed class Output {
        data class OpenCharacter(val character: RickAndMortyCharacter) : Output()
    }

}