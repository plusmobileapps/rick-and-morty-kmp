package com.plusmobileapps.rickandmorty.characters.list

import com.arkivanov.mvikotlin.core.store.Store
import com.plusmobileapps.paging.PageLoaderException
import com.plusmobileapps.rickandmorty.characters.RickAndMortyCharacter
import com.plusmobileapps.rickandmorty.characters.list.CharactersStore.Intent
import com.plusmobileapps.rickandmorty.characters.list.CharactersStore.State

internal interface CharactersStore : Store<Intent, State, Nothing> {

    data class State(
        val items: List<RickAndMortyCharacter> = emptyList(),
        val firstPageIsLoading: Boolean = false,
        val nextPageIsLoading: Boolean = false,
        val pageLoadedError: PageLoaderException? = null,
        val hasMoreToLoad: Boolean = true,
    )

    sealed class Intent {
        object LoadMoreCharacters : Intent()
    }
}