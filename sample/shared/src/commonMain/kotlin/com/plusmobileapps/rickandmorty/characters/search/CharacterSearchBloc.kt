package com.plusmobileapps.rickandmorty.characters.search

import com.arkivanov.decompose.value.Value
import com.plusmobileapps.paging.PagingDataSource
import com.plusmobileapps.rickandmorty.api.characters.CharacterGender
import com.plusmobileapps.rickandmorty.api.characters.CharacterStatus
import com.plusmobileapps.rickandmorty.characters.RickAndMortyCharacter
import com.plusmobileapps.rickandmorty.util.BackClickBloc

interface CharacterSearchBloc : BackClickBloc {

    val models: Value<Model>

    fun onSearchClicked()

    fun onLoadNextPage()

    fun onFirstPageTryAgainClicked()

    fun onNextPageTryAgainClicked()

    fun onCharacterClicked(character: RickAndMortyCharacter)

    fun onClearSearch()

    fun onQueryChanged(query: String)

    fun onClearQueryClicked()

    fun onStatusChanged(status: CharacterStatus)

    fun onClearStatusClicked()

    fun onSpeciesChanged(species: String)

    fun onClearSpeciesClicked()

    fun onGenderChanged(gender: CharacterGender)

    fun onClearGenderClicked()

    fun onFiltersClicked()

    data class Model(
        val pageLoaderState: PagingDataSource.State<RickAndMortyCharacter>,
        val query: String,
        val status: CharacterStatus?,
        val species: String,
        val gender: CharacterGender?,
        val error: String?,
        val showFilters: Boolean,
    )

    sealed class Output {
        object GoBack : Output()
        data class OpenCharacter(val id: Int) : Output()
    }
}