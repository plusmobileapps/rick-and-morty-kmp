package com.plusmobileapps.rickandmorty.charactersearch

import com.plusmobileapps.rickandmorty.TestAppComponentContext
import com.plusmobileapps.rickandmorty.characters.search.CharacterSearchBloc
import com.plusmobileapps.rickandmorty.characters.search.CharacterSearchBlocImpl
import org.kodein.mock.Mock
import org.kodein.mock.tests.TestsWithMocks
import kotlin.test.BeforeTest
import kotlin.test.Test

class CharacterSearchBlocTest : TestsWithMocks() {

    override fun setUpMocks() = injectMocks(mocker)

    @Mock lateinit var output:  (CharacterSearchBloc.Output) -> Unit

    val bloc: CharacterSearchBloc by withMocks {
        CharacterSearchBlocImpl(
            componentContext = TestAppComponentContext(),
            output = { output(it) }
        )
    }

    @BeforeTest
    fun setup() {
        every { output.invoke(isAny()) } returns Unit
    }

    @Test
    fun backClickShouldEmitOutput() {
        val bloc = bloc
        bloc.onBackClicked()
        verify { output(CharacterSearchBloc.Output.GoBack) }
    }
}