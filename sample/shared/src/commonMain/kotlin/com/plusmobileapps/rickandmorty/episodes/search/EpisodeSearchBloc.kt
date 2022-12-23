package com.plusmobileapps.rickandmorty.episodes.search

import com.arkivanov.decompose.value.Value
import com.plusmobileapps.paging.PagingDataSourceState
import com.plusmobileapps.rickandmorty.api.episodes.Episode
import com.plusmobileapps.rickandmorty.episodes.EpisodeListItem
import com.plusmobileapps.rickandmorty.util.BackClickBloc

interface EpisodeSearchBloc : BackClickBloc {

    val models: Value<Model>

    fun onSearchClicked()

    fun loadMoreResults()

    fun onFirstPageTryAgainClicked()

    fun onNextPageTryAgainClicked()

    fun onClearNameQueryClicked()

    fun onNameQueryChanged(name: String)

    fun onEpisodeCodeChanged(code: String)

    fun onClearEpisodeCodeClicked()

    fun onFiltersToggleClicked()

    fun onEpisodeClicked(episode: Episode)

    data class Model(
        val pageLoaderState: PagingDataSourceState<Episode>,
        val nameQuery: String,
        val episodeCode: String,
        val error: String?,
        val showFilters: Boolean,
    )

    sealed class Output {
        object GoBack : Output()
        data class OpenEpisode(val id: Int) : Output()
    }
}