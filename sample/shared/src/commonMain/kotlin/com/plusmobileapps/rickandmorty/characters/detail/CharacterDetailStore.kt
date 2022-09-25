package com.plusmobileapps.rickandmorty.characters.detail

import com.arkivanov.mvikotlin.core.store.Store
import com.plusmobileapps.rickandmorty.characters.RickAndMortyCharacter
import com.plusmobileapps.rickandmorty.characters.detail.CharacterDetailStore.State

internal interface CharacterDetailStore : Store<Nothing, State, Nothing> {

    data class State(
        val isLoading: Boolean = true,
        val character: RickAndMortyCharacter = RickAndMortyCharacter(),
        val error: String? = null
    )
}