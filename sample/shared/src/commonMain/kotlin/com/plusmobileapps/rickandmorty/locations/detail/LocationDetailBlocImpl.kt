package com.plusmobileapps.rickandmorty.locations.detail

import com.arkivanov.decompose.value.Value
import com.arkivanov.decompose.value.operator.map
import com.arkivanov.mvikotlin.core.instancekeeper.getStore
import com.plusmobileapps.rickandmorty.AppComponentContext
import com.plusmobileapps.rickandmorty.characters.CharactersRepository
import com.plusmobileapps.rickandmorty.characters.RickAndMortyCharacter
import com.plusmobileapps.rickandmorty.di.DI
import com.plusmobileapps.rickandmorty.locations.LocationRepository
import com.plusmobileapps.rickandmorty.util.Consumer
import com.plusmobileapps.rickandmorty.util.asValue

internal class LocationDetailBlocImpl(
    locationId: Int,
    context: AppComponentContext,
    charactersRepository: CharactersRepository,
    locationRepository: LocationRepository,
    private val output: Consumer<LocationDetailBloc.Output>
) : LocationDetailBloc, AppComponentContext by context {

    constructor(
        context: AppComponentContext,
        locationId: Int,
        di: DI,
        output: Consumer<LocationDetailBloc.Output>
    ) : this(
        locationId = locationId,
        context = context,
        charactersRepository = di.charactersRepository,
        locationRepository = di.locationRepository,
        output = output,
    )

    private val store: LocationDetailStore = instanceKeeper.getStore {
        LocationDetailStoreProvider(
            locationId = locationId,
            storeFactory = storeFactory,
            dispatchers = dispatchers,
            charactersRepository = charactersRepository,
            locationsRepository = locationRepository
        ).provide()
    }
    override val models: Value<LocationDetailBloc.Model> = store.asValue().map {
        LocationDetailBloc.Model(
            isLoadingCharacters = it.isCharactersLoading,
            isLoadingLocation = it.isLoading,
            location = it.location,
            characters = it.characters,
        )
    }

    override fun onBackClicked() {
        output.invoke(LocationDetailBloc.Output.Done)
    }

    override fun onCharacterClicked(character: RickAndMortyCharacter) {
        output.invoke(LocationDetailBloc.Output.OpenCharacter(character.id))
    }
}