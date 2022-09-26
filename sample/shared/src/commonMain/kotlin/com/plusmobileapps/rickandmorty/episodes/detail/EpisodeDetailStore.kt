package com.plusmobileapps.rickandmorty.episodes.detail

import com.arkivanov.mvikotlin.core.store.Store
import com.plusmobileapps.rickandmorty.api.episodes.Episode
import com.plusmobileapps.rickandmorty.characters.RickAndMortyCharacter
import com.plusmobileapps.rickandmorty.episodes.detail.EpisodeDetailStore.State

internal interface EpisodeDetailStore: Store<Nothing, State, Nothing> {

    data class State(
        val isLoading: Boolean = true,
        val episode: Episode = Episode(),
        val characters: List<RickAndMortyCharacter> = emptyList(),
        val isCharacterLoading: Boolean = true,
    )

}