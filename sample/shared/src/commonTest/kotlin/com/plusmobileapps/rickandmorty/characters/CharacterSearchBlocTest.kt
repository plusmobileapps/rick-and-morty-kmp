package com.plusmobileapps.rickandmorty.characters

import com.plusmobileapps.paging.PagingDataSourceState
import com.plusmobileapps.rickandmorty.TestAppComponentContext
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
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test

class CharacterSearchBlocTest : TestsWithMocks() {

    override fun setUpMocks() = injectMocks(mocker)

    @Mock
    lateinit var output: (CharacterSearchBloc.Output) -> Unit

    @Mock
    lateinit var useCase: CharacterSearchUseCase

    private val outputs = mutableListOf<Output>()

    private fun TestScope.createBloc(context: TestAppComponentContext): CharacterSearchBloc =
        CharacterSearchBlocImpl(
            componentContext = context,
            output = { output(it) },
            useCase = useCase,
        )

    @BeforeTest
    fun setup() {
        every { output.invoke(isAny(outputs)) } returns Unit
    }

    @AfterTest
    fun tearDown() {
        outputs.clear()
    }

    @Test
    fun backClickShouldEmitOutput() = runBlocTest {
        every { useCase.pageLoaderState } returns MutableStateFlow(PagingDataSourceState())
        every { output.invoke(isAny()) }
        val bloc = createBloc(it)
        bloc.onBackClicked()
        outputs.first() shouldBe Output.GoBack
    }
}

