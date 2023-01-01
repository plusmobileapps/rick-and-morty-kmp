package com.plusmobileapps.rickandmorty.episodes.search

import com.arkivanov.mvikotlin.core.store.Store
import com.plusmobileapps.paging.PagingDataSourceState
import com.plusmobileapps.rickandmorty.api.episodes.Episode
import com.plusmobileapps.rickandmorty.episodes.search.EpisodeSearchStore.Intent
import com.plusmobileapps.rickandmorty.episodes.search.EpisodeSearchStore.State

internal interface EpisodeSearchStore : Store<Intent, State, Nothing> {

    data class State(
        val pageLoaderState: PagingDataSourceState<Episode> = PagingDataSourceState(),
        val nameQuery: String = "",
        val episodeCode: String = "",
        val error: String? = null,
        val showFilters: Boolean = false,
    )

    sealed interface Intent {
        object InitiateSearch : Intent
        object LoadMoreResults : Intent
        object ClearSearch : Intent
        data class UpdateNameQuery(val query: String) : Intent
        data class UpdateEpisodeCode(val episodeCode: String) : Intent
        object ToggleFilters : Intent
    }
}