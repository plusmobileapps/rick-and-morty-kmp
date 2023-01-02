package com.plusmobileapps.rickandmorty.locations.search

import com.arkivanov.decompose.value.Value
import com.arkivanov.decompose.value.operator.map
import com.arkivanov.mvikotlin.core.instancekeeper.getStore
import com.plusmobileapps.rickandmorty.AppComponentContext
import com.plusmobileapps.rickandmorty.api.locations.Location
import com.plusmobileapps.rickandmorty.locations.search.LocationSearchBloc.Output
import com.plusmobileapps.rickandmorty.locations.search.LocationSearchStore.Intent
import com.plusmobileapps.rickandmorty.util.Consumer
import com.plusmobileapps.rickandmorty.util.asValue

internal class LocationSearchBlocImpl(
    context: AppComponentContext,
    useCase: LocationSearchUseCase,
    private val output: Consumer<Output>,
) : LocationSearchBloc, AppComponentContext by context {

    private val store: LocationSearchStore = instanceKeeper.getStore {
        LocationSearchStoreProvider(storeFactory, dispatchers, useCase).provide()
    }

    override val models: Value<LocationSearchBloc.Model> = store.asValue().map {
        LocationSearchBloc.Model(
            pageLoaderState = it.pageLoaderState,
            location = it.location,
            dimension = it.dimension,
            type = it.type,
        )
    }

    override fun onLocationChanged(location: String) {
        store.accept(Intent.UpdateLocation(location))
    }

    override fun onDimensionChanged(dimension: String) {
        store.accept(Intent.UpdateDimension(dimension))
    }

    override fun onTypeChanged(type: String) {
        store.accept(Intent.UpdateType(type))
    }

    override fun clearSearchClicked() {
        store.accept(Intent.ClearSearch)
    }

    override fun executeSearchClicked() {
        store.accept(Intent.ExecuteSearch)
    }

    override fun loadNextPage() {
        store.accept(Intent.LoadMore)
    }

    override fun onBackClicked() {
        output(Output.GoBack)
    }

    override fun onLocationClicked(location: Location) {
        output(Output.OpenLocationDetail(location.id))
    }
}