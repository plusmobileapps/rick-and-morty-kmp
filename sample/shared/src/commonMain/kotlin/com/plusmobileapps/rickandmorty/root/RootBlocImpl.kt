package com.plusmobileapps.rickandmorty.root

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.router.stack.ChildStack
import com.arkivanov.decompose.router.stack.StackNavigation
import com.arkivanov.decompose.router.stack.pop
import com.arkivanov.decompose.router.stack.push
import com.arkivanov.decompose.value.Value
import com.arkivanov.essenty.parcelable.Parcelable
import com.arkivanov.essenty.parcelable.Parcelize
import com.plusmobileapps.rickandmorty.AppComponentContext
import com.plusmobileapps.rickandmorty.DefaultAppComponentContext
import com.plusmobileapps.rickandmorty.appChildStack
import com.plusmobileapps.rickandmorty.bottomnav.BottomNavBloc
import com.plusmobileapps.rickandmorty.bottomnav.BottomNavBlocImpl
import com.plusmobileapps.rickandmorty.characters.detail.CharacterDetailBloc
import com.plusmobileapps.rickandmorty.characters.detail.CharacterDetailBlocImpl
import com.plusmobileapps.rickandmorty.characters.search.CharacterSearchBloc
import com.plusmobileapps.rickandmorty.characters.search.CharacterSearchBlocImpl
import com.plusmobileapps.rickandmorty.db.DriverFactory
import com.plusmobileapps.rickandmorty.di.DI
import com.plusmobileapps.rickandmorty.di.ServiceLocator
import com.plusmobileapps.rickandmorty.episodes.detail.EpisodeDetailBloc
import com.plusmobileapps.rickandmorty.episodes.detail.EpisodeDetailBlocImpl
import com.plusmobileapps.rickandmorty.episodes.search.EpisodeSearchBloc
import com.plusmobileapps.rickandmorty.episodes.search.EpisodeSearchBlocImpl
import com.plusmobileapps.rickandmorty.locations.detail.LocationDetailBloc
import com.plusmobileapps.rickandmorty.locations.detail.LocationDetailBlocImpl
import com.plusmobileapps.rickandmorty.locations.search.LocationSearchBloc
import com.plusmobileapps.rickandmorty.locations.search.LocationSearchBlocImpl
import com.plusmobileapps.rickandmorty.util.Consumer

fun buildRootBloc(context: ComponentContext, driverFactory: DriverFactory): RootBloc {
    val di: DI = ServiceLocator(driverFactory)
    return RootBlocImpl(
        componentContext = DefaultAppComponentContext(
            componentContext = context,
            dispatchers = di.dispatchers,
            storeFactory = di.storeFactory
        ),
        di = di
    )
}

