package com.plusmobileapps.rickandmorty.bottomnav

import com.arkivanov.decompose.router.stack.ChildStack
import com.arkivanov.decompose.value.Value
import com.plusmobileapps.rickandmorty.characters.CharactersBloc

interface BottomNavBloc {

    val routerState: Value<ChildStack<*, Child>>

    val models: Value<Model>

    fun onNavItemClicked(item: NavItem)

    data class Model(val navItems: List<NavItem>)

    data class NavItem(val selected: Boolean, val type: Type) {
        enum class Type(val id: Long) {
            CHARACTERS(1L),
            EPISODES(2L),
            ABOUT(3L),
        }
    }
    sealed class Child {
        data class Characters(val bloc: CharactersBloc) : Child()
//        data class Episodes(val bloc: EpisodesBloc) : Child()
        object About : Child()
    }

    sealed class Output {
        data class ShowCharacter(val id: Int) : Output()
        data class ShowEpisode(val id: Int) : Output()
    }
}