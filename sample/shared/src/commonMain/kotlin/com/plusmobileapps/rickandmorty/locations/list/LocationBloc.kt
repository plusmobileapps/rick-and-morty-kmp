package com.plusmobileapps.rickandmorty.locations.list

import com.arkivanov.decompose.value.Value
import com.plusmobileapps.rickandmorty.api.locations.Location
import com.plusmobileapps.rickandmorty.locations.LocationListItem

interface LocationBloc {

    val models: Value<Model>

    fun onLocationClicked(location: Location)

    fun loadMore()

    fun onSearchClicked()

    data class Model(
        val isLoading: Boolean,
        val locations: List<LocationListItem>,
        val error: String?,
    )

    sealed interface Output {
        data class OpenLocation(val location: Location) : Output
        object OpenLocationSearch : Output
    }

}