package com.plusmobileapps.rickandmorty.characters

import com.plusmobileapps.paging.PageLoaderException
import com.plusmobileapps.paging.PagingDataSourceState
import com.plusmobileapps.rickandmorty.AppComponentContext
import com.plusmobileapps.rickandmorty.characters.list.CharactersBloc
import com.plusmobileapps.rickandmorty.characters.list.CharactersBloc.Output
import com.plusmobileapps.rickandmorty.characters.list.CharactersBlocImpl
import com.plusmobileapps.rickandmorty.runBlocTest
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.flow.MutableStateFlow
import org.kodein.mock.Mock
import org.kodein.mock.tests.TestsWithMocks
import kotlin.test.Test

class CharactersBlocTest : TestsWithMocks() {

    override fun setUpMocks() = injectMocks(mocker)

    @Mock
    lateinit var output: (Output) -> Unit
    @Mock
    lateinit var repository: CharactersRepository

    private fun AppComponentContext.createBloc(): CharactersBloc =
        CharactersBlocImpl(
            componentContext = this,
            repository = repository,
            output = { output(it) }
        )

    @Test
    fun GIVEN_emptyState_WHEN_pageLoaderStateUpdates_THEN_modelIsUpdated() = runBlocTest {
        val pageLoader = MutableStateFlow(initialPagingState)
        mockPageLoaderState { pageLoader }
        everySuspending { repository.loadFirstPage() } returns Unit

        val bloc = it.createBloc()

        bloc.models.value shouldBe CharactersBloc.Model(
            characters = listOf(),
            firstPageIsLoading = false,
            nextPageIsLoading = false,
            pageLoadedError = null,
            hasMoreToLoad = false,
        )

        pageLoader.value = PagingDataSourceState(
            isNextPageLoading = true,
            isFirstPageLoading = true,
            data = listOf(character),
            pageLoaderError = PageLoaderException.NoNetworkException(true),
            hasMoreToLoad = true,
        )

        bloc.models.value shouldBe CharactersBloc.Model(
            characters = listOf(character),
            firstPageIsLoading = true,
            nextPageIsLoading = true,
            pageLoadedError = PageLoaderException.NoNetworkException(true),
            hasMoreToLoad = true,
        )
    }

    @Test
    fun WHEN_searchIsClicked_THEN_outputIsOpenCharacterSearch() = runBlocTest {
        val outputs = expectOutputs()
        mockPageLoaderState()

        it.createBloc().onSearchClicked()

        outputs.first() shouldBe Output.OpenCharacterSearch
    }

    @Test
    fun WHEN_loadMoreCharacters_THEN_repositoryLoadNextPageIsCalled() = runBlocTest {
        everySuspending { repository.loadFirstPage() } returns Unit
        everySuspending { repository.loadNextPage() } returns Unit
        mockPageLoaderState()

        it.createBloc().loadMoreCharacters()

        verifyWithSuspend {
            repository.loadFirstPage()
            repository.pageLoaderState
            repository.loadNextPage()
        }
    }


    @Test
    fun WHEN_characterClicked_THEN_outputIsOpenCharacter() = runBlocTest {
        val outputs = expectOutputs()
        mockPageLoaderState()

        it.createBloc().onCharacterClicked(character)

        outputs.first() shouldBe Output.OpenCharacter(character)
    }

    private fun mockPageLoaderState(
        mock: () -> MutableStateFlow<PagingDataSourceState<RickAndMortyCharacter>> = {
            MutableStateFlow(PagingDataSourceState())
        }
    ) {
        every { repository.pageLoaderState } returns mock()
    }

    private fun expectOutputs(): List<Output> {
        val outputs = mutableListOf<Output>()
        every { output.invoke(isAny(outputs)) } returns Unit
        return outputs
    }

    companion object {
        val character = RickAndMortyCharacter(
            id = 4,
            name = "Pickle Rick"
        )
        val initialPagingState = PagingDataSourceState<RickAndMortyCharacter>(
            isNextPageLoading = false,
            isFirstPageLoading = false,
            data = emptyList(),
            pageLoaderError = null,
            hasMoreToLoad = false,
        )
    }

}