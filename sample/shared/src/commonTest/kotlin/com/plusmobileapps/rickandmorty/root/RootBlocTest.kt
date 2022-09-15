@file:OptIn(ExperimentalCoroutinesApi::class, ExperimentalCoroutinesApi::class)

package com.plusmobileapps.rickandmorty.root

import com.plusmobileapps.rickandmorty.AppComponentContext
import com.plusmobileapps.rickandmorty.TestAppComponentContext
import com.plusmobileapps.rickandmorty.bottomnav.BottomNavBloc
import com.plusmobileapps.rickandmorty.characters.search.CharacterSearchBloc
import com.plusmobileapps.rickandmorty.root.RootBloc.Child
import com.plusmobileapps.rickandmorty.runBlocTest
import com.plusmobileapps.rickandmorty.util.Consumer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestCoroutineScheduler
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
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

    private fun AppComponentContext.createBloc(): RootBloc=
         RootBlocImpl(
            componentContext = this,
            bottomNav = { _, output ->
                bottomNavOutput = output
                bottomNavBloc
            },
            characterSearch = { _, output ->
                characterSearchOutput = output
                characterSearchBloc
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

    private val RootBloc.activeChild: Child get() = routerState.value.active.instance
}