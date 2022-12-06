package com.plusmobileapps.rickandmorty.characters

import com.plusmobileapps.paging.PagingDataSource
import com.plusmobileapps.rickandmorty.AppComponentContext
import com.plusmobileapps.rickandmorty.characters.list.CharactersBloc
import com.plusmobileapps.rickandmorty.characters.list.CharactersBlocImpl
import com.plusmobileapps.rickandmorty.runBlocTest
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
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

    private fun AppComponentContext.createBloc(): CharactersBloc =
        CharactersBlocImpl(
            componentContext = this,
            repository = repository,
            output = { output(it) }
        )

    @Test
    fun charactersUpdatingShouldUpdateModel() = runBlocTest {
        val character = RickAndMortyCharacter(id = 4)
        val charactersFlow = MutableSharedFlow<List<RickAndMortyCharacter>>()
        every { repository.pageLoaderState } returns MutableStateFlow(PagingDataSource.State())
        everySuspending { repository.getCharacters() } returns charactersFlow

        val bloc = it.createBloc()
        charactersFlow.emit(listOf(character))

        assertEquals(listOf(character), bloc.models.value.characters)
    }

}