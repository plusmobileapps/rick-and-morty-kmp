package com.plusmobileapps.rickandmorty.characters

import com.plusmobileapps.paging.PagingDataSourceState
import com.plusmobileapps.rickandmorty.TestAppComponentContext
import com.plusmobileapps.rickandmorty.characters.CharacterMocks.pickleRick
import com.plusmobileapps.rickandmorty.characters.search.CharacterSearchBloc
import com.plusmobileapps.rickandmorty.characters.search.CharacterSearchBloc.Output
import com.plusmobileapps.rickandmorty.characters.search.CharacterSearchBlocImpl
import com.plusmobileapps.rickandmorty.characters.search.CharacterSearchUseCase
import com.plusmobileapps.rickandmorty.runBlocTest
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.TestScope
import org.kodein.mock.Mock
import org.kodein.mock.tests.TestsWithMocks
import kotlin.test.Test

class CharacterSearchBlocTest : TestsWithMocks() {

    override fun setUpMocks() = injectMocks(mocker)

    @Mock
    lateinit var output: (Output) -> Unit

    @Mock
    lateinit var useCase: CharacterSearchUseCase


    private fun TestAppComponentContext.createBloc(): CharacterSearchBloc =
        CharacterSearchBlocImpl(
            componentContext = this,
            output = { output(it) },
            useCase = useCase,
        )

    @Test
    fun WHEN_backClick_THEN_outputIsGoBack() = runBlocTest {
        val outputs = expectOutputs()
        it.createBloc().onBackClicked()
        outputs.first() shouldBe Output.GoBack
    }

    @Test
    fun WHEN_characterClicked_THEN_outputIsOpenCharacter() = runBlocTest {
        val outputs = expectOutputs()
        it.createBloc().onCharacterClicked(pickleRick)
        outputs.first() shouldBe Output.OpenCharacter(pickleRick.id)
    }

    @Test
    fun WHEN_searchClicked_THEN_searchIsInitiated() = runBlocTest {
        every { useCase.loadFirstPage(QUERY, null, "", null) } returns Unit
        mockPageLoader()

        it.createBloc().apply {
            onQueryChanged(QUERY)
            onSearchClicked()
        }

        verify {
            useCase.pageLoaderState
            useCase.loadFirstPage(
                query = QUERY,
                status = null,
                species = "",
                gender = null,
            )
        }
    }

    @Test
    fun WHEN_firstPageTryAgainClicked_THEN_nextPageLoaded() = runBlocTest {
        every { useCase.loadFirstPage(QUERY, null, "", null) } returns Unit
        mockPageLoader()

        it.createBloc().apply {
            onQueryChanged(QUERY)
            onFirstPageTryAgainClicked()
        }

        verify {
            useCase.pageLoaderState
            useCase.loadFirstPage(
                query = QUERY,
                status = null,
                species = "",
                gender = null,
            )
        }
    }

    private fun expectOutputs(): List<Output> {
        mockPageLoader()
        val outputs = mutableListOf<Output>()
        every { output.invoke(isAny(outputs)) } returns Unit
        return outputs
    }

    private fun mockPageLoader(
        mock: () -> MutableStateFlow<PagingDataSourceState<RickAndMortyCharacter>> = {
            MutableStateFlow(PagingDataSourceState())
        }
    ) {
        every { useCase.pageLoaderState } returns mock()
    }

    companion object {
        const val QUERY = "Rick"
    }
}

