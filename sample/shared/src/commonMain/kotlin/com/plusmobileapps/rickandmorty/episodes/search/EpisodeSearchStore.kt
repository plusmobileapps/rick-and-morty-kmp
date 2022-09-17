package com.plusmobileapps.rickandmorty.episodes.search

import com.arkivanov.mvikotlin.core.store.Store
import com.plusmobileapps.rickandmorty.episodes.EpisodeListItem
import com.plusmobileapps.rickandmorty.episodes.search.EpisodeSearchStore.Intent
import com.plusmobileapps.rickandmorty.episodes.search.EpisodeSearchStore.State

internal interface EpisodeSearchStore : Store<Intent, State, Nothing> {

    data class State(
        val isLoading: Boolean = false,
        val nameQuery: String = "",
        val results: List<EpisodeListItem> = emptyList(),
        val episodeCode: String = "",
        val error: String? = null,
        val showFilters: Boolean = false,
    )

    sealed interface Intent {
        object InitiateSearch : Intent
        object ClearSearch : Intent
        data class UpdateNameQuery(val query: String) : Intent
        data class UpdateEpisodeCode(val episodeCode: String) : Intent
        object ToggleFilters : Intent
    }
}