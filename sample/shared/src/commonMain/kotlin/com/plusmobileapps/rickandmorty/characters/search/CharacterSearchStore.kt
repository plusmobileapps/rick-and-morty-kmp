package com.plusmobileapps.rickandmorty.characters.search

import com.arkivanov.mvikotlin.core.store.Store
import com.plusmobileapps.paging.PagingDataSource
import com.plusmobileapps.rickandmorty.api.characters.CharacterGender
import com.plusmobileapps.rickandmorty.api.characters.CharacterStatus
import com.plusmobileapps.rickandmorty.characters.RickAndMortyCharacter
import com.plusmobileapps.rickandmorty.characters.search.CharacterSearchStore.Intent
import com.plusmobileapps.rickandmorty.characters.search.CharacterSearchStore.State

internal interface CharacterSearchStore : Store<Intent, State, Nothing> {

    data class State(
        val pageLoaderState: PagingDataSource.State<RickAndMortyCharacter> = PagingDataSource.State(),
        val query: String = "",
        val status: CharacterStatus? = null,
        val species: String = "",
        val gender: CharacterGender? = null,
        val error: String? = null,
        val showFilters: Boolean = false,
        val isConnectedToNetwork: Boolean = false,
    )

    sealed class Intent {
        object InitiateSearch : Intent()
        object LoadNextPage : Intent()
        object ClearSearch : Intent()
        data class UpdateQuery(val query: String) : Intent()
        data class UpdateStatus(val status: CharacterStatus?) : Intent()
        data class UpdateSpecies(val species: String) : Intent()
        data class UpdateGender(val gender: CharacterGender?) : Intent()
        object ToggleFilters : Intent()
    }
}