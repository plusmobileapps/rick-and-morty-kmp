package com.plusmobileapps.rickandmorty.characters

import com.plusmobileapps.paging.PagingDataSourceState
import com.plusmobileapps.rickandmorty.AppComponentContext
import com.plusmobileapps.rickandmorty.characters.list.CharactersBloc
import com.plusmobileapps.rickandmorty.characters.list.CharactersBlocImpl
import com.plusmobileapps.rickandmorty.runBlocTest
import io.kotest.matchers.shouldBe
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
    fun pagingStateUpdatingShouldUpdateState() = runBlocTest {
        val character = RickAndMortyCharacter(id = 4)
        every { repository.pageLoaderState } returns MutableStateFlow(
            PagingDataSourceState(
                data = listOf(character)
            )
        )

        val bloc = it.createBloc()

        bloc.models.value shouldBe CharactersBloc.Model(
            characters = listOf(character),
            firstPageIsLoading = false,
            nextPageIsLoading = false,
            pageLoadedError = null,
            hasMoreToLoad = false,
        )
    }

}