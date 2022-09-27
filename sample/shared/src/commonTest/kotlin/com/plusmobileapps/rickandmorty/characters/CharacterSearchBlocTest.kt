package com.plusmobileapps.rickandmorty.characters

import com.plusmobileapps.rickandmorty.AppComponentContext
import com.plusmobileapps.rickandmorty.api.RickAndMortyApiClient
import com.plusmobileapps.rickandmorty.characters.search.CharacterSearchBloc
import com.plusmobileapps.rickandmorty.characters.search.CharacterSearchBlocImpl
import com.plusmobileapps.rickandmorty.runBlocTest
import org.kodein.mock.Mock
import org.kodein.mock.tests.TestsWithMocks
import kotlin.test.BeforeTest
import kotlin.test.Test

class CharacterSearchBlocTest : TestsWithMocks() {

    override fun setUpMocks() = injectMocks(mocker)

    @Mock
    lateinit var output: (CharacterSearchBloc.Output) -> Unit

    @Mock
    lateinit var api: RickAndMortyApiClient

    private fun AppComponentContext.createBloc(): CharacterSearchBloc =
        CharacterSearchBlocImpl(
            componentContext = this,
            rickAndMortyApi = api,
            output = { output(it) }
        )

    @BeforeTest
    fun setup() {
        every { output.invoke(isAny()) } returns Unit
    }

    @Test
    fun backClickShouldEmitOutput() = runBlocTest {
        val bloc = it.createBloc()
        bloc.onBackClicked()
        verify { output(CharacterSearchBloc.Output.GoBack) }
    }
}

