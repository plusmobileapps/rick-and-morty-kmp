package com.plusmobileapps.rickandmorty.root

import com.arkivanov.decompose.router.stack.ChildStack
import com.arkivanov.decompose.value.Value
import com.plusmobileapps.rickandmorty.bottomnav.BottomNavBloc
import com.plusmobileapps.rickandmorty.characters.detail.CharacterDetailBloc
import com.plusmobileapps.rickandmorty.characters.search.CharacterSearchBloc
import com.plusmobileapps.rickandmorty.episodes.detail.EpisodeDetailBloc
import com.plusmobileapps.rickandmorty.episodes.search.EpisodeSearchBloc
import com.plusmobileapps.rickandmorty.locations.detail.LocationDetailBloc

interface RootBloc {
    val routerState: Value<ChildStack<*, Child>>

    sealed class Child {
        data class BottomNav(val bloc: BottomNavBloc) : Child()
        data class CharacterSearch(val bloc: CharacterSearchBloc) : Child()
        data class EpisodeSearch(val bloc: EpisodeSearchBloc) : Child()
        data class CharacterDetail(val bloc: CharacterDetailBloc) : Child()
        data class EpisodeDetail(val bloc: EpisodeDetailBloc) : Child()
        data class LocationDetail(val bloc: LocationDetailBloc) : Child()
    }
}