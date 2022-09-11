package com.plusmobileapps.rickandmorty.characters

sealed class CharactersListItem {
    data class Character(val value: RickAndMortyCharacter) : CharactersListItem()
    data class PageLoading(val isLoading: Boolean, val hasMore: Boolean) : CharactersListItem() {
        companion object {
            const val KEY = -1
        }
    }
}