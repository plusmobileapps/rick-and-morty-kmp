package com.plusmobileapps.rickandmorty.root

import com.arkivanov.decompose.router.stack.ChildStack
import com.arkivanov.decompose.value.Value
import com.plusmobileapps.rickandmorty.bottomnav.BottomNavBloc
import com.plusmobileapps.rickandmorty.characters.search.CharacterSearchBloc

interface RootBloc {
    val routerState: Value<ChildStack<*, Child>>

    sealed class Child {
        data class BottomNav(val bloc: BottomNavBloc) : Child()
        data class CharacterSearch(val bloc: CharacterSearchBloc) : Child()
//        data class Character(val bloc: CharacterDetailBloc) : Child()
//        data class Episode(val bloc: EpisodeDetailBloc) : Child()
    }
}