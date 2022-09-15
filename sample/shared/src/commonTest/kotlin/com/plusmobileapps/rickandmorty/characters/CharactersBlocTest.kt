@file:OptIn(ExperimentalCoroutinesApi::class)

package com.plusmobileapps.rickandmorty.characters

import com.plusmobileapps.rickandmorty.TestAppComponentContext
import com.plusmobileapps.rickandmorty.characters.list.CharactersBloc
import com.plusmobileapps.rickandmorty.characters.list.CharactersBlocImpl
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.TestCoroutineScheduler
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.kodein.mock.Mock
import org.kodein.mock.tests.TestsWithMocks
import kotlin.test.Test
import kotlin.test.assertEquals

class CharactersBlocTest : TestsWithMocks() {

    override fun setUpMocks() = injectMocks(mocker)

    @Mock
    lateinit var output: (CharactersBloc.Output) -> Unit
    @Mock
    lateinit var repository: CharactersRepository

    private fun createBloc(scheduler: TestCoroutineScheduler): CharactersBloc {
        val context = TestAppComponentContext(scheduler)
        Dispatchers.setMain(context.mainDispatcher)
        return CharactersBlocImpl(
            componentContext = context,
            repository = repository,
            output = { output(it) }
        )
    }

    @Test
    fun charactersUpdatingShouldUpdateModel() = runTest {
        val character = RickAndMortyCharacter(id = 4)
        val charactersFlow = MutableSharedFlow<List<RickAndMortyCharacter>>()
        everySuspending { repository.getCharacters() } returns charactersFlow

        val bloc = createBloc(testScheduler)
        charactersFlow.emit(listOf(character))

        assertEquals(listOf(CharactersListItem.Character(character)), bloc.models.value.listItems)
    }

}