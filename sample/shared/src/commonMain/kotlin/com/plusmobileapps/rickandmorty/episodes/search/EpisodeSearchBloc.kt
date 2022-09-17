package com.plusmobileapps.rickandmorty.episodes.search

import com.arkivanov.decompose.value.Value
import com.plusmobileapps.rickandmorty.episodes.EpisodeListItem
import com.plusmobileapps.rickandmorty.util.BackClickBloc

interface EpisodeSearchBloc : BackClickBloc {

    val models: Value<Model>

    fun onSearchClicked()

    fun onClearNameQueryClicked()

    fun onNameQueryChanged(name: String)

    fun onEpisodeCodeChanged(code: String)

    fun onClearEpisodeCodeClicked()

    fun onFiltersToggleClicked()

    data class Model(
        val isLoading: Boolean,
        val nameQuery: String,
        val results: List<EpisodeListItem>,
        val episodeCode: String,
        val error: String?,
        val showFilters: Boolean,
    )

    sealed class Output {
        object GoBack : Output()
    }
}