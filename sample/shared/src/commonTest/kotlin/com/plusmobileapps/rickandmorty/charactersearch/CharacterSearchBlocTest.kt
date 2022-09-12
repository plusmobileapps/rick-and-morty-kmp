@file:OptIn(ExperimentalCoroutinesApi::class)

package com.plusmobileapps.rickandmorty.charactersearch

import com.plusmobileapps.rickandmorty.TestAppComponentContext
import com.plusmobileapps.rickandmorty.characters.search.CharacterSearchBloc
import com.plusmobileapps.rickandmorty.characters.search.CharacterSearchBlocImpl
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestCoroutineScheduler
import kotlinx.coroutines.test.runTest
import org.kodein.mock.Mock
import org.kodein.mock.tests.TestsWithMocks
import kotlin.test.BeforeTest
import kotlin.test.Test

class CharacterSearchBlocTest : TestsWithMocks() {

    override fun setUpMocks() = injectMocks(mocker)

    @Mock lateinit var output:  (CharacterSearchBloc.Output) -> Unit

    private fun createBloc(scheduler: TestCoroutineScheduler): CharacterSearchBloc =
        CharacterSearchBlocImpl(
            componentContext = TestAppComponentContext(scheduler),
            output = { output(it) }
        )

    @BeforeTest
    fun setup() {
        every { output.invoke(isAny()) } returns Unit
    }

    @Test
    fun backClickShouldEmitOutput() = runTest {
        val bloc = createBloc(testScheduler)
        bloc.onBackClicked()
        verify { output(CharacterSearchBloc.Output.GoBack) }
    }
}