package com.plusmobileapps.rickandmorty.locations.list

import com.arkivanov.decompose.value.Value
import com.arkivanov.decompose.value.operator.map
import com.arkivanov.mvikotlin.core.instancekeeper.getStore
import com.plusmobileapps.rickandmorty.AppComponentContext
import com.plusmobileapps.rickandmorty.api.locations.Location
import com.plusmobileapps.rickandmorty.locations.LocationRepository
import com.plusmobileapps.rickandmorty.locations.list.LocationBloc.Output
import com.plusmobileapps.rickandmorty.util.Consumer
import com.plusmobileapps.rickandmorty.util.asValue

internal class LocationBlocImpl(
    context: AppComponentContext,
    repository: LocationRepository,
    private val output: Consumer<Output>
) : LocationBloc, AppComponentContext by context {

    private val store: LocationStore = instanceKeeper.getStore {
        LocationStoreProvider(dispatchers, storeFactory, repository).provide()
    }
    override val models: Value<LocationBloc.Model> = store.asValue().map {
        LocationBloc.Model(
            locations = it.items,
            firstPageIsLoading = it.firstPageIsLoading,
            nextPageIsLoading = it.nextPageIsLoading,
            pageLoadedError = it.pageLoadedError,
            hasMoreToLoad = it.hasMoreToLoad,
        )
    }

    override fun onLocationClicked(location: Location) {
        output(Output.OpenLocation(location))
    }

    override fun loadMore() {
        store.accept(LocationStore.Intent.LoadMoreLocations)
    }

    override fun onSearchClicked() {
        output(Output.OpenLocationSearch)
    }
}