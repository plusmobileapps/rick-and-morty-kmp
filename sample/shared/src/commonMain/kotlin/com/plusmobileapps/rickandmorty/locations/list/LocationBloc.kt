package com.plusmobileapps.rickandmorty.locations.list

import com.arkivanov.decompose.value.Value
import com.plusmobileapps.paging.PageLoaderException
import com.plusmobileapps.rickandmorty.api.locations.Location

interface LocationBloc {

    val models: Value<Model>

    fun onLocationClicked(location: Location)

    fun loadMore()

    fun onSearchClicked()

    data class Model(
        val locations: List<Location> = emptyList(),
        val firstPageIsLoading: Boolean = false,
        val nextPageIsLoading: Boolean = false,
        val pageLoadedError: PageLoaderException? = null,
        val hasMoreToLoad: Boolean = true,
    )

    sealed interface Output {
        data class OpenLocation(val location: Location) : Output
        object OpenLocationSearch : Output
    }

}