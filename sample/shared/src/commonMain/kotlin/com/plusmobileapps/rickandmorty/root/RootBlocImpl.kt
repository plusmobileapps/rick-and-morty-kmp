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
                api = di.rickAndMortyApi,
                output = output
            )
        },
        characterDetail = { context, id, output ->
            CharacterDetailBlocImpl(
                context = context,
                repository = di.charactersRepository,
                characterId = id,
                output = output
            )
        },
        episodeDetail = { context, id, output ->
            EpisodeDetailBlocImpl(
                context = context,
                id = id,
                repository = di.episodesRepository,
                output = output
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

            is Configuration.Character -> {
                RootBloc.Child.CharacterDetail(
                    characterDetail(context, configuration.id, this::onCharacterOutput)
                )
            }
            is Configuration.Episode -> {
                RootBloc.Child.EpisodeDetail(
                    episodeDetail(context, configuration.id, this::onEpisodeDetailOutput)
                )
            }
            Configuration.CharacterSearch -> RootBloc.Child.CharacterSearch(
                characterSearch(context) { navigation.pop() }
            )
            Configuration.EpisodeSearch -> RootBloc.Child.EpisodeSearch(
                episodeSearch(context) { navigation.pop() }
            )
            is Configuration.Location -> TODO()
            Configuration.LocationSearch -> TODO()
        }
    }

    private fun onCharacterOutput(output: CharacterDetailBloc.Output) {
        when (output) {
            CharacterDetailBloc.Output.Done -> navigation.pop()
        }
    }

    private fun onBottomNavOutput(output: BottomNavBloc.Output) {
        when (output) {
            is BottomNavBloc.Output.ShowCharacter -> navigation.push(Configuration.Character(output.id))
            is BottomNavBloc.Output.ShowEpisode -> navigation.push(Configuration.Episode(output.id))
            BottomNavBloc.Output.OpenCharacterSearch -> navigation.push(Configuration.CharacterSearch)
            BottomNavBloc.Output.OpenEpisodeSearch -> navigation.push(Configuration.EpisodeSearch)
            BottomNavBloc.Output.OpenLocationSearch -> navigation.push(Configuration.LocationSearch)
            is BottomNavBloc.Output.ShowLocation -> navigation.push(Configuration.Location(output.id))
        }
    }

    private fun onEpisodeDetailOutput(output: EpisodeDetailBloc.Output) {
        when (output) {
            EpisodeDetailBloc.Output.Done -> navigation.pop()
            is EpisodeDetailBloc.Output.OpenCharacter -> navigation.push(
                Configuration.Character(output.id)
            )
        }
    }

    private sealed class Configuration : Parcelable {
        @Parcelize
        object BottomNav : Configuration()

        @Parcelize
        object CharacterSearch : Configuration()

        @Parcelize
        data class Character(val id: Int) : Configuration()

        @Parcelize
        data class Episode(val id: Int) : Configuration()

        @Parcelize
        object EpisodeSearch : Configuration()

        @Parcelize
        data class Location(val id: Int) : Configuration()

        @Parcelize
        object LocationSearch : Configuration()
    }
}