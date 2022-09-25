package com.plusmobileapps.rickandmorty.root

import com.plusmobileapps.rickandmorty.AppComponentContext
import com.plusmobileapps.rickandmorty.bottomnav.BottomNavBloc
import com.plusmobileapps.rickandmorty.characters.detail.CharacterDetailBloc
import com.plusmobileapps.rickandmorty.characters.search.CharacterSearchBloc
import com.plusmobileapps.rickandmorty.episodes.detail.EpisodeDetailBloc
import com.plusmobileapps.rickandmorty.episodes.search.EpisodeSearchBloc
import com.plusmobileapps.rickandmorty.root.RootBloc.Child
import com.plusmobileapps.rickandmorty.runBlocTest
import com.plusmobileapps.rickandmorty.util.Consumer
import org.kodein.mock.Mock
import org.kodein.mock.tests.TestsWithMocks
import kotlin.test.Test
import kotlin.test.assertTrue

class RootBlocTest : TestsWithMocks() {

    override fun setUpMocks() = injectMocks(mocker)

    @Mock
    lateinit var bottomNavBloc: BottomNavBloc
    private lateinit var bottomNavOutput: Consumer<BottomNavBloc.Output>

    @Mock
    lateinit var characterSearchBloc: CharacterSearchBloc
    private lateinit var characterSearchOutput: Consumer<CharacterSearchBloc.Output>

    @Mock
    lateinit var episodeSearchBloc: EpisodeSearchBloc
    private lateinit var episodeSearchOutput: Consumer<EpisodeSearchBloc.Output>

    @Mock
    lateinit var characterDetailBloc: CharacterDetailBloc
    private lateinit var characterDetailOutput: Consumer<CharacterDetailBloc.Output>
    private var actualCharacterId: Int? = null

    @Mock
    lateinit var episodeDetailBloc: EpisodeDetailBloc
    private lateinit var episodeDetailOutput: Consumer<EpisodeDetailBloc.Output>
    private var actualEpisodeId: Int? = null

    private fun AppComponentContext.createBloc(): RootBloc =
        RootBlocImpl(
            componentContext = this,
            bottomNav = { _, output ->
                bottomNavOutput = output
                bottomNavBloc
            },
            characterSearch = { _, output ->
                characterSearchOutput = output
                characterSearchBloc
            },
            episodeSearch = { _, output ->
                episodeSearchOutput = output
                episodeSearchBloc
            },
            characterDetail = { _, id, output ->
                characterDetailOutput = output
                actualCharacterId = id
                characterDetailBloc
            },
            episodeDetail = { _, id, output ->
                episodeDetailOutput = output
                actualEpisodeId = id
                episodeDetailBloc
            }
        )

    @Test
    fun rootInitialState() = runBlocTest {
        val bloc = it.createBloc()
        assertTrue(bloc.activeChild is Child.BottomNav)
    }

    @Test
    fun bottomNavOutput_showCharacterSearch_shouldShowCharacterSearch() = runBlocTest {
        val bloc = it.createBloc()

        bottomNavOutput(BottomNavBloc.Output.OpenCharacterSearch)
        assertTrue(bloc.activeChild is Child.CharacterSearch)

        characterSearchOutput(CharacterSearchBloc.Output.GoBack)
        assertTrue(bloc.activeChild is Child.BottomNav)
    }

    @Test
    fun bottomNavOutput_showEpisodeSearch_shouldShowEpisodeSearch() = runBlocTest {
        val bloc = it.createBloc()

        bottomNavOutput(BottomNavBloc.Output.OpenEpisodeSearch)
        assertTrue(bloc.activeChild is Child.EpisodeSearch)

        episodeSearchOutput(EpisodeSearchBloc.Output.GoBack)
        assertTrue(bloc.activeChild is Child.BottomNav)
    }

    @Test
    fun bottomNavOutput_showCharacterDetail_shouldShowCharacterDetail() = runBlocTest {
        val bloc = it.createBloc()

        bottomNavOutput(BottomNavBloc.Output.ShowCharacter(4))
        assertTrue { bloc.activeChild is Child.CharacterDetail && actualCharacterId == 4 }

        characterDetailOutput(CharacterDetailBloc.Output.Done)
        assertTrue { bloc.activeChild is Child.BottomNav }
    }

    @Test
    fun bottomNavOutput_showEpisodeDetail_shouldShowEpisodeDetail() = runBlocTest {
        val bloc = it.createBloc()

        bottomNavOutput(BottomNavBloc.Output.ShowEpisode(5))
        assertTrue { bloc.activeChild is Child.EpisodeDetail && actualEpisodeId == 5 }

        episodeDetailOutput(EpisodeDetailBloc.Output.Done)
        assertTrue { bloc.activeChild is Child.BottomNav }
    }

    private val RootBloc.activeChild: Child get() = routerState.value.active.instance
}