package com.plusmobileapps.rickandmorty.characters.list

import com.arkivanov.decompose.value.Value
import com.plusmobileapps.paging.PageLoaderException
import com.plusmobileapps.rickandmorty.characters.RickAndMortyCharacter

interface CharactersBloc {

    val models: Value<Model>

    fun onCharacterClicked(character: RickAndMortyCharacter)

    fun onSearchClicked()

    fun loadMoreCharacters()

    data class Model(
        val characters: List<RickAndMortyCharacter> = emptyList(),
        val firstPageIsLoading: Boolean = false,
        val nextPageIsLoading: Boolean = false,
        val pageLoadedError: PageLoaderException? = null,
        val hasMoreToLoad: Boolean = true,
    )

    sealed class Output {
        data class OpenCharacter(val character: RickAndMortyCharacter) : Output()
        object OpenCharacterSearch : Output()
    }

}