package com.plusmobileapps.rickandmorty.episodes

import com.plusmobileapps.rickandmorty.api.episodes.Episode

sealed class EpisodeListItem {
    data class EpisodeItem(val value: Episode) : EpisodeListItem()
    object NextPageLoading : EpisodeListItem()
}