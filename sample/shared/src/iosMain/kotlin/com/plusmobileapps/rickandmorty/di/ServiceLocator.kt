package com.plusmobileapps.rickandmorty.di

import com.arkivanov.decompose.DefaultComponentContext
import com.arkivanov.essenty.lifecycle.Lifecycle
import com.arkivanov.essenty.lifecycle.doOnDestroy
import com.plusmobileapps.rickandmorty.AppComponentContext
import com.plusmobileapps.rickandmorty.DefaultAppComponentContext
import com.plusmobileapps.rickandmorty.characters.list.CharactersBloc
import com.plusmobileapps.rickandmorty.characters.list.CharactersBlocImpl
import com.plusmobileapps.rickandmorty.db.DriverFactory
import com.plusmobileapps.rickandmorty.episodes.list.EpisodesBloc
import com.plusmobileapps.rickandmorty.episodes.list.EpisodesBlocImpl
import com.plusmobileapps.rickandmorty.locations.list.LocationBloc
import com.plusmobileapps.rickandmorty.locations.list.LocationBlocImpl

object BlocBuilder {
    val di: DI = ServiceLocator(DriverFactory())

    var charactersBlocOutput: ((CharactersBloc.Output) -> Unit)? = null

    fun createCharactersList(lifecycle: Lifecycle): CharactersBloc =
        CharactersBlocImpl(
            componentContext = lifecycle.createAppComponentContext(),
            di = di,
            output = { charactersBlocOutput?.invoke(it) }
        ).also { lifecycle.doOnDestroy { charactersBlocOutput = null } }

    var episodesBlocOutput: ((EpisodesBloc.Output) -> Unit)? = null

    fun createEpisodesBloc(lifecycle: Lifecycle): EpisodesBloc =
        EpisodesBlocImpl(
            appComponentContext = lifecycle.createAppComponentContext(),
            repository = di.episodesRepository,
            output = { episodesBlocOutput?.invoke(it) }
        ).also { lifecycle.doOnDestroy { episodesBlocOutput = null } }

    fun createLocationsBloc(lifecycle: Lifecycle): LocationBloc =
        LocationBlocImpl(
            context = lifecycle.createAppComponentContext(),
            repository = di.locationRepository,
            output = {}
        )

    private fun Lifecycle.createAppComponentContext(): AppComponentContext =
        DefaultAppComponentContext(
            componentContext = DefaultComponentContext(this),
            dispatchers = di.dispatchers,
            storeFactory = di.storeFactory
        )

}