package com.plusmobileapps.rickandmorty.bottomnav

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.router.stack.ChildStack
import com.arkivanov.decompose.router.stack.StackNavigation
import com.arkivanov.decompose.router.stack.bringToFront
import com.arkivanov.decompose.router.stack.childStack
import com.arkivanov.decompose.value.Value
import com.arkivanov.decompose.value.operator.map
import com.arkivanov.essenty.lifecycle.doOnPause
import com.arkivanov.essenty.lifecycle.doOnResume
import com.arkivanov.essenty.parcelable.Parcelable
import com.arkivanov.essenty.parcelable.Parcelize
import com.arkivanov.mvikotlin.core.instancekeeper.getStore
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.plusmobileapps.rickandmorty.bottomnav.BottomNavBloc.*
import com.plusmobileapps.rickandmorty.characters.CharactersBloc
import com.plusmobileapps.rickandmorty.characters.CharactersBlocImpl
import com.plusmobileapps.rickandmorty.di.DI
import com.plusmobileapps.rickandmorty.util.Dispatchers
import com.plusmobileapps.rickandmorty.util.Consumer
import com.plusmobileapps.rickandmorty.util.asValue

class BottomNavBlocImpl(
    componentContext: ComponentContext,
    storeFactory: StoreFactory,
    dispatchers: Dispatchers,
    private val charactersBloc: (ComponentContext, Consumer<CharactersBloc.Output>) -> CharactersBloc,
//    private val episodesBloc: (ComponentContext, Consumer<EpisodesBloc.Output>) -> EpisodesBloc,
    private val bottomNavOutput: Consumer<Output>
) : BottomNavBloc, ComponentContext by componentContext {

    constructor(
        componentContext: ComponentContext,
        di: DI,
        output: Consumer<Output>
    ) : this(
        componentContext = componentContext,
        storeFactory = di.storeFactory,
        dispatchers = di.dispatchers,
        charactersBloc = { context, characterOutput ->
            CharactersBlocImpl(
                componentContext = context,
                di = di,
                output = characterOutput
            )
        },
//        episodesBloc = { context, episodesOutput ->
//            EpisodesBlocImpl(
//                componentContext = context,
//                di = di,
//                output = episodesOutput
//            )
//        },
        bottomNavOutput = output
    )

    private val store: BottomNavigationStore = instanceKeeper.getStore {
        BottomNavStoreProvider(storeFactory, dispatchers).create()
    }

    private val navigation = StackNavigation<Configuration>()

    private val router = childStack<Configuration, BottomNavBloc.Child>(
        source = navigation,
        initialConfiguration = Configuration.Characters,
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
//                is Child.Episodes -> NavItem.Type.EPISODES
                is Child.About -> NavItem.Type.ABOUT
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
            }
        )
    }

    private fun createChild(
        configuration: Configuration,
        context: ComponentContext
    ): BottomNavBloc.Child = when (configuration) {
        Configuration.Characters -> {
            BottomNavBloc.Child.Characters(
                charactersBloc(context, this::onCharactersBlocOutput)
            )
        }
        Configuration.Episodes -> {
            TODO()
//            Child.Episodes(
//                episodesBloc(context, this::onEpisodesBlocOutput)
//            )
        }
        Configuration.About -> Child.About
    }

    private fun onCharactersBlocOutput(output: CharactersBloc.Output) {
        when (output) {
            is CharactersBloc.Output.OpenCharacter -> bottomNavOutput(
                Output.ShowCharacter(output.character.id)
            )
        }
    }
//
//    private fun onEpisodesBlocOutput(output: EpisodesBloc.Output) {
//        when (output) {
//            is EpisodesBloc.Output.OpenEpisode -> bottomNavOutput(
//                Output.ShowEpisode(output.episode.id)
//            )
//        }
//    }

    sealed class Configuration : Parcelable {
        @Parcelize
        object Characters : Configuration()

        @Parcelize
        object Episodes : Configuration()

        @Parcelize
        object About : Configuration()
    }
}