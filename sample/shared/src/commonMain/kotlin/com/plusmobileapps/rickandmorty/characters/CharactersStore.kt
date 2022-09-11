package com.plusmobileapps.rickandmorty.characters

import com.arkivanov.mvikotlin.core.store.Store
import com.plusmobileapps.rickandmorty.characters.CharactersStore.State

internal interface CharactersStore : Store<Nothing, State, Nothing> {

    data class State(
        val characters: List<RickAndMortyCharacter> = emptyList(),
        val error: String? = null,
        val isLoading: Boolean = false
    )
}