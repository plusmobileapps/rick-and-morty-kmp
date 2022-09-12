package com.plusmobileapps.rickandmorty.root

import com.plusmobileapps.rickandmorty.TestAppComponentContext
import com.plusmobileapps.rickandmorty.bottomnav.BottomNavBloc
import com.plusmobileapps.rickandmorty.characters.search.CharacterSearchBloc
import com.plusmobileapps.rickandmorty.root.RootBloc.Child
import com.plusmobileapps.rickandmorty.util.Consumer
import org.kodein.mock.Mock
import org.kodein.mock.tests.TestsWithMocks
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RootBlocTest : TestsWithMocks() {

    override fun setUpMocks() = injectMocks(mocker)

    @Mock
    lateinit var bottomNavBloc: BottomNavBloc
    private lateinit var bottomNavOutput: Consumer<BottomNavBloc.Output>
    @Mock
    lateinit var characterSearchBloc: CharacterSearchBloc
    private lateinit var characterSearchOutput: Consumer<CharacterSearchBloc.Output>

    val rootBloc: RootBloc by withMocks {
        RootBlocImpl(
            componentContext = TestAppComponentContext(),
            bottomNav = { _, output ->
                bottomNavOutput = output
                bottomNavBloc
            },
            characterSearch = { _, output ->
                characterSearchOutput = output
                characterSearchBloc
            }
        )
    }

    @Test
    fun rootInitialState() {
        val bloc = rootBloc
        assertTrue(bloc.activeChild is Child.BottomNav)
    }

    @Test
    fun bottomNavOutput_showCharacterSearch_shouldShowCharacterSearch() {
        val bloc = rootBloc

        bottomNavOutput(BottomNavBloc.Output.OpenCharacterSearch)
        assertTrue(bloc.activeChild is Child.CharacterSearch)

        characterSearchOutput(CharacterSearchBloc.Output.GoBack)
        assertTrue(bloc.activeChild is Child.BottomNav)
    }

    private val RootBloc.activeChild: Child get() = routerState.value.active.instance
}