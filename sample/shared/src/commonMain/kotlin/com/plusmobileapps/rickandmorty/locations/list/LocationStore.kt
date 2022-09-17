package com.plusmobileapps.rickandmorty.locations.list

import com.arkivanov.mvikotlin.core.store.Store
import com.plusmobileapps.rickandmorty.locations.LocationListItem
import com.plusmobileapps.rickandmorty.locations.list.LocationStore.Intent
import com.plusmobileapps.rickandmorty.locations.list.LocationStore.State

internal interface LocationStore : Store<Intent, State, Nothing> {
    data class State(
        val isLoading: Boolean = false,
        val locations: List<LocationListItem> = emptyList(),
        val error: String? = null,
    )

    sealed interface Intent {
        object LoadMoreCharacters : Intent
    }
}