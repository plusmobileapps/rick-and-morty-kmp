package com.plusmobileapps.rickandmorty.bottomnav

import com.arkivanov.decompose.router.stack.ChildStack
import com.arkivanov.decompose.router.stack.StackNavigation
import com.arkivanov.decompose.router.stack.bringToFront
import com.arkivanov.decompose.value.Value
import com.arkivanov.decompose.value.operator.map
import com.arkivanov.essenty.lifecycle.doOnPause
import com.arkivanov.essenty.lifecycle.doOnResume
import com.arkivanov.essenty.parcelable.Parcelable
import com.arkivanov.essenty.parcelable.Parcelize
import com.arkivanov.mvikotlin.core.instancekeeper.getStore
import com.plusmobileapps.rickandmorty.AppComponentContext
import com.plusmobileapps.rickandmorty.appChildStack
import com.plusmobileapps.rickandmorty.bottomnav.BottomNavBloc.*
import com.plusmobileapps.rickandmorty.characters.list.CharactersBloc
import com.plusmobileapps.rickandmorty.characters.list.CharactersBlocImpl
import com.plusmobileapps.rickandmorty.di.DI
import com.plusmobileapps.rickandmorty.episodes.list.EpisodesBloc
import com.plusmobileapps.rickandmorty.episodes.list.EpisodesBlocImpl
import com.plusmobileapps.rickandmorty.locations.list.LocationBloc
import com.plusmobileapps.rickandmorty.locations.list.LocationBlocImpl
import com.plusmobileapps.rickandmorty.util.Consumer
import com.plusmobileapps.rickandmorty.util.asValue

class BottomNavBlocImpl(
    componentContext: AppComponentContext,
    private val charactersBloc: (AppComponentContext, Consumer<CharactersBloc.Output>) -> CharactersBloc,
    private val episodesBloc: (AppComponentContext, Consumer<EpisodesBloc.Output>) -> EpisodesBloc,
    private val locationsBloc: (AppComponentContext, Consumer<LocationBloc.Output>) -> LocationBloc,
    private val bottomNavOutput: Consumer<Output>
) : BottomNavBloc, AppComponentContext by componentContext {

    constructor(
        componentContext: AppComponentContext,
        di: DI,
        output: Consumer<Output>
    ) : this(
        componentContext = componentContext,
        charactersBloc = { context, characterOutput ->
            CharactersBlocImpl(
                componentContext = context,
                di = di,
                output = characterOutput
            )
        },
        episodesBloc = { context, episodesOutput ->
            EpisodesBlocImpl(
                appComponentContext = context,
                repository = di.episodesRepository,
                output = episodesOutput
            )
        },
        locationsBloc = { context, locationOutput ->
            LocationBlocImpl(
                context = context,
                repository = di.locationRepository,
                output = locationOutput
            )
        },
        bottomNavOutput = output
    )

    private val store: BottomNavigationStore = instanceKeeper.getStore {
        BottomNavStoreProvider(storeFactory, dispatchers).create()
    }

    private val navigation = StackNavigation<Configuration>()

    private val router = appChildStack<Configuration, BottomNavBloc.Child>(
        source = navigation,
        initialStack = { listOf(Configuration.Characters) },
        handleBackButton = true,
        childFactory = ::createChild,
        key = "BottomNavRouter"
    )

    override val routerState: Value<ChildStack<*, BottomNavBloc.Child>> = router

    override val models: Value<Model> = store.asValue().map {
        Model(it.navItems)
    }

    private val routerSubscriber: (ChildStack<Configuration, Child>) -> Unit = {
        val intent = BottomNavigationStore.Intent.SelectNavItem(
            when (it.active.instance) {
                is Child.Characters -> NavItem.Type.CHARACTERS
                is Child.Episodes -> NavItem.Type.EPISODES
                is Child.About -> NavItem.Type.ABOUT
                is Child.Locations -> NavItem.Type.LOCATIONS
            }
        )
        store.accept(intent)
    }

    init {
        lifecycle.doOnResume {
            router.subscribe(routerSubscriber)
        }
        lifecycle.doOnPause {
            router.unsubscribe(routerSubscriber)
        }
    }

    override fun onNavItemClicked(item: NavItem) {
        navigation.bringToFront(
            when (item.type) {
                NavItem.Type.CHARACTERS -> Configuration.Characters
                NavItem.Type.EPISODES -> Configuration.Episodes
                NavItem.Type.ABOUT -> Configuration.About
                NavItem.Type.LOCATIONS -> Configuration.Locations
            }
        )
    }

    private fun createChild(
        configuration: Configuration,
        context: AppComponentContext
    ): BottomNavBloc.Child = when (configuration) {
        Configuration.Characters -> {
            Child.Characters(
                charactersBloc(context, this::onCharactersBlocOutput)
            )
        }
        Configuration.Episodes -> {
            Child.Episodes(episodesBloc(context, this::onEpisodesBlocOutput))
        }
        Configuration.About -> Child.About
        Configuration.Locations -> Child.Locations(
            locationsBloc(context, this::onLocationBlocOutput)
        )
    }

    private fun onCharactersBlocOutput(output: CharactersBloc.Output) {
        when (output) {
            is CharactersBloc.Output.OpenCharacter -> bottomNavOutput(
                Output.ShowCharacterDetail(output.character.id)
            )
            CharactersBloc.Output.OpenCharacterSearch -> bottomNavOutput(Output.OpenCharacterSearch)
        }
    }

    private fun onEpisodesBlocOutput(output: EpisodesBloc.Output) {
        when (output) {
            is EpisodesBloc.Output.OpenEpisode -> bottomNavOutput(Output.ShowEpisodeDetail(output.episode.id))
            EpisodesBloc.Output.OpenEpisodeSearch -> bottomNavOutput(Output.OpenEpisodeSearch)
        }
    }

    private fun onLocationBlocOutput(output: LocationBloc.Output) {
        when (output) {
            is LocationBloc.Output.OpenLocation -> bottomNavOutput(Output.ShowLocationDetail(output.location.id))
            is LocationBloc.Output.OpenLocationSearch -> bottomNavOutput(Output.OpenLocationSearch)
        }
    }

    sealed class Configuration : Parcelable {
        @Parcelize
        object Characters : Configuration()

        @Parcelize
        object Episodes : Configuration()

        @Parcelize
        object Locations : Configuration()

        @Parcelize
        object About : Configuration()
    }
}