package com.plusmobileapps.rickandmorty.characters.search

import com.arkivanov.decompose.value.Value
import com.plusmobileapps.rickandmorty.api.characters.CharacterGender
import com.plusmobileapps.rickandmorty.api.characters.CharacterStatus
import com.plusmobileapps.rickandmorty.characters.CharactersListItem

interface CharacterSearchBloc {

    val models: Value<Model>

    fun onSearchClicked()

    fun onQueryChanged(query: String)

    fun onClearQueryClicked()

    fun onStatusChanged(status: CharacterStatus)

    fun onClearStatusClicked()

    fun onSpeciesChanged(species: String)

    fun onClearSpeciesClicked()

    fun onGenderChanged(gender: CharacterGender)

    fun onClearGenderClicked()

    data class Model(
        val isLoading: Boolean,
        val query: String,
        val results: List<CharactersListItem>,
        val status: CharacterStatus?,
        val species: String,
        val gender: CharacterGender?,
        val error: String?,
    )

    sealed class Output {
        object GoBack : Output()
    }
}