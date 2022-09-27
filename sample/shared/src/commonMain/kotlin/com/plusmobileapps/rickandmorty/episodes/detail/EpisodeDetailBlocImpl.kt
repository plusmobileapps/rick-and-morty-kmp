package com.plusmobileapps.rickandmorty.episodes.detail

import com.arkivanov.decompose.value.Value
import com.arkivanov.decompose.value.operator.map
import com.arkivanov.mvikotlin.core.instancekeeper.getStore
import com.plusmobileapps.rickandmorty.AppComponentContext
import com.plusmobileapps.rickandmorty.characters.CharactersRepository
import com.plusmobileapps.rickandmorty.di.DI
import com.plusmobileapps.rickandmorty.episodes.EpisodesRepository
import com.plusmobileapps.rickandmorty.episodes.detail.EpisodeDetailBloc.Output
import com.plusmobileapps.rickandmorty.util.Consumer
import com.plusmobileapps.rickandmorty.util.asValue

internal class EpisodeDetailBlocImpl(
    context: AppComponentContext,
    episodesRepository: EpisodesRepository,
    charactersRepository: CharactersRepository,
    id: Int,
    private val output: Consumer<Output>,
) : EpisodeDetailBloc, AppComponentContext by context {

    constructor(context: AppComponentContext, di: DI, id: Int, output: Consumer<Output>): this(
        context = context,
        episodesRepository = di.episodesRepository,
        charactersRepository = di.charactersRepository,
        id = id,
        output = output,
    )

    private val store = instanceKeeper.getStore {
        EpisodeDetailStoreProvider(id, dispatchers, storeFactory, episodesRepository, charactersRepository).provide()
    }

    override val models: Value<EpisodeDetailBloc.Model> = store.asValue().map {
        EpisodeDetailBloc.Model(
            isLoadingEpisode = it.isLoading,
            episode = it.episode,
            characters = it.characters,
            isLoadingCharacters = it.isCharacterLoading,
        )
    }

    override fun onCharacterClicked(id: Int) {
        output(Output.OpenCharacter(id))
    }

    override fun onBackClicked() {
        output(Output.Done)
    }
}