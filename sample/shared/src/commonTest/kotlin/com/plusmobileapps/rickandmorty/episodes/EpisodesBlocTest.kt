package com.plusmobileapps.rickandmorty.episodes

import com.plusmobileapps.rickandmorty.AppComponentContext
import com.plusmobileapps.rickandmorty.api.episodes.Episode
import com.plusmobileapps.rickandmorty.episodes.list.EpisodesBloc
import com.plusmobileapps.rickandmorty.episodes.list.EpisodesBlocImpl
import com.plusmobileapps.rickandmorty.runBlocTest
import kotlinx.coroutines.flow.MutableSharedFlow
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
        val episodesFlow = MutableSharedFlow<List<Episode>>()
        every { repository.getEpisodes() } returns episodesFlow
        val bloc = it.createBloc()

        episodesFlow.emit(listOf(Episode(id = 4)))

        assertEquals(
            listOf<EpisodeListItem>(EpisodeListItem.EpisodeItem(Episode(id = 4))),
            bloc.models.value.episodes
        )
    }
}