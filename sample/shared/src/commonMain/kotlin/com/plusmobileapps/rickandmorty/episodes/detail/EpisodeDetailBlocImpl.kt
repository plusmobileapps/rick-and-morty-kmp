package com.plusmobileapps.rickandmorty.episodes.detail

import com.arkivanov.decompose.value.Value
import com.arkivanov.decompose.value.operator.map
import com.arkivanov.mvikotlin.core.instancekeeper.getStore
import com.plusmobileapps.rickandmorty.AppComponentContext
import com.plusmobileapps.rickandmorty.episodes.EpisodesRepository
import com.plusmobileapps.rickandmorty.episodes.detail.EpisodeDetailBloc.Output
import com.plusmobileapps.rickandmorty.util.Consumer
import com.plusmobileapps.rickandmorty.util.asValue

internal class EpisodeDetailBlocImpl(
    context: AppComponentContext,
    repository: EpisodesRepository,
    id: Int,
    private val output: Consumer<Output>,
) : EpisodeDetailBloc, AppComponentContext by context {

    private val store = instanceKeeper.getStore {
        EpisodeDetailStoreProvider(id, dispatchers, storeFactory, repository).provide()
    }

    override val models: Value<EpisodeDetailBloc.Model> = store.asValue().map {
        EpisodeDetailBloc.Model(
            isLoading = it.isLoading,
            episode = it.episode,
            characters = it.characters,
        )
    }

    override fun onCharacterClicked(id: Int) {
        output(Output.OpenCharacter(id))
    }

    override fun onBackClicked() {
        output(Output.Done)
    }
}