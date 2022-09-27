package com.plusmobileapps.rickandmorty.episodes

import com.plusmobileapps.rickandmorty.AppComponentContext
import com.plusmobileapps.rickandmorty.api.episodes.Episode
import com.plusmobileapps.rickandmorty.characters.CharactersRepository
import com.plusmobileapps.rickandmorty.characters.RickAndMortyCharacter
import com.plusmobileapps.rickandmorty.episodes.detail.EpisodeDetailBloc
import com.plusmobileapps.rickandmorty.episodes.detail.EpisodeDetailBlocImpl
import com.plusmobileapps.rickandmorty.runBlocTest
import org.kodein.mock.Mock
import org.kodein.mock.tests.TestsWithMocks
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

class EpisodeDetailBlocTest : TestsWithMocks() {

    override fun setUpMocks() = injectMocks(mocker)

    private val episodeId = 2

    @Mock
    lateinit var characterRepository: CharactersRepository

    @Mock
    lateinit var episodeRepository: EpisodesRepository

    @Mock
    lateinit var outputListener: (EpisodeDetailBloc.Output) -> Unit

    private fun AppComponentContext.createBloc(): EpisodeDetailBloc =
        EpisodeDetailBlocImpl(
            context = this,
            charactersRepository = characterRepository,
            episodesRepository = episodeRepository,
            id = episodeId,
            output = { outputListener(it) }
        )

    @BeforeTest
    fun setUp() {
        every { outputListener(isAny()) } returns Unit
    }

    @Test
    fun happyPathLoadingEpisodeDetailsAndCharacters() = runBlocTest {
        val character = RickAndMortyCharacter(id = 244)
        val episode = Episode(id = episodeId, name = "some episode", characters = listOf(
            "https://rickandmortyapi.com/api/character/244"
        ))
        everySuspending { episodeRepository.getEpisode(episodeId) } returns episode
        everySuspending { characterRepository.getCharacters(listOf(character.id)) } returns listOf(character)

        val bloc = it.createBloc()

        assertEquals(
            EpisodeDetailBloc.Model(
                isLoadingEpisode = false,
                episode = episode,
                isLoadingCharacters = false,
                characters = listOf(character),
            ),
            bloc.models.value
        )
    }

    @Test
    fun backClickEmitsDoneOutput() = runBlocTest {
        it.createBloc().onBackClicked()
        verify { outputListener.invoke(EpisodeDetailBloc.Output.Done) }
    }

    @Test
    fun characterClickedEmitsOpenCharacterOutput() = runBlocTest {
        it.createBloc().onCharacterClicked(4444)
        verify { outputListener.invoke(EpisodeDetailBloc.Output.OpenCharacter(4444)) }
    }
}