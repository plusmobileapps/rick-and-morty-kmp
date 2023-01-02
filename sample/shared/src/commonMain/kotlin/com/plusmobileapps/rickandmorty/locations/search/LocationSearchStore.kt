package com.plusmobileapps.rickandmorty.locations.search

import com.arkivanov.mvikotlin.core.store.Store
import com.plusmobileapps.paging.PagingDataSourceState
import com.plusmobileapps.rickandmorty.api.locations.Location
import com.plusmobileapps.rickandmorty.locations.search.LocationSearchStore.Intent
import com.plusmobileapps.rickandmorty.locations.search.LocationSearchStore.State

internal interface LocationSearchStore : Store<Intent, State, Nothing> {

    data class State(
        val pageLoaderState: PagingDataSourceState<Location> = PagingDataSourceState(),
        val location: String = "",
        val dimension: String = "",
        val type: String = "",
    )

    sealed interface Intent {
        data class UpdateLocation(val location: String) : Intent
        data class UpdateDimension(val dimension: String) : Intent
        data class UpdateType(val type: String) : Intent
        object ClearSearch : Intent
        object ExecuteSearch : Intent
        object LoadMore : Intent
    }

}