package com.plusmobileapps.rickandmorty.bottomnav

import com.plusmobileapps.rickandmorty.AppComponentContext
import com.plusmobileapps.rickandmorty.api.episodes.Episode
import com.plusmobileapps.rickandmorty.api.locations.Location
import com.plusmobileapps.rickandmorty.characters.RickAndMortyCharacter
import com.plusmobileapps.rickandmorty.characters.list.CharactersBloc
import com.plusmobileapps.rickandmorty.episodes.list.EpisodesBloc
import com.plusmobileapps.rickandmorty.locations.list.LocationBloc
import com.plusmobileapps.rickandmorty.runBlocTest
import com.plusmobileapps.rickandmorty.util.Consumer
import org.kodein.mock.Mock
import org.kodein.mock.tests.TestsWithMocks
import kotlin.test.*

class BottomNavBlocTest : TestsWithMocks() {

    override fun setUpMocks() = injectMocks(mocker)

    @Mock
    lateinit var charactersBloc: CharactersBloc
    lateinit var charactersOutput: Consumer<CharactersBloc.Output>

    @Mock
    lateinit var episodesBloc: EpisodesBloc
    lateinit var episodesOutput: Consumer<EpisodesBloc.Output>

    @Mock
    lateinit var locationsBloc: LocationBloc
    lateinit var locationsOutput: Consumer<LocationBloc.Output>

    @Mock
    lateinit var bottomNavOutput: (BottomNavBloc.Output) -> Unit

    private val expectedNavItemsInOrder = listOf(
        BottomNavBloc.NavItem.Type.CHARACTERS,
        BottomNavBloc.NavItem.Type.EPISODES,
        BottomNavBloc.NavItem.Type.LOCATIONS,
        BottomNavBloc.NavItem.Type.ABOUT,
    )

    @BeforeTest
    fun setup() {
        every { bottomNavOutput.invoke(isAny()) } returns Unit
    }

    private fun AppComponentContext.createBloc(): BottomNavBloc =
        BottomNavBlocImpl(
            componentContext = this,
            charactersBloc = { _, output ->
                charactersOutput = output
                charactersBloc
            },
            episodesBloc = { _, output ->
                episodesOutput = output
                episodesBloc
            },
            locationsBloc = { _, output ->
                locationsOutput = output
                locationsBloc
            },
            bottomNavOutput = bottomNavOutput
        )

    @Test
    fun initialStateIsCharacters() = runBlocTest {
        val bloc = it.createBloc()
        assertTrue { bloc.activeChild is BottomNavBloc.Child.Characters }
        bloc.assertNavItemSelected(BottomNavBloc.NavItem.Type.CHARACTERS)
    }

    @Test
    fun navItemClickingShouldBringBlocToFront() = runBlocTest {
        val bloc = it.createBloc()
        bloc.onNavItemClicked(BottomNavBloc.NavItem(false, BottomNavBloc.NavItem.Type.EPISODES))
        assertTrue { bloc.activeChild is BottomNavBloc.Child.Episodes }
        bloc.assertNavItemSelected(BottomNavBloc.NavItem.Type.EPISODES)
        bloc.onNavItemClicked(BottomNavBloc.NavItem(false, BottomNavBloc.NavItem.Type.LOCATIONS))
        assertTrue { bloc.activeChild is BottomNavBloc.Child.Locations }
        bloc.assertNavItemSelected(BottomNavBloc.NavItem.Type.LOCATIONS)
        bloc.onNavItemClicked(BottomNavBloc.NavItem(false, BottomNavBloc.NavItem.Type.ABOUT))
        assertTrue { bloc.activeChild is BottomNavBloc.Child.About }
        bloc.assertNavItemSelected(BottomNavBloc.NavItem.Type.ABOUT)
    }

    @Test
    fun charactersBlocOutputIsEmittedToBottomNavOutput() = runBlocTest {
        val bloc = it.createBloc()

        charactersOutput(CharactersBloc.Output.OpenCharacterSearch)
        verify { bottomNavOutput(BottomNavBloc.Output.OpenCharacterSearch) }

        charactersOutput(CharactersBloc.Output.OpenCharacter(RickAndMortyCharacter(10)))
        verify { bottomNavOutput(BottomNavBloc.Output.ShowCharacter(10)) }
    }

    @Test
    fun episodesBlocOutputIsEmittedToBottomNavOutput() = runBlocTest {
        val bloc = it.createBloc()
        bloc.onNavItemClicked(BottomNavBloc.NavItem(false, BottomNavBloc.NavItem.Type.EPISODES))

        episodesOutput(EpisodesBloc.Output.OpenEpisodeSearch)
        verify { bottomNavOutput(BottomNavBloc.Output.OpenEpisodeSearch) }

        episodesOutput(EpisodesBloc.Output.OpenEpisode(Episode(13)))
        verify { bottomNavOutput(BottomNavBloc.Output.ShowEpisode(13)) }
    }

    @Test
    fun locationsBlocOutputIsEmittedToBottomNavOutput() = runBlocTest {
        val bloc = it.createBloc()
        bloc.onNavItemClicked(BottomNavBloc.NavItem(false, BottomNavBloc.NavItem.Type.LOCATIONS))

        locationsOutput(LocationBloc.Output.OpenLocationSearch)
        verify { bottomNavOutput(BottomNavBloc.Output.OpenLocationSearch)}

        locationsOutput(LocationBloc.Output.OpenLocation(Location(34)))
        verify { bottomNavOutput(BottomNavBloc.Output.ShowLocation(34)) }
    }

    private val BottomNavBloc.activeChild: BottomNavBloc.Child get() = routerState.value.active.instance

    private fun BottomNavBloc.assertNavItemSelected(expectedNavItem: BottomNavBloc.NavItem.Type) {
        val actual = models.value.navItems
        expectedNavItemsInOrder.forEachIndexed { index, navItemType ->
            val navItem = actual[index]
            assertEquals(navItemType, navItem.type)
            if (expectedNavItem == navItemType) {
                assertTrue { navItem.selected }
            } else {
                assertFalse { navItem.selected }
            }
        }
    }
}