package com.plusmobileapps.rickandmorty.episodes

import com.plusmobileapps.paging.PagingDataSourceState
import com.plusmobileapps.rickandmorty.AppComponentContext
import com.plusmobileapps.rickandmorty.api.episodes.Episode
import com.plusmobileapps.rickandmorty.episodes.list.EpisodesBloc
import com.plusmobileapps.rickandmorty.episodes.list.EpisodesBlocImpl
import com.plusmobileapps.rickandmorty.runBlocTest
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.flowOf
import org.kodein.mock.Mock
import org.kodein.mock.tests.TestsWithMocks
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

class EpisodesBlocTest : TestsWithMocks() {

    override fun setUpMocks() = injectMocks(mocker)

    @Mock
    lateinit var repository: EpisodesRepository

    @Mock
    lateinit var outputListener: (EpisodesBloc.Output) -> Unit

    private fun AppComponentContext.createBloc(): EpisodesBloc =
        EpisodesBlocImpl(
            appComponentContext = this,
            repository = repository,
            output = { outputListener(it) }
        )

    @BeforeTest
    fun setUp() {
        every { outputListener(isAny()) } returns Unit
    }

    @Test
    fun episodesUpdatingShouldUpdateModel() = runBlocTest {
        everySuspending { repository.loadFirstPage() } returns Unit
        every { repository.pagingState } returns flowOf(
            PagingDataSourceState(data = listOf(Episode(id = 4)))
        )

        val bloc = it.createBloc()

        bloc.models.value.episodes shouldBe listOf(Episode(id = 4))
    }
}