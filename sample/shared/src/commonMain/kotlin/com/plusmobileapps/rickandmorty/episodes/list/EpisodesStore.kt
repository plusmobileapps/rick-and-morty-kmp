package com.plusmobileapps.rickandmorty.episodes.list

import com.arkivanov.mvikotlin.core.store.Store
import com.plusmobileapps.rickandmorty.episodes.EpisodeListItem
import com.plusmobileapps.rickandmorty.episodes.list.EpisodesStore.Intent
import com.plusmobileapps.rickandmorty.episodes.list.EpisodesStore.State

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