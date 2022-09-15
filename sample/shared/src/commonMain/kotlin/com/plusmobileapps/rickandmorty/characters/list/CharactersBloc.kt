package com.plusmobileapps.rickandmorty.characters.list

import com.arkivanov.decompose.value.Value
import com.plusmobileapps.rickandmorty.characters.CharactersListItem
import com.plusmobileapps.rickandmorty.characters.RickAndMortyCharacter

interface CharactersBloc {

    val models: Value<Model>

    fun onCharacterClicked(character: RickAndMortyCharacter)

    fun onSearchClicked()

    fun loadMoreCharacters()

    data class Model(
        val listItems: List<CharactersListItem> = emptyList(),
        val error: String? = null,
        val isLoading: Boolean = false
    )

    sealed class Output {
        data class OpenCharacter(val character: RickAndMortyCharacter) : Output()
        object OpenCharacterSearch : Output()
    }

}