package com.plusmobileapps.rickandmorty.episodes.list

import com.arkivanov.decompose.value.Value
import com.plusmobileapps.rickandmorty.api.episodes.Episode
import com.plusmobileapps.rickandmorty.episodes.EpisodeListItem

interface EpisodesBloc {
    val models: Value<Model>

    fun onEpisodeClicked(episode: Episode)

    fun loadMore()

    fun onSearchClicked()

    data class Model(
        val isLoading: Boolean,
        val episodes: List<EpisodeListItem>,
        val error: String? = null
    )

    sealed class Output {
        data class OpenEpisode(val episode: Episode) : Output()
        object OpenEpisodeSearch : Output()
    }
}