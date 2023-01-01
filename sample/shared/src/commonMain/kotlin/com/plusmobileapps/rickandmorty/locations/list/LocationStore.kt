package com.plusmobileapps.rickandmorty.locations.list

import com.arkivanov.mvikotlin.core.store.Store
import com.plusmobileapps.paging.PageLoaderException
import com.plusmobileapps.rickandmorty.api.locations.Location
import com.plusmobileapps.rickandmorty.locations.list.LocationStore.Intent
import com.plusmobileapps.rickandmorty.locations.list.LocationStore.State

internal interface LocationStore : Store<Intent, State, Nothing> {
    data class State(
        val items: List<Location> = emptyList(),
        val firstPageIsLoading: Boolean = false,
        val nextPageIsLoading: Boolean = false,
        val pageLoadedError: PageLoaderException? = null,
        val hasMoreToLoad: Boolean = true,
    )

    sealed interface Intent {
        object LoadMoreLocations : Intent
    }
}