internal class RootBlocImpl(
    componentContext: AppComponentContext,
    private val bottomNav: (AppComponentContext, Consumer<BottomNavBloc.Output>) -> BottomNavBloc,
    private val characterSearch: (AppComponentContext, Consumer<CharacterSearchBloc.Output>) -> CharacterSearchBloc,
    private val episodeSearch: (AppComponentContext, Consumer<EpisodeSearchBloc.Output>) -> EpisodeSearchBloc,
    private val characterDetail: (AppComponentContext, Int, Consumer<CharacterDetailBloc.Output>) -> CharacterDetailBloc,
    private val episodeDetail: (AppComponentContext, Int, Consumer<EpisodeDetailBloc.Output>) -> EpisodeDetailBloc,
    private val locationDetail: (AppComponentContext, Int, Consumer<LocationDetailBloc.Output>) -> LocationDetailBloc,
    private val locationSearch: (AppComponentContext, Consumer<LocationSearchBloc.Output>) -> LocationSearchBloc,
) : RootBloc, AppComponentContext by componentContext {

    constructor(componentContext: AppComponentContext, di: DI) : this(
        componentContext = componentContext,
        bottomNav = { context, output ->
            BottomNavBlocImpl(
                componentContext = context,
                di = di,
                output = output
            )
        },
        characterSearch = { context, output ->
            CharacterSearchBlocImpl(
                componentContext = context,
                di = di,
                output = output
            )
        },
        episodeSearch = { context, output ->
            EpisodeSearchBlocImpl(
                context = context,
                useCase = di.episodeSearchUseCase,
                output = output
            )
        },
        characterDetail = { context, id, output ->
            CharacterDetailBlocImpl(
                context = context,
                charactersRepository = di.charactersRepository,
                characterId = id,
                output = output
            )
        },
        episodeDetail = { context, id, output ->
            EpisodeDetailBlocImpl(
                context = context,
                id = id,
                di = di,
                output = output
            )
        },
        locationDetail = { context, id, output ->
            LocationDetailBlocImpl(
                context = context,
                locationId = id,
                output = output,
                di = di
            )
        },
        locationSearch = { context, output ->
            LocationSearchBlocImpl(
                context = context,
                useCase = di.locationSearchUseCase,
                output = output,
            )
        }
    )

    private val navigation = StackNavigation<Configuration>()

    private val router = appChildStack(
        source = navigation,
        initialStack = { listOf(Configuration.BottomNav) },
        handleBackButton = true,
        childFactory = ::createChild,
        key = "RootRouter"
    )

    override val routerState: Value<ChildStack<*, RootBloc.Child>> = router

    private fun createChild(
        configuration: Configuration,
        context: AppComponentContext
    ): RootBloc.Child {
        return when (configuration) {
            Configuration.BottomNav -> RootBloc.Child.BottomNav(
                bottomNav(context, this::onBottomNavOutput)
            )

            is Configuration.CharacterDetail -> {
                RootBloc.Child.CharacterDetail(
                    characterDetail(context, configuration.id, this::onCharacterOutput)
                )
            }
            is Configuration.EpisodeDetail -> {
                RootBloc.Child.EpisodeDetail(
                    episodeDetail(context, configuration.id, this::onEpisodeDetailOutput)
                )
            }
            Configuration.CharacterSearch -> RootBloc.Child.CharacterSearch(
                characterSearch(context, this::onCharacterSearchOutput)
            )
            Configuration.EpisodeSearch -> RootBloc.Child.EpisodeSearch(
                episodeSearch(context, this::onEpisodeSearchOutput)
            )
            is Configuration.LocationDetail -> RootBloc.Child.LocationDetail(
                locationDetail(context, configuration.id, ::onLocationDetailOutput)
            )
            Configuration.LocationSearch -> RootBloc.Child.LocationSearch(
                locationSearch(context, this::onLocationSearchOutput)
            )
        }
    }

    private fun onCharacterSearchOutput(output: CharacterSearchBloc.Output) {
        when (output) {
            CharacterSearchBloc.Output.GoBack -> navigation.pop()
            is CharacterSearchBloc.Output.OpenCharacter -> navigation.push(
                Configuration.CharacterDetail(output.id)
            )
        }
    }

    private fun onEpisodeSearchOutput(output: EpisodeSearchBloc.Output) {
        when (output) {
            EpisodeSearchBloc.Output.GoBack -> navigation.pop()
            is EpisodeSearchBloc.Output.OpenEpisode -> {
                navigation.push(Configuration.EpisodeDetail(output.id))
            }
        }
    }

    private fun onCharacterOutput(output: CharacterDetailBloc.Output) {
        when (output) {
            CharacterDetailBloc.Output.Done -> navigation.pop()
        }
    }

    private fun onBottomNavOutput(output: BottomNavBloc.Output) {
        when (output) {
            is BottomNavBloc.Output.ShowCharacterDetail -> navigation.push(
                Configuration.CharacterDetail(output.id)
            )
            is BottomNavBloc.Output.ShowEpisodeDetail -> navigation.push(
                Configuration.EpisodeDetail(output.id)
            )
            BottomNavBloc.Output.OpenCharacterSearch -> navigation.push(Configuration.CharacterSearch)
            BottomNavBloc.Output.OpenEpisodeSearch -> navigation.push(Configuration.EpisodeSearch)
            is BottomNavBloc.Output.ShowLocationDetail -> navigation.push(
                Configuration.LocationDetail(output.id)
            )
            BottomNavBloc.Output.OpenLocationSearch -> navigation.push(Configuration.LocationSearch)
        }
    }

    private fun onEpisodeDetailOutput(output: EpisodeDetailBloc.Output) {
        when (output) {
            EpisodeDetailBloc.Output.Done -> navigation.pop()
            is EpisodeDetailBloc.Output.OpenCharacter -> navigation.push(
                Configuration.CharacterDetail(output.id)
            )
        }
    }

    private fun onLocationDetailOutput(output: LocationDetailBloc.Output) {
        when (output) {
            LocationDetailBloc.Output.Done -> navigation.pop()
            is LocationDetailBloc.Output.OpenCharacter -> navigation.push(
                Configuration.CharacterDetail(output.id)
            )
        }
    }

    private fun onLocationSearchOutput(output: LocationSearchBloc.Output) {
        when (output) {
            LocationSearchBloc.Output.GoBack -> navigation.pop()
            is LocationSearchBloc.Output.OpenLocationDetail -> navigation.push(
                Configuration.LocationDetail(output.id)
            )
        }
    }

    private sealed class Configuration : Parcelable {
        @Parcelize
        object BottomNav : Configuration()

        @Parcelize
        object CharacterSearch : Configuration()

        @Parcelize
        data class CharacterDetail(val id: Int) : Configuration()

        @Parcelize
        data class EpisodeDetail(val id: Int) : Configuration()

        @Parcelize
        object EpisodeSearch : Configuration()

        @Parcelize
        data class LocationDetail(val id: Int) : Configuration()

        @Parcelize
        object LocationSearch : Configuration()
    }
}