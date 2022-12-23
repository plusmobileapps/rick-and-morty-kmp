package com.plusmobileapps.rickandmorty.episodes.list

import com.arkivanov.mvikotlin.core.store.Store
import com.plusmobileapps.paging.PageLoaderException
import com.plusmobileapps.rickandmorty.api.episodes.Episode
import com.plusmobileapps.rickandmorty.episodes.list.EpisodesStore.Intent
import com.plusmobileapps.rickandmorty.episodes.list.EpisodesStore.State

internal interface EpisodesStore : Store<Intent, State, Nothing> {

    data class State(
        val items: List<Episode> = emptyList(),
        val firstPageIsLoading: Boolean = false,
        val nextPageIsLoading: Boolean = false,
        val pageLoadedError: PageLoaderException? = null,
        val hasMoreToLoad: Boolean = true,
    )

    sealed interface Intent {
        object LoadMoreCharacters : Intent
    }
}