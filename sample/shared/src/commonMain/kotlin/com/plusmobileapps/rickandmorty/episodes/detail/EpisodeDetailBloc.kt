package com.plusmobileapps.rickandmorty.episodes.detail

import com.arkivanov.decompose.value.Value
import com.plusmobileapps.rickandmorty.api.episodes.Episode
import com.plusmobileapps.rickandmorty.characters.RickAndMortyCharacter
import com.plusmobileapps.rickandmorty.util.BackClickBloc

interface EpisodeDetailBloc : BackClickBloc {

    val models: Value<Model>

    fun onCharacterClicked(id: Int)

    data class Model(
        val isLoading: Boolean,
        val episode: Episode,
        val characters: List<RickAndMortyCharacter>,
    )

    sealed interface Output {
        object Done : Output
        data class OpenCharacter(val id: Int) : Output
    }
}