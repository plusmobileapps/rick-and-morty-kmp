package com.plusmobileapps.rickandmorty.root

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.router.stack.ChildStack
import com.arkivanov.decompose.router.stack.StackNavigation
import com.arkivanov.decompose.router.stack.childStack
import com.arkivanov.decompose.router.stack.push
import com.arkivanov.decompose.value.Value
import com.arkivanov.essenty.parcelable.Parcelable
import com.arkivanov.essenty.parcelable.Parcelize
import com.plusmobileapps.rickandmorty.bottomnav.BottomNavBloc
import com.plusmobileapps.rickandmorty.bottomnav.BottomNavBlocImpl
import com.plusmobileapps.rickandmorty.db.DriverFactory
import com.plusmobileapps.rickandmorty.di.DI
import com.plusmobileapps.rickandmorty.di.ServiceLocator
import com.plusmobileapps.rickandmorty.util.Consumer

fun buildRootBloc(context: ComponentContext, driverFactory: DriverFactory): RootBloc {
    return RootBlocImpl(
        componentContext = context,
        di = ServiceLocator(driverFactory)
    )
}

internal class RootBlocImpl(
    componentContext: ComponentContext,
    private val bottomNav: (ComponentContext, Consumer<BottomNavBloc.Output>) -> BottomNavBloc,
//    private val character: (ComponentContext, Int, Consumer<CharacterDetailBloc.Output>) -> CharacterDetailBloc,
//    private val episode: (ComponentContext, Int, Consumer<EpisodeDetailBloc.Output>) -> EpisodeDetailBloc,
) : RootBloc, ComponentContext by componentContext {

    constructor(componentContext: ComponentContext, di: DI) : this(
        componentContext = componentContext,
        bottomNav = { context, output ->
            BottomNavBlocImpl(
                componentContext = context,
                di = di,
                output = output
            )
        },
//        character = { context, id, output ->
//            CharacterDetailBlocImpl(
//                context = context,
//                di = di,
//                id = id,
//                output = output
//            )
//        },
//        episode = { context, id, output ->
//            EpisodeDetailBlocImpl(
//                context = context,
//                di = di,
//                id = id,
//                output = output
//            )
//        }
    )

    private val navigation = StackNavigation<Configuration>()

    private val router = childStack(
        source = navigation,
        initialConfiguration = Configuration.BottomNav,
        handleBackButton = true,
        childFactory = ::createChild,
        key = "RootRouter"
    )

    override val routerState: Value<ChildStack<*, RootBloc.Child>> = router

    private fun createChild(
        configuration: Configuration,
        context: ComponentContext
    ): RootBloc.Child {
        return when (configuration) {
            Configuration.BottomNav -> RootBloc.Child.BottomNav(
                bottomNav(context, this::onBottomNavOutput)
            )
            is Configuration.Character -> {
                TODO()
//                RootBloc.Child.Character(
//                    character(context, configuration.id, this::onCharacterOutput)
//                )
            }
            is Configuration.Episode -> {
                TODO()
//                RootBloc.Child.Episode(
//                    episode(context, configuration.id, this::onEpisodeDetailOutput)
//                )
            }
        }
    }

//    private fun onCharacterOutput(output: CharacterDetailBloc.Output) {
//        when (output) {
//            CharacterDetailBloc.Output.Finished -> router.pop()
//        }
//    }

    private fun onBottomNavOutput(output: BottomNavBloc.Output) {
        when (output) {
            is BottomNavBloc.Output.ShowCharacter -> navigation.push(Configuration.Character(output.id))
            is BottomNavBloc.Output.ShowEpisode -> {
                TODO()
//                router.push(Configuration.Episode(output.id))
            }
        }
    }

//    private fun onEpisodeDetailOutput(output: EpisodeDetailBloc.Output) {
//        when (output) {
//            EpisodeDetailBloc.Output.Finished -> router.pop()
//        }
//    }

    private sealed class Configuration : Parcelable {
        @Parcelize
        object BottomNav : Configuration()

        @Parcelize
        data class Character(val id: Int) : Configuration()

        @Parcelize
        data class Episode(val id: Int) : Configuration()
    }
}