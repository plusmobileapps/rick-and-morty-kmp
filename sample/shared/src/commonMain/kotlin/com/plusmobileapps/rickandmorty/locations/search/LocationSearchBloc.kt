package com.plusmobileapps.rickandmorty.locations.search

import com.arkivanov.decompose.value.Value
import com.plusmobileapps.paging.PagingDataSourceState
import com.plusmobileapps.rickandmorty.api.locations.Location
import com.plusmobileapps.rickandmorty.util.BackClickBloc

interface LocationSearchBloc : BackClickBloc {

    val models: Value<Model>

    fun onLocationChanged(location: String)

    fun onDimensionChanged(dimension: String)

    fun onTypeChanged(type: String)

    fun clearSearchClicked()

    fun executeSearchClicked()

    fun loadNextPage()

    fun onLocationClicked(location: Location)

    data class Model(
        val pageLoaderState: PagingDataSourceState<Location>,
        val location: String,
        val dimension: String,
        val type: String,
    )

    sealed class Output {
        object GoBack : Output()
        data class OpenLocationDetail(val id: Int) : Output()
    }
}