package com.plusmobileapps.rickandmorty.episodes

import com.arkivanov.mvikotlin.core.store.Store
import com.plusmobileapps.rickandmorty.episodes.EpisodesStore.Intent
import com.plusmobileapps.rickandmorty.episodes.EpisodesStore.State

internal interface EpisodesStore : Store<Intent, State, Nothing> {

    data class State(
        val isLoading: Boolean = true,
        val episodes: List<EpisodeListItem> = emptyList(),
        val error: String? = null
    )

    sealed interface Intent {
        object LoadMoreCharacters : Intent
    }
}