package com.plusmobileapps.rickandmorty.episodes.list

import com.arkivanov.decompose.value.Value
import com.plusmobileapps.paging.PageLoaderException
import com.plusmobileapps.rickandmorty.api.episodes.Episode

interface EpisodesBloc {
    val models: Value<Model>

    fun onEpisodeClicked(episode: Episode)

    fun loadMore()

    fun onSearchClicked()

    data class Model(
        val episodes: List<Episode> = emptyList(),
        val firstPageIsLoading: Boolean = false,
        val nextPageIsLoading: Boolean = false,
        val pageLoadedError: PageLoaderException? = null,
        val hasMoreToLoad: Boolean = true,
    )

    sealed class Output {
        data class OpenEpisode(val episode: Episode) : Output()
        object OpenEpisodeSearch : Output()
    }
}