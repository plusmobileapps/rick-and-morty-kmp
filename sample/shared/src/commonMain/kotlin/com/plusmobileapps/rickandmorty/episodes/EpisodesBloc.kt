package com.plusmobileapps.rickandmorty.episodes

import com.arkivanov.decompose.value.Value
import com.plusmobileapps.rickandmorty.api.episodes.Episode

interface EpisodesBloc {
    val models: Value<Model>

    fun onEpisodeClicked(episode: Episode)

    fun loadMore()

    data class Model(
        val isLoading: Boolean,
        val episodes: List<EpisodeListItem>,
        val error: String? = null
    )

    sealed class Output {
        data class OpenEpisode(val episode: Episode) : Output()
    }
}