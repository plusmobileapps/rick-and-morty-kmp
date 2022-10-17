package com.plusmobileapps.rickandmorty.di

import com.arkivanov.decompose.DefaultComponentContext
import com.arkivanov.essenty.lifecycle.Lifecycle
import com.plusmobileapps.rickandmorty.AppComponentContext
import com.plusmobileapps.rickandmorty.DefaultAppComponentContext
import com.plusmobileapps.rickandmorty.characters.detail.CharacterDetailBloc
import com.plusmobileapps.rickandmorty.characters.detail.CharacterDetailBlocImpl
import com.plusmobileapps.rickandmorty.characters.list.CharactersBloc
import com.plusmobileapps.rickandmorty.characters.list.CharactersBlocImpl
import com.plusmobileapps.rickandmorty.characters.search.CharacterSearchBloc
import com.plusmobileapps.rickandmorty.characters.search.CharacterSearchBlocImpl
import com.plusmobileapps.rickandmorty.db.DriverFactory
import com.plusmobileapps.rickandmorty.episodes.detail.EpisodeDetailBloc
import com.plusmobileapps.rickandmorty.episodes.detail.EpisodeDetailBlocImpl
import com.plusmobileapps.rickandmorty.episodes.list.EpisodesBloc
import com.plusmobileapps.rickandmorty.episodes.list.EpisodesBlocImpl
import com.plusmobileapps.rickandmorty.episodes.search.EpisodeSearchBloc
import com.plusmobileapps.rickandmorty.episodes.search.EpisodeSearchBlocImpl
import com.plusmobileapps.rickandmorty.locations.detail.LocationDetailBloc
import com.plusmobileapps.rickandmorty.locations.detail.LocationDetailBlocImpl
import com.plusmobileapps.rickandmorty.locations.list.LocationBloc
import com.plusmobileapps.rickandmorty.locations.list.LocationBlocImpl

object BlocBuilder {
    val di: DI = ServiceLocator(DriverFactory())

    fun createCharactersList(lifecycle: Lifecycle): CharactersBloc =
        CharactersBlocImpl(
            componentContext = lifecycle.createAppComponentContext(),
            di = di,
            output = {}
        )

    fun createCharacterDetailBloc(lifecycle: Lifecycle, id: Int): CharacterDetailBloc =
        CharacterDetailBlocImpl(
            characterId = id,
            context = lifecycle.createAppComponentContext(),
            charactersRepository = di.charactersRepository,
            output = {}
        )

    fun createCharacterSearchBloc(lifecycle: Lifecycle): CharacterSearchBloc =
        CharacterSearchBlocImpl(
            componentContext = lifecycle.createAppComponentContext(),
            di = di,
            output = {}
        )

    fun createEpisodesBloc(lifecycle: Lifecycle): EpisodesBloc =
        EpisodesBlocImpl(
            appComponentContext = lifecycle.createAppComponentContext(),
            repository = di.episodesRepository,
            output = { }
        )

    fun createEpisodeDetailBloc(lifecycle: Lifecycle, id: Int): EpisodeDetailBloc =
        EpisodeDetailBlocImpl(
            context = lifecycle.createAppComponentContext(),
            id = id,
            di = di,
            output = { }
        )

    fun createEpisodeSearchBloc(lifecycle: Lifecycle): EpisodeSearchBloc =
        EpisodeSearchBlocImpl(
            context = lifecycle.createAppComponentContext(),
            api = di.rickAndMortyApi,
            output = {}
        )

    fun createLocationsBloc(lifecycle: Lifecycle): LocationBloc =
        LocationBlocImpl(
            context = lifecycle.createAppComponentContext(),
            repository = di.locationRepository,
            output = {}
        )

    fun createLocationDetailBloc(lifecycle: Lifecycle, id: Int): LocationDetailBloc =
        LocationDetailBlocImpl(
            context = lifecycle.createAppComponentContext(),
            locationId = id,
            di = di,
            output = { }
        )

    private fun Lifecycle.createAppComponentContext(): AppComponentContext =
        DefaultAppComponentContext(
            componentContext = DefaultComponentContext(this),
            dispatchers = di.dispatchers,
            storeFactory = di.storeFactory
        )

}