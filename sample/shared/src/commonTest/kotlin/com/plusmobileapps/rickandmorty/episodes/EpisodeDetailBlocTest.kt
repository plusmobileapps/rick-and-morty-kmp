package com.plusmobileapps.rickandmorty.episodes

import com.plusmobileapps.rickandmorty.AppComponentContext
import com.plusmobileapps.rickandmorty.api.episodes.Episode
import com.plusmobileapps.rickandmorty.characters.CharacterMocks.pickleRick
import com.plusmobileapps.rickandmorty.characters.CharactersRepository
import com.plusmobileapps.rickandmorty.characters.RickAndMortyCharacter
import com.plusmobileapps.rickandmorty.episodes.detail.EpisodeDetailBloc
import com.plusmobileapps.rickandmorty.episodes.detail.EpisodeDetailBloc.Output
import com.plusmobileapps.rickandmorty.episodes.detail.EpisodeDetailBlocImpl
import com.plusmobileapps.rickandmorty.runBlocTest
import io.kotest.matchers.shouldBe
import org.kodein.mock.Mock
import org.kodein.mock.tests.TestsWithMocks
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

class EpisodeDetailBlocTest : TestsWithMocks() {

    override fun setUpMocks() = injectMocks(mocker)

    @Mock
    lateinit var characterRepository: CharactersRepository

    @Mock
    lateinit var episodeRepository: EpisodesRepository

    @Mock
    lateinit var outputListener: (Output) -> Unit

    private fun AppComponentContext.createBloc(): EpisodeDetailBloc =
        EpisodeDetailBlocImpl(
            context = this,
            charactersRepository = characterRepository,
            episodesRepository = episodeRepository,
            id = episode.id,
            output = { outputListener(it) }
        )

    @Test
    fun happyPathLoadingEpisodeDetailsAndCharacters() = runBlocTest {
        val character = RickAndMortyCharacter(id = 244)
        everySuspending { episodeRepository.getEpisode(episode.id) } returns episode
        everySuspending { characterRepository.getCharacters(listOf(character.id)) } returns listOf(character)

        val bloc = it.createBloc()

        bloc.models.value shouldBe EpisodeDetailBloc.Model(
            isLoadingEpisode = false,
            episode = episode,
            isLoadingCharacters = false,
            characters = listOf(character),
        )
    }

    @Test
    fun backClickEmitsDoneOutput() = runBlocTest {
        val outputs = expectOutputs()
        it.createBloc().onBackClicked()
        outputs.first() shouldBe Output.Done
    }

    @Test
    fun characterClickedEmitsOpenCharacterOutput() = runBlocTest {
        val outputs = expectOutputs()
        it.createBloc().onCharacterClicked(4444)
        outputs.first() shouldBe Output.OpenCharacter(4444)
    }

    private suspend fun expectOutputs(): List<Output> {
        mockRepository()
        val outputs = mutableListOf<Output>()
        every { outputListener(isAny(outputs)) } returns Unit
        return outputs
    }

    private suspend fun mockRepository() {
        everySuspending { episodeRepository.getEpisode(episode.id) } returns episode
        everySuspending {
            characterRepository.getCharacters(listOf(pickleRick.id))
        } returns listOf(pickleRick)
    }

    companion object {

        val episode = Episode(
            id = 2,
            name = "some episode",
            characters = listOf(
                "https://rickandmortyapi.com/api/character/244"
            )
        )
    }
}